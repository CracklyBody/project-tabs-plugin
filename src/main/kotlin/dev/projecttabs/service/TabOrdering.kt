package dev.projecttabs.service

enum class NewTabPlacement { END, AFTER_CURRENT }

data class TabName(val key: String, val name: String, val path: String?)

object TabOrdering {

    fun insert(known: List<String>, key: String, placement: NewTabPlacement, anchor: String?): List<String> {
        if (key in known) return known
        val anchorIndex = if (placement == NewTabPlacement.AFTER_CURRENT && anchor != null) known.indexOf(anchor) else -1
        return known.toMutableList().apply { if (anchorIndex >= 0) add(anchorIndex + 1, key) else add(key) }
    }

    fun arrange(known: List<String>, open: List<String>): List<String> {
        val position = known.withIndex().associate { (index, key) -> key to index }
        return open.withIndex()
            .sortedWith(compareBy({ position[it.value] ?: Int.MAX_VALUE }, { it.index }))
            .map { it.value }
    }

    fun move(known: List<String>, visible: List<String>, key: String, toIndex: Int): List<String> {
        val from = visible.indexOf(key)
        if (from < 0) return known
        val reordered = visible.toMutableList().apply {
            removeAt(from)
            add(toIndex.coerceIn(0, size), key)
        }
        val visibleKeys = visible.toSet()
        val next = reordered.iterator()
        return (known + visible.filter { it !in known }).map { if (it in visibleKeys) next.next() else it }
    }

    fun neighbor(visible: List<String>, current: String, step: Int): String? {
        if (visible.size < 2) return null
        val index = visible.indexOf(current)
        if (index < 0) return null
        return visible[Math.floorMod(index + step, visible.size)]
    }

    fun indexForNumber(number: Int, size: Int): Int? = when {
        size == 0 -> null
        number == 9 -> size - 1
        number in 1..size -> number - 1
        else -> null
    }

    fun successorOnClose(visible: List<String>, closing: String): String? {
        val index = visible.indexOf(closing)
        if (index < 0 || visible.size < 2) return null
        return if (index + 1 < visible.size) visible[index + 1] else visible[index - 1]
    }

    fun pushClosed(stack: List<String>, key: String, limit: Int): List<String> =
        (listOf(key) + stack.filter { it != key }).take(limit)

    fun prune(known: List<String>, open: Set<String>, limit: Int): List<String> {
        var excess = known.size - limit
        if (excess <= 0) return known
        val result = known.toMutableList()
        val iterator = result.listIterator(result.size)
        while (excess > 0 && iterator.hasPrevious()) {
            if (iterator.previous() !in open) {
                iterator.remove()
                excess--
            }
        }
        return result
    }

    fun displayNames(tabs: List<TabName>): Map<String, String> {
        val result = HashMap<String, String>()
        for ((name, group) in tabs.groupBy { it.name }) {
            if (group.size == 1) {
                result[group[0].key] = name
                continue
            }
            val parents = group.associate { it.key to parentFolders(it.path) }
            val maxDepth = parents.values.maxOf { it.size }
            var depth = 1
            while (depth < maxDepth && parents.values.map { it.take(depth) }.toSet().size < group.size) depth++
            for (tab in group) {
                val hint = parents.getValue(tab.key).take(depth).reversed().joinToString("/")
                result[tab.key] = if (hint.isEmpty()) name else "$name — $hint"
            }
        }
        return result
    }

    private fun parentFolders(path: String?): List<String> =
        path?.replace('\\', '/')?.trimEnd('/')?.split('/')?.dropLast(1)?.filter { it.isNotEmpty() }?.reversed()
            ?: emptyList()
}
