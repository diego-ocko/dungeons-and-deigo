package com.dungeonsanddeigo.web.dnd.tabs.main

import com.dungeonsanddeigo.dnd.rules.calcProficiency
import com.dungeonsanddeigo.i18n.*
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndFeature
import com.dungeonsanddeigo.model.DndMainInfo
import com.dungeonsanddeigo.model.DungeonsAndDragons
import com.dungeonsanddeigo.web.Repos
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.*

fun renderDndMainTab(character: Character, container: HTMLDivElement) {

    val HOMEBREW_ADD_OPTION_LABEL = t("main.homeBrewOption")
    val IS_HOMEBREW_LABEL = t("main.isHomeBrew")
    val info = Repos.mainInfo.getByCharacterId(character.id) ?: DndMainInfo(characterId = character.id)

    val form = document.createElement("div") as HTMLDivElement
    form.className = "main-tab-form"

    val customClasses = mutableListOf<String>()

    fun addField(label: String, value: String?): HTMLInputElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        val input = document.createElement("input") as HTMLInputElement
        input.value = value ?: ""
        form.appendChild(lbl)
        form.appendChild(input)
        return input
    }

    fun addNumberField(label: String, value: Int?): HTMLInputElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        val input = document.createElement("input") as HTMLInputElement
        input.type = "number"
        input.value = value?.toString() ?: ""
        form.appendChild(lbl)
        form.appendChild(input)
        return input
    }

    fun buildSubClassSelect(className: String?, currentValue: String?): HTMLDivElement {
        val wrapper = document.createElement("div") as HTMLDivElement
        val select = document.createElement("select") as HTMLSelectElement

        val emptyOpt = document.createElement("option") as HTMLOptionElement
        emptyOpt.value = ""
        emptyOpt.textContent = t("general.select")
        select.appendChild(emptyOpt)

        val subClasses = DungeonsAndDragons.subClassesFor(className)
        subClasses.forEach { sc ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = sc
            opt.textContent = tDnd("subclass", sc)
            select.appendChild(opt)
        }

        val customOpt = document.createElement("option") as HTMLOptionElement
        customOpt.value = "__custom__"
        customOpt.textContent = HOMEBREW_ADD_OPTION_LABEL
        select.appendChild(customOpt)

        if (currentValue != null && currentValue !in subClasses) {
            val existingCustom = document.createElement("option") as HTMLOptionElement
            existingCustom.value = currentValue
            existingCustom.textContent = "$currentValue $IS_HOMEBREW_LABEL"
            select.insertBefore(existingCustom, customOpt)
        }

        select.value = currentValue ?: ""

        val customInput = document.createElement("input") as HTMLInputElement
        customInput.placeholder = t("main.enterCustomSubClass")
        customInput.className = "main-tab-custom-input"

        val addBtn = document.createElement("button") as HTMLButtonElement
        addBtn.textContent = "Add"
        addBtn.className = "main-tab-custom-btn"

        select.addEventListener("change", {
            if (select.value == "__custom__") {
                customInput.classList.add("visible")
                addBtn.classList.add("visible")
            } else {
                customInput.classList.remove("visible")
                addBtn.classList.remove("visible")
            }
        })

        addBtn.addEventListener("click", {
            val customValue = customInput.value.trim()
            if (customValue.isNotEmpty()) {
                val newOpt = document.createElement("option") as HTMLOptionElement
                newOpt.value = customValue
                newOpt.textContent = "$customValue $IS_HOMEBREW_LABEL"
                select.insertBefore(newOpt, customOpt)
                select.value = customValue
                customInput.value = ""
                customInput.classList.remove("visible")
                addBtn.classList.remove("visible")
            }
        })

        wrapper.appendChild(select)
        wrapper.appendChild(customInput)
        wrapper.appendChild(addBtn)
        return wrapper
    }

    fun addSubClassSelect(label: String, className: String?, value: String?): HTMLDivElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        form.appendChild(lbl)
        val wrapper = buildSubClassSelect(className, value)
        form.appendChild(wrapper)
        return wrapper
    }

    fun addClassSelect(label: String, value: String?, subClassWrapper: () -> HTMLDivElement?): HTMLSelectElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        form.appendChild(lbl)

        val wrapper = document.createElement("div") as HTMLDivElement

        val select = document.createElement("select") as HTMLSelectElement

        val emptyOpt = document.createElement("option") as HTMLOptionElement
        emptyOpt.value = ""
        emptyOpt.textContent = t("general.select")
        select.appendChild(emptyOpt)

        DungeonsAndDragons.defaultClasses.forEach { cls ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = cls
            opt.textContent = tDnd("class", cls)
            select.appendChild(opt)
        }

        val customOpt = document.createElement("option") as HTMLOptionElement
        customOpt.value = "__custom__"
        customOpt.textContent = HOMEBREW_ADD_OPTION_LABEL
        select.appendChild(customOpt)

        Repos.customClass.getAll().forEach { cls ->
            if (cls != value) {
                val opt = document.createElement("option") as HTMLOptionElement
                opt.value = cls
                opt.textContent = "$cls $IS_HOMEBREW_LABEL"
                select.insertBefore(opt, customOpt)
            }
        }

        if (value != null && value !in DungeonsAndDragons.defaultClasses && value !in Repos.customClass.getAll()) {
            val existingCustom = document.createElement("option") as HTMLOptionElement
            existingCustom.value = value
            existingCustom.textContent = "$value $IS_HOMEBREW_LABEL"
            select.insertBefore(existingCustom, customOpt)
            customClasses.add(value)
        }

        select.value = value ?: ""

        val customInput = document.createElement("input") as HTMLInputElement
        customInput.placeholder = t("main.enterCustomClass")
        customInput.classList.add("main-tab-custom-input")

        val addBtn = document.createElement("button") as HTMLButtonElement
        addBtn.textContent = "Add"
        addBtn.classList.add("main-tab-custom-btn")

        select.addEventListener("change", {
            if (select.value == "__custom__") {
                customInput.classList.add("visible")
                addBtn.classList.add("visible")
            } else {
                customInput.classList.remove("visible")
                addBtn.classList.remove("visible")
            }
            // Rebuild sub-class dropdown
            val scWrapper = subClassWrapper()
            if (scWrapper != null) {
                val selectedClass = if (select.value == "__custom__") null else select.value
                val newContent = buildSubClassSelect(selectedClass, null)
                scWrapper.innerHTML = ""
                while (newContent.firstChild != null) {
                    scWrapper.appendChild(newContent.firstChild!!)
                }
            }
        })

        addBtn.addEventListener("click", {
            val customValue = customInput.value.trim()
            if (customValue.isNotEmpty()) {
                Repos.customClass.add(customValue)
                val newOpt = document.createElement("option") as HTMLOptionElement
                newOpt.value = customValue
                newOpt.textContent = "$customValue $IS_HOMEBREW_LABEL"
                select.insertBefore(newOpt, customOpt)
                select.value = customValue
                customInput.value = ""
                customInput.classList.remove("visible")
                addBtn.classList.remove("visible")
                // Rebuild sub-class for custom class (only Custom option)
                val scWrapper = subClassWrapper()
                if (scWrapper != null) {
                    val newContent = buildSubClassSelect(customValue, null)
                    scWrapper.innerHTML = ""
                    while (newContent.firstChild != null) {
                        scWrapper.appendChild(newContent.firstChild!!)
                    }
                }
            }
        })

        wrapper.appendChild(select)
        wrapper.appendChild(customInput)
        wrapper.appendChild(addBtn)
        form.appendChild(wrapper)

        return select
    }

    // We need references to sub-class wrappers, but they're created after the class selects.
    var mainSubClassWrapper: HTMLDivElement? = null
    var secondarySubClassWrapper: HTMLDivElement? = null

    // --- Main Class row (3 columns) ---
    val mainRow = document.createElement("div") as HTMLDivElement
    mainRow.className = "main-tab-class-row"

    fun addClassSelectTo(parent: HTMLDivElement, label: String, value: String?, subClassRef: () -> HTMLDivElement?): HTMLSelectElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        col.appendChild(lbl)
        // Reuse addClassSelect logic but append to col instead of form
        val wrapper = document.createElement("div") as HTMLDivElement
        val select = document.createElement("select") as HTMLSelectElement
        val emptyOpt = document.createElement("option") as HTMLOptionElement
        emptyOpt.value = ""
        emptyOpt.textContent = t("general.select")
        select.appendChild(emptyOpt)
        DungeonsAndDragons.defaultClasses.forEach { cls ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = cls
            opt.textContent = tDnd("class", cls)
            select.appendChild(opt)
        }
        val customOpt = document.createElement("option") as HTMLOptionElement
        customOpt.value = "__custom__"
        customOpt.textContent = HOMEBREW_ADD_OPTION_LABEL
        select.appendChild(customOpt)
        Repos.customClass.getAll().forEach { cls ->
            if (cls != value) {
                val opt = document.createElement("option") as HTMLOptionElement
                opt.value = cls
                opt.textContent = "$cls $IS_HOMEBREW_LABEL"
                select.insertBefore(opt, customOpt)
            }
        }
        if (value != null && value !in DungeonsAndDragons.defaultClasses && value !in Repos.customClass.getAll()) {
            val existingCustom = document.createElement("option") as HTMLOptionElement
            existingCustom.value = value
            existingCustom.textContent = "$value $IS_HOMEBREW_LABEL"
            select.insertBefore(existingCustom, customOpt)
        }
        select.value = value ?: ""
        val customInput = document.createElement("input") as HTMLInputElement
        customInput.placeholder = t("main.enterCustomClass")
        customInput.classList.add("main-tab-custom-input")

        val addBtn = document.createElement("button") as HTMLButtonElement
        addBtn.textContent = "Add"
        addBtn.classList.add("main-tab-custom-btn")

        select.addEventListener("change", {
            if (select.value == "__custom__") {
                customInput.classList.add("visible")
                addBtn.classList.add("visible")
            } else {
                customInput.classList.remove("visible")
                addBtn.classList.remove("visible")
            }
            val scWrapper = subClassRef()
            if (scWrapper != null) {
                val selectedClass = if (select.value == "__custom__") null else select.value
                val newContent = buildSubClassSelect(selectedClass, null)
                scWrapper.innerHTML = ""
                while (newContent.firstChild != null) { scWrapper.appendChild(newContent.firstChild!!) }
            }
        })
        addBtn.addEventListener("click", {
            val customValue = customInput.value.trim()
            if (customValue.isNotEmpty()) {
                Repos.customClass.add(customValue)
                val newOpt = document.createElement("option") as HTMLOptionElement
                newOpt.value = customValue
                newOpt.textContent = "$customValue $IS_HOMEBREW_LABEL"
                select.insertBefore(newOpt, customOpt)
                select.value = customValue
                customInput.value = ""
                customInput.classList.remove("visible")
                addBtn.classList.remove("visible")
                val scWrapper = subClassRef()
                if (scWrapper != null) {
                    val newContent = buildSubClassSelect(customValue, null)
                    scWrapper.innerHTML = ""
                    while (newContent.firstChild != null) { scWrapper.appendChild(newContent.firstChild!!) }
                }
            }
        })
        wrapper.appendChild(select)
        wrapper.appendChild(customInput)
        wrapper.appendChild(addBtn)
        col.appendChild(wrapper)
        parent.appendChild(col)
        return select
    }

    fun addSubClassSelectTo(parent: HTMLDivElement, label: String, className: String?, value: String?): HTMLDivElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        col.appendChild(lbl)
        val wrapper = buildSubClassSelect(className, value)
        col.appendChild(wrapper)
        parent.appendChild(col)
        return wrapper
    }

    fun addNumberFieldTo(parent: HTMLDivElement, label: String, value: Int?): HTMLInputElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        col.appendChild(lbl)
        val input = document.createElement("input") as HTMLInputElement
        input.type = "number"
        input.value = value?.toString() ?: ""
        col.appendChild(input)
        parent.appendChild(col)
        return input
    }

    // --- Main Class box ---
    val mainClassBox = document.createElement("div") as HTMLDivElement
    mainClassBox.className = "main-tab-box"
    val mainClassBoxTitle = document.createElement("h3") as HTMLHeadingElement
    mainClassBoxTitle.className = "main-tab-box-title"
    mainClassBoxTitle.textContent = t("main.boxMainClass")
    mainClassBox.appendChild(mainClassBoxTitle)
    mainClassBox.appendChild(mainRow)

    val mainClassSelect = addClassSelectTo(mainRow, t("main.class"), info.mainClass) { mainSubClassWrapper }
    mainSubClassWrapper = addSubClassSelectTo(mainRow, t("main.subclass"), info.mainClass, info.mainSubClass)
    val mainClassLevelInput = addNumberFieldTo(mainRow, t("main.level"), info.mainClassLevel)
    mainClassLevelInput.min = "1"
    mainClassLevelInput.max = "20"

    // --- Multiclass box ---
    val secRow = document.createElement("div") as HTMLDivElement
    secRow.className = "main-tab-class-row"

    val multiclassBox = document.createElement("div") as HTMLDivElement
    multiclassBox.className = "main-tab-box"
    val multiclassBoxTitle = document.createElement("h3") as HTMLHeadingElement
    multiclassBoxTitle.className = "main-tab-box-title"
    multiclassBoxTitle.textContent = t("main.boxMulticlass")
    multiclassBox.appendChild(multiclassBoxTitle)
    multiclassBox.appendChild(secRow)

    val secondaryClassSelect = addClassSelectTo(secRow, t("main.secondaryClass"), info.secondaryClass) { secondarySubClassWrapper }
    secondarySubClassWrapper = addSubClassSelectTo(secRow, t("main.subclass"), info.secondaryClass, info.secondarySubClass)
    val secondaryClassLevelInput = addNumberFieldTo(secRow, t("main.level"), info.secondaryClassLevel)
    secondaryClassLevelInput.min = "0"
    secondaryClassLevelInput.max = "20"

    // Enforce sum <= 20
    val levelError = document.createElement("span") as HTMLSpanElement
    levelError.className = "main-tab-level-error"
    levelError.textContent = t("main.levelSumError")

    fun validateLevels() {
        val main = mainClassLevelInput.value.toIntOrNull() ?: 0
        val sec = secondaryClassLevelInput.value.toIntOrNull() ?: 0
        if (main + sec > 20) {
            levelError.style.display = "inline"
        } else {
            levelError.style.display = "none"
        }
    }

    mainClassLevelInput.addEventListener("input", { validateLevels() })
    secondaryClassLevelInput.addEventListener("input", { validateLevels() })

    // Insert error span after the grid (will be added to container later)
    validateLevels()
    var subRaceWrapper: HTMLDivElement? = null

    fun buildSubRaceSelect(raceName: String?, currentValue: String?): HTMLDivElement {
        val wrapper = document.createElement("div") as HTMLDivElement
        val select = document.createElement("select") as HTMLSelectElement

        val emptyOpt = document.createElement("option") as HTMLOptionElement
        emptyOpt.value = ""
        emptyOpt.textContent = t("general.select")
        select.appendChild(emptyOpt)

        val subRaces = DungeonsAndDragons.subRacesFor(raceName)
        subRaces.forEach { sr ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = sr
            opt.textContent = tDnd("subrace", sr)
            select.appendChild(opt)
        }

        val customOpt = document.createElement("option") as HTMLOptionElement
        customOpt.value = "__custom__"
        customOpt.textContent = HOMEBREW_ADD_OPTION_LABEL
        select.appendChild(customOpt)

        if (currentValue != null && currentValue !in subRaces) {
            val existingCustom = document.createElement("option") as HTMLOptionElement
            existingCustom.value = currentValue
            existingCustom.textContent = "$currentValue $IS_HOMEBREW_LABEL"
            select.insertBefore(existingCustom, customOpt)
        }

        select.value = currentValue ?: ""

        val customInput = document.createElement("input") as HTMLInputElement
        customInput.placeholder = t("main.enterCustomSubRace")
        customInput.classList.add("main-tab-custom-input")

        val addBtn = document.createElement("button") as HTMLButtonElement
        addBtn.textContent = "Add"
        addBtn.classList.add("main-tab-custom-btn")

        select.addEventListener("change", {
            if (select.value == "__custom__") {
                customInput.classList.add("visible")
                addBtn.classList.add("visible")
            } else {
                customInput.classList.remove("visible")
                addBtn.classList.remove("visible")
            }
        })

        addBtn.addEventListener("click", {
            val customValue = customInput.value.trim()
            if (customValue.isNotEmpty()) {
                val newOpt = document.createElement("option") as HTMLOptionElement
                newOpt.value = customValue
                newOpt.textContent = "$customValue $IS_HOMEBREW_LABEL"
                select.insertBefore(newOpt, customOpt)
                select.value = customValue
                customInput.value = ""
                customInput.classList.remove("visible")
                addBtn.classList.remove("visible")
            }
        })

        wrapper.appendChild(select)
        wrapper.appendChild(customInput)
        wrapper.appendChild(addBtn)
        return wrapper
    }

    // Race select
    fun addRaceSelect(label: String, value: String?): HTMLSelectElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        form.appendChild(lbl)

        val wrapper = document.createElement("div") as HTMLDivElement
        val select = document.createElement("select") as HTMLSelectElement

        val emptyOpt = document.createElement("option") as HTMLOptionElement
        emptyOpt.value = ""
        emptyOpt.textContent = t("general.select")
        select.appendChild(emptyOpt)

        DungeonsAndDragons.defaultRaces.forEach { r ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = r
            opt.textContent = tDnd("race", r)
            select.appendChild(opt)
        }

        val customOpt = document.createElement("option") as HTMLOptionElement
        customOpt.value = "__custom__"
        customOpt.textContent = HOMEBREW_ADD_OPTION_LABEL
        select.appendChild(customOpt)

        if (value != null && value !in DungeonsAndDragons.defaultRaces) {
            val existingCustom = document.createElement("option") as HTMLOptionElement
            existingCustom.value = value
            existingCustom.textContent = "$value $IS_HOMEBREW_LABEL"
            select.insertBefore(existingCustom, customOpt)
        }

        select.value = value ?: ""

        val customInput = document.createElement("input") as HTMLInputElement
        customInput.placeholder = t("main.enterCustomRace")
        customInput.classList.add("main-tab-custom-input")

        val addBtn = document.createElement("button") as HTMLButtonElement
        addBtn.textContent = "Add"
        addBtn.classList.add("main-tab-custom-btn")

        fun rebuildSubRace(raceName: String?) {
            val srw = subRaceWrapper ?: return
            val newContent = buildSubRaceSelect(raceName, null)
            srw.innerHTML = ""
            while (newContent.firstChild != null) {
                srw.appendChild(newContent.firstChild!!)
            }
        }

        select.addEventListener("change", {
            if (select.value == "__custom__") {
                customInput.classList.add("visible")
                addBtn.classList.add("visible")
            } else {
                customInput.classList.remove("visible")
                addBtn.classList.remove("visible")
            }
            rebuildSubRace(if (select.value == "__custom__") null else select.value)
        })

        addBtn.addEventListener("click", {
            val customValue = customInput.value.trim()
            if (customValue.isNotEmpty()) {
                val newOpt = document.createElement("option") as HTMLOptionElement
                newOpt.value = customValue
                newOpt.textContent = "$customValue $IS_HOMEBREW_LABEL"
                select.insertBefore(newOpt, customOpt)
                select.value = customValue
                customInput.value = ""
                customInput.classList.remove("visible")
                addBtn.classList.remove("visible")
                rebuildSubRace(customValue)
            }
        })

        wrapper.appendChild(select)
        wrapper.appendChild(customInput)
        wrapper.appendChild(addBtn)
        form.appendChild(wrapper)
        return select
    }

    fun addSubRaceSelect(label: String, raceName: String?, value: String?): HTMLDivElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        form.appendChild(lbl)
        val wrapper = buildSubRaceSelect(raceName, value)
        form.appendChild(wrapper)
        return wrapper
    }

    val raceSelect = addRaceSelect(t("main.race"), info.race)
    subRaceWrapper = addSubRaceSelect(t("main.subrace"), info.race, info.subRace)
    val originInput = addField(t("main.origin"), info.origin)
    // Alignment select
    val alignmentLbl = document.createElement("label") as HTMLLabelElement
    alignmentLbl.textContent = t("main.alignment")
    form.appendChild(alignmentLbl)
    val alignmentSelect = document.createElement("select") as HTMLSelectElement
    val alignEmptyOpt = document.createElement("option") as HTMLOptionElement
    alignEmptyOpt.value = ""
    alignEmptyOpt.textContent = t("general.select")
    alignmentSelect.appendChild(alignEmptyOpt)
    DungeonsAndDragons.defaultAlignments.forEach { a ->
        val opt = document.createElement("option") as HTMLOptionElement
        opt.value = a
        opt.textContent = tDnd("alignment", a)
        alignmentSelect.appendChild(opt)
    }
    alignmentSelect.value = info.alignment ?: ""
    form.appendChild(alignmentSelect)

    // --- Race & Background box ---
    val raceBox = document.createElement("div") as HTMLDivElement
    raceBox.className = "main-tab-box"
    val raceBoxTitle = document.createElement("h3") as HTMLHeadingElement
    raceBoxTitle.className = "main-tab-box-title"
    raceBoxTitle.textContent = t("main.boxRaceBackground")
    raceBox.appendChild(raceBoxTitle)
    raceBox.appendChild(form)

    // --- 2/3 + 1/3 outer layout ---
    val classColumn = document.createElement("div") as HTMLDivElement
    classColumn.className = "main-tab-class-column"
    classColumn.appendChild(mainClassBox)
    classColumn.appendChild(multiclassBox)
    classColumn.appendChild(levelError)

    val outerGrid = document.createElement("div") as HTMLDivElement
    outerGrid.className = "main-tab-outer-grid"
    outerGrid.appendChild(classColumn)
    outerGrid.appendChild(raceBox)

    container.appendChild(outerGrid)

    // Auto-save status indicator
    val autoSaveIndicator = com.dungeonsanddeigo.web.dnd.components.autoSaveIndicator.AutoSaveIndicator(container)

    fun autoSave() {
        val mainLevel = mainClassLevelInput.value.toIntOrNull() ?: 0
        val secLevel = secondaryClassLevelInput.value.toIntOrNull() ?: 0
        if (mainLevel + secLevel > 20) return

        autoSaveIndicator.schedule {
            val mainSubSelect = mainSubClassWrapper?.querySelector("select") as? HTMLSelectElement
            val secSubSelect = secondarySubClassWrapper?.querySelector("select") as? HTMLSelectElement
            val updated = DndMainInfo(
                characterId = character.id,
                mainClass = mainClassSelect.value.ifBlank { null },
                mainSubClass = mainSubSelect?.value?.ifBlank { null },
                mainClassLevel = mainClassLevelInput.value.toIntOrNull(),
                secondaryClass = secondaryClassSelect.value.ifBlank { null },
                secondarySubClass = secSubSelect?.value?.ifBlank { null },
                secondaryClassLevel = secondaryClassLevelInput.value.toIntOrNull(),
                race = raceSelect.value.ifBlank { null },
                subRace = (subRaceWrapper?.querySelector("select") as? HTMLSelectElement)?.value?.ifBlank { null },
                origin = originInput.value.ifBlank { null },
                alignment = alignmentSelect.value.ifBlank { null }
            )
            Repos.mainInfo.save(updated)
        }
    }

    // Attach auto-save to all inputs
    container.addEventListener("input", { autoSave() })
    container.addEventListener("change", { autoSave() })

    // Track previous values for feature-deletion warnings
    var prevMainClass = info.mainClass ?: ""
    var prevSecondaryClass = info.secondaryClass ?: ""
    var prevRace = info.race ?: ""
    var prevOrigin = info.origin ?: ""
    var prevMainSubClass = info.mainSubClass
    var prevSecondarySubClass = info.secondarySubClass
    var prevSubRace = info.subRace
    var prevMainLevel = info.mainClassLevel ?: 1
    var prevSecondaryLevel = info.secondaryClassLevel ?: 0

    fun checkFeatureWarning(source: String, oldValue: String, newValue: String, matchFn: (DndFeature) -> Boolean, revert: () -> Unit) {
        if (oldValue == newValue || newValue.isBlank()) return
        val features = Repos.features.getByCharacterId(character.id)
        val affected = features.filter(matchFn)
        if (affected.isEmpty()) return
        val names = affected.joinToString(", ") { it.name.ifEmpty { "(Unnamed)" } }
        val confirmed = window.confirm("Changing $source will delete ${affected.size} feature(s): $names\n\nProceed?")
        if (confirmed) {
            affected.forEach { Repos.features.delete(it.id) }
        } else {
            revert()
        }
    }

    fun rebuildSubClass(wrapper: HTMLDivElement?, className: String?, currentValue: String?) {
        if (wrapper == null) return
        val newContent = buildSubClassSelect(className, currentValue)
        wrapper.innerHTML = ""
        while (newContent.firstChild != null) { wrapper.appendChild(newContent.firstChild!!) }
    }

    fun rebuildSubRaceWrapper(raceName: String?, currentValue: String?) {
        val srw = subRaceWrapper ?: return
        val newContent = buildSubRaceSelect(raceName, currentValue)
        srw.innerHTML = ""
        while (newContent.firstChild != null) { srw.appendChild(newContent.firstChild!!) }
    }

    mainClassSelect.addEventListener("change", {
        val newVal = mainClassSelect.value
        checkFeatureWarning("Main Class", prevMainClass, newVal,
            { it.source == "Class" && it.sourceClass == prevMainClass },
            {
                mainClassSelect.value = prevMainClass
                rebuildSubClass(mainSubClassWrapper, prevMainClass, prevMainSubClass)
            }
        )
        prevMainClass = mainClassSelect.value
        prevMainSubClass = (mainSubClassWrapper?.querySelector("select") as? HTMLSelectElement)?.value
    })

    secondaryClassSelect.addEventListener("change", {
        val newVal = secondaryClassSelect.value
        checkFeatureWarning("Secondary Class", prevSecondaryClass, newVal,
            { it.source == "Class" && it.sourceClass == prevSecondaryClass },
            {
                secondaryClassSelect.value = prevSecondaryClass
                rebuildSubClass(secondarySubClassWrapper, prevSecondaryClass, prevSecondarySubClass)
            }
        )
        prevSecondaryClass = secondaryClassSelect.value
        prevSecondarySubClass = (secondarySubClassWrapper?.querySelector("select") as? HTMLSelectElement)?.value
    })

    raceSelect.addEventListener("change", {
        val newVal = raceSelect.value
        if (newVal != "__custom__") {
            checkFeatureWarning("Race", prevRace, newVal,
                { it.source == "Race" },
                {
                    raceSelect.value = prevRace
                    rebuildSubRaceWrapper(prevRace, prevSubRace)
                }
            )
            prevRace = raceSelect.value
            prevSubRace = (subRaceWrapper?.querySelector("select") as? HTMLSelectElement)?.value
        }
    })

    originInput.addEventListener("change", {
        val newVal = originInput.value
        checkFeatureWarning("Origin", prevOrigin, newVal,
            { it.source == "Origin" },
            { originInput.value = prevOrigin }
        )
        prevOrigin = originInput.value
    })

    mainClassLevelInput.addEventListener("change", {
        val newLevel = mainClassLevelInput.value.toIntOrNull() ?: 1
        if (newLevel < prevMainLevel) {
            val currentClass = mainClassSelect.value
            val features = Repos.features.getByCharacterId(character.id)
            val affected = features.filter {
                it.source == "Class" && it.sourceClass == currentClass
                    && (it.sourceClassLevel ?: 0) > newLevel
            }
            if (affected.isNotEmpty()) {
                val names = affected.joinToString(", ") { it.name.ifEmpty { "(Unnamed)" } }
                val confirmed = window.confirm(
                    "Lowering ${currentClass} level to $newLevel will delete ${affected.size} feature(s) above that level: $names\n\nProceed?"
                )
                if (confirmed) {
                    affected.forEach { Repos.features.delete(it.id) }
                } else {
                    mainClassLevelInput.value = prevMainLevel.toString()
                    return@addEventListener
                }
            }
        }
        prevMainLevel = mainClassLevelInput.value.toIntOrNull() ?: 1
    })

    secondaryClassLevelInput.addEventListener("change", {
        val newLevel = secondaryClassLevelInput.value.toIntOrNull() ?: 0
        if (newLevel < prevSecondaryLevel) {
            val currentClass = secondaryClassSelect.value
            val features = Repos.features.getByCharacterId(character.id)
            val affected = features.filter {
                it.source == "Class" && it.sourceClass == currentClass
                    && (it.sourceClassLevel ?: 0) > newLevel
            }
            if (affected.isNotEmpty()) {
                val names = affected.joinToString(", ") { it.name.ifEmpty { "(Unnamed)" } }
                val confirmed = window.confirm(
                    "Lowering ${currentClass} level to $newLevel will delete ${affected.size} feature(s) above that level: $names\n\nProceed?"
                )
                if (confirmed) {
                    affected.forEach { Repos.features.delete(it.id) }
                } else {
                    secondaryClassLevelInput.value = prevSecondaryLevel.toString()
                    return@addEventListener
                }
            }
        }
        prevSecondaryLevel = secondaryClassLevelInput.value.toIntOrNull() ?: 0
    })
}

