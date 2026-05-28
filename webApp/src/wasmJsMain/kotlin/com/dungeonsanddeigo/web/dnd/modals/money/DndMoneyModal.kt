package com.dungeonsanddeigo.web.dnd.modals.money

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndMoney
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.components.modal.DndModal
import org.w3c.dom.*

class DndMoneyModal(
    private val character: Character,
    title: String,
    private val isAdd: Boolean
) : DndModal(
    title = title,
    size = "md",
    saveLabel = if (isAdd) t("btn.add") else t("playing.spend")
) {
    private val coinInputs = mutableListOf<HTMLInputElement>()

    override fun buildForm(form: HTMLDivElement) {
        listOf(
            t("coin.copper") to t("coin.copper.abbr"),
            t("coin.silver") to t("coin.silver.abbr"),
            t("coin.electrum") to t("coin.electrum.abbr"),
            t("coin.gold") to t("coin.gold.abbr"),
            t("coin.platinum") to t("coin.platinum.abbr")
        ).forEach { (label, abbr) ->
            val input = addNumberInput(form, "$label ($abbr)", "0").input
            coinInputs.add(input)
        }
    }

    override fun onSave(close: () -> Unit) {
        val money = Repos.money.getByCharacterId(character.id) ?: DndMoney(characterId = character.id)
        val cp = coinInputs[0].value.toIntOrNull() ?: 0
        val sp = coinInputs[1].value.toIntOrNull() ?: 0
        val ep = coinInputs[2].value.toIntOrNull() ?: 0
        val gp = coinInputs[3].value.toIntOrNull() ?: 0
        val pp = coinInputs[4].value.toIntOrNull() ?: 0
        val updated = if (isAdd) {
            money.copy(copper = money.copper + cp, silver = money.silver + sp, electrum = money.electrum + ep, gold = money.gold + gp, platinum = money.platinum + pp)
        } else {
            money.copy(copper = maxOf(money.copper - cp, 0), silver = maxOf(money.silver - sp, 0), electrum = maxOf(money.electrum - ep, 0), gold = maxOf(money.gold - gp, 0), platinum = maxOf(money.platinum - pp, 0))
        }
        Repos.money.save(updated)
        close()
    }
}
