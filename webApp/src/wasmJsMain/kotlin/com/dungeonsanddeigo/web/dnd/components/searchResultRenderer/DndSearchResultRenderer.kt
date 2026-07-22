package com.dungeonsanddeigo.web.dnd.components.searchResultRenderer

import com.dungeonsanddeigo.i18n.*
import com.dungeonsanddeigo.model.*
import kotlinx.browser.document
import org.w3c.dom.*

object DndSearchResultRenderer {

    fun renderWeapon(weapon: DndWeapon, target: HTMLDivElement, indent: Boolean = false, weaponCatProfs: Set<String> = emptySet(), weaponSpecificProfs: Set<String> = emptySet()) {
        val wDiv = document.createElement("div") as HTMLDivElement
        wDiv.className = "search-item" + if (indent) " indent" else ""

        val hasProf = weapon.category in weaponCatProfs || weapon.weaponType in weaponSpecificProfs
        val nameSpan = document.createElement("div") as HTMLDivElement
        nameSpan.className = "search-item-name"
        val equippedStr = if (weapon.isEquipped) " [${t("playing.search.equipped")}]" else " [${t("playing.search.unequipped")}]"
        val warnStr = if (!hasProf) " \u26A0\uFE0F" else ""
        nameSpan.textContent = "\u2694\uFE0F ${weapon.name}$equippedStr$warnStr"
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
            propsSpan.className = "search-item-detail"
            propsSpan.textContent = props.joinToString(", ")
            wDiv.appendChild(propsSpan)
        }

        if (weapon.additionalFeatures.isNotEmpty()) {
            val addSpan = document.createElement("div") as HTMLDivElement
            addSpan.className = "search-item-desc"
            addSpan.textContent = weapon.additionalFeatures
            wDiv.appendChild(addSpan)
        }

        val infoSpan = document.createElement("div") as HTMLDivElement
        infoSpan.className = "search-item-info"
        infoSpan.textContent = "${weapon.price} ${weapon.priceCurrency} | ${weapon.weight} kg"
        wDiv.appendChild(infoSpan)

