package com.dungeonsanddeigo.web.dnd.components.tagsField

import com.dungeonsanddeigo.i18n.t
import kotlinx.browser.document
import org.w3c.dom.*

class TagsField(
    private val container: HTMLElement,
    initialTags: List<String> = emptyList(),
    private val label: String = t("label.tags"),
    private val fullWidth: Boolean = true
) {
    private val tags = mutableListOf<String>()
    private val badgesDiv: HTMLDivElement
    private val wrapper: HTMLDivElement

    init {
        tags.addAll(initialTags)

        wrapper = document.createElement("div") as HTMLDivElement
        if (fullWidth) wrapper.className = "modal__full-width"

        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        wrapper.appendChild(lbl)

        badgesDiv = document.createElement("div") as HTMLDivElement
        badgesDiv.className = "modal__tags"
        refresh()
        wrapper.appendChild(badgesDiv)

        val addRow = document.createElement("div") as HTMLDivElement
        addRow.className = "modal__tag-add-row"
        val input = document.createElement("input") as HTMLInputElement
        input.placeholder = "Add tag..."
        addRow.appendChild(input)
        val addBtn = document.createElement("button") as HTMLButtonElement
        addBtn.textContent = t("btn.add")
        addBtn.addEventListener("click", {
            val tag = input.value.trim()
            if (tag.isNotEmpty() && tag !in tags) {
                tags.add(tag)
                refresh()
            }
            input.value = ""
        })
        addRow.appendChild(addBtn)
        wrapper.appendChild(addRow)

        container.appendChild(wrapper)
    }

    fun getTags(): List<String> = tags.toList()

    fun setVisible(visible: Boolean) {
        wrapper.style.display = if (visible) "" else "none"
    }

    private fun refresh() {
        badgesDiv.innerHTML = ""
        tags.forEach { tag ->
            val badge = document.createElement("span") as HTMLSpanElement
            badge.textContent = "$tag \u00D7"
            badge.className = "modal__tag"
            badge.addEventListener("click", {
                tags.remove(tag)
                refresh()
            })
            badgesDiv.appendChild(badge)
        }
    }
}
