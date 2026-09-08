package ru.ozon.asyncInitializers.plugin.internal.asm

import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

internal interface InjectInstrumentationParameters: InstrumentationParameters {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val configs: ListProperty<RegularFile>

}

internal fun Property<InjectInstrumentationParameters>.getConfigsFile(): List<RegularFile> {
    val params = get()
    return params.configs.get()
}



