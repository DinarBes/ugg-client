package com.ugg.client.kit.core.presentation.external.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Line icons from the design (24×24, stroke 1.6, round caps) — the same paths as in Figma.
 *
 * Drawn in black: [androidx.compose.material3.Icon] tints them, so pass the color there.
 */
object UggIcons {
    val ArrowRight by lazy { lineIcon("ArrowRight", "M5 12h14", "m12 5 7 7-7 7") }

    val ArrowLeft by lazy { lineIcon("ArrowLeft", "m12 19-7-7 7-7", "M19 12H5") }

    val Heart by lazy {
        lineIcon(
            "Heart",
            "M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z",
        )
    }

    val Bag by lazy {
        lineIcon("Bag", "M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z", "M3 6h18", "M16 10a4 4 0 0 1-8 0")
    }

    val Pin by lazy {
        lineIcon("Pin", "M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z", circle(12f, 10f, 3f))
    }

    val Clock by lazy { lineIcon("Clock", circle(12f, 12f, 10f), "M12 6v6l4 2") }

    val Home by lazy { lineIcon("Home", "m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z", "M9 22V12h6v10") }

    /** Tab «Модель». */
    val Boot by lazy { lineIcon("Boot", "M6 3h7v9.5l5.2 1.9a3 3 0 0 1 2 2.8V20H5v-5.5c0-1 .6-1.8 1-2.6Z", "M6 7h7") }

    val User by lazy { lineIcon("User", "M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2", circle(12f, 7f, 4f)) }

    val Share by lazy { lineIcon("Share", "M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8", "m16 6-4-4-4 4", "M12 2v13") }

    val ChevronDown by lazy { lineIcon("ChevronDown", "m6 9 6 6 6-6") }

    val ChevronUp by lazy { lineIcon("ChevronUp", "m18 15-6-6-6 6") }

    val Ruler by lazy {
        lineIcon(
            "Ruler",
            "M21.3 15.3a2.4 2.4 0 0 1 0 3.4l-2.6 2.6a2.4 2.4 0 0 1-3.4 0L2.7 8.7a2.41 2.41 0 0 1 0-3.4l2.6-2.6a2.41 2.41 0 0 1 3.4 0Z",
            "m14.5 12.5 2-2", "m11.5 9.5 2-2", "m8.5 6.5 2-2", "m17.5 15.5 2-2",
        )
    }

    /** Natural material. */
    val Leaf by lazy {
        lineIcon(
            "Leaf",
            "M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10Z",
            "M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12",
        )
    }

    /** Handmade. */
    val Scissors by lazy {
        lineIcon(
            "Scissors",
            circle(6f, 6f, 3f), "M8.12 8.12 12 12", "M20 4 8.12 15.88", circle(6f, 18f, 3f), "M14.8 14.8 20 20",
        )
    }

    /** Warmth, winter. */
    val Snowflake by lazy {
        lineIcon("Snowflake", "M12 2v20", "m4.9 7 14.2 10", "m4.9 17 14.2-10", "m9 4 3 2 3-2", "m9 20 3-2 3 2")
    }

    /** Warranty. */
    val ShieldCheck by lazy {
        lineIcon(
            "ShieldCheck",
            "M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z",
            "m9 12 2 2 4-4",
        )
    }

    val Plus by lazy { lineIcon("Plus", "M5 12h14", "M12 5v14") }

    val Minus by lazy { lineIcon("Minus", "M5 12h14") }

    val Gift by lazy {
        lineIcon(
            "Gift",
            "M4 8h16a1 1 0 0 1 1 1v2a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V9a1 1 0 0 1 1-1z",
            "M12 8v13",
            "M19 12v7a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-7",
            "M7.5 8a2.5 2.5 0 0 1 0-5C10 3 12 8 12 8s2-5 4.5-5a2.5 2.5 0 0 1 0 5",
        )
    }

    private fun circle(cx: Float, cy: Float, r: Float): String =
        "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"

    private fun lineIcon(name: String, vararg paths: String): ImageVector {
        val builder = ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        )

        paths.forEach { d ->
            builder.addPath(
                pathData = addPathNodes(d),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }

        return builder.build()
    }
}
