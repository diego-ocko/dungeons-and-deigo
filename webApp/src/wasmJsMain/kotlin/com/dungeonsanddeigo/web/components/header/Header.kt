package com.dungeonsanddeigo.web.components.header

import com.dungeonsanddeigo.i18n.I18n
import com.dungeonsanddeigo.i18n.Locale
import com.dungeonsanddeigo.i18n.t
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.*

fun renderHeader(container: Element, onLocaleChange: () -> Unit) {
    val header = document.createElement("div") as HTMLDivElement
    header.className = "header"

    val title = document.createElement("h1") as HTMLHeadingElement
    title.textContent = t("app.title")
    header.appendChild(title)

    val langBtn = document.createElement("button") as HTMLButtonElement
    langBtn.className = "header__lang-btn"
    langBtn.textContent = if (I18n.current == Locale.EN) "\uD83C\uDDE7\uD83C\uDDF7 Português" else "\uD83C\uDDFA\uD83C\uDDF8 English"
    langBtn.addEventListener("click", {
        if (I18n.current == Locale.EN) {
            I18n.current = Locale.PT_BR
            localStorage.setItem("app_locale", "PT_BR")
        } else {
            I18n.current = Locale.EN
            localStorage.setItem("app_locale", "EN")
        }
        onLocaleChange()
    })
    header.appendChild(langBtn)

    container.appendChild(header)
}
