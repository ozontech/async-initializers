package ru.ozon.asyncInitializers.plugin.internal.exceptions

internal class MultiConfigException(buildType: String): Exception(
  "Several identical buildTypes - $buildType were set for modification, please merge them into one list"
)
