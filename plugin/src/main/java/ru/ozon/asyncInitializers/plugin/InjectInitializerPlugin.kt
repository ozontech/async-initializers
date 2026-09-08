package ru.ozon.asyncInitializers.plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.api.variant.Variant
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import ru.ozon.asyncInitializers.plugin.internal.asm.InjectInitializeAsmClassVisitorFactory
import ru.ozon.asyncInitializers.plugin.internal.exceptions.MultiConfigException
import ru.ozon.asyncInitializers.plugin.internal.transfromResult.InjectInitializerValidateTask
import ru.ozon.asyncInitializers.plugin.internal.util.capitalizedName

/**
 * Gradle plugin, инициализирующийся через fullName самим gradle
 *
 * Занимающийся патчингом байткода для вставки [ru.ozon.gradle.componentInitializer.util.ComponentInitializer] в код библиотек
 * Валидацией вставки данный плагин может гарантировать что [ru.ozon.gradle.componentInitializer.util.ComponentInitializer] был встроен правильно
 */
@Suppress("unused")
public class InjectInitializerPlugin: Plugin<Project> {

    private companion object {
        const val IS_PLUGIN_ENABLED_PROPERTY_NAME = "ozon.component.initializer.inject.enabled"
    }

    override fun apply(target: Project): Unit = with(target) {
        val containers = objects.domainObjectContainer(InjectInitializerContainer::class.java) { name ->
            objects.newInstance(InjectInitializerContainer::class.java, name)
        }

        extensions.add("injectInitializerComponents", containers)

        pluginManager.withPlugin("com.android.application") {
            val androidComponents = extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->
                val filteredVariantContainers = containers.filter { config -> config.name == variant.buildType }
                if (filteredVariantContainers.isEmpty()) return@onVariants

                val currentContainer = filteredVariantContainers[0]
                if (filteredVariantContainers.size > 1) throw MultiConfigException(currentContainer.name)

                if (currentContainer.isTransformResultEnabled.get()) enableValidate(variant, currentContainer.configs)

                variant.instrumentation.transformClassesWith(InjectInitializeAsmClassVisitorFactory::class.java, InstrumentationScope.ALL) { parameters ->
                    parameters.configs.set(currentContainer.configs.get())
                }

                variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS)
            }
        }
    }

    private fun Project.enableValidate(
        variant: Variant,
        configs: ListProperty<RegularFile>
    ) {


        val injectInitializerValidateTask = tasks.register(
            "validateInitializersInject${variant.capitalizedName}",
            InjectInitializerValidateTask::class.java,
            object : Action<InjectInitializerValidateTask>{
                override fun execute(task: InjectInitializerValidateTask) {
                    task.configs.set(configs)
                }
            }
        )

        variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
            .use(injectInitializerValidateTask)
            .toTransform(
                ScopedArtifact.CLASSES,
                InjectInitializerValidateTask::allJars,
                InjectInitializerValidateTask::allDirectories,
                InjectInitializerValidateTask::output
            )
    }

}
