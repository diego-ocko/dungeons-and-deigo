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

public class DndMagicItemQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectByCharacterId(characterId: Long, mapper: (
    id: Long,
    characterId: Long,
    name: String,
    needSynch: Long,
    isSynched: Long,
    effect: String,
    weight: Double,
    price: Long,
    priceCurrency: String,
    tags: String,
  ) -> T): Query<T> = SelectByCharacterIdQuery(characterId) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getLong(4)!!,
      cursor.getString(5)!!,
      cursor.getDouble(6)!!,
      cursor.getLong(7)!!,
      cursor.getString(8)!!,
      cursor.getString(9)!!
    )
  }

  public fun selectByCharacterId(characterId: Long): Query<DndMagicItem> = selectByCharacterId(characterId, ::DndMagicItem)

  public fun lastInsertId(): ExecutableQuery<Long> = Query(660_580_971, driver, "DndMagicItem.sq", "lastInsertId", "SELECT last_insert_rowid()") { cursor ->
    cursor.getLong(0)!!
  }

  /**
   * @return The number of rows updated.
   */
  public fun insert(
    characterId: Long,
    name: String,
    needSynch: Long,
    isSynched: Long,
    effect: String,
    weight: Double,
    price: Long,
    priceCurrency: String,
    tags: String,
  ): QueryResult<Long> {
    val result = driver.execute(-156_516_486, """
        |INSERT INTO DndMagicItem(characterId, name, needSynch, isSynched, effect, weight, price, priceCurrency, tags)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 9) {
          var parameterIndex = 0
          bindLong(parameterIndex++, characterId)
          bindString(parameterIndex++, name)
          bindLong(parameterIndex++, needSynch)
          bindLong(parameterIndex++, isSynched)
          bindString(parameterIndex++, effect)
          bindDouble(parameterIndex++, weight)
          bindLong(parameterIndex++, price)
          bindString(parameterIndex++, priceCurrency)
          bindString(parameterIndex++, tags)
        }
    notifyQueries(-156_516_486) { emit ->
      emit("DndMagicItem")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun update(
    name: String,
    needSynch: Long,
    isSynched: Long,
    effect: String,
    weight: Double,
    price: Long,
    priceCurrency: String,
    tags: String,
    id: Long,
  ): QueryResult<Long> {
    val result = driver.execute(188_429_706, """
        |UPDATE DndMagicItem SET name = ?, needSynch = ?, isSynched = ?, effect = ?, weight = ?, price = ?, priceCurrency = ?, tags = ?
        |WHERE id = ?
        """.trimMargin(), 9) {
          var parameterIndex = 0
          bindString(parameterIndex++, name)
          bindLong(parameterIndex++, needSynch)
          bindLong(parameterIndex++, isSynched)
          bindString(parameterIndex++, effect)
          bindDouble(parameterIndex++, weight)
          bindLong(parameterIndex++, price)
          bindString(parameterIndex++, priceCurrency)
          bindString(parameterIndex++, tags)
          bindLong(parameterIndex++, id)
        }
    notifyQueries(188_429_706) { emit ->
      emit("DndMagicItem")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteById(id: Long): QueryResult<Long> {
    val result = driver.execute(1_663_188_062, """DELETE FROM DndMagicItem WHERE id = ?""", 1) {
          var parameterIndex = 0
          bindLong(parameterIndex++, id)
        }
    notifyQueries(1_663_188_062) { emit ->
      emit("DndMagicItem")
    }
    return result
  }

  private inner class SelectByCharacterIdQuery<out T : Any>(
    public val characterId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("DndMagicItem", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("DndMagicItem", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(-1_128_438_128, """SELECT DndMagicItem.id, DndMagicItem.characterId, DndMagicItem.name, DndMagicItem.needSynch, DndMagicItem.isSynched, DndMagicItem.effect, DndMagicItem.weight, DndMagicItem.price, DndMagicItem.priceCurrency, DndMagicItem.tags FROM DndMagicItem WHERE characterId = ?""", mapper, 1) {
      var parameterIndex = 0
      bindLong(parameterIndex++, characterId)
    }

    override fun toString(): String = "DndMagicItem.sq:selectByCharacterId"
  }
}
