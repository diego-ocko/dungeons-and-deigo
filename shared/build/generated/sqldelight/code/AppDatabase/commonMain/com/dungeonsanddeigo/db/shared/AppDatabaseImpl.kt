package com.dungeonsanddeigo.db.shared

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.dungeonsanddeigo.db.AppDatabase
import com.dungeonsanddeigo.db.CharacterEntityQueries
import com.dungeonsanddeigo.db.DndArmorQueries
import com.dungeonsanddeigo.db.DndBaseStatsQueries
import com.dungeonsanddeigo.db.DndConsumableQueries
import com.dungeonsanddeigo.db.DndFeaturesQueries
import com.dungeonsanddeigo.db.DndInventoryItemQueries
import com.dungeonsanddeigo.db.DndMagicItemQueries
import com.dungeonsanddeigo.db.DndMainInfoQueries
import com.dungeonsanddeigo.db.DndMoneyQueries
import com.dungeonsanddeigo.db.DndSkillsQueries
import com.dungeonsanddeigo.db.DndWeaponQueries
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<AppDatabase>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = AppDatabaseImpl.Schema

internal fun KClass<AppDatabase>.newInstance(driver: SqlDriver): AppDatabase = AppDatabaseImpl(driver)

private class AppDatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver),
    AppDatabase {
  override val characterEntityQueries: CharacterEntityQueries = CharacterEntityQueries(driver)

  override val dndArmorQueries: DndArmorQueries = DndArmorQueries(driver)

  override val dndBaseStatsQueries: DndBaseStatsQueries = DndBaseStatsQueries(driver)

  override val dndConsumableQueries: DndConsumableQueries = DndConsumableQueries(driver)

  override val dndFeaturesQueries: DndFeaturesQueries = DndFeaturesQueries(driver)

  override val dndInventoryItemQueries: DndInventoryItemQueries = DndInventoryItemQueries(driver)

  override val dndMagicItemQueries: DndMagicItemQueries = DndMagicItemQueries(driver)

  override val dndMainInfoQueries: DndMainInfoQueries = DndMainInfoQueries(driver)

  override val dndMoneyQueries: DndMoneyQueries = DndMoneyQueries(driver)

  override val dndSkillsQueries: DndSkillsQueries = DndSkillsQueries(driver)

  override val dndWeaponQueries: DndWeaponQueries = DndWeaponQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE CharacterEntity (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    name TEXT NOT NULL,
          |    sheetModelName TEXT NOT NULL,
          |    imageBase64 TEXT
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE DndArmor (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    characterId INTEGER NOT NULL,
          |    name TEXT NOT NULL,
          |    type TEXT NOT NULL,
          |    baseAC INTEGER NOT NULL DEFAULT 10,
          |    acModifier TEXT NOT NULL DEFAULT 'none',
          |    minimumStrength INTEGER NOT NULL DEFAULT 0,
          |    hasSneakDisadvantage INTEGER NOT NULL DEFAULT 0,
          |    weight REAL NOT NULL DEFAULT 0.0,
          |    price INTEGER NOT NULL DEFAULT 0,
          |    priceCurrency TEXT NOT NULL DEFAULT 'pg',
          |    isEquipped INTEGER NOT NULL DEFAULT 0,
          |    tags TEXT NOT NULL DEFAULT '',
          |    additionalFeatures TEXT NOT NULL DEFAULT '',
          |    FOREIGN KEY (characterId) REFERENCES CharacterEntity(id)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE DndBaseStats (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    characterId INTEGER NOT NULL,
          |    strValue INTEGER,
          |    strMod INTEGER,
          |    dexValue INTEGER,
          |    dexMod INTEGER,
          |    conValue INTEGER,
          |    conMod INTEGER,
          |    intValue INTEGER,
          |    intMod INTEGER,
          |    wisValue INTEGER,
          |    wisMod INTEGER,
          |    chaValue INTEGER,
          |    chaMod INTEGER,
          |    proficiency INTEGER,
          |    maxLife INTEGER,
          |    vision INTEGER,
          |    hasDarkVision INTEGER NOT NULL DEFAULT 0,
          |    speed INTEGER,
          |    hasStrRes INTEGER NOT NULL DEFAULT 0,
          |    hasDexRes INTEGER NOT NULL DEFAULT 0,
          |    hasConRes INTEGER NOT NULL DEFAULT 0,
          |    hasIntRes INTEGER NOT NULL DEFAULT 0,
          |    hasWisRes INTEGER NOT NULL DEFAULT 0,
          |    hasChaRes INTEGER NOT NULL DEFAULT 0,
          |    emptyArmorClass INTEGER,
          |    FOREIGN KEY (characterId) REFERENCES CharacterEntity(id)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE DndConsumable (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    characterId INTEGER NOT NULL,
          |    name TEXT NOT NULL,
          |    type TEXT NOT NULL,
          |    quantity INTEGER NOT NULL DEFAULT 1,
          |    effect TEXT NOT NULL DEFAULT '',
          |    price INTEGER NOT NULL DEFAULT 0,
          |    priceCurrency TEXT NOT NULL DEFAULT 'pg',
          |    weight REAL NOT NULL DEFAULT 0.0,
          |    tags TEXT NOT NULL DEFAULT '',
          |    FOREIGN KEY (characterId) REFERENCES CharacterEntity(id)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE DndFeatures (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    characterId INTEGER NOT NULL,
          |    name TEXT NOT NULL DEFAULT '',
          |    source TEXT NOT NULL,
          |    sourceClass TEXT,
          |    sourceClassLevel INTEGER,
          |    sourceOrigin TEXT,
          |    sourceRace TEXT,
          |    sourceSubRace TEXT,
          |    sourceCustom TEXT,
          |    type TEXT NOT NULL,
          |    description TEXT NOT NULL DEFAULT '',
          |    maxQuantity INTEGER,
          |    reloadRule TEXT,
          |    currentUsages INTEGER NOT NULL DEFAULT 0,
          |    FOREIGN KEY (characterId) REFERENCES CharacterEntity(id)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE DndFeatureTags (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    featureId INTEGER NOT NULL,
          |    tag TEXT NOT NULL,
          |    FOREIGN KEY (featureId) REFERENCES DndFeatures(id)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE DndInventoryItem (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    characterId INTEGER NOT NULL,
          |    category TEXT NOT NULL,
          |    name TEXT NOT NULL,
          |    description TEXT NOT NULL DEFAULT '',
          |    quantity INTEGER NOT NULL DEFAULT 1,
          |    equipped INTEGER NOT NULL DEFAULT 0,
          |    FOREIGN KEY (characterId) REFERENCES CharacterEntity(id)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE DndMagicItem (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    characterId INTEGER NOT NULL,
          |    name TEXT NOT NULL,
          |    needSynch INTEGER NOT NULL DEFAULT 0,
          |    isSynched INTEGER NOT NULL DEFAULT 0,
          |    effect TEXT NOT NULL DEFAULT '',
          |    weight REAL NOT NULL DEFAULT 0.0,
          |    price INTEGER NOT NULL DEFAULT 0,
          |    priceCurrency TEXT NOT NULL DEFAULT 'pg',
          |    tags TEXT NOT NULL DEFAULT '',
          |    FOREIGN KEY (characterId) REFERENCES CharacterEntity(id)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE DndMainInfo (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    characterId INTEGER NOT NULL,
          |    mainClass TEXT,
          |    mainSubClass TEXT,
          |    mainClassLevel INTEGER,
          |    secondaryClass TEXT,
          |    secondarySubClass TEXT,
          |    secondaryClassLevel INTEGER,
          |    race TEXT,
          |    subRace TEXT,
          |    origin TEXT,
          |    alignment TEXT,
          |    FOREIGN KEY (characterId) REFERENCES CharacterEntity(id)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE DndMoney (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    characterId INTEGER NOT NULL,
          |    copper INTEGER NOT NULL DEFAULT 0,
          |    silver INTEGER NOT NULL DEFAULT 0,
          |    electrum INTEGER NOT NULL DEFAULT 0,
          |    gold INTEGER NOT NULL DEFAULT 0,
          |    platinum INTEGER NOT NULL DEFAULT 0,
          |    lifestyle TEXT NOT NULL DEFAULT '',
          |    FOREIGN KEY (characterId) REFERENCES CharacterEntity(id)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE DndSkills (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    characterId INTEGER NOT NULL,
          |    skillsJson TEXT NOT NULL,
          |    FOREIGN KEY (characterId) REFERENCES CharacterEntity(id)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE DndWeapon (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    characterId INTEGER NOT NULL,
          |    name TEXT NOT NULL,
          |    category TEXT NOT NULL,
          |    weaponType TEXT NOT NULL,
          |    damageDice TEXT NOT NULL DEFAULT '',
          |    damageType TEXT NOT NULL DEFAULT '',
          |    ammunition INTEGER NOT NULL DEFAULT 0,
          |    finesse INTEGER NOT NULL DEFAULT 0,
          |    heavy INTEGER NOT NULL DEFAULT 0,
          |    light INTEGER NOT NULL DEFAULT 0,
          |    loading INTEGER NOT NULL DEFAULT 0,
          |    range INTEGER NOT NULL DEFAULT 0,
          |    rangeDistance INTEGER NOT NULL DEFAULT 0,
          |    rangeLongDistance INTEGER NOT NULL DEFAULT 0,
          |    reach INTEGER NOT NULL DEFAULT 0,
          |    special INTEGER NOT NULL DEFAULT 0,
          |    specialDescription TEXT NOT NULL DEFAULT '',
          |    thrown INTEGER NOT NULL DEFAULT 0,
          |    twoHanded INTEGER NOT NULL DEFAULT 0,
          |    versatile INTEGER NOT NULL DEFAULT 0,
          |    versatileDice TEXT NOT NULL DEFAULT '',
          |    silver INTEGER NOT NULL DEFAULT 0,
          |    additionalFeatures TEXT NOT NULL DEFAULT '',
          |    weight REAL NOT NULL DEFAULT 0.0,
          |    price INTEGER NOT NULL DEFAULT 0,
          |    priceCurrency TEXT NOT NULL DEFAULT 'pg',
          |    tags TEXT NOT NULL DEFAULT '',
          |    isEquipped INTEGER NOT NULL DEFAULT 0,
          |    FOREIGN KEY (characterId) REFERENCES CharacterEntity(id)
          |)
          """.trimMargin(), 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
