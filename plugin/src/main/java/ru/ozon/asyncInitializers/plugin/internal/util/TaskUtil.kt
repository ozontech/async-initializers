package ru.ozon.asyncInitializers.plugin.internal.util

import com.android.build.api.variant.Variant

internal val Variant.capitalizedName: String
    get() = name.capitalized()


private fun String.capitalized(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
