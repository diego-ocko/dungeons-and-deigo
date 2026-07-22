package com.dungeonsanddeigo.web.dnd.modals.changeWeapon

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.components.modal.Modal
import org.w3c.dom.*

class DndChangeWeaponModal(
    private val character: Character
) : Modal(
    title = t("playing.changeWeapon")
) {
    private val weapons = Repos.weapon.getByCharacterId(character.id)
    private lateinit var select: HTMLSelectElement

    override fun buildForm(form: HTMLDivElement) {
        val options = mutableListOf("" to t("inv.none"))
        weapons.forEach { w ->
            val dmgType = when (w.damageType) {
                "Bludgeoning" -> t("inv.dmg.bludgeoning")
                "Piercing" -> t("inv.dmg.piercing")
                "Slashing" -> t("inv.dmg.slashing")
                else -> w.damageType
            }
            options.add(w.id.toString() to "${w.name} (${w.damageDice} $dmgType)")
        }
        select = addSelect(form, t("playing.changeWeapon"), options, weapons.firstOrNull { it.isEquipped }?.id?.toString() ?: "").input
        select.className = "modal__select"
    }

    override fun onSave(close: () -> Unit) {
        val selectedId = select.value.toLongOrNull()
        weapons.filter { it.isEquipped }.forEach { Repos.weapon.save(it.copy(isEquipped = false)) }
        if (selectedId != null) {
            val toEquip = weapons.firstOrNull { it.id == selectedId }
            if (toEquip != null) Repos.weapon.save(toEquip.copy(isEquipped = true))
        }
        close()
    }
}
