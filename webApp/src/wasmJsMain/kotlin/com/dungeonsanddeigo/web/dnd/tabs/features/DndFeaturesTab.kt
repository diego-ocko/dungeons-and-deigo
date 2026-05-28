package com.dungeonsanddeigo.web.dnd.tabs.features

import com.dungeonsanddeigo.dnd.rules.calcProficiency
import com.dungeonsanddeigo.dnd.rules.featureTypeSortOrder
import com.dungeonsanddeigo.dnd.rules.featureSourceSortOrder
import com.dungeonsanddeigo.dnd.rules.featureSourceLabel
import com.dungeonsanddeigo.i18n.*
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndFeature
import com.dungeonsanddeigo.model.DungeonsAndDragons
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.modals.feature.DndFeatureModal
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.*

fun renderDndFeaturesTab(character: Character, container: HTMLDivElement) {
    val mainInfo = Repos.mainInfo.getByCharacterId(character.id)

    fun buildFeatureCard(f: DndFeature, onRefresh: () -> Unit): HTMLDivElement {
        val card = document.createElement("div") as HTMLDivElement
        card.className = "features-card"

        val nameEl = document.createElement("h4") as HTMLHeadingElement
        nameEl.textContent = when (f.type) {
            "Idiom", "Tool Proficiency", "Weapon/Armor Proficiency" -> tFeatureType(f.type)
            else -> f.name.ifEmpty { "(Unnamed)" }
        }
        nameEl.className = "features-card-name"
        card.appendChild(nameEl)

        val header = document.createElement("div") as HTMLDivElement
        header.className = "features-card-header"

        val left = document.createElement("div") as HTMLDivElement
        val sourceBadge = document.createElement("span") as HTMLSpanElement
        sourceBadge.textContent = tSource(f.source)
        sourceBadge.className = "features-source-badge source-${f.source.lowercase()}"
        left.appendChild(sourceBadge)

        val sourceDetail = document.createElement("span") as HTMLSpanElement
        sourceDetail.textContent = featureSourceLabel(f, ::tDnd, ::t)
        sourceDetail.className = "features-source-detail"
        left.appendChild(sourceDetail)
        header.appendChild(left)

        val typeBadge = document.createElement("span") as HTMLSpanElement
        typeBadge.textContent = tFeatureType(f.type)
        typeBadge.className = "features-type-badge"
        header.appendChild(typeBadge)
        card.appendChild(header)

        val desc = document.createElement("p") as HTMLParagraphElement
        desc.className = "features-desc"
        if (f.type == "Idiom") {
            val translatedIdioms = f.description.split(",").map { tIdiom(it.trim()) }.joinToString(", ")
            desc.textContent = "${t("features.languages")}: $translatedIdioms"
        } else if (f.type == "Tool Proficiency") {
            val translatedTools = f.description.split(",").map { tTool(it.trim()) }.joinToString(", ")
            desc.textContent = "${t("features.tools")}: $translatedTools"
        } else if (f.type == "Weapon/Armor Proficiency") {
            val parts = f.description.split(",").map { it.trim() }
            val armors = parts.filter { it.startsWith("armor:") }.map { it.removePrefix("armor:") }
            val cats = parts.filter { it.startsWith("weapon_cat:") }.map { it.removePrefix("weapon_cat:") }
            val weapons = parts.filter { it.startsWith("weapon:") }.map { it.removePrefix("weapon:") }
            val lines = mutableListOf<String>()
            if (armors.isNotEmpty()) lines.add("${t("features.armor")}: ${armors.joinToString(", ") { when(it) { "Light" -> t("features.armor.light"); "Medium" -> t("features.armor.medium"); "Heavy" -> t("features.armor.heavy"); "Shields" -> t("features.armor.shields"); else -> it } }}")
            if (cats.isNotEmpty()) lines.add("${t("features.weaponCategories")}: ${cats.joinToString(", ") { when(it) { "Simple" -> t("features.weapon.simple"); "Martial" -> t("features.weapon.martial"); else -> it } }}")
            if (weapons.isNotEmpty()) lines.add("${t("features.individualWeapons")}: ${weapons.joinToString(", ") { tWeapon(it) }}")
            desc.textContent = lines.joinToString(" | ")
        } else {
            desc.textContent = f.description
        }
        card.appendChild(desc)

        if (f.type == "Rechargable Feature" && f.maxQuantity != null) {
            val rechargeInfo = document.createElement("p") as HTMLParagraphElement
            rechargeInfo.textContent = "${t("features.uses")}: ${f.maxQuantity} | ${t("features.recharge")}: ${tReloadRule(f.reloadRule ?: "\u2014")}"
            rechargeInfo.className = "features-recharge-info"
            card.appendChild(rechargeInfo)
        }

        if (f.tags.isNotEmpty()) {
            val tagsDiv = document.createElement("div") as HTMLDivElement
            tagsDiv.className = "features-tags"
            f.tags.forEach { tag ->
                val tagSpan = document.createElement("span") as HTMLSpanElement
                tagSpan.textContent = tag
                tagSpan.className = "features-tag"
                tagsDiv.appendChild(tagSpan)
            }
            card.appendChild(tagsDiv)
        }

        val actions = document.createElement("div") as HTMLDivElement
        actions.className = "features-actions"

        val editBtn = document.createElement("button") as HTMLButtonElement
        editBtn.textContent = t("btn.edit")
        editBtn.addEventListener("click", { DndFeatureModal(character, f, mainInfo).show { onRefresh() } })
        actions.appendChild(editBtn)

        val deleteBtn = document.createElement("button") as HTMLButtonElement
        deleteBtn.textContent = t("btn.delete")
        deleteBtn.className = "features-delete-btn"
        deleteBtn.addEventListener("click", {
            Repos.features.delete(f.id)
            onRefresh()
        })
        actions.appendChild(deleteBtn)

        card.appendChild(actions)
        return card
    }

    fun refreshList() {
        container.innerHTML = ""

        val addBtn = document.createElement("button") as HTMLButtonElement
        addBtn.textContent = t("features.addNew")
        addBtn.className = "features-add-btn"
        addBtn.addEventListener("click", { DndFeatureModal(character, null, mainInfo).show { refreshList() } })
        container.appendChild(addBtn)

        val featureSort = compareBy<DndFeature> { featureTypeSortOrder(it) }
            .thenBy { featureSourceSortOrder(it) }
            .thenBy { it.sourceClassLevel ?: 0 }

        val allFeatures = Repos.features.getByCharacterId(character.id).sortedWith(featureSort)

        val proficiencyTypes = setOf("Weapon/Armor Proficiency", "Tool Proficiency", "Idiom")
        val leftFeatures = allFeatures.filter { it.type in proficiencyTypes }
        val rightFeatures = allFeatures.filter { it.type !in proficiencyTypes }

        val columns = document.createElement("div") as HTMLDivElement
        columns.className = "features-columns"

        val leftCol = document.createElement("div") as HTMLDivElement
        leftCol.className = "features-col"
        val leftTitle = document.createElement("h3") as HTMLHeadingElement
        leftTitle.textContent = t("features.profAndIdioms")
        leftCol.appendChild(leftTitle)
        if (leftFeatures.isEmpty()) {
            val empty = document.createElement("p") as HTMLParagraphElement
            empty.textContent = "No proficiencies yet."
            leftCol.appendChild(empty)
        } else {
            leftFeatures.forEach { leftCol.appendChild(buildFeatureCard(it) { refreshList() }) }
        }
        columns.appendChild(leftCol)

        val rightCol = document.createElement("div") as HTMLDivElement
        rightCol.className = "features-col"
        val rightTitle = document.createElement("h3") as HTMLHeadingElement
        rightTitle.textContent = t("features.features")
        rightCol.appendChild(rightTitle)
        if (rightFeatures.isEmpty()) {
            val empty = document.createElement("p") as HTMLParagraphElement
            empty.textContent = t("features.noFeatures")
            rightCol.appendChild(empty)
        } else {
            rightFeatures.forEach { rightCol.appendChild(buildFeatureCard(it) { refreshList() }) }
        }
        columns.appendChild(rightCol)

        container.appendChild(columns)
    }

    refreshList()
}

