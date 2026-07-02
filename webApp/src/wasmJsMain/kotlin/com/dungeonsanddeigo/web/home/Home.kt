package com.dungeonsanddeigo.web.home

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.addCharSheet.showAddCharSheet
import com.dungeonsanddeigo.web.app
import com.dungeonsanddeigo.web.components.header.renderHeader
import com.dungeonsanddeigo.web.components.modal.Modal
import com.dungeonsanddeigo.web.dnd.sheet.showCharacterDetail
import kotlinx.browser.document
import kotlinx.dom.addClass
import org.w3c.dom.*

fun showListScreen() {
    app.innerHTML = ""
    renderHeader(app) { showListScreen() }

    // Character list
    val subtitle = document.createElement("h2")
    subtitle.textContent = t("char.list")
    app.appendChild(subtitle)

    val characters = Repos.character.getAll()
    if (characters.isEmpty()) {
        val empty = document.createElement("p") as HTMLParagraphElement
        empty.textContent = t("char.noChars")
        app.appendChild(empty)
    } else {
        val ul = document.createElement("ul") as HTMLUListElement
        ul.className = "home__list"
        characters.forEach { c ->
            val li = document.createElement("li") as HTMLLIElement
            li.className = "home__list-item"
            if (c.imageBase64 != null) {
                val img = document.createElement("img") as HTMLImageElement
                img.src = c.imageBase64!!
                img.className = "home__list-img"
                li.appendChild(img)
            }
            val span = document.createElement("span") as HTMLSpanElement
            span.textContent = "${c.name} (${c.sheetModel.name})"
            span.className = "home__list-name"
            span.addEventListener("click", { showCharacterDetail(c) })
            li.appendChild(span)

            val deleteBtn = document.createElement("button") as HTMLButtonElement
            deleteBtn.textContent = "\uD83D\uDDD1\uFE0F"
            deleteBtn.title = t("char.delete")
            deleteBtn.addEventListener("click", { e ->
                e.stopPropagation()
                DeleteCharacterModal(c.id, c.name).show { showListScreen() }
            })
            li.appendChild(deleteBtn)

            ul.appendChild(li)
        }
        app.appendChild(ul)
    }

    // Create button
    val createBtn = document.createElement("button") as HTMLButtonElement
    createBtn.textContent = t("char.create")
    createBtn.addClass("btn", "btn-primary")
    createBtn.addEventListener("click", { showAddCharSheet() })
    app.appendChild(createBtn)
}

private class DeleteCharacterModal(
    private val characterId: Long,
    private val characterName: String
) : Modal(
    title = t("char.deleteTitle"),
    size = "md",
    saveLabel = t("char.confirmDelete")
) {
    override fun buildForm(form: HTMLDivElement) {
        val msg = document.createElement("p") as HTMLParagraphElement
        msg.textContent = "${t("char.deleteConfirm")} $characterName? ${t("char.deleteWarning")}"
        form.appendChild(msg)
    }

    override fun onSave(close: () -> Unit) {
        Repos.character.delete(characterId)
        close()
    }
}
