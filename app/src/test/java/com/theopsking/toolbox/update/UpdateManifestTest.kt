package com.theopsking.toolbox.update

import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateManifestTest {
    private val sha = "a".repeat(64)

    @Test fun parsesValidManifest() {
        val m = UpdateManifest.parse(
            """{"versionCode":7,"versionName":"1.0.7","apkUrl":"https://example.com/a.apk","sha256":"${sha.uppercase()}"}"""
        )
        assertEquals(7, m.versionCode)
        assertEquals(sha, m.sha256)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPlainHttp() {
        UpdateManifest.parse("""{"versionCode":7,"versionName":"1","apkUrl":"http://example.com/a.apk","sha256":"$sha"}""")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsBadChecksum() {
        UpdateManifest.parse("""{"versionCode":7,"versionName":"1","apkUrl":"https://example.com/a.apk","sha256":"nope"}""")
    }
}
