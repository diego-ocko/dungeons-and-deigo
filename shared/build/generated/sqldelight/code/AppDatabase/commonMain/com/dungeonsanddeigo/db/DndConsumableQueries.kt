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

public class DndConsumableQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectByCharacterId(characterId: Long, mapper: (
    id: Long,
    characterId: Long,
    name: String,
    type: String,
    quantity: Long,
    effect: String,
    price: Long,
    priceCurrency: String,
    weight: Double,
    tags: String,
  ) -> T): Query<T> = SelectByCharacterIdQuery(characterId) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getString(5)!!,
      cursor.getLong(6)!!,
      cursor.getString(7)!!,
      cursor.getDouble(8)!!,
      cursor.getString(9)!!
    )
  }

  public fun selectByCharacterId(characterId: Long): Query<DndConsumable> = selectByCharacterId(characterId, ::DndConsumable)

  public fun lastInsertId(): ExecutableQuery<Long> = Query(-1_696_239_896, driver, "DndConsumable.sq", "lastInsertId", "SELECT last_insert_rowid()") { cursor ->
    cursor.getLong(0)!!
  }

  /**
   * @return The number of rows updated.
   */
  public fun insert(
    characterId: Long,
    name: String,
    type: String,
    quantity: Long,
    effect: String,
    price: Long,
    priceCurrency: String,
    weight: Double,
    tags: String,
  ): QueryResult<Long> {
    val result = driver.execute(1_628_038_583, """
        |INSERT INTO DndConsumable(characterId, name, type, quantity, effect, price, priceCurrency, weight, tags)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 9) {
          var parameterIndex = 0
          bindLong(parameterIndex++, characterId)
          bindString(parameterIndex++, name)
          bindString(parameterIndex++, type)
          bindLong(parameterIndex++, quantity)
          bindString(parameterIndex++, effect)
          bindLong(parameterIndex++, price)
          bindString(parameterIndex++, priceCurrency)
          bindDouble(parameterIndex++, weight)
          bindString(parameterIndex++, tags)
        }
    notifyQueries(1_628_038_583) { emit ->
      emit("DndConsumable")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun update(
    name: String,
    type: String,
    quantity: Long,
    effect: String,
    price: Long,
    priceCurrency: String,
    weight: Double,
    tags: String,
    id: Long,
  ): QueryResult<Long> {
    val result = driver.execute(1_972_984_775, """
        |UPDATE DndConsumable SET name = ?, type = ?, quantity = ?, effect = ?, price = ?, priceCurrency = ?, weight = ?, tags = ?
        |WHERE id = ?
        """.trimMargin(), 9) {
          var parameterIndex = 0
          bindString(parameterIndex++, name)
          bindString(parameterIndex++, type)
          bindLong(parameterIndex++, quantity)
          bindString(parameterIndex++, effect)
          bindLong(parameterIndex++, price)
          bindString(parameterIndex++, priceCurrency)
          bindDouble(parameterIndex++, weight)
          bindString(parameterIndex++, tags)
          bindLong(parameterIndex++, id)
        }
    notifyQueries(1_972_984_775) { emit ->
      emit("DndConsumable")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteById(id: Long): QueryResult<Long> {
    val result = driver.execute(-1_990_656_997, """DELETE FROM DndConsumable WHERE id = ?""", 1) {
          var parameterIndex = 0
          bindLong(parameterIndex++, id)
        }
    notifyQueries(-1_990_656_997) { emit ->
      emit("DndConsumable")
    }
    return result
  }

  private inner class SelectByCharacterIdQuery<out T : Any>(
    public val characterId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("DndConsumable", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("DndConsumable", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(1_491_471_219, """SELECT DndConsumable.id, DndConsumable.characterId, DndConsumable.name, DndConsumable.type, DndConsumable.quantity, DndConsumable.effect, DndConsumable.price, DndConsumable.priceCurrency, DndConsumable.weight, DndConsumable.tags FROM DndConsumable WHERE characterId = ?""", mapper, 1) {
      var parameterIndex = 0
      bindLong(parameterIndex++, characterId)
    }

    override fun toString(): String = "DndConsumable.sq:selectByCharacterId"
  }
}
