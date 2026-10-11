package com.sriramanappindi.openwire.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sriramanappindi.openwire.games.ChessPuzzleStore
import com.sriramanappindi.openwire.games.GameStreakStore
import com.sriramanappindi.openwire.games.GkQuiz
import com.sriramanappindi.openwire.games.GkQuizStore
import com.sriramanappindi.openwire.games.SudokuStore
import com.sriramanappindi.openwire.games.localEpochDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    onBack: () -> Unit,
    onOpenSudoku: () -> Unit,
    onOpenChess: () -> Unit,
    onOpenGkQuiz: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val epochDay = remember { localEpochDay() }
    val sudokuSolved = SudokuStore.isSolvedToday(context)
    val chessSolved = ChessPuzzleStore.isSolvedToday(context)
    val gkSolved = GkQuizStore.isSolvedToday(context)
    val gkScore = if (gkSolved) GkQuizStore.scoreToday(context) else 0
    val gkQuestionCount = remember { GkQuiz.forDay(epochDay).size }
    val sudokuStreak = remember { GameStreakStore(context, GameStreakStore.SUDOKU).currentStreak(epochDay) }
    val chessStreak = remember { GameStreakStore(context, GameStreakStore.CHESS).currentStreak(epochDay) }
    val gkStreak = remember { GameStreakStore(context, GameStreakStore.GK_QUIZ).currentStreak(epochDay) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                title = { Text("Games") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(PaddingValues(horizontal = 16.dp, vertical = 12.dp)),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GameCard(
                emoji = "🔢",
                title = "Sudoku",
                subtitle = "A new puzzle every day — same one for everyone",
                solved = sudokuSolved,
                streak = sudokuStreak,
                onClick = onOpenSudoku
            )
            GameCard(
                emoji = "♞",
                title = "Chess puzzle",
                subtitle = "White to move — find the mate in one",
                solved = chessSolved,
                streak = chessStreak,
                onClick = onOpenChess
            )
            GameCard(
                emoji = "🧠",
                title = "GK Quiz",
                subtitle = "$gkQuestionCount quick general-knowledge questions, new every day",
                solved = gkSolved,
                streak = gkStreak,
                solvedSubtitle = "Scored $gkScore / $gkQuestionCount today — come back tomorrow",
                onClick = onOpenGkQuiz
            )
        }
    }
}

@Composable
private fun GameCard(
    emoji: String,
    title: String,
    subtitle: String,
    solved: Boolean,
    onClick: () -> Unit,
    streak: Int = 0,
    solvedSubtitle: String = "Solved today — come back tomorrow"
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$emoji  $title" + if (solved) "  ✅" else "",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (streak > 0) {
                    Text(
                        "🔥 $streak",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                if (solved) solvedSubtitle else subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
