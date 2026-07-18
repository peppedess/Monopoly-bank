package com.peppedess.monopolybank.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

/** Avatar circolare del giocatore con colore proprietà + emoji segnalino */
@Composable
fun PlayerAvatar(token: String, color: Color, size: Int = 48) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(
                brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.95f), color.copy(alpha = 0.7f))),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(token, fontSize = (size * 0.5).sp, textAlign = TextAlign.Center)
    }
}

/**
 * Grafico canvas dell'andamento del saldo (stile grafico ritardi di Treni Tracker):
 * linea morbida con area sfumata sotto, animata all'ingresso.
 */
@Composable
fun BalanceChart(
    values: List<Long>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (values.size < 2) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                "Il grafico apparirà dopo le prime transazioni",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(900),
        label = "chart"
    )
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    val minV = remember(values) { values.min() }
    val maxV = remember(values) { values.max() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val pad = 12f
        val range = max(1L, maxV - minV).toFloat()

        // griglia orizzontale
        for (i in 0..3) {
            val y = pad + (h - 2 * pad) * i / 3f
            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.5f)
        }

        val n = values.size
        val shownCount = max(2, (n * progress).toInt())
        fun px(i: Int) = pad + (w - 2 * pad) * i / (n - 1).toFloat()
        fun py(v: Long) = pad + (h - 2 * pad) * (1f - (v - minV) / range)

        // path smussato (quadratic midpoints)
        val path = Path()
        path.moveTo(px(0), py(values[0]))
        for (i in 1 until min(shownCount, n)) {
            val x0 = px(i - 1); val y0 = py(values[i - 1])
            val x1 = px(i); val y1 = py(values[i])
            path.quadraticTo(x0, y0, (x0 + x1) / 2f, (y0 + y1) / 2f)
        }
        val lastI = min(shownCount, n) - 1
        path.lineTo(px(lastI), py(values[lastI]))

        // area sfumata
        val area = Path().apply {
            addPath(path)
            lineTo(px(lastI), h)
            lineTo(px(0), h)
            close()
        }
        drawPath(
            area,
            brush = Brush.verticalGradient(
                listOf(lineColor.copy(alpha = 0.30f), lineColor.copy(alpha = 0.02f)),
                startY = 0f, endY = h
            )
        )
        drawPath(
            path,
            color = lineColor,
            style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // pallino finale
        drawCircle(lineColor, radius = 9f, center = Offset(px(lastI), py(values[lastI])))
        drawCircle(Color.White, radius = 4f, center = Offset(px(lastI), py(values[lastI])))
    }
}
