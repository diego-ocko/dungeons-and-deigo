package com.dungeonsanddeigo.db

import kotlin.Long
import kotlin.String

public data class DndSkills(
  public val id: Long,
  public val characterId: Long,
  public val skillsJson: String,
)
