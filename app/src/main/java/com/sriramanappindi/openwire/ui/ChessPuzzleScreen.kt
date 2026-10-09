package com.sriramanappindi.openwire.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sriramanappindi.openwire.games.ChessBoard
import com.sriramanappindi.openwire.games.ChessPuzzleStore
import com.sriramanappindi.openwire.games.ChessPuzzles
import com.sriramanappindi.openwire.games.localEpochDay
import kotlinx.coroutines.delay

private fun boardAfter(fen: String, movesUci: List<String>, count: Int): Array<Char?> {
    var board = ChessBoard.parseFen(fen)
    for (i in 0 until count) {
        board = ChessBoard.applyMove(board, ChessBoard.parseUci(movesUci[i]))
    }
    return board
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessPuzzleScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val epochDay = remember { localEpochDay() }
    val puzzle = remember(epochDay) { ChessPuzzles.forDay(epochDay) }
    val store = remember { ChessPuzzleStore(context) }

    var movesPlayed by remember { mutableStateOf(store.load(epochDay)) }
    var board by remember { mutableStateOf(boardAfter(puzzle.fen, puzzle.solutionUci, movesPlayed)) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var wrongSquare by remember { mutableStateOf<Int?>(null) }
    var showHint by remember { mutableStateOf(false) }
    val isSolved = movesPlayed >= puzzle.solutionUci.size

    LaunchedEffect(wrongSquare) {
        if (wrongSquare != null) {
            delay(450)
            wrongSquare = null
        }
    }

    fun handleTap(idx: Int) {
        if (isSolved) return
        val piece = board[idx]
        if (piece != null && piece.isUpperCase()) {
            selected = idx
            return
        }
        val from = selected ?: return
        val attempted = ChessBoard.squareName(from) + ChessBoard.squareName(idx)
        val expected = puzzle.solutionUci[movesPlayed]
        selected = null
        if (expected.startsWith(attempted)) {
            board = ChessBoard.applyMove(board, ChessBoard.parseUci(expected))
            movesPlayed += 1
            store.save(epochDay, movesPlayed)
        } else {
            wrongSquare = idx
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                title = { Text("Chess puzzle") },
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
                if (isSolved) "🎉 Checkmate! Come back tomorrow for a new one." else "White to move — find the mate in one",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSolved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, top = 8.dp, end = 24.dp)
            )

            ChessBoardView(
                board = board,
                selected = selected,
                wrongSquare = wrongSquare,
                onTap = ::handleTap
            )

            if (showHint && !isSolved) {
                Text(
                    "💡 ${puzzle.hint}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            } else if (!isSolved) {
                Button(onClick = { showHint = true }, modifier = Modifier.padding(top = 12.dp)) {
                    Text("Hint")
                }
            }
        }
    }
}

@Composable
private fun ChessBoardView(
    board: Array<Char?>,
    selected: Int?,
    wrongSquare: Int?,
    onTap: (Int) -> Unit
) {
    val lightSquare = Color(0xFFE8D3B9)
    val darkSquare = Color(0xFF8E6A4F)
    val selectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    val wrongColor = MaterialTheme.colorScheme.error.copy(alpha = 0.55f)

    Box(
        modifier = Modifier
            .padding(20.dp)
            .aspectRatio(1f)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (uiRow in 0 until 8) {
                val rank = 7 - uiRow
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    for (file in 0 until 8) {
                        val idx = rank * 8 + file
                        val isLight = (rank + file) % 2 == 1
                        val bg = when (idx) {
                            selected -> selectedColor
                            wrongSquare -> wrongColor
                            else -> if (isLight) lightSquare else darkSquare
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(bg)
                                .clickable { onTap(idx) },
                            contentAlignment = Alignment.Center
                        ) {
                            ChessBoard.glyph(board[idx])?.let { glyph ->
                                Text(glyph, fontSize = 30.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
