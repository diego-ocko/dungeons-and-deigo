package com.dungeonsanddeigo.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class DndArmor(
  public val id: Long,
  public val characterId: Long,
  public val name: String,
  public val type: String,
  public val baseAC: Long,
  public val acModifier: String,
  public val minimumStrength: Long,
  public val hasSneakDisadvantage: Long,
  public val weight: Double,
  public val price: Long,
  public val priceCurrency: String,
  public val isEquipped: Long,
  public val tags: String,
  public val additionalFeatures: String,
)
