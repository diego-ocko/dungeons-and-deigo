package com.dungeonsanddeigo.db

import kotlin.Long
import kotlin.String

public data class DndFeatureTags(
  public val id: Long,
  public val featureId: Long,
  public val tag: String,
)
