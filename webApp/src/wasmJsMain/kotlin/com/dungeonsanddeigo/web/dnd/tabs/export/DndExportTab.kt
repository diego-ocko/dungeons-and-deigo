package com.dungeonsanddeigo.web.dnd.tabs.export

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.*

fun renderDndExportTab(character: Character, container: HTMLDivElement) {
    val ctx = ExportContext(character)

    val wrapper = (document.createElement("div") as HTMLDivElement).also { it.className = "export-tab" }

    // Top bar — visible on screen only
    val topBar = (document.createElement("div") as HTMLDivElement).also { it.className = "export-tab__topbar" }
    val pdfBtn = (document.createElement("button") as HTMLButtonElement).also {
        it.textContent = t("export.generatePdf")
        it.className = "btn-primary export-tab__pdf-btn"
        it.addEventListener("click", { window.print() })
    }
    topBar.appendChild(pdfBtn)
    wrapper.appendChild(topBar)

    // Sheet — hidden on screen, rendered when printing
    val sheet = (document.createElement("div") as HTMLDivElement).also {
        it.className = "export-sheet"
        it.id = "export-print-area"
    }

    buildExportPage1(ctx).also { sheet.appendChild(it) }
    buildExportPage2(ctx)?.also { sheet.appendChild(it) }
    buildExportPage3(ctx)?.also { sheet.appendChild(it) }
    buildExportPage4(ctx)?.also { sheet.appendChild(it) }
    buildExportPage5(ctx)?.also { sheet.appendChild(it) }

    wrapper.appendChild(sheet)
    container.appendChild(wrapper)
}
