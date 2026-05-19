package com.dungeonsanddeigo.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class DndMoneyQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectByCharacterId(characterId: Long, mapper: (
    id: Long,
    characterId: Long,
    copper: Long,
    silver: Long,
    electrum: Long,
    gold: Long,
    platinum: Long,
    lifestyle: String,
  ) -> T): Query<T> = SelectByCharacterIdQuery(characterId) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getLong(3)!!,
      cursor.getLong(4)!!,
      cursor.getLong(5)!!,
      cursor.getLong(6)!!,
      cursor.getString(7)!!
    )
  }

  public fun selectByCharacterId(characterId: Long): Query<DndMoney> = selectByCharacterId(characterId, ::DndMoney)

  /**
   * @return The number of rows updated.
   */
  public fun insert(
    characterId: Long,
    copper: Long,
    silver: Long,
    electrum: Long,
    gold: Long,
    platinum: Long,
    lifestyle: String,
  ): QueryResult<Long> {
    val result = driver.execute(899_579_738, """INSERT INTO DndMoney(characterId, copper, silver, electrum, gold, platinum, lifestyle) VALUES (?, ?, ?, ?, ?, ?, ?)""", 7) {
          var parameterIndex = 0
          bindLong(parameterIndex++, characterId)
          bindLong(parameterIndex++, copper)
          bindLong(parameterIndex++, silver)
          bindLong(parameterIndex++, electrum)
          bindLong(parameterIndex++, gold)
          bindLong(parameterIndex++, platinum)
          bindString(parameterIndex++, lifestyle)
        }
    notifyQueries(899_579_738) { emit ->
      emit("DndMoney")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun update(
    copper: Long,
    silver: Long,
    electrum: Long,
    gold: Long,
    platinum: Long,
    lifestyle: String,
    characterId: Long,
  ): QueryResult<Long> {
    val result = driver.execute(1_244_525_930, """UPDATE DndMoney SET copper = ?, silver = ?, electrum = ?, gold = ?, platinum = ?, lifestyle = ? WHERE characterId = ?""", 7) {
          var parameterIndex = 0
          bindLong(parameterIndex++, copper)
          bindLong(parameterIndex++, silver)
          bindLong(parameterIndex++, electrum)
          bindLong(parameterIndex++, gold)
          bindLong(parameterIndex++, platinum)
          bindString(parameterIndex++, lifestyle)
          bindLong(parameterIndex++, characterId)
        }
    notifyQueries(1_244_525_930) { emit ->
      emit("DndMoney")
    }
    return result
  }

  private inner class SelectByCharacterIdQuery<out T : Any>(
    public val characterId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("DndMoney", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("DndMoney", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(-225_873_232, """SELECT DndMoney.id, DndMoney.characterId, DndMoney.copper, DndMoney.silver, DndMoney.electrum, DndMoney.gold, DndMoney.platinum, DndMoney.lifestyle FROM DndMoney WHERE characterId = ?""", mapper, 1) {
      var parameterIndex = 0
      bindLong(parameterIndex++, characterId)
    }

    override fun toString(): String = "DndMoney.sq:selectByCharacterId"
  }
}
