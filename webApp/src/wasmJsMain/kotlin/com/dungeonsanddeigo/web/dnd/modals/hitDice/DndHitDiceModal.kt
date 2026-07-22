package com.dungeonsanddeigo.web.dnd.modals.hitDice

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.components.modal.Modal
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.*

class DndHitDiceModal(
    private val die: String,
    private val storageKey: String,
    private val currentUsed: Int,
    private val character: Character
) : Modal(
    title = "${t("playing.useHitDie")} ($die)",
    size = "sm",
    saveLabel = t("playing.apply")
) {
    private lateinit var input: HTMLInputElement

    override fun buildForm(form: HTMLDivElement) {
        input = addNumberInput(form, t("playing.rollValue"), "", min = "1").input
    }

    override fun onSave(close: () -> Unit) {
        val rolled = input.value.toIntOrNull()
        if (rolled != null && rolled > 0) {
            localStorage.setItem(storageKey, (currentUsed + 1).toString())
            val lifeKey = "dnd_playing_life_${character.id}"
            val stats = Repos.baseStats.getByCharacterId(character.id)
            val maxLife = stats?.maxLife ?: 0
            val currentLife = localStorage.getItem(lifeKey)?.toIntOrNull() ?: maxLife
            val newLife = minOf(currentLife + rolled, maxLife)
            localStorage.setItem(lifeKey, newLife.toString())
            close()
        }
    }
}
