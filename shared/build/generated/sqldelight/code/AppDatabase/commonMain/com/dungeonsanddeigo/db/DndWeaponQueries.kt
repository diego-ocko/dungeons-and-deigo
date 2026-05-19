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

public class DndWeaponQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectByCharacterId(characterId: Long, mapper: (
    id: Long,
    characterId: Long,
    name: String,
    category: String,
    weaponType: String,
    damageDice: String,
    damageType: String,
    ammunition: Long,
    finesse: Long,
    heavy: Long,
    light: Long,
    loading: Long,
    range: Long,
    rangeDistance: Long,
    rangeLongDistance: Long,
    reach: Long,
    special: Long,
    specialDescription: String,
    thrown: Long,
    twoHanded: Long,
    versatile: Long,
    versatileDice: String,
    silver: Long,
    additionalFeatures: String,
    weight: Double,
    price: Long,
    priceCurrency: String,
    tags: String,
    isEquipped: Long,
  ) -> T): Query<T> = SelectByCharacterIdQuery(characterId) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getLong(11)!!,
      cursor.getLong(12)!!,
      cursor.getLong(13)!!,
      cursor.getLong(14)!!,
      cursor.getLong(15)!!,
      cursor.getLong(16)!!,
      cursor.getString(17)!!,
      cursor.getLong(18)!!,
      cursor.getLong(19)!!,
      cursor.getLong(20)!!,
      cursor.getString(21)!!,
      cursor.getLong(22)!!,
      cursor.getString(23)!!,
      cursor.getDouble(24)!!,
      cursor.getLong(25)!!,
      cursor.getString(26)!!,
      cursor.getString(27)!!,
      cursor.getLong(28)!!
    )
  }

  public fun selectByCharacterId(characterId: Long): Query<DndWeapon> = selectByCharacterId(characterId, ::DndWeapon)

  public fun lastInsertId(): ExecutableQuery<Long> = Query(492_666_657, driver, "DndWeapon.sq", "lastInsertId", "SELECT last_insert_rowid()") { cursor ->
    cursor.getLong(0)!!
  }

  /**
   * @return The number of rows updated.
   */
  public fun insert(
    characterId: Long,
    name: String,
    category: String,
    weaponType: String,
    damageDice: String,
    damageType: String,
    ammunition: Long,
    finesse: Long,
    heavy: Long,
    light: Long,
    loading: Long,
    range: Long,
    rangeDistance: Long,
    rangeLongDistance: Long,
    reach: Long,
    special: Long,
    specialDescription: String,
    thrown: Long,
    twoHanded: Long,
    versatile: Long,
    versatileDice: String,
    silver: Long,
    additionalFeatures: String,
    weight: Double,
    price: Long,
    priceCurrency: String,
    tags: String,
    isEquipped: Long,
  ): QueryResult<Long> {
    val result = driver.execute(1_643_959_472, """
        |INSERT INTO DndWeapon(characterId, name, category, weaponType, damageDice, damageType, ammunition, finesse, heavy, light, loading, range, rangeDistance, rangeLongDistance, reach, special, specialDescription, thrown, twoHanded, versatile, versatileDice, silver, additionalFeatures, weight, price, priceCurrency, tags, isEquipped)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 28) {
          var parameterIndex = 0
          bindLong(parameterIndex++, characterId)
          bindString(parameterIndex++, name)
          bindString(parameterIndex++, category)
          bindString(parameterIndex++, weaponType)
          bindString(parameterIndex++, damageDice)
          bindString(parameterIndex++, damageType)
          bindLong(parameterIndex++, ammunition)
          bindLong(parameterIndex++, finesse)
          bindLong(parameterIndex++, heavy)
          bindLong(parameterIndex++, light)
          bindLong(parameterIndex++, loading)
          bindLong(parameterIndex++, range)
          bindLong(parameterIndex++, rangeDistance)
          bindLong(parameterIndex++, rangeLongDistance)
          bindLong(parameterIndex++, reach)
          bindLong(parameterIndex++, special)
          bindString(parameterIndex++, specialDescription)
          bindLong(parameterIndex++, thrown)
          bindLong(parameterIndex++, twoHanded)
          bindLong(parameterIndex++, versatile)
          bindString(parameterIndex++, versatileDice)
          bindLong(parameterIndex++, silver)
          bindString(parameterIndex++, additionalFeatures)
          bindDouble(parameterIndex++, weight)
          bindLong(parameterIndex++, price)
          bindString(parameterIndex++, priceCurrency)
          bindString(parameterIndex++, tags)
          bindLong(parameterIndex++, isEquipped)
        }
    notifyQueries(1_643_959_472) { emit ->
      emit("DndWeapon")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun update(
    name: String,
    category: String,
    weaponType: String,
    damageDice: String,
    damageType: String,
    ammunition: Long,
    finesse: Long,
    heavy: Long,
    light: Long,
    loading: Long,
    range: Long,
    rangeDistance: Long,
    rangeLongDistance: Long,
    reach: Long,
    special: Long,
    specialDescription: String,
    thrown: Long,
    twoHanded: Long,
    versatile: Long,
    versatileDice: String,
    silver: Long,
    additionalFeatures: String,
    weight: Double,
    price: Long,
    priceCurrency: String,
    tags: String,
    isEquipped: Long,
    id: Long,
  ): QueryResult<Long> {
    val result = driver.execute(1_988_905_664, """
        |UPDATE DndWeapon SET name = ?, category = ?, weaponType = ?, damageDice = ?, damageType = ?, ammunition = ?, finesse = ?, heavy = ?, light = ?, loading = ?, range = ?, rangeDistance = ?, rangeLongDistance = ?, reach = ?, special = ?, specialDescription = ?, thrown = ?, twoHanded = ?, versatile = ?, versatileDice = ?, silver = ?, additionalFeatures = ?, weight = ?, price = ?, priceCurrency = ?, tags = ?, isEquipped = ?
        |WHERE id = ?
        """.trimMargin(), 28) {
          var parameterIndex = 0
          bindString(parameterIndex++, name)
          bindString(parameterIndex++, category)
          bindString(parameterIndex++, weaponType)
          bindString(parameterIndex++, damageDice)
          bindString(parameterIndex++, damageType)
          bindLong(parameterIndex++, ammunition)
          bindLong(parameterIndex++, finesse)
          bindLong(parameterIndex++, heavy)
          bindLong(parameterIndex++, light)
          bindLong(parameterIndex++, loading)
          bindLong(parameterIndex++, range)
          bindLong(parameterIndex++, rangeDistance)
          bindLong(parameterIndex++, rangeLongDistance)
          bindLong(parameterIndex++, reach)
          bindLong(parameterIndex++, special)
          bindString(parameterIndex++, specialDescription)
          bindLong(parameterIndex++, thrown)
          bindLong(parameterIndex++, twoHanded)
          bindLong(parameterIndex++, versatile)
          bindString(parameterIndex++, versatileDice)
          bindLong(parameterIndex++, silver)
          bindString(parameterIndex++, additionalFeatures)
          bindDouble(parameterIndex++, weight)
          bindLong(parameterIndex++, price)
          bindString(parameterIndex++, priceCurrency)
          bindString(parameterIndex++, tags)
          bindLong(parameterIndex++, isEquipped)
          bindLong(parameterIndex++, id)
        }
    notifyQueries(1_988_905_664) { emit ->
      emit("DndWeapon")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteById(id: Long): QueryResult<Long> {
    val result = driver.execute(-388_381_036, """DELETE FROM DndWeapon WHERE id = ?""", 1) {
          var parameterIndex = 0
          bindLong(parameterIndex++, id)
        }
    notifyQueries(-388_381_036) { emit ->
      emit("DndWeapon")
    }
    return result
  }

  private inner class SelectByCharacterIdQuery<out T : Any>(
    public val characterId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("DndWeapon", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("DndWeapon", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(-748_750_566, """SELECT DndWeapon.id, DndWeapon.characterId, DndWeapon.name, DndWeapon.category, DndWeapon.weaponType, DndWeapon.damageDice, DndWeapon.damageType, DndWeapon.ammunition, DndWeapon.finesse, DndWeapon.heavy, DndWeapon.light, DndWeapon.loading, DndWeapon.range, DndWeapon.rangeDistance, DndWeapon.rangeLongDistance, DndWeapon.reach, DndWeapon.special, DndWeapon.specialDescription, DndWeapon.thrown, DndWeapon.twoHanded, DndWeapon.versatile, DndWeapon.versatileDice, DndWeapon.silver, DndWeapon.additionalFeatures, DndWeapon.weight, DndWeapon.price, DndWeapon.priceCurrency, DndWeapon.tags, DndWeapon.isEquipped FROM DndWeapon WHERE characterId = ?""", mapper, 1) {
      var parameterIndex = 0
      bindLong(parameterIndex++, characterId)
    }

    override fun toString(): String = "DndWeapon.sq:selectByCharacterId"
  }
}
