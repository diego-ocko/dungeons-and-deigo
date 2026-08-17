package com.dungeonsanddeigo.web.dnd.tabs.export

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tStat
import com.dungeonsanddeigo.model.DndInventoryItem
import com.dungeonsanddeigo.web.dnd.components.damageTypes.DamageTypes
import org.w3c.dom.*

fun buildExportPage3(ctx: ExportContext): HTMLDivElement? {
    val spells = ctx.spells
    val weapons = ctx.weapons
    val armors = ctx.armors
    val magicItems = ctx.magicItems
    val keyItems = ctx.items.filter { it.category == "Key Items, Loot and others" }

    val hasContent = spells.isNotEmpty() || weapons.isNotEmpty() || armors.isNotEmpty() ||
        magicItems.isNotEmpty() || keyItems.isNotEmpty()
    if (!hasContent) return null

    val page = ctx.div("export-page")
    page.appendChild(ctx.buildHeader(t("export.page3Title")))

    val content = ctx.div("export-page3")

    // ── Helper ────────────────────────────────────────────────────────────────

    fun metaLine(card: HTMLDivElement, label: String, value: String) {
        if (value.isEmpty()) return
        val row = ctx.div("export-detail-card__meta-row")
        val b = (document.createElement("b") as HTMLElement)
        b.textContent = "$label: "
        row.appendChild(b)
        row.appendChild(document.createTextNode(value))
        card.appendChild(row)
    }

    // ── Spells ────────────────────────────────────────────────────────────────
    if (spells.isNotEmpty()) {
        val circleOrder = listOf(
            "Cantrip",
            "Circle 1", "Circle 2", "Circle 3", "Circle 4", "Circle 5",
            "Circle 6", "Circle 7", "Circle 8", "Circle 9"
        )
        val spellsByCircle = spells.groupBy { it.circle }

        circleOrder.forEach { circle ->
            val circleSpells = spellsByCircle[circle]?.sortedBy { it.name } ?: return@forEach

            val circleHeader = ctx.div("export-spell-circle-header")
            circleHeader.textContent = ctx.circleStr(circle)
            content.appendChild(circleHeader)

            circleSpells.forEach { spell ->
                val card = ctx.div("export-detail-card")

                val schoolEmoji = when (spell.school) {
                    "Abjuration"    -> "🛡️"
                    "Conjuration"   -> "🌀"
                    "Divination"    -> "🔮"
                    "Enchantment"   -> "💫"
                    "Evocation"     -> "🔥"
                    "Illusion"      -> "🎭"
                    "Necromancy"    -> "💀"
                    "Transmutation" -> "⚗️"
                    else -> ""
                }

                val nameRow = ctx.div("export-detail-card__name")
                nameRow.textContent = buildString {
                    if (schoolEmoji.isNotEmpty()) append("$schoolEmoji ")
                    append(spell.name)
                }
                card.appendChild(nameRow)

                if (spell.school.isNotEmpty())
                    card.appendChild(ctx.span("export-detail-card__school", t("magic.school.${spell.school.lowercase()}")))

                if (spell.castingTime.isNotEmpty()) {
                    val ct = if (spell.canBeRitual) "${spell.castingTime} (${t("magic.ritual")})" else spell.castingTime
                    metaLine(card, "🕰️ Conj.", ct)
                }
                if (spell.range.isNotEmpty()) metaLine(card, t("magic.range"), spell.range)

                val components = mutableListOf<String>()
                if (spell.hasVerbal) components.add("V")
                if (spell.hasSomatic) components.add("S")
                if (spell.hasMaterial) components.add("M")
                if (components.isNotEmpty()) {
                    val compStr = if (spell.hasMaterial && spell.materialComponents.isNotEmpty())
                        "${components.joinToString(", ")} (${spell.materialComponents})"
                    else components.joinToString(", ")
                    metaLine(card, t("magic.components"), compStr)
                }

                if (spell.duration.isNotEmpty()) {
                    val dur = if (spell.needsConcentration) "${t("magic.concentration")}: ${spell.duration}"
                              else spell.duration
                    metaLine(card, t("magic.duration"), dur)
                }

                if (spell.description.isNotEmpty())
                    card.appendChild(ctx.p("export-detail-card__desc", spell.description))
                if (spell.higherCircles.isNotEmpty())
                    card.appendChild(ctx.p("export-detail-card__higher", spell.higherCircles))

                content.appendChild(card)
            }
        }
    }

    // ── Items section header ──────────────────────────────────────────────────
    val hasItems = weapons.isNotEmpty() || armors.isNotEmpty() || magicItems.isNotEmpty() || keyItems.isNotEmpty()
    if (hasItems) {
        content.appendChild(ctx.sectionLabel(t("export.items")))
    }

    // ── Weapons ───────────────────────────────────────────────────────────────
    if (weapons.isNotEmpty()) {
        val header = ctx.div("export-spell-circle-header")
        header.textContent = t("inventory.weapons")
        content.appendChild(header)

        fun dmgTypeLabel(type: String) = DamageTypes.getEntry(type)

        fun weaponTypeLabel(wt: String) = when (wt) {
            "Club"            -> t("inv.wt.club")
            "Dagger"          -> t("inv.wt.dagger")
            "Greatclub"       -> t("inv.wt.greatclub")
            "Handaxe"         -> t("inv.wt.handaxe")
            "Javelin"         -> t("inv.wt.javelin")
            "Light Hammer"    -> t("inv.wt.lightHammer")
            "Mace"            -> t("inv.wt.mace")
            "Quarterstaff"    -> t("inv.wt.quarterstaff")
            "Sickle"          -> t("inv.wt.sickle")
            "Spear"           -> t("inv.wt.spear")
            "Crossbow (Light)"-> t("inv.wt.crossbowLight")
            "Dart"            -> t("inv.wt.dart")
            "Shortbow"        -> t("inv.wt.shortbow")
            "Sling"           -> t("inv.wt.sling")
            "Battleaxe"       -> t("inv.wt.battleaxe")
            "Flail"           -> t("inv.wt.flail")
            "Glaive"          -> t("inv.wt.glaive")
            "Greataxe"        -> t("inv.wt.greataxe")
            "Greatsword"      -> t("inv.wt.greatsword")
            "Halberd"         -> t("inv.wt.halberd")
            "Lance"           -> t("inv.wt.lance")
            "Longsword"       -> t("inv.wt.longsword")
            "Maul"            -> t("inv.wt.maul")
            "Morningstar"     -> t("inv.wt.morningstar")
            "Pike"            -> t("inv.wt.pike")
            "Rapier"          -> t("inv.wt.rapier")
            "Scimitar"        -> t("inv.wt.scimitar")
            "Shortsword"      -> t("inv.wt.shortsword")
            "Trident"         -> t("inv.wt.trident")
            "War Pick"        -> t("inv.wt.warPick")
            "Warhammer"       -> t("inv.wt.warhammer")
            "Whip"            -> t("inv.wt.whip")
            "Blowgun"         -> t("inv.wt.blowgun")
            "Crossbow (Hand)" -> t("inv.wt.crossbowHand")
            "Crossbow (Heavy)"-> t("inv.wt.crossbowHeavy")
            "Longbow"         -> t("inv.wt.longbow")
            "Net"             -> t("inv.wt.net")
            else -> wt
        }

        weapons.sortedBy { it.name }.forEach { w ->
            val card = ctx.div("export-detail-card")

            val nameRow = ctx.div("export-detail-card__name")
            nameRow.textContent = "⚔️ ${w.name}"
            card.appendChild(nameRow)

            // Type as school-style subtitle
            val typeStr = buildString {
                append(when (w.category) {
                    "Simple"  -> t("inv.simpleWeapon")
                    "Martial" -> t("inv.martialWeapon")
                    else -> w.category
                })
                if (w.weaponType.isNotEmpty()) append(" · ${weaponTypeLabel(w.weaponType)}")
            }
            if (typeStr.isNotEmpty())
                card.appendChild(ctx.span("export-detail-card__school", typeStr))

            if (w.damageDice.isNotEmpty())
                metaLine(card, t("inv.atkTable.damage"), "${w.damageDice} ${dmgTypeLabel(w.damageType)}")
            if (w.versatile && w.versatileDice.isNotEmpty())
                metaLine(card, t("inv.twoHandedShort"), "${w.versatileDice} ${dmgTypeLabel(w.damageType)}")
            if (w.range && w.rangeDistance > 0)
                metaLine(card, t("inv.atkTable.range"), "${w.rangeDistance}/${w.rangeLongDistance}m")

            val props = buildList {
                if (w.light) add(t("inv.light"))
                if (w.heavy) add(t("inv.heavy"))
                if (w.finesse) add(t("inv.finesse"))
                if (w.thrown) add(t("inv.thrown"))
                if (w.reach) add(t("inv.reach"))
                if (w.twoHanded) add(t("inv.twoHanded"))
                if (w.loading) add(t("inv.loading"))
                if (w.ammunition) add(t("inv.ammunition"))
                if (w.silver) add(t("inv.silver"))
            }
            if (props.isNotEmpty()) metaLine(card, t("inv.category"), props.joinToString(", "))
            if (w.specialDescription.isNotEmpty()) metaLine(card, t("inv.special"), w.specialDescription)
            if (w.additionalFeatures.isNotEmpty())
                card.appendChild(ctx.p("export-detail-card__desc", w.additionalFeatures))

            content.appendChild(card)
        }
    }

    // ── Armors ────────────────────────────────────────────────────────────────
    if (armors.isNotEmpty()) {
        val header = ctx.div("export-spell-circle-header")
        header.textContent = t("inventory.armor")
        content.appendChild(header)

        fun armorTypeLabel(type: String) = when (type) {
            "Light Armor"  -> t("inv.lightArmor")
            "Medium Armor" -> t("inv.mediumArmor")
            "Heavy Armor"  -> t("inv.heavyArmor")
            "Shield"       -> t("inv.shield")
            "Clothes"      -> t("inv.clothes")
            else -> type
        }

        armors.sortedBy { it.name }.forEach { a ->
            val card = ctx.div("export-detail-card")

            val nameRow = ctx.div("export-detail-card__name")
            nameRow.textContent = "🛡 ${a.name}"
            card.appendChild(nameRow)

            if (a.type.isNotEmpty())
                card.appendChild(ctx.span("export-detail-card__school", armorTypeLabel(a.type)))

            metaLine(card, "CA", buildString {
                append(a.baseAC)
                when (a.acModifier) {
                    "Dex"          -> append(" + ${tStat("Dex")}")
                    "Dex (Max: 2)" -> append(" + ${tStat("Dex")} (máx 2)")
                }
            })
            if (a.minimumStrength > 0) metaLine(card, "${tStat("Str")} mín.", a.minimumStrength.toString())
            if (a.hasSneakDisadvantage) metaLine(card, t("inv.sneakDisadvWarn"), "")
            if (a.additionalFeatures.isNotEmpty())
                card.appendChild(ctx.p("export-detail-card__desc", a.additionalFeatures))

            content.appendChild(card)
        }
    }

    // ── Magic items ───────────────────────────────────────────────────────────
    if (magicItems.isNotEmpty()) {
        val header = ctx.div("export-spell-circle-header")
        header.textContent = t("inventory.magicItems")
        content.appendChild(header)

        magicItems.sortedBy { it.name }.forEach { m ->
            val card = ctx.div("export-detail-card")

            val nameRow = ctx.div("export-detail-card__name")
            nameRow.textContent = "✨ ${m.name}"
            card.appendChild(nameRow)

            if (m.needSynch)
                card.appendChild(ctx.span("export-detail-card__school", t("inv.needSynch")))

            if (m.weight > 0) metaLine(card, t("inv.weight"), "${m.weight}kg")
            if (m.price > 0) metaLine(card, t("inv.price"), "${m.price} ${m.priceCurrency}")
            if (m.effect.isNotEmpty())
                card.appendChild(ctx.p("export-detail-card__desc", m.effect))

            content.appendChild(card)
        }
    }

    // ── Key Items ─────────────────────────────────────────────────────────────
    if (keyItems.isNotEmpty()) {
        val header = ctx.div("export-spell-circle-header")
        header.textContent = t("inventory.other")
        content.appendChild(header)

        keyItems.sortedBy { it.name }.forEach { item ->
            val card = ctx.div("export-detail-card")

            val nameRow = ctx.div("export-detail-card__name")
            nameRow.textContent = "🗝️ ${item.name}"
            card.appendChild(nameRow)

            if (item.quantity > 1) metaLine(card, t("inv.quantity"), item.quantity.toString())
            if (item.weight > 0) metaLine(card, t("inv.weight"), "${item.weight}kg")
            if (item.price > 0) metaLine(card, t("inv.price"), "${item.price} ${item.priceCurrency}")
            if (item.description.isNotEmpty())
                card.appendChild(ctx.p("export-detail-card__desc", item.description))

            content.appendChild(card)
        }
    }

    page.appendChild(content)
    return page
}

private val document get() = kotlinx.browser.document
