package com.dungeonsanddeigo.web.dnd.tabs.export

import com.dungeonsanddeigo.i18n.t
import kotlinx.browser.document
import kotlinx.browser.localStorage
import com.dungeonsanddeigo.web.dnd.components.damageTypes.DamageTypes
import org.w3c.dom.*

// Page 2: Appearance (cols 1-3) | History/Faction (cols 4-7) | Personality (cols 8-10)
fun buildExportPage2(ctx: ExportContext): HTMLDivElement? {
    val appearance = ctx.appearance
    val backstory = ctx.backstory
    if (appearance == null && backstory == null) return null

    val page = ctx.div("export-page")
    page.appendChild(ctx.buildHeader(t("export.page2Title")))

    val grid = ctx.div("export-page2")

    // ── Column 1-3: Aparência ──────────────────────────────────────────────
    val colAppearance = ctx.div("export-p2-col export-p2-col--appearance")
    colAppearance.appendChild(ctx.sectionLabel(t("export.p2.appearance")))

    if (appearance != null) {
        // Character image — prefer localStorage override, fall back to model field
        val imgKey = "char_image_${ctx.character.id}"
        val imgData = localStorage.getItem(imgKey)?.takeIf { it.isNotEmpty() }
            ?: ctx.character.imageBase64?.takeIf { it.isNotEmpty() }
        if (imgData != null) {
            val img = document.createElement("img") as HTMLImageElement
            img.src = imgData
            img.className = "export-p2-portrait"
            img.alt = ctx.character.name
            colAppearance.appendChild(img)
        }

        // Physical stats
        val physGrid = ctx.div("export-p2-phys-grid")
        fun physItem(label: String, value: String) {
            if (value.isEmpty()) return
            val item = ctx.div("export-appearance-item")
            item.appendChild(ctx.div("export-appearance-item__label").also { it.textContent = label })
            item.appendChild(ctx.div("export-appearance-item__val").also { it.textContent = value })
            physGrid.appendChild(item)
        }
        physItem(t("bg.age"), appearance.age)
        physItem(t("bg.height"), appearance.height)
        physItem(t("bg.weight"), appearance.weight)
        physItem(t("bg.eyeColor"), appearance.eyeColor)
        physItem(t("bg.skinColor"), appearance.skinColor)
        physItem(t("bg.hairColor"), appearance.hairColor)
        colAppearance.appendChild(physGrid)

        if (appearance.description.isNotEmpty()) {
            colAppearance.appendChild(ctx.sectionLabel(t("bg.appearanceDesc")))
            colAppearance.appendChild(ctx.p("export-bg-full-text", appearance.description))
        }
    }

    grid.appendChild(colAppearance)

    // ── Columns 4-7: História ──────────────────────────────────────────────
    val colHistory = ctx.div("export-p2-col export-p2-col--history")
    colHistory.appendChild(ctx.sectionLabel(t("export.p2.history")))

    if (backstory != null) {
        fun bgField(label: String, text: String) {
            if (text.isEmpty()) return
            colHistory.appendChild(ctx.sectionLabel(label))
            colHistory.appendChild(ctx.p("export-bg-full-text", text))
        }
        bgField(t("bg.charBackstory"), backstory.characterBackstory)
        if (backstory.hasFaction && backstory.factionName.isNotEmpty()) {
            bgField(backstory.factionName, backstory.factionBackstory)
        }
    }

    grid.appendChild(colHistory)

    // ── Columns 8-10: Personalidade ───────────────────────────────────────
    val colPersonality = ctx.div("export-p2-col export-p2-col--personality")
    colPersonality.appendChild(ctx.sectionLabel(t("export.p2.personality")))

    if (backstory != null) {
        fun traitField(label: String, text: String) {
            if (text.isEmpty()) return
            val card = ctx.div("export-p2-trait-card")
            card.appendChild(ctx.div("export-p2-trait-card__label").also { it.textContent = label })
            card.appendChild(ctx.p("export-p2-trait-card__text", text))
            colPersonality.appendChild(card)
        }
        traitField(t("bg.personalityTraits"), backstory.personalityTraits)
        traitField(t("bg.ideals"), backstory.ideals)
        traitField(t("bg.bonds"), backstory.bonds)
        traitField(t("bg.defects"), backstory.defects)
        traitField(t("bg.habits"), backstory.habits)
    }

    grid.appendChild(colPersonality)

    page.appendChild(grid)

    // ── Inventário ─────────────────────────────────────────────────────────
    val hasInventory = ctx.weapons.isNotEmpty() || ctx.armors.isNotEmpty() ||
        ctx.consumables.isNotEmpty() || ctx.magicItems.isNotEmpty() || ctx.items.isNotEmpty()

    if (hasInventory) {
        val invSection = ctx.div("export-p2-inventory")
        invSection.appendChild(ctx.sectionLabel(t("export.p2.inventory")))

        val table = document.createElement("table") as HTMLTableElement
        table.className = "export-inv-table"

        val thead = document.createElement("thead") as HTMLTableSectionElement
        val headerRow = document.createElement("tr") as HTMLTableRowElement
        listOf(
            t("label.name"),
            t("inv.type"),
            t("inv.quantity"),
            t("inv.weight"),
            t("inv.price")
        ).forEach { col ->
            val th = document.createElement("th") as HTMLTableCellElement
            th.textContent = col
            headerRow.appendChild(th)
        }
        thead.appendChild(headerRow)
        table.appendChild(thead)

        val tbody = document.createElement("tbody") as HTMLTableSectionElement

        fun fmtWeight(w: Double) = if (w > 0.0) "${w.toInt().takeIf { it.toDouble() == w } ?: w} kg" else "—"
        fun fmtPrice(p: Int, cur: String) = if (p > 0) "$p $cur" else "—"

        fun addRow(name: String, type: String, qty: String, weight: String, price: String) {
            val tr = document.createElement("tr") as HTMLTableRowElement
            listOf(name, type, qty, weight, price).forEach { cell ->
                val td = document.createElement("td") as HTMLTableCellElement
                td.textContent = cell
                tr.appendChild(td)
            }
            tbody.appendChild(tr)
        }

        fun armorTypeLabel(type: String) = when (type) {
            "Light Armor"  -> t("inv.lightArmor")
            "Medium Armor" -> t("inv.mediumArmor")
            "Heavy Armor"  -> t("inv.heavyArmor")
            "Shield"       -> t("inv.shield")
            "Clothes"      -> t("inv.clothes")
            else -> type
        }

        fun weaponTypeLabel(weaponType: String) = when (weaponType) {
            "Club"             -> t("inv.wt.club")
            "Dagger"           -> t("inv.wt.dagger")
            "Greatclub"        -> t("inv.wt.greatclub")
            "Handaxe"          -> t("inv.wt.handaxe")
            "Javelin"          -> t("inv.wt.javelin")
            "Light Hammer"     -> t("inv.wt.lightHammer")
            "Mace"             -> t("inv.wt.mace")
            "Quarterstaff"     -> t("inv.wt.quarterstaff")
            "Sickle"           -> t("inv.wt.sickle")
            "Spear"            -> t("inv.wt.spear")
            "Crossbow (Light)" -> t("inv.wt.crossbowLight")
            "Dart"             -> t("inv.wt.dart")
            "Shortbow"         -> t("inv.wt.shortbow")
            "Sling"            -> t("inv.wt.sling")
            "Battleaxe"        -> t("inv.wt.battleaxe")
            "Flail"            -> t("inv.wt.flail")
            "Glaive"           -> t("inv.wt.glaive")
            "Greataxe"         -> t("inv.wt.greataxe")
            "Greatsword"       -> t("inv.wt.greatsword")
            "Halberd"          -> t("inv.wt.halberd")
            "Lance"            -> t("inv.wt.lance")
            "Longsword"        -> t("inv.wt.longsword")
            "Maul"             -> t("inv.wt.maul")
            "Morningstar"      -> t("inv.wt.morningstar")
            "Pike"             -> t("inv.wt.pike")
            "Rapier"           -> t("inv.wt.rapier")
            "Scimitar"         -> t("inv.wt.scimitar")
            "Shortsword"       -> t("inv.wt.shortsword")
            "Trident"          -> t("inv.wt.trident")
            "War Pick"         -> t("inv.wt.warPick")
            "Warhammer"        -> t("inv.wt.warhammer")
            "Whip"             -> t("inv.wt.whip")
            "Blowgun"          -> t("inv.wt.blowgun")
            "Crossbow (Hand)"  -> t("inv.wt.crossbowHand")
            "Crossbow (Heavy)" -> t("inv.wt.crossbowHeavy")
            "Longbow"          -> t("inv.wt.longbow")
            "Net"              -> t("inv.wt.net")
            else -> weaponType
        }

        fun weaponCategoryLabel(cat: String) = when (cat) {
            "Simple"  -> t("features.weapon.simple")
            "Martial" -> t("features.weapon.martial")
            "Others"  -> t("features.weapon.others")
            else -> cat
        }

        fun consumableTypeLabel(type: String) = when (type) {
            "Healing Potion" -> t("inv.consumable.healingPotion")
            "Magic Potion"   -> t("inv.consumable.magicPotion")
            "Food"           -> t("inv.consumable.food")
            "Ammunition"     -> t("inv.consumable.ammunition")
            "Other"          -> t("inv.consumable.other")
            else -> type
        }

        fun invCategoryLabel(cat: String) = when (cat) {
            "Armor"                          -> t("inventory.armor")
            "Weapons"                        -> t("inventory.weapons")
            "Magic Items"                    -> t("inventory.magicItems")
            "Potions Ammo and Ration"        -> t("inventory.potions")
            "Key Items, Loot and others"     -> t("inventory.other")
            else -> cat
        }

        fun dmgTypeLabel(dmgType: String) = DamageTypes.getEntry(dmgType)

        ctx.weapons.forEach { w ->
            val catLabel = weaponCategoryLabel(w.category)
            val typeBase = if (w.weaponType.isNotEmpty()) "${catLabel} - ${weaponTypeLabel(w.weaponType)}" else catLabel
            val props = buildList {
                if (w.damageDice.isNotEmpty()) add("${w.damageDice} ${dmgTypeLabel(w.damageType)}")
                if (w.light)      add(t("inv.light"))
                if (w.heavy)      add(t("inv.heavy"))
                if (w.finesse)    add(t("inv.finesse"))
                if (w.versatile)  add(t("inv.versatile"))
                if (w.thrown)     add(t("inv.thrown"))
                if (w.reach)      add(t("inv.reach"))
                if (w.twoHanded)  add(t("inv.twoHanded"))
                if (w.loading)    add(t("inv.loading"))
                if (w.silver)     add(t("inv.silver"))
                if (w.special)    add(t("inv.special"))
            }
            val nameLabel = if (props.isNotEmpty()) "${w.name} (${props.joinToString(", ")})" else w.name
            addRow(
                name   = nameLabel,
                type   = typeBase,
                qty    = "1",
                weight = fmtWeight(w.weight),
                price  = fmtPrice(w.price, w.priceCurrency)
            )
        }
        ctx.armors.forEach { a ->
            addRow(
                name   = a.name,
                type   = armorTypeLabel(a.type),
                qty    = "1",
                weight = fmtWeight(a.weight),
                price  = fmtPrice(a.price, a.priceCurrency)
            )
        }
        ctx.consumables.forEach { c ->
            val nameWithEffect = if (c.effect.isNotEmpty()) "${c.name} (${c.effect})" else c.name
            addRow(
                name   = nameWithEffect,
                type   = consumableTypeLabel(c.type),
                qty    = c.quantity.toString(),
                weight = fmtWeight(c.weight * c.quantity),
                price  = fmtPrice(c.price * c.quantity, c.priceCurrency)
            )
        }
        ctx.magicItems.forEach { m ->
            val magicType = if (m.needSynch) t("export.p2.magicItemSynch") else t("export.p2.magicItem")
            addRow(
                name   = m.name,
                type   = magicType,
                qty    = "1",
                weight = fmtWeight(m.weight),
                price  = fmtPrice(m.price, m.priceCurrency)
            )
        }
        ctx.items.forEach { i ->
            addRow(
                name   = i.name,
                type   = invCategoryLabel(i.category),
                qty    = i.quantity.toString(),
                weight = fmtWeight(i.weight * i.quantity),
                price  = fmtPrice(i.price * i.quantity, i.priceCurrency)
            )
        }

        table.appendChild(tbody)
        invSection.appendChild(table)
        page.appendChild(invSection)
    }

    return page
}
