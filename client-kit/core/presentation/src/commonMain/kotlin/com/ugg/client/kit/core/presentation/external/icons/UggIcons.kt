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
