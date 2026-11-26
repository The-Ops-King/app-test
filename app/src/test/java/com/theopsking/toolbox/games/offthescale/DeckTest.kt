package com.theopsking.toolbox.games.offthescale

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DeckTest {
    private val deck = Deck.parse(File("src/main/assets/${Deck.ASSET_PATH}").readText())

    @Test fun `every card is well formed`() {
        assertTrue("deck is empty", deck.isNotEmpty())
        assertEquals("duplicate ids", deck.size, deck.map { it.id }.toSet().size)
        deck.forEach { card ->
            assertTrue("${card.id}: prompt must end with ?", card.text.trim().endsWith("?"))
            assertEquals("${card.id}: needs exactly 3 scales", 3, card.scales.size)
            card.scales.forEach { s ->
                assertTrue("${card.id}: blank scale end", s.low.isNotBlank() && s.high.isNotBlank())
            }
            val all = card.text + card.scales.joinToString { it.low + it.high }
            assertTrue("${card.id}: no em/en dashes", '—' !in all && '–' !in all)
        }
    }

    @Test fun `label scales expand to least and most`() {
        val parsed = Deck.parse("""{"prompts":[{"id":"x","text":"Q?","scales":[{"label":"useful"},{"low":"Cutest","high":"Scariest"}]}]}""")
        assertEquals(Scale("Least useful", "Most useful"), parsed[0].scales[0])
        assertEquals(Scale("Cutest", "Scariest"), parsed[0].scales[1])
    }

    @Test fun `shuffle bag shows every card once per pass and never repeats back to back`() {
        val size = deck.size
        for (seed in 0L until 200L) {
            val draws = (0 until size * 4).map { Deck.cardIndexAt(seed, it, size) }
            draws.chunked(size).forEach { pass -> assertEquals((0 until size).toSet(), pass.toSet()) }
            draws.zipWithNext().forEach { (a, b) -> assertNotEquals("seed $seed repeated a card", a, b) }
        }
    }

    @Test fun `same seed and position give the same card`() {
        assertEquals(Deck.cardIndexAt(42, 77, deck.size), Deck.cardIndexAt(42, 77, deck.size))
    }
}
