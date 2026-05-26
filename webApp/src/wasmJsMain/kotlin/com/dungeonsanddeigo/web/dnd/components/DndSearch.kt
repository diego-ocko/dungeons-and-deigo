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

    fun renderWeapon(weapon: DndWeapon, target: HTMLDivElement, indent: Boolean = false, weaponCatProfs: Set<String> = emptySet(), weaponSpecificProfs: Set<String> = emptySet(), onEquippedFound: () -> Unit = {}) {
        if (weapon.isEquipped) onEquippedFound()
        val wDiv = document.createElement("div") as HTMLDivElement
        wDiv.style.fontSize = "12px"
        wDiv.style.color = "#555"
        wDiv.style.marginBottom = "4px"
        if (indent) wDiv.style.paddingLeft = "12px"

        val hasProf = weapon.category in weaponCatProfs || weapon.weaponType in weaponSpecificProfs
        val nameSpan = document.createElement("div") as HTMLDivElement
        val equippedStr = if (weapon.isEquipped) " [${t("playing.search.equipped")}]" else " [${t("playing.search.unequipped")}]"
        val warnStr = if (!hasProf) " \u26A0\uFE0F" else ""
        nameSpan.textContent = "\u2694\uFE0F ${weapon.name}$equippedStr$warnStr"
        nameSpan.style.fontWeight = "bold"
        wDiv.appendChild(nameSpan)

        val props = mutableListOf<String>()
        if (weapon.ammunition) props.add(t("inv.ammunition"))
        if (weapon.finesse) props.add(t("inv.finesse"))
        if (weapon.heavy) props.add(t("inv.heavy"))
        if (weapon.light) props.add(t("inv.light"))
        if (weapon.loading) props.add(t("inv.loading"))
        if (weapon.range) props.add(t("inv.range"))
        if (weapon.reach) props.add(t("inv.reach"))
        if (weapon.thrown) props.add(t("inv.thrown"))
        if (weapon.twoHanded) props.add(t("inv.twoHanded"))
        if (weapon.versatile) props.add(t("inv.versatile"))
        if (weapon.silver) props.add(t("inv.silver"))
        if (weapon.special) props.add(t("inv.special"))

        if (props.isNotEmpty()) {
            val propsSpan = document.createElement("div") as HTMLDivElement
            propsSpan.textContent = props.joinToString(", ")
            propsSpan.style.paddingLeft = "12px"
            propsSpan.style.color = "#777"
            wDiv.appendChild(propsSpan)
        }

        if (weapon.additionalFeatures.isNotEmpty()) {
            val addSpan = document.createElement("div") as HTMLDivElement
            addSpan.textContent = weapon.additionalFeatures
            addSpan.style.paddingLeft = "12px"
            addSpan.style.color = "#777"
            addSpan.style.fontStyle = "italic"
            wDiv.appendChild(addSpan)
        }

        val infoSpan = document.createElement("div") as HTMLDivElement
        infoSpan.textContent = "${weapon.price} ${weapon.priceCurrency} | ${weapon.weight} kg"
        infoSpan.style.paddingLeft = "12px"
        infoSpan.style.color = "#999"
        infoSpan.style.fontSize = "11px"
        wDiv.appendChild(infoSpan)

        target.appendChild(wDiv)
    }

    fun renderArmor(armor: DndArmor, target: HTMLDivElement, indent: Boolean = false, armorProfs: Set<String> = emptySet(), strValue: Int? = null) {
        val aDiv = document.createElement("div") as HTMLDivElement
        aDiv.style.fontSize = "12px"
        aDiv.style.color = "#555"
        aDiv.style.marginBottom = "4px"
        if (indent) aDiv.style.paddingLeft = "12px"

        val profKey = when (armor.type) {
            "Light Armor" -> "Light"
            "Medium Armor" -> "Medium"
            "Heavy Armor" -> "Heavy"
            "Shield" -> "Shields"
            else -> null
        }
        val hasProf = armor.type == "Clothes" || (profKey != null && profKey in armorProfs)
        val lacksStr = armor.minimumStrength > 0 && (strValue ?: 0) < armor.minimumStrength
        val showWarning = !hasProf || lacksStr

        val typeStr = when (armor.type) {
            "Light Armor" -> t("inv.lightArmor")
            "Medium Armor" -> t("inv.mediumArmor")
            "Heavy Armor" -> t("inv.heavyArmor")
            "Shield" -> t("inv.shield")
            "Clothes" -> t("inv.clothes")
            else -> armor.type
        }
        val equippedStr = if (armor.isEquipped) " [${t("playing.search.equipped")}]" else " [${t("playing.search.unequipped")}]"
        val warnStr = if (showWarning) " \u26A0\uFE0F" else ""

        val nameSpan = document.createElement("div") as HTMLDivElement
        nameSpan.textContent = "\uD83D\uDEE1\uFE0F ${armor.name} ($typeStr)$equippedStr$warnStr"
        nameSpan.style.fontWeight = "bold"
        aDiv.appendChild(nameSpan)

        if (armor.hasSneakDisadvantage) {
            val sneakSpan = document.createElement("div") as HTMLDivElement
            sneakSpan.textContent = t("inv.sneakDisadvShort")
            sneakSpan.style.paddingLeft = "12px"
            sneakSpan.style.color = "#c00"
            aDiv.appendChild(sneakSpan)
        }

        if (armor.additionalFeatures.isNotEmpty()) {
            val addSpan = document.createElement("div") as HTMLDivElement
            addSpan.textContent = armor.additionalFeatures
            addSpan.style.paddingLeft = "12px"
            addSpan.style.color = "#777"
            addSpan.style.fontStyle = "italic"
            aDiv.appendChild(addSpan)
        }

        val infoSpan = document.createElement("div") as HTMLDivElement
        infoSpan.textContent = "${armor.price} ${armor.priceCurrency} | ${armor.weight} kg"
        infoSpan.style.paddingLeft = "12px"
        infoSpan.style.color = "#999"
        infoSpan.style.fontSize = "11px"
        aDiv.appendChild(infoSpan)

        target.appendChild(aDiv)
    }

    fun renderMagicItem(item: DndMagicItem, target: HTMLDivElement, indent: Boolean = false) {
        val mDiv = document.createElement("div") as HTMLDivElement
        mDiv.style.fontSize = "12px"
        mDiv.style.color = "#555"
        mDiv.style.marginBottom = "4px"
        if (indent) mDiv.style.paddingLeft = "12px"

        val nameSpan = document.createElement("div") as HTMLDivElement
        val synchStr = if (item.needSynch) {
            if (item.isSynched) " [${t("playing.search.synched")}]" else " [${t("playing.search.needsSynch")}] \u26A0\uFE0F"
        } else ""
        nameSpan.textContent = "\u2728 ${item.name}$synchStr"
        nameSpan.style.fontWeight = "bold"
        mDiv.appendChild(nameSpan)

        if (item.effect.isNotEmpty()) {
            val effectSpan = document.createElement("div") as HTMLDivElement
            effectSpan.textContent = item.effect
            effectSpan.style.paddingLeft = "12px"
            effectSpan.style.color = "#777"
            effectSpan.style.fontStyle = "italic"
            mDiv.appendChild(effectSpan)
        }

        val infoSpan = document.createElement("div") as HTMLDivElement
        infoSpan.textContent = "${item.price} ${item.priceCurrency} | ${item.weight} kg"
        infoSpan.style.paddingLeft = "12px"
        infoSpan.style.color = "#999"
        infoSpan.style.fontSize = "11px"
        mDiv.appendChild(infoSpan)

        target.appendChild(mDiv)
    }

    fun renderConsumable(item: DndConsumable, target: HTMLDivElement, indent: Boolean = false) {
        val cDiv = document.createElement("div") as HTMLDivElement
        cDiv.style.fontSize = "12px"
        cDiv.style.color = "#555"
        cDiv.style.marginBottom = "4px"
        if (indent) cDiv.style.paddingLeft = "12px"

        val typeStr = when (item.type) {
            "Healing Potion" -> t("inv.consumable.healingPotion")
            "Magic Potion" -> t("inv.consumable.magicPotion")
            "Food" -> t("inv.consumable.food")
            "Ammunition" -> t("inv.consumable.ammunition")
            "Other" -> t("inv.consumable.other")
            else -> item.type
        }
        val icon = when (item.type) {
            "Healing Potion", "Magic Potion" -> "\uD83E\uDDEA"
            "Food" -> "\uD83C\uDF56"
            "Ammunition" -> "\uD83C\uDFF9"
            else -> "\uD83D\uDCE6"
        }

        val nameSpan = document.createElement("div") as HTMLDivElement
        nameSpan.textContent = "$icon ${item.name} ($typeStr)"
        nameSpan.style.fontWeight = "bold"
        cDiv.appendChild(nameSpan)

        if (item.effect.isNotEmpty()) {
            val effectSpan = document.createElement("div") as HTMLDivElement
            effectSpan.textContent = item.effect
            effectSpan.style.paddingLeft = "12px"
            effectSpan.style.color = "#777"
            effectSpan.style.fontStyle = "italic"
            cDiv.appendChild(effectSpan)
        }

        val infoSpan = document.createElement("div") as HTMLDivElement
        infoSpan.textContent = "${item.price} ${item.priceCurrency} | ${item.weight} kg"
        infoSpan.style.paddingLeft = "12px"
        infoSpan.style.color = "#999"
        infoSpan.style.fontSize = "11px"
        cDiv.appendChild(infoSpan)

        target.appendChild(cDiv)
    }

    fun renderKeyItem(item: DndInventoryItem, target: HTMLDivElement, indent: Boolean = false) {
        val kDiv = document.createElement("div") as HTMLDivElement
        kDiv.style.fontSize = "12px"
        kDiv.style.color = "#555"
        kDiv.style.marginBottom = "4px"
        if (indent) kDiv.style.paddingLeft = "12px"

        val nameSpan = document.createElement("div") as HTMLDivElement
        nameSpan.textContent = "\uD83D\uDD11 ${item.name}"
        nameSpan.style.fontWeight = "bold"
        kDiv.appendChild(nameSpan)

        if (item.description.isNotEmpty()) {
            val descSpan = document.createElement("div") as HTMLDivElement
            descSpan.textContent = item.description
            descSpan.style.paddingLeft = "12px"
            descSpan.style.color = "#777"
            descSpan.style.fontStyle = "italic"
            kDiv.appendChild(descSpan)
        }

        val infoSpan = document.createElement("div") as HTMLDivElement
        infoSpan.textContent = "${item.price} ${item.priceCurrency} | ${item.weight} kg"
        infoSpan.style.paddingLeft = "12px"
        infoSpan.style.color = "#999"
        infoSpan.style.fontSize = "11px"
        kDiv.appendChild(infoSpan)

        target.appendChild(kDiv)
    }

    fun renderFeature(feat: DndFeature, target: HTMLDivElement, indent: Boolean = false) {
        val fDiv = document.createElement("div") as HTMLDivElement
        fDiv.style.fontSize = "12px"
        fDiv.style.color = "#555"
        fDiv.style.marginBottom = "4px"
        if (indent) fDiv.style.paddingLeft = "12px"

        val nameSpan = document.createElement("div") as HTMLDivElement
        val displayName = when (feat.type) {
            "Idiom" -> t("features.idiom")
            "Tool Proficiency" -> t("features.toolProf")
            "Weapon/Armor Proficiency" -> t("features.weaponArmorProf")
            else -> feat.name
        }
        nameSpan.textContent = "\u2022 $displayName"
        nameSpan.style.fontWeight = "bold"
        fDiv.appendChild(nameSpan)

        if (feat.description.isNotEmpty()) {
            val descSpan = document.createElement("div") as HTMLDivElement
            descSpan.style.paddingLeft = "12px"
            descSpan.style.color = "#777"

            descSpan.textContent = when (feat.type) {
                "Idiom" -> {
                    val translated = feat.description.split(",").map { tIdiom(it.trim()) }.joinToString(", ")
                    "${t("features.languages")}: $translated"
                }
                "Weapon/Armor Proficiency" -> {
                    val parts = feat.description.split(",").map { it.trim() }
                    val armors = parts.filter { it.startsWith("armor:") }.map { it.removePrefix("armor:") }
                    val cats = parts.filter { it.startsWith("weapon_cat:") }.map { it.removePrefix("weapon_cat:") }
                    val weapons = parts.filter { it.startsWith("weapon:") }.map { it.removePrefix("weapon:") }
                    val lines = mutableListOf<String>()
                    if (armors.isNotEmpty()) lines.add("${t("features.armor")}: ${armors.joinToString(", ") { when(it) { "Light" -> t("features.armor.light"); "Medium" -> t("features.armor.medium"); "Heavy" -> t("features.armor.heavy"); "Shields" -> t("features.armor.shields"); else -> it } }}")
                    if (cats.isNotEmpty()) lines.add("${t("features.weaponCategories")}: ${cats.joinToString(", ") { when(it) { "Simple" -> t("features.weapon.simple"); "Martial" -> t("features.weapon.martial"); else -> it } }}")
                    if (weapons.isNotEmpty()) lines.add("${t("features.individualWeapons")}: ${weapons.joinToString(", ") { tWeapon(it) }}")
                    lines.joinToString(" | ")
                }
                else -> feat.description
            }

            fDiv.appendChild(descSpan)
        }

        target.appendChild(fDiv)
    }

    searchInput.addEventListener("input", {
        val query = searchInput.value.trim().lowercase()
        resultsDiv.innerHTML = ""

        // Clear all highlights
        document.querySelectorAll("[data-feature-id]").let { nodes ->
            for (i in 0 until nodes.length) {
                (nodes.item(i) as? HTMLDivElement)?.classList?.remove("focused")
            }
        }
        document.querySelectorAll(".weapon-atk-row").let { nodes ->
            for (i in 0 until nodes.length) {
                (nodes.item(i) as? HTMLElement)?.classList?.remove("focused")
            }
        }
        document.querySelectorAll("[data-consumable-id]").let { nodes ->
            for (i in 0 until nodes.length) {
                (nodes.item(i) as? HTMLDivElement)?.classList?.remove("focused")
            }
        }

        if (query.length < 3) return@addEventListener

        val highlightIds = mutableSetOf<Long>()
        val highlightConsumableIds = mutableSetOf<Long>()
        var highlightAttacks = false

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
        val weapons = Repos.weapon.getByCharacterId(character.id)
        val shownWeaponIds = mutableSetOf<Long>()

        // Gather weapon proficiencies
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

        val armors = Repos.armor.getByCharacterId(character.id)
        val shownArmorIds = mutableSetOf<Long>()
        val magicItems = Repos.magicItem.getByCharacterId(character.id)
        val shownMagicItemIds = mutableSetOf<Long>()
        val consumables = Repos.consumable.getByCharacterId(character.id)
        val shownConsumableIds = mutableSetOf<Long>()
        val keyItems = Repos.inventory.getByCategory(character.id, "Key Items, Loot and others")
        val shownKeyItemIds = mutableSetOf<Long>()

        fun renderConsumablesByTag(tag: String) {
            consumables.filter { c -> c.tags.any { it.lowercase() == tag.lowercase() } && c.id !in shownConsumableIds }.forEach { c ->
                shownConsumableIds.add(c.id)
                highlightConsumableIds.add(c.id)
                renderConsumable(c, resultsDiv, indent = true)
            }
        }

        fun renderKeyItemsByTag(tag: String) {
            keyItems.filter { k -> k.tags.any { it.lowercase() == tag.lowercase() } && k.id !in shownKeyItemIds }.forEach { k ->
                shownKeyItemIds.add(k.id)
                renderKeyItem(k, resultsDiv, indent = true)
            }
        }

        fun renderMagicItemsByTag(tag: String) {
            magicItems.filter { m -> m.tags.any { it.lowercase() == tag.lowercase() } && m.id !in shownMagicItemIds }.forEach { m ->
                shownMagicItemIds.add(m.id)
                renderMagicItem(m, resultsDiv, indent = true)
            }
            renderConsumablesByTag(tag)
            renderKeyItemsByTag(tag)
        }

        fun renderArmorsByTag(tag: String) {
            armors.filter { a -> a.tags.any { it.lowercase() == tag.lowercase() } && a.id !in shownArmorIds }.forEach { a ->
                shownArmorIds.add(a.id)
                renderArmor(a, resultsDiv, indent = true, armorProfs = armorProfs, strValue = stats.strValue)
            }
            renderMagicItemsByTag(tag)
        }

        fun renderWeaponsByTag(tag: String) {
            weapons.filter { w -> w.tags.any { it.lowercase() == tag.lowercase() } && w.id !in shownWeaponIds }.forEach { w ->
                shownWeaponIds.add(w.id)
                renderWeapon(w, resultsDiv, indent = true, weaponCatProfs = weaponCatProfs, weaponSpecificProfs = weaponSpecificProfs) { highlightAttacks = true }
            }
            renderArmorsByTag(tag)
        }

        fun renderFeaturesByTag(tag: String) {
            val matching = features.filter { feat -> feat.tags.any { it.lowercase() == tag.lowercase() } }
            matching.forEach { feat ->
                if (feat.type == "Rechargable Feature") highlightIds.add(feat.id)
                renderFeature(feat, resultsDiv, indent = true)
            }
            renderWeaponsByTag(tag)
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

        // Features by name match or translated type match
        val alreadyRendered = mutableSetOf<Long>()
        val typeTranslations = mapOf(
            "Idiom" to t("features.idiom"),
            "Tool Proficiency" to t("features.toolProf"),
            "Weapon/Armor Proficiency" to t("features.weaponArmorProf")
        )

        // Combined results for Idiom, Tool, Weapon/Armor
        val combinedTypes = listOf("Idiom", "Tool Proficiency", "Weapon/Armor Proficiency")
        combinedTypes.forEach { type ->
            val translatedType = typeTranslations[type] ?: return@forEach
            if (!translatedType.lowercase().contains(query)) return@forEach
            val matching = features.filter { it.type == type }
            if (matching.isEmpty()) return@forEach
            matching.forEach { alreadyRendered.add(it.id) }

            val fDiv = document.createElement("div") as HTMLDivElement
            fDiv.style.fontSize = "12px"
            fDiv.style.color = "#555"
            fDiv.style.marginBottom = "4px"

            val nameSpan = document.createElement("div") as HTMLDivElement
            nameSpan.textContent = "\u2022 $translatedType"
            nameSpan.style.fontWeight = "bold"
            fDiv.appendChild(nameSpan)

            val descSpan = document.createElement("div") as HTMLDivElement
            descSpan.style.paddingLeft = "12px"
            descSpan.style.color = "#777"

            descSpan.textContent = when (type) {
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
                    val armors = allParts.filter { it.startsWith("armor:") }.map { it.removePrefix("armor:") }.distinct()
                    val cats = allParts.filter { it.startsWith("weapon_cat:") }.map { it.removePrefix("weapon_cat:") }.distinct()
                    val weapons = allParts.filter { it.startsWith("weapon:") }.map { it.removePrefix("weapon:") }.distinct()
                    val lines = mutableListOf<String>()
                    if (armors.isNotEmpty()) lines.add("${t("features.armor")}: ${armors.joinToString(", ") { when(it) { "Light" -> t("features.armor.light"); "Medium" -> t("features.armor.medium"); "Heavy" -> t("features.armor.heavy"); "Shields" -> t("features.armor.shields"); else -> it } }}")
                    if (cats.isNotEmpty()) lines.add("${t("features.weaponCategories")}: ${cats.joinToString(", ") { when(it) { "Simple" -> t("features.weapon.simple"); "Martial" -> t("features.weapon.martial"); else -> it } }}")
                    if (weapons.isNotEmpty()) lines.add("${t("features.individualWeapons")}: ${weapons.joinToString(", ") { tWeapon(it) }}")
                    lines.joinToString(" | ")
                }
                else -> ""
            }

            fDiv.appendChild(descSpan)
            resultsDiv.appendChild(fDiv)
        }

        // Other features by name match
        val nameMatched = features.filter { it.id !in alreadyRendered && it.name.lowercase().contains(query) }
        nameMatched.forEach { feat ->
            alreadyRendered.add(feat.id)
            if (feat.type == "Rechargable Feature") highlightIds.add(feat.id)
            renderFeature(feat, resultsDiv)

            // Features that have this feature's name as a tag
            features.filter { it.id !in alreadyRendered && it.tags.any { t -> t.lowercase() == feat.name.lowercase() } }.forEach { related ->
                alreadyRendered.add(related.id)
                if (related.type == "Rechargable Feature") highlightIds.add(related.id)
                renderFeature(related, resultsDiv, indent = true)
            }
            // Weapons that have this feature's name as a tag
            renderWeaponsByTag(feat.name)
            // Armors that have this feature's name as a tag
            renderArmorsByTag(feat.name)
            // Magic items that have this feature's name as a tag
            renderMagicItemsByTag(feat.name)
            // Consumables that have this feature's name as a tag
            renderConsumablesByTag(feat.name)
        }

        // Features with a tag partially matching the query
        features.filter { it.id !in alreadyRendered && it.tags.any { tag -> tag.lowercase().contains(query) } }.forEach { feat ->
            alreadyRendered.add(feat.id)
            if (feat.type == "Rechargable Feature") highlightIds.add(feat.id)
            renderFeature(feat, resultsDiv)
        }

        // Weapons by name match
        weapons.filter { it.id !in shownWeaponIds && it.name.lowercase().contains(query) }.forEach { w ->
            shownWeaponIds.add(w.id)
            renderWeapon(w, resultsDiv, weaponCatProfs = weaponCatProfs, weaponSpecificProfs = weaponSpecificProfs) { highlightAttacks = true }
            // Armors that have this weapon's name as a tag
            renderArmorsByTag(w.name)
        }

        // Weapons by tag partial match
        weapons.filter { it.id !in shownWeaponIds && it.tags.any { tag -> tag.lowercase().contains(query) } }.forEach { w ->
            shownWeaponIds.add(w.id)
            renderWeapon(w, resultsDiv, weaponCatProfs = weaponCatProfs, weaponSpecificProfs = weaponSpecificProfs) { highlightAttacks = true }
        }

        // Armors by name match
        armors.filter { it.id !in shownArmorIds && it.name.lowercase().contains(query) }.forEach { a ->
            shownArmorIds.add(a.id)
            renderArmor(a, resultsDiv, armorProfs = armorProfs, strValue = stats.strValue)
        }

        // Armors by tag partial match
        armors.filter { it.id !in shownArmorIds && it.tags.any { tag -> tag.lowercase().contains(query) } }.forEach { a ->
            shownArmorIds.add(a.id)
            renderArmor(a, resultsDiv, armorProfs = armorProfs, strValue = stats.strValue)
        }

        // Magic items by name match
        magicItems.filter { it.id !in shownMagicItemIds && it.name.lowercase().contains(query) }.forEach { m ->
            shownMagicItemIds.add(m.id)
            renderMagicItem(m, resultsDiv)
        }

        // Magic items by tag partial match
        magicItems.filter { it.id !in shownMagicItemIds && it.tags.any { tag -> tag.lowercase().contains(query) } }.forEach { m ->
            shownMagicItemIds.add(m.id)
            renderMagicItem(m, resultsDiv)
        }

        // Consumables by name match
        consumables.filter { it.id !in shownConsumableIds && it.name.lowercase().contains(query) }.forEach { c ->
            shownConsumableIds.add(c.id)
            highlightConsumableIds.add(c.id)
            renderConsumable(c, resultsDiv)
        }

        // Consumables by tag partial match
        consumables.filter { it.id !in shownConsumableIds && it.tags.any { tag -> tag.lowercase().contains(query) } }.forEach { c ->
            shownConsumableIds.add(c.id)
            highlightConsumableIds.add(c.id)
            renderConsumable(c, resultsDiv)
        }

        // Key items by name match
        keyItems.filter { it.id !in shownKeyItemIds && it.name.lowercase().contains(query) }.forEach { k ->
            shownKeyItemIds.add(k.id)
            renderKeyItem(k, resultsDiv)
        }

        // Key items by description match
        keyItems.filter { it.id !in shownKeyItemIds && it.description.lowercase().contains(query) }.forEach { k ->
            shownKeyItemIds.add(k.id)
            renderKeyItem(k, resultsDiv)
        }

        // Key items by tag partial match
        keyItems.filter { it.id !in shownKeyItemIds && it.tags.any { tag -> tag.lowercase().contains(query) } }.forEach { k ->
            shownKeyItemIds.add(k.id)
            renderKeyItem(k, resultsDiv)
        }

        // Highlight rechargeable features in Special Actions
        highlightIds.forEach { id ->
            document.querySelector("[data-feature-id='$id']")?.let {
                (it as HTMLDivElement).classList.add("focused")
            }
        }

        // Highlight attack rows if equipped weapon found
        if (highlightAttacks) {
            document.querySelectorAll(".weapon-atk-row").let { nodes ->
                for (i in 0 until nodes.length) {
                    (nodes.item(i) as? HTMLElement)?.classList?.add("focused")
                }
            }
        }

        // Highlight consumables in Special Actions
        highlightConsumableIds.forEach { id ->
            document.querySelector("[data-consumable-id='$id']")?.let {
                (it as HTMLDivElement).classList.add("focused")
            }
        }
    })
}
