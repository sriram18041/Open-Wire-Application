package com.sriramanappindi.openwire.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sriramanappindi.openwire.games.SudokuGenerator
import com.sriramanappindi.openwire.games.SudokuStore
import com.sriramanappindi.openwire.games.localEpochDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val epochDay = remember { localEpochDay() }
    val puzzle = remember(epochDay) { SudokuGenerator.forDay(epochDay) }
    val store = remember { SudokuStore(context) }

    var cells by remember { mutableStateOf(store.load(epochDay) ?: puzzle.givens.copyOf()) }
    var selected by remember { mutableStateOf<Int?>(null) }
    val isSolved = cells.contentEquals(puzzle.solution)

    LaunchedEffect(cells) {
        store.save(epochDay, cells)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                title = { Text("Sudoku") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (isSolved) "🎉 Solved! Come back tomorrow for a new one." else "Today's puzzle · same for everyone",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSolved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            SudokuGrid(
                cells = cells,
                givens = puzzle.givens,
                selected = selected,
                onSelect = { selected = it }
            )

            NumberPad(
                onNumber = { n ->
                    val idx = selected
                    if (idx != null && puzzle.givens[idx] == 0) {
                        val next = cells.copyOf()
                        next[idx] = n
                        cells = next
                    }
                },
                onErase = {
                    val idx = selected
                    if (idx != null && puzzle.givens[idx] == 0) {
                        val next = cells.copyOf()
                        next[idx] = 0
                        cells = next
                    }
                }
            )
        }
    }
}

@Composable
private fun SudokuGrid(
    cells: IntArray,
    givens: IntArray,
    selected: Int?,
    onSelect: (Int) -> Unit
) {
    val gridLineColor = MaterialTheme.colorScheme.onBackground
    val cellBorderColor = MaterialTheme.colorScheme.outlineVariant
    val highlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    val peerHighlight = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    val errorColor = MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .padding(20.dp)
            .aspectRatio(1f)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0 until 9) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    for (col in 0 until 9) {
                        val idx = row * 9 + col
                        val value = cells[idx]
                        val isGiven = givens[idx] != 0
                        val isSelected = selected == idx
                        val isPeer = selected != null && selected != idx &&
                            (selected / 9 == row || selected % 9 == col ||
                                (selected / 9 / 3 == row / 3 && selected % 9 / 3 == col / 3))
                        val conflict = value != 0 && hasConflict(cells, idx)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    when {
                                        isSelected -> highlightColor
                                        isPeer -> peerHighlight
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onSelect(idx) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (value != 0) {
                                Text(
                                    value.toString(),
                                    color = when {
                                        conflict -> errorColor
                                        isGiven -> MaterialTheme.colorScheme.onSurface
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    fontWeight = if (isGiven) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellSize = size.width / 9f
            for (i in 0..9) {
                val thick = i % 3 == 0
                val width = if (thick) 2.5.dp.toPx() else 0.6.dp.toPx()
                val color = if (thick) gridLineColor else cellBorderColor
                drawLine(color, Offset(i * cellSize, 0f), Offset(i * cellSize, size.height), width)
                drawLine(color, Offset(0f, i * cellSize), Offset(size.width, i * cellSize), width)
            }
        }
    }
}

private fun hasConflict(cells: IntArray, idx: Int): Boolean {
    val row = idx / 9
    val col = idx % 9
    val value = cells[idx]
    for (other in 0 until 81) {
        if (other == idx) continue
        if (cells[other] != value) continue
        val oRow = other / 9
        val oCol = other % 9
        if (oRow == row || oCol == col || (oRow / 3 == row / 3 && oCol / 3 == col / 3)) return true
    }
    return false
}

@Composable
private fun NumberPad(onNumber: (Int) -> Unit, onErase: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        for (n in 1..9) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .clickable { onNumber(n) },
                contentAlignment = Alignment.Center
            ) {
                Text(n.toString(), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .clickable { onErase() },
            contentAlignment = Alignment.Center
        ) {
            Text("✕", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        }
    }
}
