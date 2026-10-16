package dev.projecttabs.ui

internal object TabLayout {

    data class Result(val visible: List<Int>, val widths: List<Int>, val overflow: Boolean)

    fun compute(
        preferred: List<Int>,
        available: Int,
        minWidth: Int,
        gap: Int,
        overflowWidth: Int,
        activeIndex: Int,
    ): Result {
        val count = preferred.size
        if (count == 0) return Result(emptyList(), emptyList(), false)

        val gaps = gap * (count - 1)
        val all = (0 until count).toList()
        if (preferred.sum() + gaps <= available) return Result(all, preferred, false)
        if (minWidth.toLong() * count + gaps <= available) {
            return Result(all, fitWidths(preferred, available - gaps, minWidth), false)
        }

        val room = available - overflowWidth - gap
        val fitting = ((room + gap) / (minWidth + gap)).coerceIn(1, count)
        val visible = (0 until fitting).toMutableList()
        if (activeIndex >= fitting) visible[fitting - 1] = activeIndex
        val widths = fitWidths(visible.map { preferred[it] }, room - gap * (fitting - 1), minWidth)
        return Result(visible, widths, true)
    }

    fun fitWidths(preferred: List<Int>, total: Int, minWidth: Int): List<Int> {
        var remaining = total
        var left = preferred.size
        var cap = Int.MAX_VALUE
        for (width in preferred.sorted()) {
            if (width.toLong() * left <= remaining) {
                remaining -= width
                left--
            } else {
                cap = remaining / left
                break
            }
        }
        val limit = maxOf(cap, minWidth)
        return preferred.map { minOf(it, limit) }
    }
}
