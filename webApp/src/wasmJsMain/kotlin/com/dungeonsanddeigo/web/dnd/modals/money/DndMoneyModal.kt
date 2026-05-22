package com.dungeonsanddeigo.web.dnd.modals.money

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tCurrency
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndMoney
import com.dungeonsanddeigo.web.Repos
import kotlinx.browser.document
import org.w3c.dom.*

fun showMoneyModal(character: Character, title: String, isAdd: Boolean, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"
    overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"
    overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")

    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"
    modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "350px"; modal.style.width = "90%"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = title
    modal.appendChild(titleEl)

    val form = document.createElement("div") as HTMLDivElement
    form.style.setProperty("display", "grid")
    form.style.setProperty("grid-template-columns", "1fr 1fr")
    form.style.setProperty("gap", "8px")

    data class CoinInput(val label: String, val abbr: String, val input: HTMLInputElement)
    val coinInputs = mutableListOf<CoinInput>()

    listOf(t("coin.copper") to t("coin.copper.abbr"), t("coin.silver") to t("coin.silver.abbr"), t("coin.electrum") to t("coin.electrum.abbr"), t("coin.gold") to t("coin.gold.abbr"), t("coin.platinum") to t("coin.platinum.abbr")).forEach { (label, abbr) ->
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = "$label ($abbr)"; lbl.style.fontWeight = "bold"; lbl.style.fontSize = "13px"
        form.appendChild(lbl)
        val input = document.createElement("input") as HTMLInputElement
        input.type = "number"; input.min = "0"; input.value = "0"
        input.style.padding = "4px"
        form.appendChild(input)
        coinInputs.add(CoinInput(label, abbr, input))
    }
    modal.appendChild(form)

    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
    btnRow.style.marginTop = "16px"; btnRow.style.justifyContent = "flex-end"

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = t("btn.cancel")
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val confirmBtn = document.createElement("button") as HTMLButtonElement
    confirmBtn.textContent = if (isAdd) "Add" else "Spend"
    confirmBtn.addEventListener("click", {
        val money = Repos.money.getByCharacterId(character.id) ?: DndMoney(characterId = character.id)
        val cp = coinInputs[0].input.value.toIntOrNull() ?: 0
        val sp = coinInputs[1].input.value.toIntOrNull() ?: 0
        val ep = coinInputs[2].input.value.toIntOrNull() ?: 0
        val gp = coinInputs[3].input.value.toIntOrNull() ?: 0
        val pp = coinInputs[4].input.value.toIntOrNull() ?: 0
        val updated = if (isAdd) {
            money.copy(
                copper = money.copper + cp, silver = money.silver + sp,
                electrum = money.electrum + ep, gold = money.gold + gp, platinum = money.platinum + pp
            )
        } else {
            money.copy(
                copper = maxOf(money.copper - cp, 0), silver = maxOf(money.silver - sp, 0),
                electrum = maxOf(money.electrum - ep, 0), gold = maxOf(money.gold - gp, 0),
                platinum = maxOf(money.platinum - pp, 0)
            )
        }
        Repos.money.save(updated)
        document.body?.removeChild(overlay)
        onDone()
    })
    btnRow.appendChild(confirmBtn)
    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

