package com.dungeonsanddeigo.web.dnd.components.damageTypes

import com.dungeonsanddeigo.i18n.t
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLLIElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.HTMLUListElement

data class DamageTypeItem(
    val value: String,
    val label: String,
    val emoji: String
)

enum class DamageTypeEmojiType {
    WITH_EMOJI,
    ONLY_EMOJI,
    NO_EMOJI
}

object DamageTypes {
    private val mergedEntries = listOf(
        DamageTypeItem("Acid", t("magic.dmg.acid"), "🧪"),
        DamageTypeItem("Bludgeoning", t("inv.dmg.bludgeoning"), "🔨"),
        DamageTypeItem("Cold", t("magic.dmg.cold"), "❄️"),
        DamageTypeItem("Fire", t("magic.dmg.fire"), "🔥"),
        DamageTypeItem("Force", t("magic.dmg.force"), "💢"),
        DamageTypeItem("Lightning", t("magic.dmg.lightning"), "⚡️"),
        DamageTypeItem("Necrotic", t("magic.dmg.necrotic"), "💀"),
        DamageTypeItem("Piercing", t("inv.dmg.piercing"), "🏹"),
        DamageTypeItem("Poison", t("magic.dmg.poison"), "🦠"),
        DamageTypeItem("Psychic", t("magic.dmg.psychic"), "🪬"),
        DamageTypeItem("Radiant", t("magic.dmg.radiant"), "🌟"),
        DamageTypeItem("Slashing", t("inv.dmg.slashing"), "🤺"),
        DamageTypeItem("Thunder", t("magic.dmg.thunder"), "⛈️")
    )

    val values: List<String>
        get() = mergedEntries.map { it.value }

    fun entries(): List<DamageTypeItem> = mergedEntries

    fun getEntry(entry: String, emojiType: DamageTypeEmojiType = DamageTypeEmojiType.WITH_EMOJI): String {
        val item = mergedEntries.firstOrNull { it.value.equals(entry, ignoreCase = true) }
            ?: return entry

        return when (emojiType) {
            DamageTypeEmojiType.WITH_EMOJI -> if (item.emoji.isNotEmpty()) "${item.emoji} ${item.label}" else item.label
            DamageTypeEmojiType.ONLY_EMOJI -> item.emoji
            DamageTypeEmojiType.NO_EMOJI -> item.label
        }
    }

    fun label(value: String): String = mergedEntries.firstOrNull { it.value.equals(value, ignoreCase = true) }?.label ?: value

    fun emoji(value: String): String = mergedEntries.firstOrNull { it.value.equals(value, ignoreCase = true) }?.emoji ?: ""

    fun renderList(container: HTMLElement): HTMLUListElement {
        val list = document.createElement("ul") as HTMLUListElement
        list.className = "damage-types-list"

        mergedEntries.forEach { item ->
            val li = document.createElement("li") as HTMLLIElement
            li.className = "damage-types-list__item"

            val emojiSpan = document.createElement("span") as HTMLSpanElement
            emojiSpan.className = "damage-types-list__emoji"
            emojiSpan.textContent = item.emoji
            li.appendChild(emojiSpan)

            val labelSpan = document.createElement("span") as HTMLSpanElement
            labelSpan.className = "damage-types-list__label"
            labelSpan.textContent = item.label
            li.appendChild(labelSpan)

            list.appendChild(li)
        }

        container.appendChild(list)
        return list
    }
}