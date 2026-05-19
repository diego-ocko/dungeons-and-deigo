package com.dungeonsanddeigo.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class DndBaseStatsQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectByCharacterId(characterId: Long, mapper: (
    id: Long,
    characterId: Long,
    strValue: Long?,
    strMod: Long?,
    dexValue: Long?,
    dexMod: Long?,
    conValue: Long?,
    conMod: Long?,
    intValue: Long?,
    intMod: Long?,
    wisValue: Long?,
    wisMod: Long?,
    chaValue: Long?,
    chaMod: Long?,
    proficiency: Long?,
    maxLife: Long?,
    vision: Long?,
    hasDarkVision: Long,
    speed: Long?,
    hasStrRes: Long,
    hasDexRes: Long,
    hasConRes: Long,
    hasIntRes: Long,
    hasWisRes: Long,
    hasChaRes: Long,
    emptyArmorClass: Long?,
  ) -> T): Query<T> = SelectByCharacterIdQuery(characterId) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2),
      cursor.getLong(3),
      cursor.getLong(4),
      cursor.getLong(5),
      cursor.getLong(6),
      cursor.getLong(7),
      cursor.getLong(8),
      cursor.getLong(9),
      cursor.getLong(10),
      cursor.getLong(11),
      cursor.getLong(12),
      cursor.getLong(13),
      cursor.getLong(14),
      cursor.getLong(15),
      cursor.getLong(16),
      cursor.getLong(17)!!,
      cursor.getLong(18),
      cursor.getLong(19)!!,
      cursor.getLong(20)!!,
      cursor.getLong(21)!!,
      cursor.getLong(22)!!,
      cursor.getLong(23)!!,
      cursor.getLong(24)!!,
      cursor.getLong(25)
    )
  }

  public fun selectByCharacterId(characterId: Long): Query<DndBaseStats> = selectByCharacterId(characterId, ::DndBaseStats)

  /**
   * @return The number of rows updated.
   */
  public fun insert(
    characterId: Long,
    strValue: Long?,
    strMod: Long?,
    dexValue: Long?,
    dexMod: Long?,
    conValue: Long?,
    conMod: Long?,
    intValue: Long?,
    intMod: Long?,
    wisValue: Long?,
    wisMod: Long?,
    chaValue: Long?,
    chaMod: Long?,
    proficiency: Long?,
    maxLife: Long?,
    vision: Long?,
    hasDarkVision: Long,
    speed: Long?,
    hasStrRes: Long,
    hasDexRes: Long,
    hasConRes: Long,
    hasIntRes: Long,
    hasWisRes: Long,
    hasChaRes: Long,
    emptyArmorClass: Long?,
  ): QueryResult<Long> {
    val result = driver.execute(-241_296_408, """
        |INSERT INTO DndBaseStats(characterId, strValue, strMod, dexValue, dexMod, conValue, conMod, intValue, intMod, wisValue, wisMod, chaValue, chaMod, proficiency, maxLife, vision, hasDarkVision, speed, hasStrRes, hasDexRes, hasConRes, hasIntRes, hasWisRes, hasChaRes, emptyArmorClass)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 25) {
          var parameterIndex = 0
          bindLong(parameterIndex++, characterId)
          bindLong(parameterIndex++, strValue)
          bindLong(parameterIndex++, strMod)
          bindLong(parameterIndex++, dexValue)
          bindLong(parameterIndex++, dexMod)
          bindLong(parameterIndex++, conValue)
          bindLong(parameterIndex++, conMod)
          bindLong(parameterIndex++, intValue)
          bindLong(parameterIndex++, intMod)
          bindLong(parameterIndex++, wisValue)
          bindLong(parameterIndex++, wisMod)
          bindLong(parameterIndex++, chaValue)
          bindLong(parameterIndex++, chaMod)
          bindLong(parameterIndex++, proficiency)
          bindLong(parameterIndex++, maxLife)
          bindLong(parameterIndex++, vision)
          bindLong(parameterIndex++, hasDarkVision)
          bindLong(parameterIndex++, speed)
          bindLong(parameterIndex++, hasStrRes)
          bindLong(parameterIndex++, hasDexRes)
          bindLong(parameterIndex++, hasConRes)
          bindLong(parameterIndex++, hasIntRes)
          bindLong(parameterIndex++, hasWisRes)
          bindLong(parameterIndex++, hasChaRes)
          bindLong(parameterIndex++, emptyArmorClass)
        }
    notifyQueries(-241_296_408) { emit ->
      emit("DndBaseStats")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun update(
    strValue: Long?,
    strMod: Long?,
    dexValue: Long?,
    dexMod: Long?,
    conValue: Long?,
    conMod: Long?,
    intValue: Long?,
    intMod: Long?,
    wisValue: Long?,
    wisMod: Long?,
    chaValue: Long?,
    chaMod: Long?,
    proficiency: Long?,
    maxLife: Long?,
    vision: Long?,
    hasDarkVision: Long,
    speed: Long?,
    hasStrRes: Long,
    hasDexRes: Long,
    hasConRes: Long,
    hasIntRes: Long,
    hasWisRes: Long,
    hasChaRes: Long,
    emptyArmorClass: Long?,
    characterId: Long,
  ): QueryResult<Long> {
    val result = driver.execute(103_649_784, """
        |UPDATE DndBaseStats SET strValue = ?, strMod = ?, dexValue = ?, dexMod = ?, conValue = ?, conMod = ?, intValue = ?, intMod = ?, wisValue = ?, wisMod = ?, chaValue = ?, chaMod = ?, proficiency = ?, maxLife = ?, vision = ?, hasDarkVision = ?, speed = ?, hasStrRes = ?, hasDexRes = ?, hasConRes = ?, hasIntRes = ?, hasWisRes = ?, hasChaRes = ?, emptyArmorClass = ?
        |WHERE characterId = ?
        """.trimMargin(), 25) {
          var parameterIndex = 0
          bindLong(parameterIndex++, strValue)
          bindLong(parameterIndex++, strMod)
          bindLong(parameterIndex++, dexValue)
          bindLong(parameterIndex++, dexMod)
          bindLong(parameterIndex++, conValue)
          bindLong(parameterIndex++, conMod)
          bindLong(parameterIndex++, intValue)
          bindLong(parameterIndex++, intMod)
          bindLong(parameterIndex++, wisValue)
          bindLong(parameterIndex++, wisMod)
          bindLong(parameterIndex++, chaValue)
          bindLong(parameterIndex++, chaMod)
          bindLong(parameterIndex++, proficiency)
          bindLong(parameterIndex++, maxLife)
          bindLong(parameterIndex++, vision)
          bindLong(parameterIndex++, hasDarkVision)
          bindLong(parameterIndex++, speed)
          bindLong(parameterIndex++, hasStrRes)
          bindLong(parameterIndex++, hasDexRes)
          bindLong(parameterIndex++, hasConRes)
          bindLong(parameterIndex++, hasIntRes)
          bindLong(parameterIndex++, hasWisRes)
          bindLong(parameterIndex++, hasChaRes)
          bindLong(parameterIndex++, emptyArmorClass)
          bindLong(parameterIndex++, characterId)
        }
    notifyQueries(103_649_784) { emit ->
      emit("DndBaseStats")
    }
    return result
  }

  private inner class SelectByCharacterIdQuery<out T : Any>(
    public val characterId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("DndBaseStats", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("DndBaseStats", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(1_556_950_242, """SELECT DndBaseStats.id, DndBaseStats.characterId, DndBaseStats.strValue, DndBaseStats.strMod, DndBaseStats.dexValue, DndBaseStats.dexMod, DndBaseStats.conValue, DndBaseStats.conMod, DndBaseStats.intValue, DndBaseStats.intMod, DndBaseStats.wisValue, DndBaseStats.wisMod, DndBaseStats.chaValue, DndBaseStats.chaMod, DndBaseStats.proficiency, DndBaseStats.maxLife, DndBaseStats.vision, DndBaseStats.hasDarkVision, DndBaseStats.speed, DndBaseStats.hasStrRes, DndBaseStats.hasDexRes, DndBaseStats.hasConRes, DndBaseStats.hasIntRes, DndBaseStats.hasWisRes, DndBaseStats.hasChaRes, DndBaseStats.emptyArmorClass FROM DndBaseStats WHERE characterId = ?""", mapper, 1) {
      var parameterIndex = 0
      bindLong(parameterIndex++, characterId)
    }

    override fun toString(): String = "DndBaseStats.sq:selectByCharacterId"
  }
}
