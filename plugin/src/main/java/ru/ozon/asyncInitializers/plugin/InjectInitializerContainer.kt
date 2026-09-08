package ru.ozon.asyncInitializers.plugin

import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class InjectInitializerContainer @Inject constructor(
    public val name: String,
    objects: ObjectFactory
) {

    /**
     * Файл, содержащий список команд для плагина, используя которые будет происходить патчинг
     */
    public val configs: ListProperty<RegularFile> = objects.listProperty(RegularFile::class.java)

    /**
     * Проверка после патчинга, что все заявленные классы были найдены и пропатчены
     *
     * В текущей реализации является долгой процедурой
     */
    public val isTransformResultEnabled: Property<Boolean> = objects.property(Boolean::class.java)
        .convention(false)

}
