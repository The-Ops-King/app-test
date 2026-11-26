package com.theopsking.toolbox.update

import org.json.JSONObject

/** Mirrors update.json, which CI publishes next to every release APK. */
data class UpdateManifest(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val sha256: String,
) {
    companion object {
        fun parse(json: String): UpdateManifest {
            val o = JSONObject(json)
            val manifest = UpdateManifest(
                versionCode = o.getInt("versionCode"),
                versionName = o.getString("versionName"),
                apkUrl = o.getString("apkUrl"),
                sha256 = o.getString("sha256").lowercase(),
            )
            require(manifest.apkUrl.startsWith("https://")) { "Update URL must be https" }
            require(manifest.sha256.matches(Regex("[0-9a-f]{64}"))) { "Bad sha256 in update.json" }
            return manifest
        }
    }
}
