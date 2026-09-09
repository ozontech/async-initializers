package ru.ozon.asyncInitializers.plugin.internal.util

import org.junit.jupiter.api.Test
import ru.ozon.asyncInitializers.plugin.internal.image.images.TypeImage
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ConfigReservedTypesTest {

    @Test
    fun `direct name-to-primitive mapping`() {
        assertEquals(TypeImage.Primitive.INTEGER, ConfigReservedTypes.findFirstAssociated("Int"))
        assertEquals(TypeImage.Primitive.SHORT, ConfigReservedTypes.findFirstAssociated("Short"))
        assertEquals(TypeImage.Primitive.BOOLEAN, ConfigReservedTypes.findFirstAssociated("Boolean"))
        assertEquals(TypeImage.Primitive.BYTE, ConfigReservedTypes.findFirstAssociated("Byte"))
        assertEquals(TypeImage.Primitive.LONG, ConfigReservedTypes.findFirstAssociated("Long"))
        assertEquals(TypeImage.Primitive.CHAR, ConfigReservedTypes.findFirstAssociated("Char"))
        assertEquals(TypeImage.Primitive.FLOAT, ConfigReservedTypes.findFirstAssociated("Float"))
        assertEquals(TypeImage.Primitive.DOUBLE, ConfigReservedTypes.findFirstAssociated("Double"))
        assertEquals(TypeImage.Primitive.VOID, ConfigReservedTypes.findFirstAssociated("Unit"))
    }

    @Test
    fun `reverse primitive-to-name mapping`() {
        assertEquals("Int", ConfigReservedTypes.findSecondAssociated(TypeImage.Primitive.INTEGER))
        assertEquals("Short", ConfigReservedTypes.findSecondAssociated(TypeImage.Primitive.SHORT))
        assertEquals("Boolean", ConfigReservedTypes.findSecondAssociated(TypeImage.Primitive.BOOLEAN))
        assertEquals("Byte", ConfigReservedTypes.findSecondAssociated(TypeImage.Primitive.BYTE))
        assertEquals("Long", ConfigReservedTypes.findSecondAssociated(TypeImage.Primitive.LONG))
        assertEquals("Char", ConfigReservedTypes.findSecondAssociated(TypeImage.Primitive.CHAR))
        assertEquals("Float", ConfigReservedTypes.findSecondAssociated(TypeImage.Primitive.FLOAT))
        assertEquals("Double", ConfigReservedTypes.findSecondAssociated(TypeImage.Primitive.DOUBLE))
        assertEquals("Unit", ConfigReservedTypes.findSecondAssociated(TypeImage.Primitive.VOID))
    }

    @Test
    fun `String maps to an object in both directions`() {
        assertEquals(
            TypeImage.Object("java.lang.String"),
            ConfigReservedTypes.findFirstAssociated("String")
        )
        assertEquals(
            "String",
            ConfigReservedTypes.findSecondAssociated(TypeImage.Object("java.lang.String"))
        )
    }

    @Test
    fun `unreserved name returns null`() {
        assertNull(ConfigReservedTypes.findFirstAssociated("CustomType"))
    }

    @Test
    fun `unreserved primitive returns null`() {
        assertNull(ConfigReservedTypes.findSecondAssociated(TypeImage.Object("java.lang.Object")))
    }
}
