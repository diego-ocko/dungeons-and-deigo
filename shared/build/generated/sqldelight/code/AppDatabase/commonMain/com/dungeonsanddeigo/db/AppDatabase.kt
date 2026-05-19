package com.dungeonsanddeigo.db

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.dungeonsanddeigo.db.shared.newInstance
import com.dungeonsanddeigo.db.shared.schema
import kotlin.Unit

public interface AppDatabase : Transacter {
  public val characterEntityQueries: CharacterEntityQueries

  public val dndArmorQueries: DndArmorQueries

  public val dndBaseStatsQueries: DndBaseStatsQueries

  public val dndConsumableQueries: DndConsumableQueries

  public val dndFeaturesQueries: DndFeaturesQueries

  public val dndInventoryItemQueries: DndInventoryItemQueries

  public val dndMagicItemQueries: DndMagicItemQueries

  public val dndMainInfoQueries: DndMainInfoQueries

  public val dndMoneyQueries: DndMoneyQueries

  public val dndSkillsQueries: DndSkillsQueries

  public val dndWeaponQueries: DndWeaponQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = AppDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): AppDatabase = AppDatabase::class.newInstance(driver)
  }
}
