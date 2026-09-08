package ru.ozon.asyncInitializers.plugin.internal.exceptions

internal class MultiConfigException(buildType: String): Exception(
  "Задано несколько одинаковых buildType - $buildType для модификации, пожалуйства смержите их в один список"
)
