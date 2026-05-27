package com.dungeonsanddeigo.web.dnd.components.search

import com.dungeonsanddeigo.dnd.rules.calcModifier
import com.dungeonsanddeigo.dnd.rules.calcProficiency
import com.dungeonsanddeigo.i18n.*
import com.dungeonsanddeigo.model.*
import com.dungeonsanddeigo.search.DndSearchEngine
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.components.searchResultRenderer.DndSearchResultRenderer
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.*

class DndSearch(private val character: Character, private val container: HTMLDivElement) {
    private val engine = DndSearchEngine
    private val renderer = DndSearchResultRenderer
    private val searchInput: HTMLInputElement
    private val notesCheckbox: HTMLInputElement?
    private val plainCheckbox: HTMLInputElement?
    private val resultsDiv: HTMLDivElement

    init {
        val searchRow = document.createElement("div") as HTMLDivElement
        searchRow.className = "search-row"

        searchInput = document.createElement("input") as HTMLInputElement
        searchInput.type = "text"
        searchInput.placeholder = "Search..."
        searchRow.appendChild(searchInput)

        val clearBtn = document.createElement("button") as HTMLButtonElement
        clearBtn.textContent = "\uD83E\uDDF9"
        clearBtn.addEventListener("click", {
            searchInput.value = ""
            searchInput.dispatchEvent(org.w3c.dom.events.Event("input"))
        })
        searchRow.appendChild(clearBtn)
        container.appendChild(searchRow)

        addCheckbox(t("playing.search.notesCheckbox"), "search-notes")
        addCheckbox(t("playing.search.plainCheckbox"), "search-plain")

        resultsDiv = document.createElement("div") as HTMLDivElement
        resultsDiv.className = "search-results"
        container.appendChild(resultsDiv)

        notesCheckbox = container.querySelector("#search-notes") as? HTMLInputElement
        plainCheckbox = container.querySelector("#search-plain") as? HTMLInputElement

        notesCheckbox?.addEventListener("change", { onSearch() })
        plainCheckbox?.addEventListener("change", { onSearch() })
        searchInput.addEventListener("input", { onSearch() })
    }

    private fun addCheckbox(label: String, id: String) {
        val row = document.createElement("div") as HTMLDivElement
        row.className = "search-checkbox"
        val cb = document.createElement("input") as HTMLInputElement
        cb.type = "checkbox"
        cb.id = id
        row.appendChild(cb)
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        lbl.htmlFor = id
        row.appendChild(lbl)
        container.appendChild(row)
    }

    private fun clearHighlights() {
        listOf("[data-feature-id]", ".weapon-atk-row", ".magic-atk-row", "[data-consumable-id]", "div[data-spell-id]").forEach { selector ->
            document.querySelectorAll(selector).let { nodes ->
                for (i in 0 until nodes.length) {
                    (nodes.item(i) as? HTMLElement)?.classList?.remove("focused")
                }
            }
        }
    }

    private fun applyHighlights(featureIds: Set<Long>, consumableIds: Set<Long>, spellIds: Set<Long>, nonAttackSpellIds: Set<Long>, weaponAttacks: Boolean) {
        featureIds.forEach { id ->
            document.querySelector("[data-feature-id='$id']")?.let { (it as HTMLElement).classList.add("focused") }
        }
        consumableIds.forEach { id ->
            document.querySelector("[data-consumable-id='$id']")?.let { (it as HTMLElement).classList.add("focused") }
        }
        spellIds.forEach { id ->
            document.querySelector(".magic-atk-row[data-spell-id='$id']")?.let { (it as HTMLElement).classList.add("focused") }
        }
        nonAttackSpellIds.forEach { id ->
            document.querySelector("div[data-spell-id='$id']")?.let { (it as HTMLElement).classList.add("focused") }
        }
        if (weaponAttacks) {
            document.querySelectorAll(".weapon-atk-row").let { nodes ->
                for (i in 0 until nodes.length) { (nodes.item(i) as? HTMLElement)?.classList?.add("focused") }
            }
        }
    }

