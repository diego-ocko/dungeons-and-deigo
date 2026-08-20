package com.dungeonsanddeigo.web.dnd.tabs.export

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.*

fun renderDndExportTab(character: Character, container: HTMLDivElement) {
    val wrapper = (document.createElement("div") as HTMLDivElement).also { it.className = "export-tab" }

    // ── Options panel ─────────────────────────────────────────────────────
    val optionsBox = (document.createElement("div") as HTMLDivElement).also { it.className = "export-options" }
    optionsBox.appendChild((document.createElement("h3") as HTMLHeadingElement).also {
        it.textContent = t("export.exportOptions")
        it.className = "export-options__title"
    })

    fun checkbox(labelKey: String): HTMLInputElement {
        val row = (document.createElement("label") as HTMLLabelElement).also { it.className = "export-options__check-row" }
        val cb = (document.createElement("input") as HTMLInputElement).also {
            it.type = "checkbox"
            it.className = "export-options__checkbox"
            it.checked = true
        }
        row.appendChild(cb)
        row.appendChild((document.createElement("span") as HTMLSpanElement).also {
            it.textContent = t(labelKey)
            it.className = "export-options__check-label"
        })
        optionsBox.appendChild(row)
        return cb
    }

    val addPlayerCheckbox      = (document.createElement("input") as HTMLInputElement).also { it.type = "checkbox"; it.className = "export-options__checkbox" }.also {
        val row = (document.createElement("label") as HTMLLabelElement).also { l -> l.className = "export-options__check-row" }
        row.appendChild(it)
        row.appendChild((document.createElement("span") as HTMLSpanElement).also { s ->
            s.textContent = t("export.addPlayerName"); s.className = "export-options__check-label"
        })
        optionsBox.appendChild(row)
    }
    val playerInput = (document.createElement("input") as HTMLInputElement).also {
        it.type = "text"
        it.placeholder = t("export.playerNamePlaceholder")
        it.className = "export-options__text-input"
        it.style.display = "none"
    }
    optionsBox.appendChild(playerInput)
    addPlayerCheckbox.addEventListener("change", {
        playerInput.style.display = if (addPlayerCheckbox.checked) "block" else "none"
    })

    val showHpCheckbox           = checkbox("export.opt.showHp")
    val showLimitedUsageCheckbox = checkbox("export.opt.showLimitedUsage")
    val showHitDiceUsedCheckbox  = checkbox("export.opt.showHitDiceUsed")
    val showDeathSavesCheckbox   = checkbox("export.opt.showDeathSaveResults")
    val showSpellSlotsUsedCheckbox = checkbox("export.opt.showSpellSlotsUsed")
    val showPreparedSpellsCheckbox = checkbox("export.opt.showPreparedSpells")

    wrapper.appendChild(optionsBox)

    // ── Sheet ─────────────────────────────────────────────────────────────
    val sheet = (document.createElement("div") as HTMLDivElement).also {
        it.className = "export-sheet"
        it.id = "export-print-area"
    }
    rebuildSheet(character, sheet, Options())

    // ── Top bar ───────────────────────────────────────────────────────────
    val topBar = (document.createElement("div") as HTMLDivElement).also { it.className = "export-tab__topbar" }
    val pdfBtn = (document.createElement("button") as HTMLButtonElement).also {
        it.textContent = t("export.generatePdf")
        it.className = "btn-primary export-tab__pdf-btn"
    }
    pdfBtn.addEventListener("click", {
        rebuildSheet(character, sheet, Options(
            playerName          = if (addPlayerCheckbox.checked) playerInput.value else null,
            showHp               = showHpCheckbox.checked,
            showLimitedUsage     = showLimitedUsageCheckbox.checked,
            showHitDiceUsed      = showHitDiceUsedCheckbox.checked,
            showDeathSaveResults = showDeathSavesCheckbox.checked,
            showSpellSlotsUsed   = showSpellSlotsUsedCheckbox.checked,
            showPreparedSpells   = showPreparedSpellsCheckbox.checked,
        ))
        window.print()
    })
    topBar.appendChild(pdfBtn)
    wrapper.appendChild(topBar)

    wrapper.appendChild(sheet)
    container.appendChild(wrapper)
}

private data class Options(
    val playerName: String? = null,
    val showHp: Boolean = true,
    val showLimitedUsage: Boolean = true,
    val showHitDiceUsed: Boolean = true,
    val showDeathSaveResults: Boolean = true,
    val showSpellSlotsUsed: Boolean = true,
    val showPreparedSpells: Boolean = true,
)

private fun rebuildSheet(character: Character, sheet: HTMLDivElement, opts: Options) {
    sheet.innerHTML = ""
    val ctx = ExportContext(
        character            = character,
        playerName           = opts.playerName,
        showHp               = opts.showHp,
        showLimitedUsage     = opts.showLimitedUsage,
        showHitDiceUsed      = opts.showHitDiceUsed,
        showDeathSaveResults = opts.showDeathSaveResults,
        showSpellSlotsUsed   = opts.showSpellSlotsUsed,
        showPreparedSpells   = opts.showPreparedSpells,
    )
    buildExportPage1(ctx).also { sheet.appendChild(it) }
    buildExportPage2(ctx)?.also { sheet.appendChild(it) }
    buildExportPage3(ctx)?.also { sheet.appendChild(it) }
    buildExportPage4(ctx)?.also { sheet.appendChild(it) }
}
