package com.dungeonsanddeigo.web.dnd.tabs.inventory

import com.dungeonsanddeigo.dnd.rules.calcModifier
import com.dungeonsanddeigo.dnd.rules.calcProficiency
import com.dungeonsanddeigo.i18n.*
import com.dungeonsanddeigo.model.*
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.modals.armor.DndArmorModal
import com.dungeonsanddeigo.web.dnd.modals.weapon.DndWeaponModal
import com.dungeonsanddeigo.web.dnd.modals.magicItem.DndMagicItemModal
import com.dungeonsanddeigo.web.dnd.modals.consumable.DndConsumableModal
import com.dungeonsanddeigo.web.dnd.modals.inventoryItem.DndInventoryItemModal
import com.dungeonsanddeigo.web.dnd.components.autoSaveIndicator.AutoSaveIndicator
import kotlinx.browser.document
import org.w3c.dom.*

fun renderDndInventoryTab(character: Character, container: HTMLDivElement) {
    val sections = listOf("Armor", "Weapons", "Magic Items", "Money", "Potions Ammo and Ration", "Key Items, Loot and others")

    fun refresh() {
        container.innerHTML = ""
        val grid = document.createElement("div") as HTMLDivElement
        grid.className = "inv-grid"

        sections.forEach { section ->
            val box = document.createElement("div") as HTMLDivElement
            box.className = "inv-box"

            val header = document.createElement("div") as HTMLDivElement
            header.className = "inv-header"

            val title = document.createElement("h3") as HTMLHeadingElement
            title.textContent = tSection(section)
            header.appendChild(title)

            if (section != "Money") {
                val addBtn = document.createElement("button") as HTMLButtonElement
                addBtn.textContent = t("inv.add")
                addBtn.className = "btn-primary"
                addBtn.addEventListener("click", {
                    when (section) {
                        "Armor" -> DndArmorModal(character, null).show { refresh() }
                        "Weapons" -> DndWeaponModal(character, null).show { refresh() }
                        "Magic Items" -> DndMagicItemModal(character, null).show { refresh() }
                        "Potions Ammo and Ration" -> DndConsumableModal(character, null).show { refresh() }
                        else -> DndInventoryItemModal(character, section, null).show { refresh() }
                    }
                })
                header.appendChild(addBtn)
            }

            box.appendChild(header)

            if (section == "Money") {
                // Money section: editable fields with auto-save
                val money = Repos.money.getByCharacterId(character.id) ?: DndMoney(characterId = character.id)

                // Collect all items for totals
                val allArmors    = Repos.armor.getByCharacterId(character.id)
                val allWeapons   = Repos.weapon.getByCharacterId(character.id)
                val allMagic     = Repos.magicItem.getByCharacterId(character.id)
                val allConsumables = Repos.consumable.getByCharacterId(character.id)
                val allInvItems  = listOf("Key Items, Loot and others", "Potions Ammo and Ration")
                    .flatMap { Repos.inventory.getByCategory(character.id, it) }

                // Convert all prices to copper for summing, then back per currency
                fun toCp(price: Int, currency: String): Int = price * when (currency) {
                    "pp" -> 1000; "pg" -> 100; "pe" -> 50; "ps" -> 10; else -> 1
                }
                val totalCp = allArmors.sumOf { toCp(it.price, it.priceCurrency) } +
                    allWeapons.sumOf { toCp(it.price, it.priceCurrency) } +
                    allMagic.sumOf { toCp(it.price, it.priceCurrency) } +
                    allConsumables.sumOf { toCp(it.price, it.priceCurrency) * it.quantity } +
                    allInvItems.sumOf { toCp(it.price, it.priceCurrency) }

                // Break down total into each currency (largest first)
                var remaining = totalCp
                val sumPp = remaining / 1000; remaining %= 1000
                val sumPg = remaining / 100;  remaining %= 100
                val sumPe = remaining / 50;   remaining %= 50
                val sumPs = remaining / 10;   remaining %= 10
                val sumCp = remaining

                val totalWeight = allArmors.sumOf { it.weight } +
                    allWeapons.sumOf { it.weight } +
                    allMagic.sumOf { it.weight } +
                    allConsumables.sumOf { it.weight * it.quantity } +
                    allInvItems.sumOf { it.weight }

                val coinsGrid = document.createElement("div") as HTMLDivElement
                coinsGrid.className = "inv-coins-grid"

                fun addCoinField(label: String, emoji: String, abbr: String, value: Int, itemSum: Int): HTMLInputElement {
                    val row = document.createElement("div") as HTMLDivElement
                    row.className = "inv-coin-row"
                    val lbl = document.createElement("span") as HTMLSpanElement
                    lbl.textContent = label
                    lbl.className = "inv-coin-label"
                    row.appendChild(lbl)
                    val input = document.createElement("input") as HTMLInputElement
                    input.type = "number"; input.min = "0"
                    input.value = value.toString()
                    row.appendChild(input)
                    val abbrSpan = document.createElement("span") as HTMLSpanElement
                    abbrSpan.textContent = "$emoji $abbr"
                    abbrSpan.className = "inv-coin-abbr"
                    row.appendChild(abbrSpan)
                    if (itemSum > 0) {
                        val sumSpan = document.createElement("span") as HTMLSpanElement
                        sumSpan.textContent = "+ ${t("inv.itemsSum")}: $itemSum$emoji $abbr"
                        sumSpan.className = "inv-coin-sum"
                        row.appendChild(sumSpan)
                    }
                    coinsGrid.appendChild(row)
                    return input
                }

                val cpInput = addCoinField(t("coin.copper"), "🥉", t("coin.copper.abbr"), money.copper, sumCp)
                val spInput = addCoinField(t("coin.silver"), "🥈", t("coin.silver.abbr"), money.silver, sumPs)
                val epInput = addCoinField(t("coin.electrum"), "🪙", t("coin.electrum.abbr"), money.electrum, sumPe)
                val gpInput = addCoinField(t("coin.gold"), "🥇", t("coin.gold.abbr"), money.gold, sumPg)
                val ppInput = addCoinField(t("coin.platinum"), "💎", t("coin.platinum.abbr"), money.platinum, sumPp)

                // Lifestyle
                val lifeRow = document.createElement("div") as HTMLDivElement
                lifeRow.className = "inv-coin-row"
                val lifeLbl = document.createElement("span") as HTMLSpanElement
                lifeLbl.textContent = t("inv.lifestyle")
                lifeLbl.className = "inv-coin-label"
                lifeRow.appendChild(lifeLbl)
                val lifeSelect = document.createElement("select") as HTMLSelectElement
                val emptyOpt = document.createElement("option") as HTMLOptionElement
                emptyOpt.value = ""; emptyOpt.textContent = "--"
                lifeSelect.appendChild(emptyOpt)
                DndMoney.lifestyles.forEach { ls ->
                    val opt = document.createElement("option") as HTMLOptionElement
                    opt.value = ls; opt.textContent = tLifestyle(ls)
                    lifeSelect.appendChild(opt)
                }
                lifeSelect.value = money.lifestyle
                lifeRow.appendChild(lifeSelect)
                val weightRounded = (totalWeight * 10).toInt().toDouble() / 10
                val weightSpan = document.createElement("span") as HTMLSpanElement
                weightSpan.textContent = "${t("inv.itemsWeight")}: ${weightRounded} kg"
                weightSpan.className = "inv-coin-label"
                lifeRow.appendChild(weightSpan)
                coinsGrid.appendChild(lifeRow)

                // Cost per day label
                val costLabel = document.createElement("span") as HTMLSpanElement
                costLabel.className = "inv-cost-label"

                fun lifestyleCost(ls: String): String = when (ls) {
                    "Wretched" -> t("inv.noCost")
                    "Squalid" -> "1 ${t("coin.silver.abbr")} / ${t("inv.perDay")}"
                    "Poor" -> "2 ${t("coin.silver.abbr")} / ${t("inv.perDay")}"
                    "Modest" -> "1 ${t("coin.gold.abbr")} / ${t("inv.perDay")}"
                    "Comfortable" -> "2 ${t("coin.gold.abbr")} / ${t("inv.perDay")}"
                    "Wealthy" -> "4 ${t("coin.gold.abbr")} / ${t("inv.perDay")}"
                    "Aristocratic" -> "10 ${t("coin.gold.abbr")} / ${t("inv.perDay")}"
                    else -> ""
                }

                fun updateCostLabel() {
                    val cost = lifestyleCost(lifeSelect.value)
                    costLabel.textContent = if (cost.isNotEmpty()) "${t("inv.costPerDay")}: $cost" else ""
                }
                updateCostLabel()
                lifeSelect.addEventListener("change", { updateCostLabel() })
                coinsGrid.appendChild(costLabel)

                box.appendChild(coinsGrid)

                val autoSave = AutoSaveIndicator(box)
                fun autoSaveMoney() {
                    autoSave.schedule {
                        Repos.money.save(DndMoney(
                            characterId = character.id,
                            copper = cpInput.value.toIntOrNull() ?: 0,
                            silver = spInput.value.toIntOrNull() ?: 0,
                            electrum = epInput.value.toIntOrNull() ?: 0,
                            gold = gpInput.value.toIntOrNull() ?: 0,
                            platinum = ppInput.value.toIntOrNull() ?: 0,
                            lifestyle = lifeSelect.value
                        ))
                    }
                }
                coinsGrid.addEventListener("input", { autoSaveMoney() })
                coinsGrid.addEventListener("change", { autoSaveMoney() })
            } else if (section == "Armor") {
                // Armor section
                val armorTypeOrder = mapOf("Light Armor" to 0, "Medium Armor" to 1, "Heavy Armor" to 2, "Shield" to 3, "Clothes" to 4)
                val armors = Repos.armor.getByCharacterId(character.id)
                    .sortedWith(compareByDescending<DndArmor> { it.isEquipped }
                        .thenBy { armorTypeOrder[it.type] ?: 5 }
                        .thenBy { it.name })

                // Calculate AC
                val stats = Repos.baseStats.getByCharacterId(character.id)
                val dexMod = stats?.dexValue?.let { calcModifier(it) } ?: 0
                val noArmorAC = stats?.emptyArmorClass ?: 10
                val equippedArmor = armors.firstOrNull { it.isEquipped && it.type != "Shield" && it.type != "Clothes" }
                val equippedShield = armors.firstOrNull { it.isEquipped && it.type == "Shield" }
                val shieldAC = equippedShield?.baseAC ?: 0

                val totalAC = if (equippedArmor == null) {
                    noArmorAC + shieldAC
                } else {
                    val armorBase = equippedArmor.baseAC
                    val modBonus = when (equippedArmor.acModifier) {
                        "Dex" -> dexMod
                        "Dex (Max: 2)" -> minOf(dexMod, 2)
                        else -> 0
                    }
                    armorBase + modBonus + shieldAC
                }

                // Display AC
                // Check proficiencies and stats for warnings
                val features = Repos.features.getByCharacterId(character.id)
                val weaponArmorFeatures = features.filter { it.type == "Weapon/Armor Proficiency" || it.type == t("stats.proficiency") }
                val armorProficiencies = mutableSetOf<String>()
                weaponArmorFeatures.forEach { f ->
                    f.description.split(",").map { it.trim() }.forEach { entry ->
                        if (entry.startsWith("armor:")) armorProficiencies.add(entry.removePrefix("armor:").trim())
                    }
                }

                // Check if equipped armor/shield lacks proficiency
                val equippedArmorProfKey = when (equippedArmor?.type) {
                    "Light Armor" -> "Light"
                    "Medium Armor" -> "Medium"
                    "Heavy Armor" -> "Heavy"
                    else -> null
                }
                val equippedShieldProfKey = if (equippedShield != null) "Shields" else null
                val lacksArmorProf = equippedArmor != null && equippedArmorProfKey != null && equippedArmorProfKey !in armorProficiencies
                val lacksShieldProf = equippedShield != null && equippedShieldProfKey != null && equippedShieldProfKey !in armorProficiencies
                val lacksAnyProf = lacksArmorProf || lacksShieldProf

                // Display AC with optional warning
                val acRow = document.createElement("div") as HTMLDivElement
                acRow.className = "inv-ac-row"

                val acDisplay = document.createElement("div") as HTMLDivElement
                acDisplay.className = "inv-ac-display"
                val acNumber = document.createElement("span") as HTMLSpanElement
                acNumber.textContent = totalAC.toString()
                acNumber.className = "inv-ac-number"
                acDisplay.appendChild(acNumber)
                val acLabel = document.createElement("div") as HTMLDivElement
                acLabel.textContent = t("stats.ac")
                acLabel.className = "inv-ac-label"
                acDisplay.appendChild(acLabel)
                acRow.appendChild(acDisplay)

                // Strength warning check
                val charStrength = stats?.strValue ?: 0
                val charSpeed = stats?.speed ?: 0
                val lacksStrength = equippedArmor != null && equippedArmor.minimumStrength > 0 && charStrength < equippedArmor.minimumStrength

                // Warnings column
                val warningsDiv = document.createElement("div") as HTMLDivElement
                warningsDiv.className = "inv-warnings"

                if (lacksAnyProf) {
                    val warning = document.createElement("div") as HTMLDivElement
                    warning.textContent = "\u26A0\uFE0F " + t("inv.lackProf")
                    warning.className = "inv-warning"
                    warningsDiv.appendChild(warning)
                }

                if (equippedArmor?.hasSneakDisadvantage == true) {
                    val sneakWarn = document.createElement("div") as HTMLDivElement
                    sneakWarn.textContent = "\u26A0\uFE0F " + t("inv.sneakDisadvWarn")
                    sneakWarn.className = "inv-warning"
                    warningsDiv.appendChild(sneakWarn)
                }

                if (lacksStrength) {
                    val strWarn = document.createElement("div") as HTMLDivElement
                    strWarn.textContent = "\u26A0\uFE0F " + t("inv.needStrength")
                    strWarn.className = "inv-warning"
                    warningsDiv.appendChild(strWarn)
                }

                acRow.appendChild(warningsDiv)

                // Speed display
                val speedDisplay = document.createElement("div") as HTMLDivElement
                speedDisplay.className = "inv-speed-display"
                val effectiveSpeed = if (lacksStrength) charSpeed - 3 else charSpeed
                val speedNumber = document.createElement("span") as HTMLSpanElement
                speedNumber.textContent = "${effectiveSpeed}m"
                speedNumber.className = "inv-speed-number" + if (lacksStrength) " reduced" else ""
                speedDisplay.appendChild(speedNumber)
                val speedLabel = document.createElement("div") as HTMLDivElement
                speedLabel.textContent = t("stats.speed")
                speedLabel.className = "inv-speed-label"
                speedDisplay.appendChild(speedLabel)
                acRow.appendChild(speedDisplay)

                box.appendChild(acRow)

                if (armors.isEmpty()) {
                    val placeholder = document.createElement("p") as HTMLParagraphElement
                    placeholder.textContent = t("inv.noArmor")
                    placeholder.className = "inv-placeholder"
                    
                    box.appendChild(placeholder)
                } else {
                    armors.forEach { armor ->
                        val row = document.createElement("div") as HTMLDivElement
                        row.className = "inv-row"

                        val line1 = document.createElement("div") as HTMLDivElement
                        line1.className = "inv-row-line1"

                        val nameSpan = document.createElement("span") as HTMLSpanElement
                        val eqIcon = if (armor.isEquipped) "\u2705 " else ""
                        nameSpan.textContent = "$eqIcon${armor.name}"
                        nameSpan.className = "inv-row-name"
                        line1.appendChild(nameSpan)

                        val actions = document.createElement("div") as HTMLDivElement
                        actions.className = "inv-row-actions"
                        
                        val editBtn = document.createElement("button") as HTMLButtonElement
                        editBtn.textContent = "✏️"
                        editBtn.className = "inv-row-btn"
                        editBtn.addEventListener("click", { DndArmorModal(character, armor).show { refresh() } })
                        actions.appendChild(editBtn)
                        val delBtn = document.createElement("button") as HTMLButtonElement
                        delBtn.textContent = "🗑"
                        delBtn.className = "inv-row-del-btn"
                        delBtn.addEventListener("click", { Repos.armor.delete(armor.id); refresh() })
                        actions.appendChild(delBtn)
                        line1.appendChild(actions)
                        row.appendChild(line1)

                        // Check warnings
                        val profKey = when (armor.type) {
                            "Light Armor" -> "Light"
                            "Medium Armor" -> "Medium"
                            "Heavy Armor" -> "Heavy"
                            "Shield" -> "Shields"
                            else -> ""
                        }
                        val hasProficiency = armor.type == "Clothes" || profKey in armorProficiencies
                        val hasStrWarning = armor.minimumStrength > 0 && charStrength < armor.minimumStrength

                        val line2 = document.createElement("div") as HTMLDivElement
                        line2.className = "inv-row-line2"
                        val sneakStr = if (armor.hasSneakDisadvantage) " | " + t("inv.sneakDisadvShort") else ""
                        val strStr = if (armor.minimumStrength > 0) " | ${t("inv.minStr")}: ${armor.minimumStrength}" else ""
                        if (armor.type == "Clothes") {
                            val featText = if (armor.additionalFeatures.isNotEmpty()) {
                                val truncated = if (armor.additionalFeatures.length > 50) armor.additionalFeatures.take(50) + "..." else armor.additionalFeatures
                                " | $truncated"
                            } else ""
                            line2.textContent = "${tArmorType(armor.type)} | ${armor.weight}kg | ${armor.price} ${tCurrency(armor.priceCurrency)}$featText"
                        } else {
                            if (!hasProficiency || hasStrWarning) {
                                val warnBadge = document.createElement("span") as HTMLSpanElement
                                warnBadge.textContent = "\u26A0\uFE0F "
                                line2.appendChild(warnBadge)
                            }
                            val infoSpan = document.createElement("span") as HTMLSpanElement
                            infoSpan.textContent = "${tArmorType(armor.type)} | ${t("inv.baseAC")}: ${armor.baseAC} (${tAcModifier(armor.acModifier)})$strStr$sneakStr | ${armor.weight}kg | ${armor.price} ${tCurrency(armor.priceCurrency)}"
                            line2.appendChild(infoSpan)
                        }
                        row.appendChild(line2)

                        box.appendChild(row)
                    }
                }
            } else if (section == "Weapons") {
                val weapons = Repos.weapon.getByCharacterId(character.id)

                // Gather weapon proficiencies from features
                val features = Repos.features.getByCharacterId(character.id)
                val weaponCatProfs = mutableSetOf<String>()
                val weaponSpecificProfs = mutableSetOf<String>()
                features.filter { it.type == "Weapon/Armor Proficiency" }.forEach { f ->
                    f.description.split(",").map { it.trim() }.forEach { entry ->
                        when {
                            entry.startsWith("weapon_cat:") -> weaponCatProfs.add(entry.removePrefix("weapon_cat:"))
                            entry.startsWith("weapon:") -> weaponSpecificProfs.add(entry.removePrefix("weapon:"))
                        }
                    }
                }

                // Attacks section (equipped weapons)
                val equippedWeapon = weapons.firstOrNull { it.isEquipped }
                if (equippedWeapon != null) {
                    val stats = Repos.baseStats.getByCharacterId(character.id)
                    val mainInfo = Repos.mainInfo.getByCharacterId(character.id)
                    val totalLevel = (mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0)
                    val profBonus = calcProficiency(totalLevel)
                    val strMod = stats?.strValue?.let { calcModifier(it) } ?: 0
                    val dexMod = stats?.dexValue?.let { calcModifier(it) } ?: 0

                    val hasProfEq = equippedWeapon.category in weaponCatProfs
                            || equippedWeapon.weaponType in weaponSpecificProfs
                    val isRanged = equippedWeapon.range && !equippedWeapon.thrown

                    val atkTable = document.createElement("table") as HTMLTableElement
                    atkTable.className = "inv-atk-table"

                    // Header
                    val thead = document.createElement("thead")
                    val headerRow = document.createElement("tr") as HTMLTableRowElement
                    listOf(t("inv.atkTable.name"), t("inv.atkTable.range"), t("inv.atkTable.test"), t("inv.atkTable.damage"), t("inv.atkTable.notes")).forEach { h ->
                        val th = document.createElement("th") as HTMLTableCellElement
                        th.textContent = h
                        headerRow.appendChild(th)
                    }
                    thead.appendChild(headerRow)
                    atkTable.appendChild(thead)

                    val tbody = document.createElement("tbody")

                    fun addAttackRow(useDex: Boolean, twoHanded: Boolean = false, thrown: Boolean = false) {
                        val mod = if (useDex) dexMod else strMod
                        val atkMod = mod + (if (hasProfEq) profBonus else 0)

                        val tr = document.createElement("tr") as HTMLTableRowElement

                        // Name
                        val tdName = document.createElement("td") as HTMLTableCellElement
                        val statLabel = if (equippedWeapon.finesse) " (${if (useDex) tStat("Dex") else tStat("Str")})" else ""
                        val handLabel = if (twoHanded) " [${t("inv.twoHandedShort")}]" else ""
                        val thrownLabel = if (thrown) " [${t("inv.thrownShort")}]" else ""
                        tdName.textContent = "${equippedWeapon.name}$statLabel$handLabel$thrownLabel"
                        tr.appendChild(tdName)

                        // Range
                        val tdRange = document.createElement("td") as HTMLTableCellElement
                        tdRange.textContent = if (isRanged || thrown) "${equippedWeapon.rangeDistance}/${equippedWeapon.rangeLongDistance}m" else t("inv.melee")
                        tr.appendChild(tdRange)

                        // Test
                        val tdTest = document.createElement("td") as HTMLTableCellElement
                        val testSign = if (atkMod >= 0) "+" else ""
                        tdTest.textContent = "$testSign$atkMod"
                        tr.appendChild(tdTest)

                        // Damage
                        val tdDmg = document.createElement("td") as HTMLTableCellElement
                        val dice = if (twoHanded) equippedWeapon.versatileDice else equippedWeapon.damageDice
                        val dmgStr = if (isRanged || thrown) {
                            "$dice ${when(equippedWeapon.damageType) { "Bludgeoning" -> t("inv.dmg.bludgeoning"); "Piercing" -> t("inv.dmg.piercing"); "Slashing" -> t("inv.dmg.slashing"); else -> equippedWeapon.damageType }}"
                        } else {
                            val modSign = if (mod >= 0) "+" else ""
                            "$dice$modSign$mod ${when(equippedWeapon.damageType) { "Bludgeoning" -> t("inv.dmg.bludgeoning"); "Piercing" -> t("inv.dmg.piercing"); "Slashing" -> t("inv.dmg.slashing"); else -> equippedWeapon.damageType }}"
                        }
                        tdDmg.textContent = dmgStr
                        tr.appendChild(tdDmg)

                        // Notes (icons + additional features)
                        val tdNotes = document.createElement("td") as HTMLTableCellElement
                        val icons = mutableListOf<String>()
                        if (equippedWeapon.silver) icons.add("\uD83E\uDD48")
                        if (equippedWeapon.heavy) icons.add("\u2693")
                        if (equippedWeapon.light) icons.add("\uD83E\uDEB6")
                        if (equippedWeapon.reach) icons.add("\uD83D\uDCAB")
                        if (equippedWeapon.ammunition) icons.add("\uD83C\uDFF9")
                        if (equippedWeapon.loading) icons.add("\u23F3")
                        val extras = mutableListOf<String>()
                        if (icons.isNotEmpty()) extras.add(icons.joinToString(""))
                        if (equippedWeapon.additionalFeatures.isNotEmpty()) {
                            val maxLen = 50 - (extras.firstOrNull()?.length ?: 0)
                            val feat = if (equippedWeapon.additionalFeatures.length > maxLen)
                                equippedWeapon.additionalFeatures.take(maxLen) + "..." else equippedWeapon.additionalFeatures
                            extras.add(feat)
                        }
                        tdNotes.textContent = extras.joinToString(" ")
                        tr.appendChild(tdNotes)

                        tbody.appendChild(tr)
                    }

                    if (isRanged) {
                        addAttackRow(useDex = true)
                    } else if (equippedWeapon.finesse) {
                        addAttackRow(useDex = false)
                        if (equippedWeapon.versatile) addAttackRow(useDex = false, twoHanded = true)
                        addAttackRow(useDex = true)
                        if (equippedWeapon.versatile) addAttackRow(useDex = true, twoHanded = true)
                        if (equippedWeapon.thrown) {
                            addAttackRow(useDex = false, thrown = true)
                            addAttackRow(useDex = true, thrown = true)
                        }
                    } else {
                        addAttackRow(useDex = false)
                        if (equippedWeapon.versatile) addAttackRow(useDex = false, twoHanded = true)
                        if (equippedWeapon.thrown) addAttackRow(useDex = false, thrown = true)
                    }

                    atkTable.appendChild(tbody)
                    box.appendChild(atkTable)
                }

                if (weapons.isEmpty()) {
                    val placeholder = document.createElement("p") as HTMLParagraphElement
                    placeholder.textContent = t("inv.noWeapons")
                    placeholder.className = "inv-placeholder"
                    
                    box.appendChild(placeholder)
                } else {
                    weapons.sortedByDescending { it.isEquipped }.forEach { weapon ->
                        val hasProficiency = weapon.category in weaponCatProfs
                                || weapon.weaponType in weaponSpecificProfs

                        val row = document.createElement("div") as HTMLDivElement
                        row.className = "inv-row"
                        
                        

                        val line1 = document.createElement("div") as HTMLDivElement
                        line1.className = "inv-row-line1"
                        
                        

                        val nameSpan = document.createElement("span") as HTMLSpanElement
                        val eqIcon = if (weapon.isEquipped) "\u2705 " else ""
                        val warnIcon = if (!hasProficiency) "\u26A0\uFE0F " else ""
                        nameSpan.textContent = "$warnIcon$eqIcon${weapon.name}"
                        nameSpan.className = "inv-row-name"
                        line1.appendChild(nameSpan)

                        val actions = document.createElement("div") as HTMLDivElement
                        actions.className = "inv-row-actions"
                        
                        val editBtn = document.createElement("button") as HTMLButtonElement
                        editBtn.textContent = "✏️"
                        editBtn.className = "inv-row-btn"
                        editBtn.addEventListener("click", { DndWeaponModal(character, weapon).show { refresh() } })
                        actions.appendChild(editBtn)
                        val delBtn = document.createElement("button") as HTMLButtonElement
                        delBtn.textContent = "🗑"
                        delBtn.className = "inv-row-del-btn"
                        delBtn.addEventListener("click", { Repos.weapon.delete(weapon.id); refresh() })
                        actions.appendChild(delBtn)
                        line1.appendChild(actions)
                        row.appendChild(line1)

                        val line2 = document.createElement("div") as HTMLDivElement
                        line2.className = "inv-row-line2"
                        val props = mutableListOf<String>()
                        props.add(when(weapon.category) { "Simple" -> t("features.weapon.simple"); "Martial" -> t("features.weapon.martial"); else -> weapon.category })
                        props.add("${weapon.damageDice} ${when(weapon.damageType) { "Bludgeoning" -> t("inv.dmg.bludgeoning"); "Piercing" -> t("inv.dmg.piercing"); "Slashing" -> t("inv.dmg.slashing"); else -> weapon.damageType }}")
                        if (weapon.versatile) props.add("${t("inv.versatile")} (${weapon.versatileDice})")
                        if (weapon.range) props.add("${t("inv.range")} ${weapon.rangeDistance}/${weapon.rangeLongDistance}m")
                        props.add("${weapon.weight}kg")
                        props.add("${weapon.price} ${tCurrency(weapon.priceCurrency)}")
                        // Property icons
                        val icons = mutableListOf<String>()
                        if (weapon.silver) icons.add("\uD83E\uDD48") // silver medal
                        if (weapon.heavy) icons.add("\u2693") // anchor = heavy
                        if (weapon.light) icons.add("\uD83E\uDEB6") // feather = light
                        if (weapon.reach) icons.add("\uD83D\uDCAB") // reach
                        if (weapon.ammunition) icons.add("\uD83C\uDFF9") // bow = ammo
                        if (weapon.loading) icons.add("\u23F3") // hourglass = loading
                        if (icons.isNotEmpty()) props.add(icons.joinToString(""))
                        // Features text
                        val featuresText = listOf(weapon.specialDescription, weapon.additionalFeatures)
                            .filter { it.isNotEmpty() }.joinToString(" | ")
                        if (featuresText.isNotEmpty()) {
                            val truncated = if (featuresText.length > 50) featuresText.take(50) + "..." else featuresText
                            props.add(truncated)
                        }
                        line2.textContent = props.joinToString(" | ")
                        row.appendChild(line2)

                        box.appendChild(row)
                    }
                }
            } else if (section == "Magic Items") {
                val magicItems = Repos.magicItem.getByCharacterId(character.id)
                    .sortedWith(compareBy<DndMagicItem> {
                        when {
                            it.needSynch && it.isSynched -> 0
                            !it.needSynch -> 1
                            else -> 2
                        }
                    }.thenBy { it.name })

                // Synch counter
                val synchedCount = magicItems.count { it.needSynch && it.isSynched }
                val synchInfo = document.createElement("div") as HTMLDivElement
                synchInfo.className = "inv-synch-info" + if (synchedCount > 3) " over-limit" else ""
                synchInfo.textContent = "${t("inv.synchedItems")}: $synchedCount / ${t("inv.max")}: 3"
                box.appendChild(synchInfo)

                if (magicItems.isEmpty()) {
                    val placeholder = document.createElement("p") as HTMLParagraphElement
                    placeholder.textContent = t("inv.noMagicItems")
                    placeholder.className = "inv-placeholder"
                    
                    box.appendChild(placeholder)
                } else {
                    magicItems.forEach { item ->
                        val row = document.createElement("div") as HTMLDivElement
                        row.className = "inv-row"
                        
                        

                        val line1 = document.createElement("div") as HTMLDivElement
                        line1.className = "inv-row-line1"
                        
                        

                        val nameSpan = document.createElement("span") as HTMLSpanElement
                        val synchIcon = if (!item.needSynch) "\uD83D\uDD35 "
                            else if (item.isSynched) "\u2705 " else ""
                        nameSpan.textContent = "$synchIcon${item.name}"
                        nameSpan.className = "inv-row-name"
                        line1.appendChild(nameSpan)

                        val actions = document.createElement("div") as HTMLDivElement
                        actions.className = "inv-row-actions"
                        
                        val editBtn = document.createElement("button") as HTMLButtonElement
                        editBtn.textContent = "✏️"
                        editBtn.className = "inv-row-btn"
                        editBtn.addEventListener("click", { DndMagicItemModal(character, item).show { refresh() } })
                        actions.appendChild(editBtn)
                        val delBtn = document.createElement("button") as HTMLButtonElement
                        delBtn.textContent = "🗑"
                        delBtn.className = "inv-row-del-btn"
                        delBtn.addEventListener("click", { Repos.magicItem.delete(item.id); refresh() })
                        actions.appendChild(delBtn)
                        line1.appendChild(actions)
                        row.appendChild(line1)

                        val line2 = document.createElement("div") as HTMLDivElement
                        line2.className = "inv-row-line2"
                        val effectStr = if (item.effect.isNotEmpty()) {
                            val truncated = if (item.effect.length > 80) item.effect.take(80) + "..." else item.effect
                            "$truncated | "
                        } else ""
                        line2.textContent = "$effectStr${item.weight}kg | ${item.price} ${tCurrency(item.priceCurrency)}"
                        row.appendChild(line2)

                        box.appendChild(row)
                    }
                }
            } else if (section == "Potions Ammo and Ration") {
                val consumables = Repos.consumable.getByCharacterId(character.id)
                if (consumables.isEmpty()) {
                    val placeholder = document.createElement("p") as HTMLParagraphElement
                    placeholder.textContent = t("inv.noItems")
                    placeholder.className = "inv-placeholder"
                    
                    box.appendChild(placeholder)
                } else {
                    consumables.forEach { item ->
                        val row = document.createElement("div") as HTMLDivElement
                        row.className = "inv-row"
                        
                        

                        val line1 = document.createElement("div") as HTMLDivElement
                        line1.className = "inv-row-line1"
                        
                        

                        val nameSpan = document.createElement("span") as HTMLSpanElement
                        val qtyStr = if (item.quantity > 1) " x${item.quantity}" else ""
                        nameSpan.textContent = "${item.name}$qtyStr"
                        nameSpan.className = "inv-row-name"
                        line1.appendChild(nameSpan)

                        val actions = document.createElement("div") as HTMLDivElement
                        actions.className = "inv-row-actions"
                        
                        val editBtn = document.createElement("button") as HTMLButtonElement
                        editBtn.textContent = "✏️"
                        editBtn.className = "inv-row-btn"
                        editBtn.addEventListener("click", { DndConsumableModal(character, item).show { refresh() } })
                        actions.appendChild(editBtn)
                        val delBtn = document.createElement("button") as HTMLButtonElement
                        delBtn.textContent = "🗑"
                        delBtn.className = "inv-row-del-btn"
                        delBtn.addEventListener("click", { Repos.consumable.delete(item.id); refresh() })
                        actions.appendChild(delBtn)
                        line1.appendChild(actions)
                        row.appendChild(line1)

                        val line2 = document.createElement("div") as HTMLDivElement
                        line2.className = "inv-row-line2"
                        val effectStr = if (item.effect.isNotEmpty()) {
                            val truncated = if (item.effect.length > 50) item.effect.take(50) + "..." else item.effect
                            " | $truncated"
                        } else ""
                        line2.textContent = "${when(item.type) { "Healing Potion" -> t("inv.consumable.healingPotion"); "Magic Potion" -> t("inv.consumable.magicPotion"); "Food" -> t("inv.consumable.food"); "Ammunition" -> t("inv.consumable.ammunition"); "Other" -> t("inv.consumable.other"); else -> item.type }}$effectStr | ${item.weight}kg | ${item.price} ${tCurrency(item.priceCurrency)}"
                        row.appendChild(line2)

                        box.appendChild(row)
                    }
                }
            } else {
            // Key Items, Loot and others
            val items = Repos.inventory.getByCategory(character.id, section).sortedBy { it.name }
            if (items.isEmpty()) {
                val placeholder = document.createElement("p") as HTMLParagraphElement
                placeholder.textContent = t("inv.noItems")
                placeholder.className = "inv-placeholder"
                
                box.appendChild(placeholder)
            } else {
                items.forEach { item ->
                    val row = document.createElement("div") as HTMLDivElement
                    row.className = "inv-row"
                    
                    

                    val line1 = document.createElement("div") as HTMLDivElement
                    line1.className = "inv-row-line1"
                    
                    

                    val nameSpan = document.createElement("span") as HTMLSpanElement
                    nameSpan.textContent = item.name
                    nameSpan.className = "inv-row-name"
                    line1.appendChild(nameSpan)

                    val actions = document.createElement("div") as HTMLDivElement
                    actions.className = "inv-row-actions"
                    
                    val editBtn = document.createElement("button") as HTMLButtonElement
                    editBtn.textContent = "✏️"
                        editBtn.className = "inv-row-btn"
                    editBtn.addEventListener("click", {
                        DndInventoryItemModal(character, section, item).show { refresh() }
                    })
                    actions.appendChild(editBtn)
                    val delBtn = document.createElement("button") as HTMLButtonElement
                    delBtn.textContent = "🗑"
                        delBtn.className = "inv-row-del-btn"
                    delBtn.addEventListener("click", { Repos.inventory.delete(item.id); refresh() })
                    actions.appendChild(delBtn)
                    line1.appendChild(actions)
                    row.appendChild(line1)

                    if (item.description.isNotEmpty()) {
                        val line2 = document.createElement("div") as HTMLDivElement
                        line2.className = "inv-row-line2"
                        val truncated = if (item.description.length > 80) item.description.take(80) + "..." else item.description
                        line2.textContent = truncated
                        row.appendChild(line2)
                    }

                    box.appendChild(row)
                }
            }
            } // end else (non-Money)

            grid.appendChild(box)
        }

        container.appendChild(grid)
    }

    refresh()
}

