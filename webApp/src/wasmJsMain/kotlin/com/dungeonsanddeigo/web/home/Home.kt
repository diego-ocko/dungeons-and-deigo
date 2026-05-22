package com.dungeonsanddeigo.web.home

import com.dungeonsanddeigo.i18n.I18n
import com.dungeonsanddeigo.i18n.Locale
import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.addCharSheet.showAddCharSheet
import com.dungeonsanddeigo.web.app
import com.dungeonsanddeigo.web.dnd.sheet.showCharacterDetail
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.*

fun showListScreen() {
    app.innerHTML = ""

    val title = document.createElement("h1")
    title.textContent = t("app.title")
    app.appendChild(title)

    // Language switcher
    val langDiv = document.createElement("div") as HTMLDivElement
    langDiv.style.marginBottom = "12px"
    val langBtn = document.createElement("button") as HTMLButtonElement
    langBtn.textContent = if (I18n.current == Locale.EN) "\uD83C\uDDE7\uD83C\uDDF7 Português" else "\uD83C\uDDFA\uD83C\uDDF8 English"
    langBtn.style.fontSize = "12px"
    langBtn.addEventListener("click", {
        if (I18n.current == Locale.EN) {
            I18n.current = Locale.PT_BR
            localStorage.setItem("app_locale", "PT_BR")
        } else {
            I18n.current = Locale.EN
            localStorage.setItem("app_locale", "EN")
        }
        showListScreen()
    })
    langDiv.appendChild(langBtn)
    app.appendChild(langDiv)

    // Character list
    val characters = Repos.character.getAll()
    if (characters.isEmpty()) {
        val empty = document.createElement("p") as HTMLParagraphElement
        empty.textContent = t("char.noChars")
        app.appendChild(empty)
    } else {
        val ul = document.createElement("ul") as HTMLUListElement
        ul.style.listStyle = "none"
        ul.style.padding = "0"
        characters.forEach { c ->
            val li = document.createElement("li") as HTMLLIElement
            li.style.display = "flex"
            li.style.alignItems = "center"
            li.style.marginBottom = "8px"
            li.style.cursor = "pointer"
            if (c.imageBase64 != null) {
                val img = document.createElement("img") as HTMLImageElement
                img.src = c.imageBase64!!
                img.style.width = "40px"
                img.style.height = "40px"
                img.style.marginRight = "8px"
                img.style.borderRadius = "4px"
                li.appendChild(img)
            }
            val span = document.createElement("span")
            span.textContent = "${c.name} (${c.sheetModel.name})"
            li.appendChild(span)
            li.addEventListener("click", { showCharacterDetail(c) })
            ul.appendChild(li)
        }
        app.appendChild(ul)
    }

    // Create button
    val createBtn = document.createElement("button") as HTMLButtonElement
    createBtn.textContent = t("char.create")
    createBtn.addEventListener("click", { showAddCharSheet() })
    app.appendChild(createBtn)
}
