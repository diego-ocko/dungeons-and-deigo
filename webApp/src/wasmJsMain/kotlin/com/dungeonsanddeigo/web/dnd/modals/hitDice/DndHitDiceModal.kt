package com.dungeonsanddeigo.web.dnd.modals.hitDice

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DungeonsAndDragons
import com.dungeonsanddeigo.web.Repos
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.*

fun showHitDiceModal(
    die: String,
    storageKey: String,
    currentUsed: Int,
    character: Character,
    onDone: () -> Unit
) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.className = "modal-overlay"

    val modal = document.createElement("div") as HTMLDivElement
    modal.className = "modal modal--sm"

    val title = document.createElement("h3") as HTMLHeadingElement
    title.textContent = "${t("playing.useHitDie")} ($die)"
    modal.appendChild(title)

    val desc = document.createElement("p") as HTMLParagraphElement
    desc.textContent = "${t("playing.rollValue")}:"
    desc.className = "modal__desc"
    modal.appendChild(desc)

    val input = document.createElement("input") as HTMLInputElement
    input.type = "number"; input.min = "1"
    input.placeholder = "Rolled value"
    
    modal.appendChild(input)

    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.className = "modal__buttons"
    

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = t("btn.cancel")
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val confirmBtn = document.createElement("button") as HTMLButtonElement
    confirmBtn.textContent = t("playing.apply")
    confirmBtn.addEventListener("click", {
        val rolled = input.value.toIntOrNull()
        if (rolled != null && rolled > 0) {
            // Add 1 usage
            localStorage.setItem(storageKey, (currentUsed + 1).toString())
            // Add rolled value to current life
            val lifeKey = "dnd_playing_life_${character.id}"
            val stats = Repos.baseStats.getByCharacterId(character.id)
            val maxLife = stats?.maxLife ?: 0
            val currentLife = localStorage.getItem(lifeKey)?.toIntOrNull() ?: maxLife
            val newLife = minOf(currentLife + rolled, maxLife)
            localStorage.setItem(lifeKey, newLife.toString())
            document.body?.removeChild(overlay)
            onDone()
        }
    })
    btnRow.appendChild(confirmBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

