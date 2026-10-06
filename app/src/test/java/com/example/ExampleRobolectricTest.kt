package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Les Mystères de Bourges à vos oreilles", appName)
  }

  @Test
  fun `verify audio languages and multilingual narration`() {
    val languages = com.example.ui.viewmodel.AppLanguage.values()
    assertEquals(6, languages.size)
    val codes = languages.map { it.code }
    org.junit.Assert.assertTrue(codes.containsAll(listOf("FR", "EN", "ES", "DE", "NL", "IT")))

    val testSite = com.example.data.Site(
      id = 1,
      title = "Cathédrale Saint-Étienne",
      description = "Cathédrale gothique",
      narrationText = "Narration française",
      latitude = 47.0805,
      longitude = 2.3991,
      category = "CATHEDRAL"
    )

    // Verify Italian narration resolution
    val narrationIt = com.example.audio.MultilingualNarrationService.getNarration(testSite, "IT")
    org.junit.Assert.assertTrue(narrationIt.contains("Cattedrale") || narrationIt.contains("Benvenuti"))

    // Verify Spanish narration resolution
    val narrationEs = com.example.audio.MultilingualNarrationService.getNarration(testSite, "ES")
    org.junit.Assert.assertTrue(narrationEs.contains("Catedral") || narrationEs.contains("Bienvenidos"))
  }

  @Test
  fun `verify all csv sites parsed`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val csvContent = context.assets.open("poi_export.csv").use { it.readBytes().toString(Charsets.UTF_8) }
    val parsed = com.example.data.CsvManager.parseSitesFromCsv(csvContent)
    println("PARSED_CSV_COUNT=" + parsed.size)
    org.junit.Assert.assertEquals(170, parsed.size)
  }
}