        target.appendChild(wDiv)
    }

    fun renderArmor(armor: DndArmor, target: HTMLDivElement, indent: Boolean = false, armorProfs: Set<String> = emptySet(), strValue: Int? = null) {
        val aDiv = document.createElement("div") as HTMLDivElement
        aDiv.className = "search-item" + if (indent) " indent" else ""

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
        nameSpan.className = "search-item-name"
        nameSpan.textContent = "\uD83D\uDEE1\uFE0F ${armor.name} ($typeStr)$equippedStr$warnStr"
        aDiv.appendChild(nameSpan)

        if (armor.hasSneakDisadvantage) {
            val sneakSpan = document.createElement("div") as HTMLDivElement
            sneakSpan.className = "search-item-warn"
            sneakSpan.textContent = t("inv.sneakDisadvShort")
            aDiv.appendChild(sneakSpan)
        }

        if (armor.additionalFeatures.isNotEmpty()) {
            val addSpan = document.createElement("div") as HTMLDivElement
            addSpan.className = "search-item-desc"
            addSpan.textContent = armor.additionalFeatures
            aDiv.appendChild(addSpan)
        }

        val infoSpan = document.createElement("div") as HTMLDivElement
        infoSpan.className = "search-item-info"
        infoSpan.textContent = "${armor.price} ${armor.priceCurrency} | ${armor.weight} kg"
        aDiv.appendChild(infoSpan)

        target.appendChild(aDiv)
    }

    fun renderMagicItem(item: DndMagicItem, target: HTMLDivElement, indent: Boolean = false) {
        val mDiv = document.createElement("div") as HTMLDivElement
        mDiv.className = "search-item" + if (indent) " indent" else ""

        val nameSpan = document.createElement("div") as HTMLDivElement
        nameSpan.className = "search-item-name"
        val synchStr = if (item.needSynch) {
            if (item.isSynched) " [${t("playing.search.synched")}]" else " [${t("playing.search.needsSynch")}] \u26A0\uFE0F"
        } else ""
        nameSpan.textContent = "\u2728 ${item.name}$synchStr"
        mDiv.appendChild(nameSpan)

        if (item.effect.isNotEmpty()) {
            val effectSpan = document.createElement("div") as HTMLDivElement
            effectSpan.className = "search-item-desc"
            effectSpan.textContent = item.effect
            mDiv.appendChild(effectSpan)
        }

        val infoSpan = document.createElement("div") as HTMLDivElement
        infoSpan.className = "search-item-info"
        infoSpan.textContent = "${item.price} ${item.priceCurrency} | ${item.weight} kg"
        mDiv.appendChild(infoSpan)

        target.appendChild(mDiv)
    }

    fun renderConsumable(item: DndConsumable, target: HTMLDivElement, indent: Boolean = false) {
        val cDiv = document.createElement("div") as HTMLDivElement
        cDiv.className = "search-item" + if (indent) " indent" else ""

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
        nameSpan.className = "search-item-name"
        nameSpan.textContent = "$icon ${item.name} ($typeStr)"
        cDiv.appendChild(nameSpan)

        if (item.effect.isNotEmpty()) {
            val effectSpan = document.createElement("div") as HTMLDivElement
            effectSpan.className = "search-item-desc"
            effectSpan.textContent = item.effect
            cDiv.appendChild(effectSpan)
        }

        val infoSpan = document.createElement("div") as HTMLDivElement
        infoSpan.className = "search-item-info"
        infoSpan.textContent = "${item.price} ${item.priceCurrency} | ${item.weight} kg"
        cDiv.appendChild(infoSpan)

        target.appendChild(cDiv)
    }

    fun renderKeyItem(item: DndInventoryItem, target: HTMLDivElement, indent: Boolean = false) {
        val kDiv = document.createElement("div") as HTMLDivElement
        kDiv.className = "search-item" + if (indent) " indent" else ""

        val nameSpan = document.createElement("div") as HTMLDivElement
        nameSpan.className = "search-item-name"
        nameSpan.textContent = "\uD83D\uDD11 ${item.name}"
        kDiv.appendChild(nameSpan)

        if (item.description.isNotEmpty()) {
            val descSpan = document.createElement("div") as HTMLDivElement
            descSpan.className = "search-item-desc"
            descSpan.textContent = item.description
            kDiv.appendChild(descSpan)
        }

        val infoSpan = document.createElement("div") as HTMLDivElement
        infoSpan.className = "search-item-info"
        infoSpan.textContent = "${item.price} ${item.priceCurrency} | ${item.weight} kg"
        kDiv.appendChild(infoSpan)

        target.appendChild(kDiv)
    }

    fun renderSpell(spell: DndSpell, target: HTMLDivElement, indent: Boolean = false, spellDC: Int = 0) {
        val sDiv = document.createElement("div") as HTMLDivElement
        sDiv.className = "search-item" + if (indent) " indent" else ""

        val circleStr = if (spell.circle == "Cantrip") t("magic.cantrips") else tCircle(spell.circle)
        val nameSpan = document.createElement("div") as HTMLDivElement
        nameSpan.className = "search-item-name"
        nameSpan.textContent = "\u2728 ${spell.name} ($circleStr)"
        sDiv.appendChild(nameSpan)

        val details = mutableListOf<String>()
        if (spell.school.isNotEmpty()) details.add(tSchool(spell.school))
        if (spell.castingTime.isNotEmpty()) details.add(spell.castingTime)
        if (spell.duration.isNotEmpty()) details.add(spell.duration)
        if (spell.range.isNotEmpty()) details.add("${spell.range}m")
        if (details.isNotEmpty()) {
            val detailSpan = document.createElement("div") as HTMLDivElement
            detailSpan.className = "search-item-detail"
            detailSpan.textContent = details.joinToString(" | ")
            sDiv.appendChild(detailSpan)
        }

        val flags = mutableListOf<String>()
        val components = mutableListOf<String>()
        if (spell.hasVerbal) components.add("V")
        if (spell.hasSomatic) components.add("S")
        if (spell.hasMaterial) components.add("M")
        if (components.isNotEmpty()) flags.add(components.joinToString(""))
        if (spell.needsConcentration) flags.add(t("magic.concShort"))
        if (spell.canBeRitual) flags.add(t("magic.ritualShort"))
        if (spell.higherCircles.isNotEmpty()) flags.add("\u2B06\uFE0F")
        if (flags.isNotEmpty()) {
            val flagSpan = document.createElement("div") as HTMLDivElement
            flagSpan.className = "search-item-flags"
            flagSpan.textContent = flags.joinToString(", ")
            sDiv.appendChild(flagSpan)
        }

        if (spell.description.isNotEmpty()) {
            val descSpan = document.createElement("div") as HTMLDivElement
            descSpan.className = "search-item-desc"
            descSpan.textContent = spell.description
            sDiv.appendChild(descSpan)
        }

        if (spell.higherCircles.isNotEmpty()) {
            val higherSpan = document.createElement("div") as HTMLDivElement
            higherSpan.className = "search-item-flags"
            higherSpan.textContent = "\u2B06\uFE0F ${t("magic.higherShort")} ${spell.higherCircles}"
            sDiv.appendChild(higherSpan)
        }

        if (spell.needsSavingThrow && spell.savingThrowAbility.isNotEmpty()) {
            val saveSpan = document.createElement("div") as HTMLDivElement
            saveSpan.className = "search-item-save"
            saveSpan.textContent = "${t("playing.search.spellSave")} ${tStat(spell.savingThrowAbility)} (${t("playing.dc")} $spellDC)"
            sDiv.appendChild(saveSpan)
        }

        target.appendChild(sDiv)
    }

    fun renderFeature(feat: DndFeature, target: HTMLDivElement, indent: Boolean = false) {
        val fDiv = document.createElement("div") as HTMLDivElement
        fDiv.className = "search-item" + if (indent) " indent" else ""

        val nameSpan = document.createElement("div") as HTMLDivElement
        nameSpan.className = "search-item-name"
        val displayName = when (feat.type) {
            "Idiom" -> t("features.idiom")
            "Tool Proficiency" -> t("features.toolProf")
            "Weapon/Armor Proficiency" -> t("features.weaponArmorProf")
            else -> feat.name
        }
        nameSpan.textContent = "\u2022 $displayName"
        fDiv.appendChild(nameSpan)

        if (feat.description.isNotEmpty()) {
            val descSpan = document.createElement("div") as HTMLDivElement
            descSpan.className = "search-item-desc"

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

    fun renderStat(name: String, modifier: String, saveValue: String, target: HTMLDivElement) {
        val div = document.createElement("div") as HTMLDivElement
        div.className = "search-stat"
        val testLine = document.createElement("div") as HTMLDivElement
        testLine.textContent = "${t("playing.search.test")} $name: $modifier"
        div.appendChild(testLine)
        val saveLine = document.createElement("div") as HTMLDivElement
        saveLine.textContent = "${t("playing.search.save")} $name: $saveValue"
        div.appendChild(saveLine)
        target.appendChild(div)
    }

    fun renderSkill(name: String, trained: Boolean, totalValue: Int, target: HTMLDivElement) {
        val div = document.createElement("div") as HTMLDivElement
        div.className = "search-stat"
        val trainedStr = if (trained) t("playing.search.trained") else t("playing.search.untrained")
        val valStr = if (totalValue >= 0) "+$totalValue" else "$totalValue"
        div.textContent = "${t("playing.search.test")} $name ($trainedStr): $valStr"
        target.appendChild(div)
    }

    fun renderNoteField(icon: String, label: String, value: String, target: HTMLDivElement) {
        val div = document.createElement("div") as HTMLDivElement
        div.className = "search-stat"
        val lbl = document.createElement("div") as HTMLDivElement
        lbl.className = "search-field-label"
        lbl.textContent = "$icon $label"
        div.appendChild(lbl)
        val val_ = document.createElement("div") as HTMLDivElement
        val_.className = "search-field-value"
        val_.textContent = value
        div.appendChild(val_)
        target.appendChild(div)
    }

    fun renderNote(note: DndNote, target: HTMLDivElement) {
        val div = document.createElement("div") as HTMLDivElement
        div.className = "search-stat"
        val titleSpan = document.createElement("div") as HTMLDivElement
        titleSpan.className = "search-note-title"
        titleSpan.textContent = "\uD83D\uDCDD ${note.title}"
        div.appendChild(titleSpan)
        if (note.session.isNotEmpty()) {
            val sessionSpan = document.createElement("div") as HTMLDivElement
            sessionSpan.className = "search-note-session"
            sessionSpan.textContent = note.session
            div.appendChild(sessionSpan)
        }
        if (note.note.isNotEmpty()) {
            val noteSpan = document.createElement("div") as HTMLDivElement
            noteSpan.className = "search-note-content"
            noteSpan.textContent = note.note
            div.appendChild(noteSpan)
        }
        target.appendChild(div)
    }

    fun renderCombinedProficiency(translatedType: String, description: String, target: HTMLDivElement) {
        val fDiv = document.createElement("div") as HTMLDivElement
        fDiv.className = "search-item"
        val nameSpan = document.createElement("div") as HTMLDivElement
        nameSpan.className = "search-item-name"
        nameSpan.textContent = "\u2022 $translatedType"
        fDiv.appendChild(nameSpan)
        val descSpan = document.createElement("div") as HTMLDivElement
        descSpan.className = "search-item-detail"
        descSpan.textContent = description
        fDiv.appendChild(descSpan)
        target.appendChild(fDiv)
    }
}
