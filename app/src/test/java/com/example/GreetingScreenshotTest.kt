package com.example

import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Template antigo referenciava um composable Greeting inexistente.
 * Desabilitado para não bloquear a suíte; screenshot real fica fora deste escopo.
 */
@RunWith(RobolectricTestRunner::class)
class GreetingScreenshotTest {

  @Ignore("Greeting composable removido do app; teste legado inválido")
  @Test
  fun greeting_screenshot() = Unit
}
