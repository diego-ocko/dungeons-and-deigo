package com.dungeonsanddeigo.web.dnd.components.weightAndPrice

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tCurrency
import kotlinx.browser.document
import org.w3c.dom.*

data class WeightAndPriceValues(
    val weight: Double,
    val price: Int,
    val priceCurrency: String
)

private val currencyEmoji = mapOf(
    "pc" to "🥉",
    "ps" to "🥈",
    "pe" to "🪙",
    "pg" to "🥇",
    "pp" to "💎"
)

class WeightAndPriceField(container: HTMLElement) {

    private val weightInput: HTMLInputElement
    private val priceInput: HTMLInputElement
    private val currSelect: HTMLSelectElement

    private val currencies = listOf("pc", "ps", "pe", "pg", "pp")

    init {
        val row = document.createElement("div") as HTMLDivElement
        row.className = "weight-and-price modal__full-width"

        // Weight section
        val weightSection = document.createElement("div") as HTMLDivElement
        weightSection.className = "weight-and-price__section"
        val weightLbl = document.createElement("label") as HTMLLabelElement
        weightLbl.textContent = t("inv.weight")
        weightSection.appendChild(weightLbl)
        val weightInner = document.createElement("div") as HTMLDivElement
        weightInner.className = "weight-and-price__input-row"
        weightInput = document.createElement("input") as HTMLInputElement
        weightInput.type = "number"; weightInput.step = "0.1"; weightInput.min = "0"
        weightInner.appendChild(weightInput)
        val kgSpan = document.createElement("span") as HTMLSpanElement
        kgSpan.textContent = "kg"
        kgSpan.className = "weight-and-price__unit"
        weightInner.appendChild(kgSpan)
        weightSection.appendChild(weightInner)
        row.appendChild(weightSection)

        // Price section
        val priceSection = document.createElement("div") as HTMLDivElement
        priceSection.className = "weight-and-price__section"
        val priceLbl = document.createElement("label") as HTMLLabelElement
        priceLbl.textContent = t("inv.price")
        priceSection.appendChild(priceLbl)
        val priceInner = document.createElement("div") as HTMLDivElement
        priceInner.className = "weight-and-price__input-row"
        priceInput = document.createElement("input") as HTMLInputElement
        priceInput.type = "number"; priceInput.min = "0"
        priceInner.appendChild(priceInput)
        currSelect = document.createElement("select") as HTMLSelectElement
        currencies.forEach { c ->
            val o = document.createElement("option") as HTMLOptionElement
            o.value = c
            o.textContent = "${currencyEmoji[c]} ${tCurrency(c)}"
            currSelect.appendChild(o)
        }
        priceInner.appendChild(currSelect)
        priceSection.appendChild(priceInner)
        row.appendChild(priceSection)

        container.appendChild(row)
    }

    fun setValues(weight: Double, price: Int, priceCurrency: String) {
        weightInput.value = weight.toString()
        priceInput.value = price.toString()
        currSelect.value = priceCurrency
    }

    fun getValues() = WeightAndPriceValues(
        weight = weightInput.value.toDoubleOrNull() ?: 0.0,
        price = priceInput.value.toIntOrNull() ?: 0,
        priceCurrency = currSelect.value
    )
}
