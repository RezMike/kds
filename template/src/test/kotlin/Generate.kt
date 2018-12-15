import java.io.File

object Generate {
    @JvmStatic
    fun main(args: Array<String>) {
        println("CWD: ${File(".").absolutePath}")
        synchronize(
            File("template/src/main/kotlin/com/soywiz/kds/TGenArrayList.kt"),
            File("src/commonMain/kotlin/com/soywiz/kds/ArrayList.kt"),
            includeFloat = true
        )
        synchronize(
            File("template/src/main/kotlin/com/soywiz/kds/TGenCircularList.kt"),
            File("src/commonMain/kotlin/com/soywiz/kds/CircularList.kt"),
            includeFloat = true,
            includeGeneric = true
        )
        synchronize(
            File("template/src/main/kotlin/com/soywiz/kds/TGenArray2.kt"),
            File("src/commonMain/kotlin/com/soywiz/kds/Array2.kt"),
            includeFloat = true,
            includeGeneric = true
        )
        synchronize(
            File("template/src/main/kotlin/com/soywiz/kds/TGenLinkedList.kt"),
            File("src/commonMain/kotlin/com/soywiz/kds/LinkedList.kt"),
            includeFloat = true,
            includeGeneric = true
        )
        synchronize(
            File("template/src/main/kotlin/com/soywiz/kds/TGenPriorityQueue.kt"),
            File("src/commonMain/kotlin/com/soywiz/kds/PriorityQueue.kt"),
            includeFloat = true,
            includeGeneric = true
        )
        synchronize(
            File("template/src/main/kotlin/com/soywiz/kds/TGenStack.kt"),
            File("src/commonMain/kotlin/com/soywiz/kds/Stack.kt"),
            includeFloat = true,
            includeGeneric = true
        )
    }

    fun synchronize(src: File, dst: File, includeFloat: Boolean = true, includeGeneric: Boolean = false) {
        val content = src.readText()
        val parts = content.split("// GENERIC\n")
        val head = parts[0].trim()
        val generic = parts.getOrElse(1) { "" }

        val types = listOf("Int", "Double") + if (includeFloat) listOf("Float") else listOf()

        dst.writeText(
            ("$head\n\n// AUTOGENERATED: DO NOT MODIFY MANUALLY!\n\n" + (if (includeGeneric) "$generic\n\n" else "") + types.map { "// $it\n" + generic.replaceTemplate(it) }.joinToString("\n\n"))
                .restoreCollectionKinds()
        )
    }

    private fun String.restoreCollectionKinds(): String = this
        .restoreCollectionKinds("Int")
        .restoreCollectionKinds("Float")
        .restoreCollectionKinds("Double")

    private fun String.restoreCollectionKinds(kind: String): String {
        return this.replace("${kind}List", "List<$kind>")

            .replace("${kind}MutableIterator", "MutableIterator<$kind>")
            .replace("${kind}MutableCollection", "MutableCollection<$kind>")

            .replace("${kind}Iterable", "Iterable<$kind>")
            .replace("${kind}Iterator", "Iterator<$kind>")
            .replace("${kind}Collection", "Collection<$kind>")

            .replace("${kind}Comparable", "Comparable<$kind>")
            .replace("${kind}Comparator", "Comparator<$kind>")
    }

    fun String.replaceTemplate(kind: String): String {
        val lkind = kind.toLowerCase()
        return this
            .replace("arrayOfNulls<Any>", "${kind}Array")
            .replace("arrayOfNulls<TGen>", "${kind}Array")
            .replace("<reified TGen>", "")
            .replace("<reified TGen : Comparable<TGen>>", "")
            .replace("Iterable<TGen>", "Iterable<$kind>")
            .replace("Collection<TGen>", "Collection<$kind>")
            .replace("fun <TGen>", "fun")
            .replace("arrayListOf<TGen>", "${lkind}ArrayListOf")
            .replace("Array<TGen>", "${kind}Array")
            .replace("Array<out TGen>", "${kind}Array")
            .replace(Regex("""(\w+)<TGen>""")) {
                val base = it.groupValues[1]
                val name = base.replace("TGen", "")
                if (base == "Iterator") {
                    "Iterator<$kind>"
                } else {
                    "$kind$name"
                }
            }
            .replace(Regex("""(\w+)<\*/\*TGen\*/>""")) {
                val base = it.groupValues[1]
                val name = base.replace("TGen", "")
                if (base == "Iterator") {
                    "Iterator<$kind>"
                } else {
                    "$kind$name"
                }
            }
            .replace(": TGen", ": $kind")
            .replace("-> TGen", "-> $kind")
            .replace("as TGen", "as $kind")
            .replace("(TGen)", "($kind)")
            .replace("TGen, ", "$kind, ")
            .replace("TGen", kind)
            .replace("tgen", lkind)

    }

    /*
    @JvmStatic
    fun main(args: Array<String>) {
        File("src/commonMain/kotlin/com/soywiz/kds/Stack.kt").synchronize()
        File("src/commonMain/kotlin/com/soywiz/kds/Queue.kt").synchronize(includeFloat = false)
        File("src/commonMain/kotlin/com/soywiz/kds/CircularList.kt").synchronize(includeFloat = false)
        File("src/commonMain/kotlin/com/soywiz/kds/LinkedList.kt").synchronize(includeFloat = false)
        File("src/commonMain/kotlin/com/soywiz/kds/Array2.kt").synchronize()
        File("src/commonMain/kotlin/com/soywiz/kds/ArrayList.kt").synchronize()
    }

    fun File.synchronize(includeFloat: Boolean = true) {
        val content = this.readText()
        val parts = content.split("// GENERIC\n")
        val head = parts[0].trim()
        val genericSpecific = parts.getOrElse(1) { "" }
        val parts2 = genericSpecific.split("// SPECIFIC - Do not modify from here\n")
        val generic = parts2[0].trim()
        val specific = parts2.getOrElse(1) { "" }
        val intText = generic.replaceTemplate("Int")
        val floatText = if (includeFloat) generic.replaceTemplate("Float") else ""
        val doubleText = generic.replaceTemplate("Double")
        val newSpecific = listOf(intText, floatText, doubleText).filter { it.isNotEmpty() }.joinToString("\n\n")
        this.writeText("$head\n\n// GENERIC\n\n$generic\n\n// SPECIFIC - Do not modify from here\n\n$newSpecific\n")
    }

    fun String.replaceTemplate(kind: String): String {
        val lkind = kind.toLowerCase()
        return this
            .replace("arrayListOf<T>", "${lkind}ArrayListOf")
            .replace("Array<T>", "${kind}Array")
            .replace("Array<out T>", "${kind}Array")
            .replace(Regex("""(\w+)<T>""")) {
                val base = it.groupValues[1]
                val name = base.replace("Generic", "")
                if (base == "Iterator") {
                    "Iterator<$kind>"
                } else {
                    "$kind$name"
                }
            }
            .replace(": T", ": $kind")
            .replace("-> T", "-> $kind")
            .replace("as T", "as $kind")
            .replace("(T)", "($kind)")
            .replace("T, ", "$kind, ")
            .replace("arrayOfNulls<Any>", "${kind}Array")
            .replace("Generic", kind)
            .replace("generic", lkind)
            .replace("fun <T> ", "fun ")
    }
    */
}
