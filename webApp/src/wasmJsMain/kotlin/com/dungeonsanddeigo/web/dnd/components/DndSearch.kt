package com.dungeonsanddeigo.web.dnd.components

import com.dungeonsanddeigo.dnd.rules.calcModifier
import com.dungeonsanddeigo.dnd.rules.calcProficiency
import com.dungeonsanddeigo.i18n.*
import com.dungeonsanddeigo.model.*
import com.dungeonsanddeigo.web.Repos
import kotlinx.browser.document
import org.w3c.dom.*

fun renderDndSearch(character: Character, container: HTMLDivElement) {
    val searchRow = document.createElement("div") as HTMLDivElement
    searchRow.style.display = "flex"
    searchRow.style.setProperty("gap", "4px")
    searchRow.style.marginBottom = "10px"

    val searchInput = document.createElement("input") as HTMLInputElement
    searchInput.type = "text"
    searchInput.placeholder = "Search..."
    searchInput.style.setProperty("flex", "1")
    searchInput.style.padding = "8px"
    searchInput.style.boxSizing = "border-box"
    searchRow.appendChild(searchInput)

    val clearBtn = document.createElement("button") as HTMLButtonElement
    clearBtn.textContent = "\uD83E\uDDF9"
    clearBtn.addEventListener("click", {
        searchInput.value = ""
        searchInput.dispatchEvent(org.w3c.dom.events.Event("input"))
    })
    searchRow.appendChild(clearBtn)

    container.appendChild(searchRow)

    fun addCheckbox(label: String, id: String) {
        val row = document.createElement("div") as HTMLDivElement
        row.style.display = "flex"
        row.style.alignItems = "center"
        row.style.setProperty("gap", "6px")
        row.style.marginBottom = "6px"

        val cb = document.createElement("input") as HTMLInputElement
        cb.type = "checkbox"
        cb.id = id
        row.appendChild(cb)

        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        lbl.htmlFor = id
        lbl.style.fontSize = "13px"
        row.appendChild(lbl)

        container.appendChild(row)
    }

    addCheckbox("Notes", "search-notes")
    addCheckbox("Plain Search", "search-plain")

    val resultsDiv = document.createElement("div") as HTMLDivElement
    resultsDiv.style.marginTop = "10px"
    container.appendChild(resultsDiv)

    searchInput.addEventListener("input", {
        val query = searchInput.value.trim().lowercase()
        resultsDiv.innerHTML = ""
        if (query.length < 3) return@addEventListener

        val stats = Repos.baseStats.getByCharacterId(character.id) ?: DndBaseStats(characterId = character.id)
        val mainInfo = Repos.mainInfo.getByCharacterId(character.id)
        val totalLevel = (mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0)
        val prof = calcProficiency(totalLevel)

        data class StatEntry(val name: String, val value: Int?, val hasSave: Boolean)

        val statEntries = listOf(
            StatEntry(tStat("Strength"), stats.strValue, stats.hasStrRes),
            StatEntry(tStat("Dexterity"), stats.dexValue, stats.hasDexRes),
            StatEntry(tStat("Constitution"), stats.conValue, stats.hasConRes),
            StatEntry(tStat("Intelligence"), stats.intValue, stats.hasIntRes),
            StatEntry(tStat("Wisdom"), stats.wisValue, stats.hasWisRes),
            StatEntry(tStat("Charisma"), stats.chaValue, stats.hasChaRes)
        )

        val features = Repos.features.getByCharacterId(character.id)

        fun renderFeaturesByTag(tag: String) {
            val matching = features.filter { feat -> feat.tags.any { it.lowercase() == tag.lowercase() } }
            matching.forEach { feat ->
                val fDiv = document.createElement("div") as HTMLDivElement
                fDiv.style.fontSize = "12px"
                fDiv.style.color = "#555"
                fDiv.style.paddingLeft = "12px"
                fDiv.style.marginBottom = "4px"

                val nameSpan = document.createElement("div") as HTMLDivElement
                nameSpan.textContent = "\u2022 ${feat.name}"
                nameSpan.style.fontWeight = "bold"
                fDiv.appendChild(nameSpan)

                if (feat.description.isNotEmpty()) {
                    val descSpan = document.createElement("div") as HTMLDivElement
                    descSpan.textContent = feat.description
                    descSpan.style.paddingLeft = "12px"
                    descSpan.style.color = "#777"
                    fDiv.appendChild(descSpan)
                }

                resultsDiv.appendChild(fDiv)
            }
        }

        statEntries.filter { it.name.lowercase().contains(query) }.forEach { entry ->
            val mod = entry.value?.let { calcModifier(it) } ?: 0
            val modStr = if (mod >= 0) "+$mod" else "$mod"
            val saveVal = mod + (if (entry.hasSave) prof else 0)
            val saveStr = if (saveVal >= 0) "+$saveVal" else "$saveVal"

            val div = document.createElement("div") as HTMLDivElement
            div.style.marginBottom = "4px"
            div.style.fontSize = "13px"

            val testLine = document.createElement("div") as HTMLDivElement
            testLine.textContent = "${t("playing.search.test")} ${entry.name}: $modStr"
            div.appendChild(testLine)

            val saveLine = document.createElement("div") as HTMLDivElement
            saveLine.textContent = "${t("playing.search.save")} ${entry.name}: $saveStr"
            div.appendChild(saveLine)

            resultsDiv.appendChild(div)
            renderFeaturesByTag(entry.name)
        }

        // Skills
        val skills = Repos.skills.getByCharacterId(character.id) ?: DndSkills(characterId = character.id)
        DndSkills.skillNames.forEach { skillName ->
            val translated = t("skill.$skillName")
            if (!translated.lowercase().contains(query)) return@forEach
            val entry = skills.getEntry(skillName)
            val valStr = if (entry.totalValue >= 0) "+${entry.totalValue}" else "${entry.totalValue}"
            val trainedStr = if (entry.isTrained) t("playing.search.trained") else t("playing.search.untrained")

            val div = document.createElement("div") as HTMLDivElement
            div.style.marginBottom = "4px"
            div.style.fontSize = "13px"
            div.textContent = "${t("playing.search.test")} $translated ($trainedStr): $valStr"
            resultsDiv.appendChild(div)
            renderFeaturesByTag(translated)
        }
    })
}
