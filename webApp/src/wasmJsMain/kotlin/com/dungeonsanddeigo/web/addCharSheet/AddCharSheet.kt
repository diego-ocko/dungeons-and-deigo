package com.dungeonsanddeigo.web.addCharSheet

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.availableSheetModels
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.app
import com.dungeonsanddeigo.web.dnd.sheet.showCharacterDetail
import com.dungeonsanddeigo.web.home.showListScreen
import kotlinx.browser.document
import org.w3c.dom.*
import org.w3c.files.FileReader

fun showAddCharSheet() {
    app.innerHTML = ""

    val title = document.createElement("h2") as HTMLHeadingElement
    title.textContent = t("char.create")
    app.appendChild(title)

    val form = document.createElement("div") as HTMLDivElement
    form.style.setProperty("display", "grid")
    form.style.setProperty("gap", "12px")
    form.style.maxWidth = "400px"

    // Character Name
    val nameLabel = document.createElement("label") as HTMLLabelElement
    nameLabel.textContent = t("char.name")
    nameLabel.style.fontWeight = "bold"
    form.appendChild(nameLabel)
    val nameInput = document.createElement("input") as HTMLInputElement
    nameInput.style.padding = "6px"
    form.appendChild(nameInput)

    // Character Model dropdown
    val modelLabel = document.createElement("label") as HTMLLabelElement
    modelLabel.textContent = t("char.model")
    modelLabel.style.fontWeight = "bold"
    form.appendChild(modelLabel)
    val modelSelect = document.createElement("select") as HTMLSelectElement
    modelSelect.style.padding = "6px"
    availableSheetModels.forEachIndexed { index, model ->
        val option = document.createElement("option") as HTMLOptionElement
        option.value = index.toString()
        option.textContent = model.name
        modelSelect.appendChild(option)
    }
    form.appendChild(modelSelect)

    // Image upload
    val imageLabel = document.createElement("label") as HTMLLabelElement
    imageLabel.textContent = t("char.image")
    imageLabel.style.fontWeight = "bold"
    form.appendChild(imageLabel)
    val imageInput = document.createElement("input") as HTMLInputElement
    imageInput.type = "file"
    imageInput.accept = "image/*"
    form.appendChild(imageInput)

    var imageBase64: String? = null
    imageInput.addEventListener("change", {
        val file = imageInput.files?.item(0)
        if (file != null) {
            val reader = FileReader()
            reader.onload = {
                val result = reader.result
                if (result != null) {
                    imageBase64 = result.toString()
                }
                Unit
            }
            reader.readAsDataURL(file)
        }
    })

    // Buttons
    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.style.display = "flex"
    btnRow.style.setProperty("gap", "8px")
    btnRow.style.marginTop = "8px"

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = t("btn.cancel")
    cancelBtn.addEventListener("click", { showListScreen() })
    btnRow.appendChild(cancelBtn)

    val submitBtn = document.createElement("button") as HTMLButtonElement
    submitBtn.textContent = t("btn.submit")
    submitBtn.addEventListener("click", {
        val charName = nameInput.value
        val selectedModel = availableSheetModels[modelSelect.value.toInt()]
        val character = Character(name = charName, sheetModelName = selectedModel.name, imageBase64 = imageBase64)
        val id = Repos.character.insert(character)
        showCharacterDetail(character.copy(id = id))
    })
    btnRow.appendChild(submitBtn)

    form.appendChild(btnRow)
    app.appendChild(form)
}
