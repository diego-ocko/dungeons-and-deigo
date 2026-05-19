package com.dungeonsanddeigo.db

import app.cash.sqldelight.ExecutableQuery
import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Double
import kotlin.Long
import kotlin.String

public class DndArmorQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectByCharacterId(characterId: Long, mapper: (
    id: Long,
    characterId: Long,
    name: String,
    type: String,
    baseAC: Long,
    acModifier: String,
    minimumStrength: Long,
    hasSneakDisadvantage: Long,
    weight: Double,
    price: Long,
    priceCurrency: String,
    isEquipped: Long,
    tags: String,
    additionalFeatures: String,
  ) -> T): Query<T> = SelectByCharacterIdQuery(characterId) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getString(5)!!,
      cursor.getLong(6)!!,
      cursor.getLong(7)!!,
      cursor.getDouble(8)!!,
      cursor.getLong(9)!!,
      cursor.getString(10)!!,
      cursor.getLong(11)!!,
      cursor.getString(12)!!,
      cursor.getString(13)!!
    )
  }

  public fun selectByCharacterId(characterId: Long): Query<DndArmor> = selectByCharacterId(characterId, ::DndArmor)

  public fun lastInsertId(): ExecutableQuery<Long> = Query(-1_013_422_902, driver, "DndArmor.sq", "lastInsertId", "SELECT last_insert_rowid()") { cursor ->
    cursor.getLong(0)!!
  }

  /**
   * @return The number of rows updated.
   */
  public fun insert(
    characterId: Long,
    name: String,
    type: String,
    baseAC: Long,
    acModifier: String,
    minimumStrength: Long,
    hasSneakDisadvantage: Long,
    weight: Double,
    price: Long,
    priceCurrency: String,
    isEquipped: Long,
    tags: String,
    additionalFeatures: String,
  ): QueryResult<Long> {
    val result = driver.execute(-249_860_839, """
        |INSERT INTO DndArmor(characterId, name, type, baseAC, acModifier, minimumStrength, hasSneakDisadvantage, weight, price, priceCurrency, isEquipped, tags, additionalFeatures)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 13) {
          var parameterIndex = 0
          bindLong(parameterIndex++, characterId)
          bindString(parameterIndex++, name)
          bindString(parameterIndex++, type)
          bindLong(parameterIndex++, baseAC)
          bindString(parameterIndex++, acModifier)
          bindLong(parameterIndex++, minimumStrength)
          bindLong(parameterIndex++, hasSneakDisadvantage)
          bindDouble(parameterIndex++, weight)
          bindLong(parameterIndex++, price)
          bindString(parameterIndex++, priceCurrency)
          bindLong(parameterIndex++, isEquipped)
          bindString(parameterIndex++, tags)
          bindString(parameterIndex++, additionalFeatures)
        }
    notifyQueries(-249_860_839) { emit ->
      emit("DndArmor")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun update(
    name: String,
    type: String,
    baseAC: Long,
    acModifier: String,
    minimumStrength: Long,
    hasSneakDisadvantage: Long,
    weight: Double,
    price: Long,
    priceCurrency: String,
    isEquipped: Long,
    tags: String,
    additionalFeatures: String,
    id: Long,
  ): QueryResult<Long> {
    val result = driver.execute(95_085_353, """
        |UPDATE DndArmor SET name = ?, type = ?, baseAC = ?, acModifier = ?, minimumStrength = ?, hasSneakDisadvantage = ?, weight = ?, price = ?, priceCurrency = ?, isEquipped = ?, tags = ?, additionalFeatures = ?
        |WHERE id = ?
        """.trimMargin(), 13) {
          var parameterIndex = 0
          bindString(parameterIndex++, name)
          bindString(parameterIndex++, type)
          bindLong(parameterIndex++, baseAC)
          bindString(parameterIndex++, acModifier)
          bindLong(parameterIndex++, minimumStrength)
          bindLong(parameterIndex++, hasSneakDisadvantage)
          bindDouble(parameterIndex++, weight)
          bindLong(parameterIndex++, price)
          bindString(parameterIndex++, priceCurrency)
          bindLong(parameterIndex++, isEquipped)
          bindString(parameterIndex++, tags)
          bindString(parameterIndex++, additionalFeatures)
          bindLong(parameterIndex++, id)
        }
    notifyQueries(95_085_353) { emit ->
      emit("DndArmor")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteById(id: Long): QueryResult<Long> {
    val result = driver.execute(481_559_165, """DELETE FROM DndArmor WHERE id = ?""", 1) {
          var parameterIndex = 0
          bindLong(parameterIndex++, id)
        }
    notifyQueries(481_559_165) { emit ->
      emit("DndArmor")
    }
    return result
  }

  private inner class SelectByCharacterIdQuery<out T : Any>(
    public val characterId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("DndArmor", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("DndArmor", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(-1_733_356_719, """SELECT DndArmor.id, DndArmor.characterId, DndArmor.name, DndArmor.type, DndArmor.baseAC, DndArmor.acModifier, DndArmor.minimumStrength, DndArmor.hasSneakDisadvantage, DndArmor.weight, DndArmor.price, DndArmor.priceCurrency, DndArmor.isEquipped, DndArmor.tags, DndArmor.additionalFeatures FROM DndArmor WHERE characterId = ?""", mapper, 1) {
      var parameterIndex = 0
      bindLong(parameterIndex++, characterId)
    }

    override fun toString(): String = "DndArmor.sq:selectByCharacterId"
  }
}
