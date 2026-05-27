package com.dungeonsanddeigo.web.dnd.components.autoSaveIndicator

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.*

class AutoSaveIndicator(container: HTMLElement) {
    private val el = (document.createElement("span") as HTMLSpanElement).also {
        it.className = "auto-save-indicator"
        container.appendChild(it)
    }
    private var timeout = 0

    fun schedule(delayMs: Int = 500, save: () -> Unit) {
        el.textContent = "Saving..."
        el.className = "auto-save-indicator saving"
        if (timeout != 0) window.clearTimeout(timeout)
        timeout = window.setTimeout({
            save()
            el.textContent = "\u2713 Saved"
            el.className = "auto-save-indicator saved"
            null
        }, delayMs)
    }
}
