package ru.ozon.asyncInitializers.plugin.internal.parser

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.ozon.asyncInitializers.plugin.internal.exceptions.CannotParseLineException
import ru.ozon.asyncInitializers.plugin.internal.image.images.MethodImage
import ru.ozon.asyncInitializers.plugin.internal.image.images.ModifiedImage
import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage
import java.io.File
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class ConfigFileParserTest {

    @field:TempDir
    lateinit var tempDir: Path

    private fun configFile(name: String, content: String): File {
        val file = tempDir.resolve(name).toFile()
        file.writeText(content)
        return file
    }

    private fun read(files: List<File>): List<ModifiedImage> =
        ConfigFileParser.readModifiedTypes(files)

    @Test
    fun `строка с блоком ignore парсится в образ с игнорируемыми методами`() {
        val file = configFile(
            "c1.config",
            """
                inject ru.ozon.init.MyInitializer toPublicMethods ru.ozon.victim.Victim {
                    ignore fun a(Int)
                }
            """.trimIndent()
        )

        val images = read(listOf(file))

        assertEquals(1, images.size)
        val image = images[0]
        assertEquals(TypeImage.Object("ru.ozon.victim.Victim"), image.victim)
        assertEquals(TypeImage.Object("ru.ozon.init.MyInitializer"), image.initializer)
        assertEquals(
            setOf(MethodImage("a", listOf(TypeImage.Primitive.INTEGER), TypeImage.Primitive.VOID)),
            image.ignoredMethods
        )
    }

    @Test
    fun `строка без блока имеет пустой набор игнорируемых методов`() {
        val file = configFile(
            "c2.config",
            "inject ru.ozon.init.MyInitializer toPublicMethods ru.ozon.victim.Victim"
        )

        val images = read(listOf(file))

        assertEquals(1, images.size)
        assertTrue(images[0].ignoredMethods.isEmpty())
    }

    @Test
    fun `неподдерживаемый синтаксис строки не парсится`() {
        val file = configFile(
            "c3.config",
            "inject ru.ozon.init.MyInitializer toPrivateMethods ru.ozon.victim.Victim"
        )

        assertTrue(read(listOf(file)).isEmpty())
    }

    @Test
    fun `некорректная строка внутри ignore-блока кидает исключение`() {
        val file = configFile(
            "c4.config",
            """
                inject ru.ozon.init.MyInitializer toPublicMethods ru.ozon.victim.Victim {
                    какая-то белиберда
                }
            """.trimIndent()
        )

        assertFailsWith<CannotParseLineException> { read(listOf(file)) }
    }

    @Test
    fun `пустые и бланковые строки пропускаются`() {
        val file = configFile(
            "c5.config",
            "\n\n   \ninject ru.ozon.init.MyInitializer toPublicMethods ru.ozon.victim.Victim\n\n\n"
        )

        val images = read(listOf(file))

        assertEquals(1, images.size)
    }

    @Test
    fun `несколько файлов конкатенируются в общий список образов`() {
        val first = configFile(
            "f1.config",
            "inject ru.ozon.init.A toPublicMethods ru.ozon.victim.V"
        )
        val second = configFile(
            "f2.config",
            "inject ru.ozon.init.B toPublicMethods ru.ozon.victim.W"
        )

        val images = read(listOf(first, second))

        assertEquals(2, images.size)
        assertEquals(TypeImage.Object("ru.ozon.victim.V"), images[0].victim)
        assertEquals(TypeImage.Object("ru.ozon.victim.W"), images[1].victim)
    }
}
