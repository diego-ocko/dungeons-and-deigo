package com.dungeonsanddeigo.web.dnd.components.autoSaveIndicator

import com.dungeonsanddeigo.i18n.t
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.*

class AutoSaveIndicator(container: HTMLElement) {
    private val el = (document.createElement("span") as HTMLSpanElement).also {
        it.className = "auto-save-indicator"
        document.body?.appendChild(it) ?: container.appendChild(it)
    }
    private var saveTimeout = 0
    private var hideTimeout = 0

    fun schedule(delayMs: Int = 500, save: () -> Unit) {
        el.textContent = t("status.saving")
        el.className = "auto-save-indicator saving"
        if (saveTimeout != 0) window.clearTimeout(saveTimeout)
        if (hideTimeout != 0) window.clearTimeout(hideTimeout)
        saveTimeout = window.setTimeout({
            save()
            el.textContent = t("status.saved")
            el.className = "auto-save-indicator saved"
            hideTimeout = window.setTimeout({
                el.className = "auto-save-indicator hidden"
                null
            }, 10_000)
            null
        }, delayMs)
    }
}
