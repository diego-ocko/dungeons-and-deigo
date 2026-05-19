package com.dungeonsanddeigo.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class DndConsumable(
  public val id: Long,
  public val characterId: Long,
  public val name: String,
  public val type: String,
  public val quantity: Long,
  public val effect: String,
  public val price: Long,
  public val priceCurrency: String,
  public val weight: Double,
  public val tags: String,
)
