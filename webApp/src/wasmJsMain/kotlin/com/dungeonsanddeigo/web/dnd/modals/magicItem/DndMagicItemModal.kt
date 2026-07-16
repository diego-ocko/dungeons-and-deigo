package com.dungeonsanddeigo.web.dnd.modals.magicItem

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndMagicItem
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.components.modal.Modal
import com.dungeonsanddeigo.web.dnd.components.tagsField.TagsField
import com.dungeonsanddeigo.web.dnd.components.weightAndPrice.WeightAndPriceField
import kotlinx.browser.document
import org.w3c.dom.*

class DndMagicItemModal(
    private val character: Character,
    private val existing: DndMagicItem?
) : Modal(
    title = if (existing != null) t("inv.editMagicItem") else t("inv.addMagicItem"),
    size = "lg"
) {
    private lateinit var nameInput: HTMLInputElement
    private lateinit var needSynchCb: HTMLInputElement
    private lateinit var isSynchedCb: HTMLInputElement
    private lateinit var isSynchedLbl: HTMLLabelElement
    private lateinit var effectInput: HTMLTextAreaElement
    private lateinit var weightAndPriceField: WeightAndPriceField
    private lateinit var tagsField: TagsField

    override fun buildForm(form: HTMLDivElement) {
        nameInput = addInput(form, t("label.name"), existing?.name ?: "").input

        // Synch checkboxes
        val synchDiv = document.createElement("div") as HTMLDivElement
        synchDiv.className = "modal__checkbox-row modal__full-width"
        val needSynchLbl = document.createElement("label") as HTMLLabelElement
        needSynchCb = document.createElement("input") as HTMLInputElement
        needSynchCb.type = "checkbox"; needSynchCb.checked = existing?.needSynch ?: false
        needSynchLbl.appendChild(needSynchCb); needSynchLbl.append(t("inv.needSynch"))
        synchDiv.appendChild(needSynchLbl)

        isSynchedLbl = document.createElement("label") as HTMLLabelElement
        isSynchedCb = document.createElement("input") as HTMLInputElement
        isSynchedCb.type = "checkbox"; isSynchedCb.checked = existing?.isSynched ?: false
        isSynchedLbl.appendChild(isSynchedCb); isSynchedLbl.append(t("inv.isSynched"))
        synchDiv.appendChild(isSynchedLbl)
        needSynchCb.addEventListener("change", {
            isSynchedLbl.style.display = if (needSynchCb.checked) "" else "none"
        })
        isSynchedLbl.style.display = if (existing?.needSynch == true) "" else "none"
        form.appendChild(synchDiv)

        effectInput = addTextarea(form, t("inv.effect"), existing?.effect ?: "", rows = 3, classes = arrayOf("modal__full-width")).input
        weightAndPriceField = WeightAndPriceField(form)
        weightAndPriceField.setValues(existing?.weight ?: 0.0, existing?.price ?: 0, existing?.priceCurrency ?: "pg")

        // Tags
        tagsField = TagsField(form, existing?.tags ?: emptyList())
    }

    override fun onSave(close: () -> Unit) {
        val wp = weightAndPriceField.getValues()
        val item = DndMagicItem(
            id = existing?.id ?: 0,
            characterId = character.id,
            name = nameInput.value,
            needSynch = needSynchCb.checked,
            isSynched = isSynchedCb.checked,
            effect = effectInput.value,
            weight = wp.weight,
            price = wp.price,
            priceCurrency = wp.priceCurrency,
            tags = tagsField.getTags()
        )
        Repos.magicItem.save(item)
        close()
    }
}
