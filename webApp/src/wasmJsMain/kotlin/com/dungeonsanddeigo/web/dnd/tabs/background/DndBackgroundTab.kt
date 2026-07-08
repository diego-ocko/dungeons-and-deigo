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
    appearanceBox.className = "bg-box"

    val appearanceTitle = document.createElement("h4") as HTMLHeadingElement
    appearanceTitle.textContent = t("background.appearance")
    appearanceBox.appendChild(appearanceTitle)

    val content = document.createElement("div") as HTMLDivElement
    content.className = "bg-content"

    // Character picture
    val imgContainer = document.createElement("div") as HTMLDivElement
    imgContainer.className = "bg-img-container"

    val charImageKey = "char_image_${character.id}"
    val currentImage = localStorage.getItem(charImageKey) ?: character.imageBase64

    if (currentImage != null) {
        val img = document.createElement("img") as HTMLImageElement
        img.src = currentImage
        img.className = "bg-img"
        imgContainer.appendChild(img)
    } else {
        val placeholder = document.createElement("div") as HTMLDivElement
        placeholder.className = "bg-img-placeholder"
        placeholder.textContent = "\uD83D\uDDBC\uFE0F"
        imgContainer.appendChild(placeholder)
    }

    // Change picture button
    val changeImgBtn = document.createElement("button") as HTMLButtonElement
    changeImgBtn.textContent = "\uD83D\uDCF7 " + t("bg.change")
    changeImgBtn.className = "bg-change-img-btn"

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
    nameLabel.className = "bg-name-label"
    nameDiv.appendChild(nameLabel)

    val charNameKey = "char_name_${character.id}"
    val currentName = localStorage.getItem(charNameKey) ?: character.name

    val nameInput = document.createElement("input") as HTMLInputElement
    nameInput.value = currentName
    nameInput.className = "bg-name-input"
    nameDiv.appendChild(nameInput)

    val nameStatus = document.createElement("span") as HTMLSpanElement
    nameStatus.className = "bg-status"
    nameDiv.appendChild(nameStatus)

    var nameSaveTimeout = 0
    nameInput.addEventListener("input", {
        nameStatus.textContent = t("status.saving")
        nameStatus.style.color = "gray"
        if (nameSaveTimeout != 0) window.clearTimeout(nameSaveTimeout)
        nameSaveTimeout = window.setTimeout({
            localStorage.setItem(charNameKey, nameInput.value)
            // Update header name
            val headerName = document.getElementById("header-char-name")
            if (headerName != null) headerName.textContent = nameInput.value
            nameStatus.textContent = t("status.saved")
            nameStatus.style.color = "green"
            null
        }, 500)
    })

    content.appendChild(nameDiv)

    appearanceBox.appendChild(content)

    // Appearance details form
    val appearance = Repos.appearance.getByCharacterId(character.id) ?: com.dungeonsanddeigo.model.DndAppearance(characterId = character.id)

    val detailsForm = document.createElement("div") as HTMLDivElement
    detailsForm.className = "bg-details-form"

    fun addDetailField(label: String, value: String): HTMLInputElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        col.appendChild(lbl)
        val input = document.createElement("input") as HTMLInputElement
        input.value = value
        col.appendChild(input)
        detailsForm.appendChild(col)
        return input
    }

    fun addColorField(label: String, value: String): HTMLInputElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        col.appendChild(lbl)
        val row = document.createElement("div") as HTMLDivElement
        row.className = "bg-color-row"

        val colorPicker = document.createElement("input") as HTMLInputElement
        colorPicker.type = "color"
        colorPicker.className = "bg-color-picker"
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
        colorLabel.className = "bg-color-label"
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
    descLabel.className = "bg-desc-label"
    appearanceBox.appendChild(descLabel)
    val descInput = document.createElement("textarea") as HTMLTextAreaElement
    descInput.value = appearance.description
    descInput.rows = 4; descInput.style.width = "100%"
    appearanceBox.appendChild(descInput)

    // Auto-save
    val appearanceStatus = document.createElement("span") as HTMLSpanElement
    appearanceStatus.className = "bg-status"
    appearanceBox.appendChild(appearanceStatus)

    var appearanceSaveTimeout = 0
    fun autoSaveAppearance() {
        appearanceStatus.textContent = t("status.saving")
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
            appearanceStatus.textContent = t("status.saved")
            appearanceStatus.style.color = "green"
            null
        }, 500)
    }

    appearanceBox.addEventListener("input", { autoSaveAppearance() })

    // Layout: Appearance (left) + Backstory (right)
    val bgRow = document.createElement("div") as HTMLDivElement
    bgRow.className = "bg-row"
    bgRow.appendChild(appearanceBox)

    // === BACKSTORY BOX ===
    val backstoryBox = document.createElement("div") as HTMLDivElement
    backstoryBox.className = "bg-box"

    val backstoryTitle = document.createElement("h4") as HTMLHeadingElement
    backstoryTitle.textContent = t("background.backstory")
    backstoryBox.appendChild(backstoryTitle)

    val backstory = Repos.backstory.getByCharacterId(character.id) ?: com.dungeonsanddeigo.model.DndBackstory(characterId = character.id)

    // Personality fields grid
    val personalityGrid = document.createElement("div") as HTMLDivElement
    personalityGrid.className = "bg-personality-grid"

    fun addTextArea(parent: HTMLDivElement, label: String, value: String, rows: Int = 3): HTMLTextAreaElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
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
    factionSection.className = "bg-faction-section"

    val factionCheckLabel = document.createElement("label") as HTMLLabelElement
    factionCheckLabel.className = "bg-faction-check-label"
    val factionCb = document.createElement("input") as HTMLInputElement
    factionCb.type = "checkbox"; factionCb.checked = backstory.hasFaction
    factionCb.className = "bg-faction-cb"
    factionCheckLabel.appendChild(factionCb)
    factionCheckLabel.append(t("bg.hasFaction"))
    factionSection.appendChild(factionCheckLabel)

    val factionDetails = document.createElement("div") as HTMLDivElement
    factionDetails.className = "bg-faction-details"
    factionDetails.style.display = if (backstory.hasFaction) "block" else "none"

    val factionNameLbl = document.createElement("label") as HTMLLabelElement
    factionNameLbl.textContent = t("bg.factionName")
    factionDetails.appendChild(factionNameLbl)
    val factionNameInput = document.createElement("input") as HTMLInputElement
    factionNameInput.value = backstory.factionName
    factionDetails.appendChild(factionNameInput)

    val factionSymbolLbl = document.createElement("label") as HTMLLabelElement
    factionSymbolLbl.textContent = t("bg.factionSymbol")
    factionDetails.appendChild(factionSymbolLbl)

    if (backstory.factionSymbol != null) {
        val symbolImg = document.createElement("img") as HTMLImageElement
        symbolImg.src = backstory.factionSymbol!!
        symbolImg.className = "bg-faction-symbol-img"
        factionDetails.appendChild(symbolImg)
    }

    val symbolFileInput = document.createElement("input") as HTMLInputElement
    symbolFileInput.type = "file"; symbolFileInput.accept = "image/*"
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
    charBackstoryLbl.textContent = t("bg.charBackstory")
    charBackstoryLbl.className = "bg-char-backstory-label"
    backstoryBox.appendChild(charBackstoryLbl)
    val charBackstoryInput = document.createElement("textarea") as HTMLTextAreaElement
    charBackstoryInput.value = backstory.characterBackstory
    charBackstoryInput.rows = 10; charBackstoryInput.style.width = "100%"
    backstoryBox.appendChild(charBackstoryInput)

    // Auto-save
    val backstoryStatus = document.createElement("span") as HTMLSpanElement
    backstoryStatus.className = "bg-status"
    backstoryBox.appendChild(backstoryStatus)

    var backstorySaveTimeout = 0
    fun autoSaveBackstory() {
        backstoryStatus.textContent = t("status.saving")
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
            backstoryStatus.textContent = t("status.saved")
            backstoryStatus.style.color = "green"
            null
        }, 500)
    }

    backstoryBox.addEventListener("input", { autoSaveBackstory() })
    backstoryBox.addEventListener("change", { autoSaveBackstory() })

    bgRow.appendChild(backstoryBox)
    container.appendChild(bgRow)
}

