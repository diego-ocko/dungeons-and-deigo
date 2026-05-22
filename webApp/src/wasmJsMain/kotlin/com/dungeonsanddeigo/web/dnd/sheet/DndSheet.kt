package com.dungeonsanddeigo.web.dnd.sheet

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DungeonsAndDragons
import com.dungeonsanddeigo.web.app
import com.dungeonsanddeigo.web.dnd.tabs.background.renderDndBackgroundTab
import com.dungeonsanddeigo.web.dnd.tabs.features.renderDndFeaturesTab
import com.dungeonsanddeigo.web.dnd.tabs.inventory.renderDndInventoryTab
import com.dungeonsanddeigo.web.dnd.tabs.magic.renderDndMagicTab
import com.dungeonsanddeigo.web.dnd.tabs.main.renderDndMainTab
import com.dungeonsanddeigo.web.dnd.tabs.notes.renderDndNotesTab
import com.dungeonsanddeigo.web.dnd.tabs.playing.renderDndPlayingTab
import com.dungeonsanddeigo.web.dnd.tabs.stats.renderDndStatsTab
import com.dungeonsanddeigo.web.home.showListScreen
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.*

fun showCharacterDetail(character: Character) {
    app.innerHTML = ""
    app.style.display = "flex"
    app.style.height = "100vh"

    val leftPanel = document.createElement("div") as HTMLDivElement
    leftPanel.style.width = "100%"
    leftPanel.style.padding = "16px"
    leftPanel.style.overflowY = "auto"

    // Header
    val header = document.createElement("div") as HTMLDivElement
    header.style.display = "flex"
    header.style.alignItems = "center"
    header.style.marginBottom = "16px"

    val headerImgKey = "char_image_${character.id}"
    val headerImage = localStorage.getItem(headerImgKey) ?: character.imageBase64
    if (headerImage != null) {
        val img = document.createElement("img") as HTMLImageElement
        img.src = headerImage
        img.id = "header-char-img"
        img.style.width = "80px"
        img.style.height = "80px"
        img.style.marginRight = "16px"
        img.style.borderRadius = "8px"
        header.appendChild(img)
    }

    val info = document.createElement("div") as HTMLDivElement
    val nameEl = document.createElement("h1")
    nameEl.id = "header-char-name"
    val charNameKey = "char_name_${character.id}"
    nameEl.textContent = localStorage.getItem(charNameKey) ?: character.name
    info.appendChild(nameEl)
    val modelEl = document.createElement("p")
    modelEl.textContent = "Sheet: ${character.sheetModel.name}"
    info.appendChild(modelEl)
    header.appendChild(info)

    leftPanel.appendChild(header)

    // Tabs
    val tabBar = document.createElement("div") as HTMLDivElement
    tabBar.style.display = "flex"
    tabBar.style.borderBottom = "1px solid #ccc"
    tabBar.style.marginBottom = "16px"

    val tabContent = document.createElement("div") as HTMLDivElement

    character.sheetModel.tabs.forEachIndexed { index, tabName ->
        val tabBtn = document.createElement("button") as HTMLButtonElement
        val tabDisplayName = when (tabName) {
            "Playing" -> t("tab.playing")
            "Main" -> t("tab.main")
            "Stats" -> t("tab.stats")
            "Features" -> t("tab.features")
            "Magic" -> t("tab.magic")
            "Inventory" -> t("tab.inventory")
            "Background" -> t("tab.background")
            "Notes" -> t("tab.notes")
            else -> tabName
        }
        tabBtn.textContent = tabDisplayName
        tabBtn.style.padding = "8px 16px"
        tabBtn.style.border = "none"
        tabBtn.style.cursor = "pointer"
        tabBtn.style.backgroundColor = if (index == 0) "#e0e0e0" else "transparent"
        tabBtn.addEventListener("click", {
            val buttons = tabBar.querySelectorAll("button")
            for (i in 0 until buttons.length) {
                (buttons.item(i) as? HTMLButtonElement)?.style?.backgroundColor = "transparent"
            }
            tabBtn.style.backgroundColor = "#e0e0e0"
            renderTabContent(tabName, character, tabContent)
        })
        tabBar.appendChild(tabBtn)
    }

    leftPanel.appendChild(tabBar)

    renderTabContent(character.sheetModel.tabs.first(), character, tabContent)
    leftPanel.appendChild(tabContent)

    // Back button
    val backBtn = document.createElement("button") as HTMLButtonElement
    backBtn.textContent = t("btn.back")
    backBtn.style.marginTop = "16px"
    backBtn.addEventListener("click", {
        app.style.display = ""
        app.style.height = ""
        showListScreen()
    })
    leftPanel.appendChild(backBtn)

    app.appendChild(leftPanel)
}

private fun renderTabContent(tabName: String, character: Character, container: HTMLDivElement) {
    container.innerHTML = ""
    if (character.sheetModel is DungeonsAndDragons) {
        when (tabName) {
            "Main" -> renderDndMainTab(character, container)
            "Stats" -> renderDndStatsTab(character, container)
            "Features" -> renderDndFeaturesTab(character, container)
            "Magic" -> renderDndMagicTab(character, container)
            "Inventory" -> renderDndInventoryTab(character, container)
            "Playing" -> renderDndPlayingTab(character, container)
            "Background" -> renderDndBackgroundTab(character, container)
            "Notes" -> renderDndNotesTab(character, container)
            else -> {
                val placeholder = document.createElement("p")
                placeholder.textContent = "$tabName content"
                container.appendChild(placeholder)
            }
        }
    } else {
        val placeholder = document.createElement("p")
        placeholder.textContent = "$tabName content"
        container.appendChild(placeholder)
    }
}