    private fun onSearch() {
        val query = searchInput.value.trim().lowercase()
        resultsDiv.innerHTML = ""
        clearHighlights()

        if (query.length < 3) return

        if (notesCheckbox?.checked == true) {
            searchNotes(query)
        } else {
            searchMain(query)
        }
    }

    private fun searchNotes(query: String) {
        val plainSearch = plainCheckbox?.checked == true
        val appearance = Repos.appearance.getByCharacterId(character.id)
        val backstory = Repos.backstory.getByCharacterId(character.id)
        val notes = Repos.note.getByCharacterId(character.id)

        val appearanceLabels = if (appearance != null) listOf(
            t("bg.age") to appearance.age,
            t("bg.height") to appearance.height,
            t("bg.weight") to appearance.weight,
            t("bg.appearanceDesc") to appearance.description
        ) else emptyList()

        val backstoryLabels = if (backstory != null) listOf(
            t("bg.personalityTraits") to backstory.personalityTraits,
            t("bg.ideals") to backstory.ideals,
            t("bg.bonds") to backstory.bonds,
            t("bg.defects") to backstory.defects,
            t("bg.habits") to backstory.habits,
            t("bg.factionName") to backstory.factionName,
            t("bg.factionBackstory") to backstory.factionBackstory,
            t("bg.charBackstory") to backstory.characterBackstory
        ) else emptyList()

        val results = engine.searchNotes(query, plainSearch, appearance, backstory, notes, appearanceLabels, backstoryLabels)

        results.appearanceFields.forEach { renderer.renderNoteField(it.icon, it.label, it.value, resultsDiv) }
        results.backstoryFields.forEach { renderer.renderNoteField(it.icon, it.label, it.value, resultsDiv) }
        results.notes.forEach { renderer.renderNote(it, resultsDiv) }
    }

