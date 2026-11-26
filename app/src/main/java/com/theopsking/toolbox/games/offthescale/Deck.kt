package com.theopsking.toolbox.games.offthescale

import org.json.JSONObject
import kotlin.random.Random

/** One end-to-end scale. 1 is [low], 10 is [high]. */
data class Scale(val low: String, val high: String)

data class PromptCard(val id: String, val text: String, val scales: List<Scale>)

object Deck {
    const val ASSET_PATH = "off_the_scale/prompts.json"

    /**
     * Each scale in prompts.json is either {"label": "useful"}, which becomes
     * "Least useful" / "Most useful", or {"low": "Cutest", "high": "Scariest"}
     * when the two ends need different words.
     */
    fun parse(json: String): List<PromptCard> {
        val prompts = JSONObject(json).getJSONArray("prompts")
        return List(prompts.length()) { i ->
            val p = prompts.getJSONObject(i)
            val scales = p.getJSONArray("scales")
            PromptCard(
                id = p.getString("id"),
                text = p.getString("text"),
                scales = List(scales.length()) { j ->
                    val s = scales.getJSONObject(j)
                    if (s.has("label")) {
                        val label = s.getString("label")
                        Scale(low = "Least $label", high = "Most $label")
                    } else {
                        Scale(low = s.getString("low"), high = s.getString("high"))
                    }
                },
            )
        }
    }

    /**
     * Card order as a shuffle bag: every card shows once before any repeats, and a
     * new pass never starts with the card that ended the last one. Pure function of
     * (seed, position), so saving those two numbers restores the exact deck.
     */
    fun cardIndexAt(seed: Long, position: Int, deckSize: Int): Int {
        require(deckSize > 0)
        val pass = position / deckSize
        return passOrder(seed, pass, deckSize)[position % deckSize]
    }

    private fun passOrder(seed: Long, pass: Int, deckSize: Int): List<Int> {
        val order = (0 until deckSize).shuffled(Random(seed + pass)).toMutableList()
        if (pass > 0 && deckSize > 1) {
            val previousLast = passOrder(seed, pass - 1, deckSize).last()
            if (order[0] == previousLast) {
                order[0] = order[1].also { order[1] = order[0] }
            }
        }
        return order
    }
}
