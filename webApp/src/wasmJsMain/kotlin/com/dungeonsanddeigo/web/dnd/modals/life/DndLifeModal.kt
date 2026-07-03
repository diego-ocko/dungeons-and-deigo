package com.dungeonsanddeigo.web.dnd.modals.life

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.web.components.modal.Modal
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement

class DndLifeModal(
    title: String,
    private val onConfirm: (Int) -> Unit
) : Modal(title, "sm", saveLabel = t("playing.confirm")) {

    private lateinit var input: HTMLInputElement

    override fun buildForm(form: HTMLDivElement) {
        val field = addNumberInput(form, "", "0")
        input = field.input
        input.style.fontSize = "18px"
        input.style.textAlign = "center"
    }

    override fun onSave(close: () -> Unit) {
        val value = input.value.toIntOrNull() ?: 0
        if (value > 0) onConfirm(value)
        close()
    }
}
