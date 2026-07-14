package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.PlayerEntity

@Composable
fun TacticalPitchView(
    players: List<PlayerEntity>,
    onPlayerSelected: (PlayerEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF134E5E),
                            Color(0xFF71B280)
                        )
                    )
                )
        ) {
            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()

            // 1. Draw Football Field Pitch Markings
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 3.dp.toPx()
                val lineAlpha = 0.5f
                val lineColor = Color.White.copy(alpha = lineAlpha)

                // Outer border padding
                val padX = 20.dp.toPx()
                val padY = 20.dp.toPx()
                val fieldW = width - (padX * 2)
                val fieldH = height - (padY * 2)

                // Outer Pitch Boundary Rect
                drawRect(
                    color = lineColor,
                    topLeft = Offset(padX, padY),
                    size = Size(fieldW, fieldH),
                    style = Stroke(width = strokeWidth)
                )

                // Halfway Line
                val midY = height / 2f
                drawLine(
                    color = lineColor,
                    start = Offset(padX, midY),
                    end = Offset(width - padX, midY),
                    strokeWidth = strokeWidth
                )

                // Center Circle
                drawCircle(
                    color = lineColor,
                    radius = 45.dp.toPx(),
                    center = Offset(width / 2f, midY),
                    style = Stroke(width = strokeWidth)
                )

                // Center Spot
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = Offset(width / 2f, midY)
                )

                // Penalty Box (Top Goal Area)
                val penBoxW = fieldW * 0.5f
                val penBoxH = fieldH * 0.16f
                drawRect(
                    color = lineColor,
                    topLeft = Offset(width / 2f - penBoxW / 2f, padY),
                    size = Size(penBoxW, penBoxH),
                    style = Stroke(width = strokeWidth)
                )

                // Goal Area (Small Box) Top
                val goalAreaW = fieldW * 0.22f
                val goalAreaH = fieldH * 0.06f
                drawRect(
                    color = lineColor,
                    topLeft = Offset(width / 2f - goalAreaW / 2f, padY),
                    size = Size(goalAreaW, goalAreaH),
                    style = Stroke(width = strokeWidth)
                )

                // Penalty Box (Bottom Goal Area)
                drawRect(
                    color = lineColor,
                    topLeft = Offset(width / 2f - penBoxW / 2f, height - padY - penBoxH),
                    size = Size(penBoxW, penBoxH),
                    style = Stroke(width = strokeWidth)
                )

                // Goal Area (Small Box) Bottom
                drawRect(
                    color = lineColor,
                    topLeft = Offset(width / 2f - goalAreaW / 2f, height - padY - goalAreaH),
                    size = Size(goalAreaW, goalAreaH),
                    style = Stroke(width = strokeWidth)
                )
            }

            // 2. Position and Map Players onto the Pitch
            // We have 5 players typical positions: GK, DF, MF, FW1, FW2
            val positionCoords = mutableListOf<PlayerCoordinates>()

            val gkPlayer = players.find { it.position == "GK" }
            if (gkPlayer != null) {
                positionCoords.add(PlayerCoordinates(gkPlayer, 0.5f, 0.88f))
            }

            val dfPlayers = players.filter { it.position == "DF" }
            dfPlayers.forEachIndexed { idx, p ->
                val xRatio = if (dfPlayers.size == 1) 0.5f else (0.3f + (idx * 0.4f))
                positionCoords.add(PlayerCoordinates(p, xRatio, 0.68f))
            }

            val mfPlayers = players.filter { it.position == "MF" }
            mfPlayers.forEachIndexed { idx, p ->
                val xRatio = if (mfPlayers.size == 1) 0.5f else (0.25f + (idx * 0.5f))
                positionCoords.add(PlayerCoordinates(p, xRatio, 0.48f))
            }

            val fwPlayers = players.filter { it.position == "FW" }
            fwPlayers.forEachIndexed { idx, p ->
                val xRatio = if (fwPlayers.size == 1) 0.5f else (0.3f + (idx * 0.4f))
                positionCoords.add(PlayerCoordinates(p, xRatio, 0.24f))
            }

            // Draw Player Nodes
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(players) {
                        detectTapGestures { offset ->
                            // Find if any player node was tapped
                            val radiusPx = 22.dp.toPx()
                            val clickedCoord = positionCoords.find { coord ->
                                val pX = coord.xRatio * width
                                val pY = coord.yRatio * height
                                val dist = kotlin.math.hypot(offset.x - pX, offset.y - pY)
                                dist <= radiusPx
                            }
                            if (clickedCoord != null) {
                                onPlayerSelected(clickedCoord.player)
                            }
                        }
                    }
            ) {
                val nodeRadius = 18.dp.toPx()

                positionCoords.forEach { coord ->
                    val pX = coord.xRatio * width
                    val pY = coord.yRatio * height

                    // Node Outer Shadow/Glow
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.3f),
                        radius = nodeRadius + 3.dp.toPx(),
                        center = Offset(pX, pY)
                    )

                    // Node Core (Jersey Circle)
                    val jerseyColor = when (coord.player.position) {
                        "GK" -> Color(0xFFF7971E) // Orange
                        "DF" -> Color(0xFF1D976C) // Greenish Blue
                        "MF" -> Color(0xFF2193B0) // Blueish
                        else -> Color(0xFFE65C00) // Forward Red/Orange
                    }

                    drawCircle(
                        color = jerseyColor,
                        radius = nodeRadius,
                        center = Offset(pX, pY)
                    )

                    // Draw Shirt Ring accent
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = nodeRadius - 3.dp.toPx(),
                        center = Offset(pX, pY),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Draw Player Number text
                    val numberText = coord.player.number.toString()
                    val numberLayout = textMeasurer.measure(
                        text = numberText,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                    drawText(
                        textLayoutResult = numberLayout,
                        topLeft = Offset(
                            pX - (numberLayout.size.width / 2f),
                            pY - (numberLayout.size.height / 2f)
                        )
                    )

                    // Draw Player Name under the node
                    val nameLayout = textMeasurer.measure(
                        text = coord.player.name.substringBefore(" "), // show first name or initials
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    )
                    drawText(
                        textLayoutResult = nameLayout,
                        topLeft = Offset(
                            pX - (nameLayout.size.width / 2f),
                            pY + nodeRadius + 4.dp.toPx()
                        )
                    )
                }
            }

            // Overlay label instruction
            Text(
                text = "Interactive Tactical Pitch • Tap players",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

private data class PlayerCoordinates(
    val player: PlayerEntity,
    val xRatio: Float,
    val yRatio: Float
)
