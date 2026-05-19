package com.dungeonsanddeigo.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class DndWeapon(
  public val id: Long,
  public val characterId: Long,
  public val name: String,
  public val category: String,
  public val weaponType: String,
  public val damageDice: String,
  public val damageType: String,
  public val ammunition: Long,
  public val finesse: Long,
  public val heavy: Long,
  public val light: Long,
  public val loading: Long,
  public val range: Long,
  public val rangeDistance: Long,
  public val rangeLongDistance: Long,
  public val reach: Long,
  public val special: Long,
  public val specialDescription: String,
  public val thrown: Long,
  public val twoHanded: Long,
  public val versatile: Long,
  public val versatileDice: String,
  public val silver: Long,
  public val additionalFeatures: String,
  public val weight: Double,
  public val price: Long,
  public val priceCurrency: String,
  public val tags: String,
  public val isEquipped: Long,
)