    private fun searchMain(query: String) {
        val plainSearch = plainCheckbox?.checked == true
        val stats = Repos.baseStats.getByCharacterId(character.id) ?: DndBaseStats(characterId = character.id)
        val mainInfo = Repos.mainInfo.getByCharacterId(character.id)
        val totalLevel = (mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0)
        val prof = calcProficiency(totalLevel)

        val magicAbility = DungeonsAndDragons.spellcastingAbilityFor(mainInfo?.mainClass, mainInfo?.mainSubClass)
            ?: localStorage.getItem("dnd_custom_spell_ability_${character.id}_${mainInfo?.mainClass}")?.takeIf { it != "__none__" }
        val spellAbilityMod = when (magicAbility) {
            "Cha" -> stats.chaValue?.let { calcModifier(it) } ?: 0
            "Int" -> stats.intValue?.let { calcModifier(it) } ?: 0
            "Wis" -> stats.wisValue?.let { calcModifier(it) } ?: 0
            else -> 0
        }
        val spellDC = spellAbilityMod + prof + 8

        val features = Repos.features.getByCharacterId(character.id)
        val weapons = Repos.weapon.getByCharacterId(character.id)
        val armors = Repos.armor.getByCharacterId(character.id)
        val magicItems = Repos.magicItem.getByCharacterId(character.id)
        val consumables = Repos.consumable.getByCharacterId(character.id)
        val keyItems = Repos.inventory.getByCategory(character.id, "Key Items, Loot and others")
        val spells = Repos.spell.getByCharacterId(character.id)

        // Proficiencies
        val weaponCatProfs = mutableSetOf<String>()
        val weaponSpecificProfs = mutableSetOf<String>()
        val armorProfs = mutableSetOf<String>()
        features.filter { it.type == "Weapon/Armor Proficiency" }.forEach { f ->
            f.description.split(",").map { it.trim() }.forEach { entry ->
                when {
                    entry.startsWith("weapon_cat:") -> weaponCatProfs.add(entry.removePrefix("weapon_cat:"))
                    entry.startsWith("weapon:") -> weaponSpecificProfs.add(entry.removePrefix("weapon:"))
                    entry.startsWith("armor:") -> armorProfs.add(entry.removePrefix("armor:"))
                }
            }
        }

        // Tracking sets
        val shownWeaponIds = mutableSetOf<Long>()
        val shownArmorIds = mutableSetOf<Long>()
        val shownMagicItemIds = mutableSetOf<Long>()
        val shownConsumableIds = mutableSetOf<Long>()
        val shownKeyItemIds = mutableSetOf<Long>()
        val shownSpellIds = mutableSetOf<Long>()
        val alreadyRendered = mutableSetOf<Long>()

        // Highlight tracking
        val highlightFeatureIds = mutableSetOf<Long>()
        val highlightConsumableIds = mutableSetOf<Long>()
        val highlightSpellIds = mutableSetOf<Long>()
        val highlightNonAttackSpellIds = mutableSetOf<Long>()
        var highlightWeaponAttacks = false

        // Helper to render all items by tag
        fun renderAllByTag(tag: String) {
            engine.filterFeaturesByTag(tag, features, alreadyRendered, plainSearch).forEach { feat ->
                alreadyRendered.add(feat.id)
                if (feat.type == "Rechargable Feature") highlightFeatureIds.add(feat.id)
                renderer.renderFeature(feat, resultsDiv, indent = true)
            }
            engine.filterWeaponsByTag(tag, weapons, shownWeaponIds, plainSearch).forEach { w ->
                shownWeaponIds.add(w.id)
                if (w.isEquipped) highlightWeaponAttacks = true
                renderer.renderWeapon(w, resultsDiv, indent = true, weaponCatProfs = weaponCatProfs, weaponSpecificProfs = weaponSpecificProfs)
            }
            engine.filterArmorsByTag(tag, armors, shownArmorIds, plainSearch).forEach { a ->
                shownArmorIds.add(a.id)
                renderer.renderArmor(a, resultsDiv, indent = true, armorProfs = armorProfs, strValue = stats.strValue)
            }
            engine.filterMagicItemsByTag(tag, magicItems, shownMagicItemIds, plainSearch).forEach { m ->
                shownMagicItemIds.add(m.id)
                renderer.renderMagicItem(m, resultsDiv, indent = true)
            }
            engine.filterConsumablesByTag(tag, consumables, shownConsumableIds, plainSearch).forEach { c ->
                shownConsumableIds.add(c.id)
                highlightConsumableIds.add(c.id)
                renderer.renderConsumable(c, resultsDiv, indent = true)
            }
            engine.filterKeyItemsByTag(tag, keyItems, shownKeyItemIds, plainSearch).forEach { k ->
                shownKeyItemIds.add(k.id)
                renderer.renderKeyItem(k, resultsDiv, indent = true)
            }
            engine.filterSpellsByTag(tag, spells, shownSpellIds, plainSearch).forEach { s ->
                shownSpellIds.add(s.id)
                if (s.isAttack) highlightSpellIds.add(s.id) else highlightNonAttackSpellIds.add(s.id)
                renderer.renderSpell(s, resultsDiv, indent = true, spellDC = spellDC)
            }
        }

        // Stats
        data class StatEntry(val name: String, val abbr: String, val value: Int?, val hasSave: Boolean)
        val statEntries = listOf(
            StatEntry(tStat("Strength"), tStat("Str"), stats.strValue, stats.hasStrRes),
            StatEntry(tStat("Dexterity"), tStat("Dex"), stats.dexValue, stats.hasDexRes),
            StatEntry(tStat("Constitution"), tStat("Con"), stats.conValue, stats.hasConRes),
            StatEntry(tStat("Intelligence"), tStat("Int"), stats.intValue, stats.hasIntRes),
            StatEntry(tStat("Wisdom"), tStat("Wis"), stats.wisValue, stats.hasWisRes),
            StatEntry(tStat("Charisma"), tStat("Cha"), stats.chaValue, stats.hasChaRes)
        )

        statEntries.filter { it.name.lowercase().contains(query) }.forEach { entry ->
            val mod = entry.value?.let { calcModifier(it) } ?: 0
            val modStr = if (mod >= 0) "+$mod" else "$mod"
            val saveVal = mod + (if (entry.hasSave) prof else 0)
            val saveStr = if (saveVal >= 0) "+$saveVal" else "$saveVal"
            renderer.renderStat(entry.name, modStr, saveStr, resultsDiv)
            renderAllByTag(entry.name)
            if (plainSearch) renderAllByTag(entry.abbr)
        }

        // Skills
        val skills = Repos.skills.getByCharacterId(character.id) ?: DndSkills(characterId = character.id)
        DndSkills.skillNames.forEach { skillName ->
            val translated = t("skill.$skillName")
            if (!translated.lowercase().contains(query)) return@forEach
            val entry = skills.getEntry(skillName)
            renderer.renderSkill(translated, entry.isTrained, entry.totalValue, resultsDiv)
            renderAllByTag(translated)
            if (plainSearch && translated != skillName) renderAllByTag(skillName)
        }

        // Combined proficiency types
        val typeTranslations = mapOf(
            "Idiom" to t("features.idiom"),
            "Tool Proficiency" to t("features.toolProf"),
            "Weapon/Armor Proficiency" to t("features.weaponArmorProf")
        )
        listOf("Idiom", "Tool Proficiency", "Weapon/Armor Proficiency").forEach { type ->
            val translatedType = typeTranslations[type] ?: return@forEach
            if (!translatedType.lowercase().contains(query)) return@forEach
            val matching = features.filter { it.type == type }
            if (matching.isEmpty()) return@forEach
            matching.forEach { alreadyRendered.add(it.id) }

            val description = when (type) {
                "Idiom" -> {
                    val allIdioms = matching.flatMap { it.description.split(",").map { s -> s.trim() } }.distinct()
                    "${t("features.languages")}: ${allIdioms.map { tIdiom(it) }.joinToString(", ")}"
                }
                "Tool Proficiency" -> {
                    val allTools = matching.flatMap { it.description.split(",").map { s -> s.trim() } }.distinct()
                    "${t("features.tools")}: ${allTools.map { tTool(it) }.joinToString(", ")}"
                }
                "Weapon/Armor Proficiency" -> {
                    val allParts = matching.flatMap { it.description.split(",").map { s -> s.trim() } }
                    val a = allParts.filter { it.startsWith("armor:") }.map { it.removePrefix("armor:") }.distinct()
                    val c = allParts.filter { it.startsWith("weapon_cat:") }.map { it.removePrefix("weapon_cat:") }.distinct()
                    val w = allParts.filter { it.startsWith("weapon:") }.map { it.removePrefix("weapon:") }.distinct()
                    val lines = mutableListOf<String>()
                    if (a.isNotEmpty()) lines.add("${t("features.armor")}: ${a.joinToString(", ") { when(it) { "Light" -> t("features.armor.light"); "Medium" -> t("features.armor.medium"); "Heavy" -> t("features.armor.heavy"); "Shields" -> t("features.armor.shields"); else -> it } }}")
                    if (c.isNotEmpty()) lines.add("${t("features.weaponCategories")}: ${c.joinToString(", ") { when(it) { "Simple" -> t("features.weapon.simple"); "Martial" -> t("features.weapon.martial"); else -> it } }}")
                    if (w.isNotEmpty()) lines.add("${t("features.individualWeapons")}: ${w.joinToString(", ") { tWeapon(it) }}")
                    lines.joinToString(" | ")
                }
                else -> ""
            }
            renderer.renderCombinedProficiency(translatedType, description, resultsDiv)
        }

        // Features by name
        engine.filterFeaturesByName(query, features, alreadyRendered, plainSearch).forEach { feat ->
            alreadyRendered.add(feat.id)
            if (feat.type == "Rechargable Feature") highlightFeatureIds.add(feat.id)
            renderer.renderFeature(feat, resultsDiv)

            engine.findRelatedFeatures(feat, features, alreadyRendered, plainSearch).forEach { related ->
                alreadyRendered.add(related.id)
                if (related.type == "Rechargable Feature") highlightFeatureIds.add(related.id)
                renderer.renderFeature(related, resultsDiv, indent = true)
            }
            renderAllByTag(feat.name)
            feat.tags.forEach { tag -> renderAllByTag(tag) }
        }

        // Features by tag partial match
        features.filter { it.id !in alreadyRendered && engine.filterByTagPartial(query, it.tags) }.forEach { feat ->
            alreadyRendered.add(feat.id)
            if (feat.type == "Rechargable Feature") highlightFeatureIds.add(feat.id)
            renderer.renderFeature(feat, resultsDiv)
        }

        // Weapons by name
        engine.filterWeaponsByName(query, weapons, shownWeaponIds, plainSearch).forEach { w ->
            shownWeaponIds.add(w.id)
            if (w.isEquipped) highlightWeaponAttacks = true
            renderer.renderWeapon(w, resultsDiv, weaponCatProfs = weaponCatProfs, weaponSpecificProfs = weaponSpecificProfs)
            renderAllByTag(w.name)
        }
        // Weapons by tag
        weapons.filter { it.id !in shownWeaponIds && engine.filterByTagPartial(query, it.tags) }.forEach { w ->
            shownWeaponIds.add(w.id)
            if (w.isEquipped) highlightWeaponAttacks = true
            renderer.renderWeapon(w, resultsDiv, weaponCatProfs = weaponCatProfs, weaponSpecificProfs = weaponSpecificProfs)
        }

        // Armors by name
        engine.filterArmorsByName(query, armors, shownArmorIds, plainSearch).forEach { a ->
            shownArmorIds.add(a.id)
            renderer.renderArmor(a, resultsDiv, armorProfs = armorProfs, strValue = stats.strValue)
        }
        // Armors by tag
        armors.filter { it.id !in shownArmorIds && engine.filterByTagPartial(query, it.tags) }.forEach { a ->
            shownArmorIds.add(a.id)
            renderer.renderArmor(a, resultsDiv, armorProfs = armorProfs, strValue = stats.strValue)
        }

        // Magic items by name
        engine.filterMagicItemsByName(query, magicItems, shownMagicItemIds, plainSearch).forEach { m ->
            shownMagicItemIds.add(m.id)
            renderer.renderMagicItem(m, resultsDiv)
        }
        // Magic items by tag
        magicItems.filter { it.id !in shownMagicItemIds && engine.filterByTagPartial(query, it.tags) }.forEach { m ->
            shownMagicItemIds.add(m.id)
            renderer.renderMagicItem(m, resultsDiv)
        }

        // Consumables by name
        engine.filterConsumablesByName(query, consumables, shownConsumableIds, plainSearch).forEach { c ->
            shownConsumableIds.add(c.id)
            highlightConsumableIds.add(c.id)
            renderer.renderConsumable(c, resultsDiv)
        }
        // Consumables by tag
        consumables.filter { it.id !in shownConsumableIds && engine.filterByTagPartial(query, it.tags) }.forEach { c ->
            shownConsumableIds.add(c.id)
            highlightConsumableIds.add(c.id)
            renderer.renderConsumable(c, resultsDiv)
        }

        // Key items by name
        engine.filterKeyItemsByName(query, keyItems, shownKeyItemIds, plainSearch).forEach { k ->
            shownKeyItemIds.add(k.id)
            renderer.renderKeyItem(k, resultsDiv)
        }
        // Key items by tag
        keyItems.filter { it.id !in shownKeyItemIds && engine.filterByTagPartial(query, it.tags) }.forEach { k ->
            shownKeyItemIds.add(k.id)
            renderer.renderKeyItem(k, resultsDiv)
        }

        // Spells by name
        engine.filterSpellsByName(query, spells, shownSpellIds, plainSearch).forEach { s ->
            shownSpellIds.add(s.id)
            if (s.isAttack) highlightSpellIds.add(s.id) else highlightNonAttackSpellIds.add(s.id)
            renderer.renderSpell(s, resultsDiv, spellDC = spellDC)
            renderAllByTag(s.name)
            s.tags.forEach { tag -> renderAllByTag(tag) }
        }
        // Spells by tag
        spells.filter { it.id !in shownSpellIds && engine.filterByTagPartial(query, it.tags) }.forEach { s ->
            shownSpellIds.add(s.id)
            if (s.isAttack) highlightSpellIds.add(s.id) else highlightNonAttackSpellIds.add(s.id)
            renderer.renderSpell(s, resultsDiv, spellDC = spellDC)
            renderAllByTag(s.name)
            s.tags.forEach { tag -> renderAllByTag(tag) }
        }

        applyHighlights(highlightFeatureIds, highlightConsumableIds, highlightSpellIds, highlightNonAttackSpellIds, highlightWeaponAttacks)
    }
}
