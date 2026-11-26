package com.theopsking.toolbox.update

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.theopsking.toolbox.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class Available(val manifest: UpdateManifest) : UpdateState
    data class NeedsPermission(val manifest: UpdateManifest) : UpdateState
    data class Downloading(val manifest: UpdateManifest, val progress: Float?) : UpdateState
    data object Installing : UpdateState
    data class Failed(val message: String, val manifest: UpdateManifest?) : UpdateState
}

/**
 * App-wide updater. Lives outside any screen so a download keeps going when you
 * switch screens. Flow: check update.json -> download APK -> verify sha256 ->
 * hand it to PackageInstaller. Android itself also refuses the APK unless it is
 * signed with the same key as the installed app.
 */
@SuppressLint("StaticFieldLeak") // holds the application context only
object UpdateController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private lateinit var appContext: Context
    private var job: Job? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun checkForUpdate() {
        if (job?.isActive == true) return
        job = scope.launch {
            _state.value = UpdateState.Checking
            _state.value = try {
                val manifest = withContext(Dispatchers.IO) {
                    UpdateManifest.parse(httpGet(BuildConfig.UPDATE_MANIFEST_URL))
                }
                if (manifest.versionCode > BuildConfig.VERSION_CODE) {
                    UpdateState.Available(manifest)
                } else {
                    UpdateState.UpToDate
                }
            } catch (e: Exception) {
                UpdateState.Failed("Couldn't check for updates: ${e.message}", null)
            }
        }
    }

    fun install(manifest: UpdateManifest) {
        if (job?.isActive == true) return
        if (!appContext.packageManager.canRequestPackageInstalls()) {
            _state.value = UpdateState.NeedsPermission(manifest)
            return
        }
        job = scope.launch {
            try {
                _state.value = UpdateState.Downloading(manifest, null)
                val apk = withContext(Dispatchers.IO) {
                    download(manifest) { p -> _state.value = UpdateState.Downloading(manifest, p) }
                }
                _state.value = UpdateState.Installing
                withContext(Dispatchers.IO) { commitInstall(apk) }
            } catch (e: Exception) {
                _state.value = UpdateState.Failed("Update failed: ${e.message}", manifest)
            }
        }
    }

    /** Opens the system screen where the user allows this app to install updates. */
    fun openInstallPermissionSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}"),
        )
        context.startActivity(intent)
    }

    internal fun reportInstallFailure(message: String) {
        _state.value = UpdateState.Failed("Install failed: $message", null)
    }

    private fun httpGet(url: String): String {
        val conn = open(url)
        try {
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private fun download(manifest: UpdateManifest, onProgress: (Float?) -> Unit): File {
        val dir = File(appContext.cacheDir, "updates").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val target = File(dir, "update-${manifest.versionCode}.apk")
        val digest = MessageDigest.getInstance("SHA-256")
        val conn = open(manifest.apkUrl)
        try {
            val total = conn.contentLengthLong.takeIf { it > 0 }
            conn.inputStream.use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var done = 0L
                    var lastReported = -1
                    while (true) {
                        val n = input.read(buffer)
                        if (n < 0) break
                        output.write(buffer, 0, n)
                        digest.update(buffer, 0, n)
                        done += n
                        if (total != null) {
                            val pct = (done * 100 / total).toInt()
                            if (pct != lastReported) {
                                lastReported = pct
                                scope.launch { onProgress(pct / 100f) }
                            }
                        }
                    }
                }
            }
        } finally {
            conn.disconnect()
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }
        if (actual != manifest.sha256) {
            target.delete()
            error("downloaded file doesn't match its checksum")
        }
        return target
    }

    private fun commitInstall(apk: File) {
        val installer = appContext.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(appContext.packageName)
            setSize(apk.length())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Self-updates can skip the confirm dialog on Android 12+.
                // Android still shows it when it decides it has to.
                setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                setPackageSource(PackageInstaller.PACKAGE_SOURCE_DOWNLOADED_FILE)
            }
        }
        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            apk.inputStream().use { input ->
                session.openWrite("base.apk", 0, apk.length()).use { out ->
                    input.copyTo(out)
                    session.fsync(out)
                }
            }
            val callback = Intent(appContext, InstallResultReceiver::class.java)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
            val pending = PendingIntent.getBroadcast(appContext, sessionId, callback, flags)
            session.commit(pending.intentSender)
        }
    }

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            useCaches = false
            instanceFollowRedirects = true
            setRequestProperty("Cache-Control", "no-cache")
            if (responseCode !in 200..299) {
                val code = responseCode
                disconnect()
                error("server returned HTTP $code")
            }
        }
}
