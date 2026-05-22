package com.dungeonsanddeigo.web.dnd.tabs.background

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.util.hexToColorName
import com.dungeonsanddeigo.util.namedColors
import com.dungeonsanddeigo.web.Repos
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.w3c.dom.*
import org.w3c.files.FileReader

fun renderDndBackgroundTab(character: Character, container: HTMLDivElement) {
    // Appearance section
    val appearanceBox = document.createElement("div") as HTMLDivElement
    appearanceBox.style.border = "1px solid #ccc"
    appearanceBox.style.borderRadius = "8px"
    appearanceBox.style.padding = "16px"
    appearanceBox.style.setProperty("flex", "1")

    val appearanceTitle = document.createElement("h4") as HTMLHeadingElement
    appearanceTitle.textContent = t("background.appearance")
    appearanceTitle.style.margin = "0 0 12px 0"
    appearanceBox.appendChild(appearanceTitle)

    val content = document.createElement("div") as HTMLDivElement
    content.style.display = "flex"
    content.style.setProperty("gap", "16px")
    content.style.alignItems = "center"

    // Character picture
    val imgContainer = document.createElement("div") as HTMLDivElement
    imgContainer.style.textAlign = "center"

    val charImageKey = "char_image_${character.id}"
    val currentImage = localStorage.getItem(charImageKey) ?: character.imageBase64

    if (currentImage != null) {
        val img = document.createElement("img") as HTMLImageElement
        img.src = currentImage
        img.style.maxWidth = "300px"
        img.style.borderRadius = "8px"
        imgContainer.appendChild(img)
    } else {
        val placeholder = document.createElement("div") as HTMLDivElement
        placeholder.style.width = "120px"
        placeholder.style.height = "120px"
        placeholder.style.borderRadius = "8px"
        placeholder.style.backgroundColor = "#eee"
        placeholder.style.display = "flex"
        placeholder.style.alignItems = "center"
        placeholder.style.justifyContent = "center"
        placeholder.textContent = "\uD83D\uDDBC\uFE0F"
        placeholder.style.fontSize = "32px"
        imgContainer.appendChild(placeholder)
    }

    // Change picture button
    val changeImgBtn = document.createElement("button") as HTMLButtonElement
    changeImgBtn.textContent = "\uD83D\uDCF7 " + t("bg.change")
    changeImgBtn.style.fontSize = "11px"
    changeImgBtn.style.marginTop = "8px"
    changeImgBtn.style.display = "block"

    val fileInput = document.createElement("input") as HTMLInputElement
    fileInput.type = "file"
    fileInput.accept = "image/*"
    fileInput.style.display = "none"

    changeImgBtn.addEventListener("click", { fileInput.click() })
    fileInput.addEventListener("change", {
        val file = fileInput.files?.item(0)
        if (file != null) {
            val reader = FileReader()
            reader.onload = {
                val result = reader.result
                if (result != null) {
                    val newImage = result.toString()
                    // Update character image in localStorage
                    val charImageKey = "char_image_${character.id}"
                    localStorage.setItem(charImageKey, newImage)
                    // Update header image
                    val headerImg = document.getElementById("header-char-img") as? HTMLImageElement
                    if (headerImg != null) {
                        headerImg.src = newImage
                    }
                    container.innerHTML = ""
                    renderDndBackgroundTab(character.copy(imageBase64 = newImage), container)
                }
                Unit
            }
            reader.readAsDataURL(file)
        }
    })

    imgContainer.appendChild(changeImgBtn)
    imgContainer.appendChild(fileInput)
    content.appendChild(imgContainer)

    // Character name (editable with autosave)
    val nameDiv = document.createElement("div") as HTMLDivElement
    val nameLabel = document.createElement("div") as HTMLDivElement
    nameLabel.textContent = t("label.name")
    nameLabel.style.fontSize = "12px"
    nameLabel.style.color = "#666"
    nameLabel.style.marginBottom = "4px"
    nameDiv.appendChild(nameLabel)

    val charNameKey = "char_name_${character.id}"
    val currentName = localStorage.getItem(charNameKey) ?: character.name

    val nameInput = document.createElement("input") as HTMLInputElement
    nameInput.value = currentName
    nameInput.style.fontSize = "24px"
    nameInput.style.fontWeight = "bold"
    nameInput.style.border = "none"
    nameInput.style.borderBottom = "1px solid #ccc"
    nameInput.style.outline = "none"
    nameInput.style.width = "100%"
    nameDiv.appendChild(nameInput)

    val nameStatus = document.createElement("span") as HTMLSpanElement
    nameStatus.style.fontSize = "11px"
    nameStatus.style.display = "block"
    nameStatus.style.marginTop = "4px"
    nameDiv.appendChild(nameStatus)

    var nameSaveTimeout = 0
    nameInput.addEventListener("input", {
        nameStatus.textContent = "Saving..."
        nameStatus.style.color = "gray"
        if (nameSaveTimeout != 0) window.clearTimeout(nameSaveTimeout)
        nameSaveTimeout = window.setTimeout({
            localStorage.setItem(charNameKey, nameInput.value)
            // Update header name
            val headerName = document.getElementById("header-char-name")
            if (headerName != null) headerName.textContent = nameInput.value
            nameStatus.textContent = "\u2713 Saved"
            nameStatus.style.color = "green"
            null
        }, 500)
    })

    content.appendChild(nameDiv)

    appearanceBox.appendChild(content)

    // Appearance details form
    val appearance = Repos.appearance.getByCharacterId(character.id) ?: com.dungeonsanddeigo.model.DndAppearance(characterId = character.id)

    val detailsForm = document.createElement("div") as HTMLDivElement
    detailsForm.style.setProperty("display", "grid")
    detailsForm.style.setProperty("grid-template-columns", "1fr 1fr 1fr")
    detailsForm.style.setProperty("gap", "10px")
    detailsForm.style.marginTop = "16px"

    fun addDetailField(label: String, value: String): HTMLInputElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label; lbl.style.fontWeight = "bold"; lbl.style.fontSize = "12px"
        lbl.style.display = "block"; lbl.style.marginBottom = "4px"
        col.appendChild(lbl)
        val input = document.createElement("input") as HTMLInputElement
        input.value = value; input.style.width = "100%"; input.style.padding = "4px"
        col.appendChild(input)
        detailsForm.appendChild(col)
        return input
    }

    fun addColorField(label: String, value: String): HTMLInputElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label; lbl.style.fontWeight = "bold"; lbl.style.fontSize = "12px"
        lbl.style.display = "block"; lbl.style.marginBottom = "4px"
        col.appendChild(lbl)
        val row = document.createElement("div") as HTMLDivElement
        row.style.display = "flex"; row.style.setProperty("gap", "8px"); row.style.alignItems = "center"

        val colorPicker = document.createElement("input") as HTMLInputElement
        colorPicker.type = "color"
        colorPicker.style.width = "30px"; colorPicker.style.height = "30px"
        colorPicker.style.border = "none"; colorPicker.style.cursor = "pointer"
        // Set initial color from saved name
        if (value.isNotEmpty()) {
            val rgb = namedColors[value]
            if (rgb != null) {
                colorPicker.value = "#${rgb.first.toString(16).padStart(2, '0')}${rgb.second.toString(16).padStart(2, '0')}${rgb.third.toString(16).padStart(2, '0')}"
            }
        }
        row.appendChild(colorPicker)

        val colorLabel = document.createElement("span") as HTMLSpanElement
        colorLabel.textContent = value.ifEmpty { t("bg.pickColor") }
        colorLabel.style.fontSize = "14px"
        row.appendChild(colorLabel)

        // Hidden input to carry the value for autosave
        val input = document.createElement("input") as HTMLInputElement
        input.type = "hidden"
        input.value = value
        row.appendChild(input)

        colorPicker.addEventListener("input", {
            val name = hexToColorName(colorPicker.value)
            colorLabel.textContent = name
            input.value = name
            // Trigger autosave
            val event = document.createEvent("Event")
            event.initEvent("input", true, true)
            appearanceBox.dispatchEvent(event)
        })

        col.appendChild(row)
        detailsForm.appendChild(col)
        return input
    }

    val ageInput = addDetailField(t("bg.age"), appearance.age)
    val heightInput = addDetailField(t("bg.height"), appearance.height)
    val weightInput = addDetailField(t("bg.weight"), appearance.weight)
    val eyeColorInput = addColorField(t("bg.eyeColor"), appearance.eyeColor)
    val skinColorInput = addColorField(t("bg.skinColor"), appearance.skinColor)
    val hairColorInput = addColorField(t("bg.hairColor"), appearance.hairColor)

    appearanceBox.appendChild(detailsForm)

    // Description textarea
    val descLabel = document.createElement("label") as HTMLLabelElement
    descLabel.textContent = t("bg.appearanceDesc")
    descLabel.style.fontWeight = "bold"; descLabel.style.fontSize = "12px"
    descLabel.style.display = "block"; descLabel.style.marginTop = "12px"; descLabel.style.marginBottom = "4px"
    appearanceBox.appendChild(descLabel)
    val descInput = document.createElement("textarea") as HTMLTextAreaElement
    descInput.value = appearance.description
    descInput.rows = 4; descInput.style.width = "100%"
    appearanceBox.appendChild(descInput)

    // Auto-save
    val appearanceStatus = document.createElement("span") as HTMLSpanElement
    appearanceStatus.style.fontSize = "11px"; appearanceStatus.style.display = "block"
    appearanceStatus.style.marginTop = "6px"
    appearanceBox.appendChild(appearanceStatus)

    var appearanceSaveTimeout = 0
    fun autoSaveAppearance() {
        appearanceStatus.textContent = "Saving..."
        appearanceStatus.style.color = "gray"
        if (appearanceSaveTimeout != 0) window.clearTimeout(appearanceSaveTimeout)
        appearanceSaveTimeout = window.setTimeout({
            Repos.appearance.save(com.dungeonsanddeigo.model.DndAppearance(
                characterId = character.id,
                age = ageInput.value,
                height = heightInput.value,
                weight = weightInput.value,
                eyeColor = eyeColorInput.value,
                skinColor = skinColorInput.value,
                hairColor = hairColorInput.value,
                description = descInput.value
            ))
            appearanceStatus.textContent = "\u2713 Saved"
            appearanceStatus.style.color = "green"
            null
        }, 500)
    }

    appearanceBox.addEventListener("input", { autoSaveAppearance() })

    // Layout: Appearance (left) + Backstory (right)
    val bgRow = document.createElement("div") as HTMLDivElement
    bgRow.style.display = "flex"
    bgRow.style.setProperty("gap", "16px")
    bgRow.style.alignItems = "flex-start"
    bgRow.appendChild(appearanceBox)

    // === BACKSTORY BOX ===
    val backstoryBox = document.createElement("div") as HTMLDivElement
    backstoryBox.style.border = "1px solid #ccc"
    backstoryBox.style.borderRadius = "8px"
    backstoryBox.style.padding = "16px"
    backstoryBox.style.setProperty("flex", "1")

    val backstoryTitle = document.createElement("h4") as HTMLHeadingElement
    backstoryTitle.textContent = t("background.backstory")
    backstoryTitle.style.margin = "0 0 12px 0"
    backstoryBox.appendChild(backstoryTitle)

    val backstory = Repos.backstory.getByCharacterId(character.id) ?: com.dungeonsanddeigo.model.DndBackstory(characterId = character.id)

    // Personality fields grid
    val personalityGrid = document.createElement("div") as HTMLDivElement
    personalityGrid.style.setProperty("display", "grid")
    personalityGrid.style.setProperty("grid-template-columns", "1fr 1fr")
    personalityGrid.style.setProperty("gap", "12px")
    personalityGrid.style.marginBottom = "12px"

    fun addTextArea(parent: HTMLDivElement, label: String, value: String, rows: Int = 3): HTMLTextAreaElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label; lbl.style.fontWeight = "bold"; lbl.style.fontSize = "12px"
        lbl.style.display = "block"; lbl.style.marginBottom = "4px"
        col.appendChild(lbl)
        val ta = document.createElement("textarea") as HTMLTextAreaElement
        ta.value = value; ta.rows = rows; ta.style.width = "100%"
        col.appendChild(ta)
        parent.appendChild(col)
        return ta
    }

    val traitsInput = addTextArea(personalityGrid, t("bg.personalityTraits"), backstory.personalityTraits)
    val idealsInput = addTextArea(personalityGrid, t("bg.ideals"), backstory.ideals)
    val bondsInput = addTextArea(personalityGrid, t("bg.bonds"), backstory.bonds)
    val defectsInput = addTextArea(personalityGrid, t("bg.defects"), backstory.defects)
    val habitsInput = addTextArea(personalityGrid, t("bg.habits"), backstory.habits)

    backstoryBox.appendChild(personalityGrid)

    // Faction section
    val factionSection = document.createElement("div") as HTMLDivElement
    factionSection.style.borderTop = "1px solid #eee"
    factionSection.style.paddingTop = "12px"
    factionSection.style.marginBottom = "12px"

    val factionCheckLabel = document.createElement("label") as HTMLLabelElement
    factionCheckLabel.style.fontWeight = "bold"; factionCheckLabel.style.fontSize = "13px"
    val factionCb = document.createElement("input") as HTMLInputElement
    factionCb.type = "checkbox"; factionCb.checked = backstory.hasFaction
    factionCb.style.marginRight = "6px"
    factionCheckLabel.appendChild(factionCb)
    factionCheckLabel.append(t("bg.hasFaction"))
    factionSection.appendChild(factionCheckLabel)

    val factionDetails = document.createElement("div") as HTMLDivElement
    factionDetails.style.marginTop = "10px"
    factionDetails.style.display = if (backstory.hasFaction) "block" else "none"

    // Faction Name
    val factionNameLbl = document.createElement("label") as HTMLLabelElement
    factionNameLbl.textContent = t("bg.factionName"); factionNameLbl.style.fontWeight = "bold"; factionNameLbl.style.fontSize = "12px"
    factionNameLbl.style.display = "block"; factionNameLbl.style.marginBottom = "4px"
    factionDetails.appendChild(factionNameLbl)
    val factionNameInput = document.createElement("input") as HTMLInputElement
    factionNameInput.value = backstory.factionName; factionNameInput.style.width = "100%"; factionNameInput.style.padding = "4px"
    factionNameInput.style.marginBottom = "10px"
    factionDetails.appendChild(factionNameInput)

    // Faction Symbol
    val factionSymbolLbl = document.createElement("label") as HTMLLabelElement
    factionSymbolLbl.textContent = t("bg.factionSymbol"); factionSymbolLbl.style.fontWeight = "bold"; factionSymbolLbl.style.fontSize = "12px"
    factionSymbolLbl.style.display = "block"; factionSymbolLbl.style.marginBottom = "4px"
    factionDetails.appendChild(factionSymbolLbl)

    if (backstory.factionSymbol != null) {
        val symbolImg = document.createElement("img") as HTMLImageElement
        symbolImg.src = backstory.factionSymbol!!
        symbolImg.style.maxWidth = "100px"; symbolImg.style.borderRadius = "4px"
        symbolImg.style.display = "block"; symbolImg.style.marginBottom = "6px"
        factionDetails.appendChild(symbolImg)
    }

    val symbolFileInput = document.createElement("input") as HTMLInputElement
    symbolFileInput.type = "file"; symbolFileInput.accept = "image/*"
    symbolFileInput.style.marginBottom = "10px"
    factionDetails.appendChild(symbolFileInput)

    var factionSymbolBase64: String? = backstory.factionSymbol
    symbolFileInput.addEventListener("change", {
        val file = symbolFileInput.files?.item(0)
        if (file != null) {
            val reader = FileReader()
            reader.onload = {
                val result = reader.result
                if (result != null) {
                    factionSymbolBase64 = result.toString()
                    // Trigger save
                    val event = document.createEvent("Event")
                    event.initEvent("input", true, true)
                    backstoryBox.dispatchEvent(event)
                }
                Unit
            }
            reader.readAsDataURL(file)
        }
    })

    // Faction Backstory
    val factionBackstoryInput = addTextArea(factionDetails, t("bg.factionBackstory"), backstory.factionBackstory)

    factionCb.addEventListener("change", {
        factionDetails.style.display = if (factionCb.checked) "block" else "none"
    })

    factionSection.appendChild(factionDetails)
    backstoryBox.appendChild(factionSection)

    // Character Backstory
    val charBackstoryLbl = document.createElement("label") as HTMLLabelElement
    charBackstoryLbl.textContent = t("bg.charBackstory"); charBackstoryLbl.style.fontWeight = "bold"; charBackstoryLbl.style.fontSize = "12px"
    charBackstoryLbl.style.display = "block"; charBackstoryLbl.style.marginBottom = "4px"
    charBackstoryLbl.style.borderTop = "1px solid #eee"; charBackstoryLbl.style.paddingTop = "12px"
    backstoryBox.appendChild(charBackstoryLbl)
    val charBackstoryInput = document.createElement("textarea") as HTMLTextAreaElement
    charBackstoryInput.value = backstory.characterBackstory
    charBackstoryInput.rows = 10; charBackstoryInput.style.width = "100%"
    backstoryBox.appendChild(charBackstoryInput)

    // Auto-save
    val backstoryStatus = document.createElement("span") as HTMLSpanElement
    backstoryStatus.style.fontSize = "11px"; backstoryStatus.style.display = "block"
    backstoryStatus.style.marginTop = "6px"
    backstoryBox.appendChild(backstoryStatus)

    var backstorySaveTimeout = 0
    fun autoSaveBackstory() {
        backstoryStatus.textContent = "Saving..."
        backstoryStatus.style.color = "gray"
        if (backstorySaveTimeout != 0) window.clearTimeout(backstorySaveTimeout)
        backstorySaveTimeout = window.setTimeout({
            Repos.backstory.save(com.dungeonsanddeigo.model.DndBackstory(
                characterId = character.id,
                personalityTraits = traitsInput.value,
                ideals = idealsInput.value,
                bonds = bondsInput.value,
                defects = defectsInput.value,
                habits = habitsInput.value,
                hasFaction = factionCb.checked,
                factionName = factionNameInput.value,
                factionSymbol = factionSymbolBase64,
                factionBackstory = factionBackstoryInput.value,
                characterBackstory = charBackstoryInput.value
            ))
            backstoryStatus.textContent = "\u2713 Saved"
            backstoryStatus.style.color = "green"
            null
        }, 500)
    }

    backstoryBox.addEventListener("input", { autoSaveBackstory() })
    backstoryBox.addEventListener("change", { autoSaveBackstory() })

    bgRow.appendChild(backstoryBox)
    container.appendChild(bgRow)
}

