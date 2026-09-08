package ru.ozon.asyncInitializers.plugin.internal.image.images

import ru.ozon.asyncInitializers.plugin.internal.image.Image
import ru.ozon.asyncInitializers.plugin.internal.util.ConfigReservedTypes

/**
 * Образ классов, используемых в JVM
 *
 * Примитивы, обьекты, лямды (пока не поддерживается), массивы (пока не поддерживается)
 */
internal sealed interface TypeImage: Image {
    val jvmName: String

    enum class Primitive(
        override val jvmName: String,
        val wrapperJavaName: Object,
    ): TypeImage {
        INTEGER(
            jvmName = "I",
            wrapperJavaName = Object("java.lang.Integer"),
        ),
        SHORT(
            jvmName = "S",
            wrapperJavaName = Object("java.lang.Short"),
        ),
        BOOLEAN(
            jvmName = "Z",
            wrapperJavaName = Object("java.lang.Boolean"),
        ),
        BYTE(
            jvmName = "B",
            wrapperJavaName = Object("java.lang.Byte"),
        ),
        LONG(
            jvmName = "J",
            wrapperJavaName = Object("java.lang.Long"),
        ),
        CHAR(
            jvmName = "C",
            wrapperJavaName = Object("java.lang.Character"),
        ),
        FLOAT(
            jvmName = "F",
            wrapperJavaName = Object("java.lang.Float"),
        ),
        DOUBLE(
            jvmName = "I",
            wrapperJavaName = Object("java.lang.Double"),
        ),
        VOID(
            jvmName = "V",
            wrapperJavaName = Object("java.lang.Void"),
        );

        override fun toString(): String {
            val reservedName = ConfigReservedTypes.findSecondAssociated(this)
            checkNotNull(reservedName) {  "Не зарезервированый примитив $this" }

            return reservedName
        }
    }

    data class Object(
        val javaName: String
    ): TypeImage {

        init {
            check(javaName.isNotBlank()) { "javaName не должен быть пустым" }
            check(!javaName.contains(" ")) { "javaName содержит пробел \"$javaName\"" }
        }

        override val jvmName: String = "L${javaName.replace(".","/")};"

        override fun toString(): String {
            val reservedName = ConfigReservedTypes.findSecondAssociated(this)
            if (reservedName != null) return reservedName
            return javaName
        }
    }

    data class Lamda(
        override val jvmName: String
    ): TypeImage {

        constructor(countParams: Int): this("Lkotlin.jvm.functions.Function${countParams};")

        companion object{
            val LAMDA_JVM_PATTERN = Regex("Lkotlin.jvm.functions.Function\\d;")
        }

        override fun toString(): String {
            return "Lamda"
        }
    }

    data class Array(
        val typeImage: TypeImage
    ): TypeImage {

        override val jvmName: String = "[${typeImage.jvmName}"

        override fun toString(): String {
            return "Array<$typeImage>"
        }

    }

}
