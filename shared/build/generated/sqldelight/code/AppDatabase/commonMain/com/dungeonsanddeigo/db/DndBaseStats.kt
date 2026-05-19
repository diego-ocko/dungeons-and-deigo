package com.dungeonsanddeigo.db

import kotlin.Long

public data class DndBaseStats(
  public val id: Long,
  public val characterId: Long,
  public val strValue: Long?,
  public val strMod: Long?,
  public val dexValue: Long?,
  public val dexMod: Long?,
  public val conValue: Long?,
  public val conMod: Long?,
  public val intValue: Long?,
  public val intMod: Long?,
  public val wisValue: Long?,
  public val wisMod: Long?,
  public val chaValue: Long?,
  public val chaMod: Long?,
  public val proficiency: Long?,
  public val maxLife: Long?,
  public val vision: Long?,
  public val hasDarkVision: Long,
  public val speed: Long?,
  public val hasStrRes: Long,
  public val hasDexRes: Long,
  public val hasConRes: Long,
  public val hasIntRes: Long,
  public val hasWisRes: Long,
  public val hasChaRes: Long,
  public val emptyArmorClass: Long?,
)
