package com.example.branding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompanyLogoStoreTest {

    @Test
    fun acceptsHttpsAlfatechHost() {
        val url = "https://sac2.alfatechtelecom.com.br/central_assinante_web/assets/img/logo.png"
        assertEquals(url, CompanyLogoStore.sanitize(url))
        assertEquals("sac2.alfatechtelecom.com.br", CompanyLogoStore.hostForLog(url))
    }

    @Test
    fun rejectsForeignHost() {
        assertNull(CompanyLogoStore.sanitize("https://evil.example/logo.png"))
    }

    @Test
    fun rejectsJavascriptScheme() {
        assertNull(CompanyLogoStore.sanitize("javascript:alert(1)"))
    }

    @Test
    fun normalizesIxcDataApplicationImage() {
        val raw = "data:application/image;base64,AAAA"
        val out = CompanyLogoStore.sanitize(raw)
        assertNotNull(out)
        assertTrue(out!!.startsWith("data:image/png;base64,"))
    }
}
