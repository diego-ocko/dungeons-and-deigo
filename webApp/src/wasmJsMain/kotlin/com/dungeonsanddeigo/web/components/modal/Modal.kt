package com.dungeonsanddeigo.web.components.modal

import com.dungeonsanddeigo.i18n.t
import kotlinx.browser.document
import org.w3c.dom.*

abstract class Modal(
    private val title: String,
    private val size: String = "",
    private val saveLabel: String? = null,
    private val cancelLabel: String? = null,
    private val hideSave: Boolean = false
) {
    protected lateinit var overlay: HTMLDivElement
    protected lateinit var form: HTMLDivElement
    private var idCounter = 0

    data class FormField<T : HTMLElement>(val label: HTMLLabelElement, val input: T)

    abstract fun buildForm(form: HTMLDivElement)
    abstract fun onSave(close: () -> Unit)

    private fun nextId(): String = "modal-field-${hashCode()}-${idCounter++}"

    protected fun addLabel(form: HTMLDivElement, text: String, inputId: String, vararg classes: String): HTMLLabelElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = text
        lbl.htmlFor = inputId
        classes.forEach { lbl.classList.add(it) }
        form.appendChild(lbl)
        return lbl
    }

    protected fun addInput(form: HTMLDivElement, label: String, value: String = "", type: String = "text", vararg classes: String): FormField<HTMLInputElement> {
        val id = nextId()
        val lbl = addLabel(form, label, id, *classes)
        val input = document.createElement("input") as HTMLInputElement
        input.id = id
        input.type = type
        input.value = value
        classes.forEach { input.classList.add(it) }
        form.appendChild(input)
        return FormField(lbl, input)
    }

    protected fun addNumberInput(form: HTMLDivElement, label: String, value: String = "", min: String = "0", vararg classes: String): FormField<HTMLInputElement> {
        val field = addInput(form, label, value, "number", *classes)
        field.input.min = min
        return field
    }

    protected fun addSelect(form: HTMLDivElement, label: String, options: List<Pair<String, String>>, value: String = "", vararg classes: String): FormField<HTMLSelectElement> {
        val id = nextId()
        val lbl = addLabel(form, label, id, *classes)
        val select = document.createElement("select") as HTMLSelectElement
        select.id = id
        options.forEach { (v, text) ->
            val o = document.createElement("option") as HTMLOptionElement
            o.value = v; o.textContent = text
            select.appendChild(o)
        }
        select.value = value
        classes.forEach { select.classList.add(it) }
        form.appendChild(select)
        return FormField(lbl, select)
    }

    protected fun addTextarea(form: HTMLDivElement, label: String, value: String = "", rows: Int = 3, vararg classes: String): FormField<HTMLTextAreaElement> {
        val id = nextId()
        val lbl = addLabel(form, label, id, *classes)
        val ta = document.createElement("textarea") as HTMLTextAreaElement
        ta.id = id
        ta.value = value
        ta.rows = rows
        classes.forEach { ta.classList.add(it) }
        form.appendChild(ta)
        return FormField(lbl, ta)
    }

    fun show(onDone: () -> Unit = {}) {
        overlay = document.createElement("div") as HTMLDivElement
        overlay.className = "modal-overlay"

        val sizeClass = when (size) {
            "sm" -> " modal--sm"
            "md" -> " modal--md"
            "lg" -> " modal--lg"
            "wide" -> " modal--wide"
            "xl" -> " modal--xl"
            else -> ""
        }
        val modal = document.createElement("div") as HTMLDivElement
        modal.className = "modal$sizeClass"

        val titleEl = document.createElement("h3") as HTMLHeadingElement
        titleEl.className = "modal__title"
        titleEl.textContent = title
        modal.appendChild(titleEl)

        form = document.createElement("div") as HTMLDivElement
        form.className = "modal__form"
        buildForm(form)
        modal.appendChild(form)

        val btnRow = document.createElement("div") as HTMLDivElement
        btnRow.className = "modal__buttons"

        val cancelBtn = document.createElement("button") as HTMLButtonElement
        cancelBtn.textContent = cancelLabel ?: t("btn.cancel")
        cancelBtn.className = "modal__btn-secondary"
        cancelBtn.addEventListener("click", { close() })
        btnRow.appendChild(cancelBtn)

        if (!hideSave) {
            val saveBtn = document.createElement("button") as HTMLButtonElement
            saveBtn.textContent = saveLabel ?: t("btn.save")
            saveBtn.className = "modal__btn-primary"
            saveBtn.addEventListener("click", {
                onSave {
                    close()
                    onDone()
                }
            })
            btnRow.appendChild(saveBtn)
        }

        modal.appendChild(btnRow)
        overlay.appendChild(modal)
        document.body?.appendChild(overlay)
    }

    protected fun close() {
        document.body?.removeChild(overlay)
    }
}
