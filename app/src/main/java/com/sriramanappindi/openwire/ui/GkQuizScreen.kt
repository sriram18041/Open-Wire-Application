package com.sriramanappindi.openwire.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sriramanappindi.openwire.games.GkQuiz
import com.sriramanappindi.openwire.games.GkQuizStore
import com.sriramanappindi.openwire.games.localEpochDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GkQuizScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val epochDay = remember { localEpochDay() }
    val questions = remember(epochDay) { GkQuiz.forDay(epochDay) }
    val store = remember { GkQuizStore(context) }

    var answers by remember { mutableStateOf(store.load(epochDay, questions.size)) }
    var current by remember {
        mutableStateOf(answers.indexOfFirst { it == -1 }.let { if (it == -1) 0 else it })
    }

    val allAnswered = answers.none { it == -1 }
    val score = questions.indices.count { answers[it] == questions[it].correctIndex }

    fun choose(optionIndex: Int) {
        if (answers[current] != -1) return // already locked in, no changing it
        val updated = answers.copyOf()
        updated[current] = optionIndex
        answers = updated
        store.save(epochDay, updated)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                title = { Text("GK Quiz") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            if (allAnswered) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎉", style = MaterialTheme.typography.displayMedium)
                        Text(
                            "You scored $score / ${questions.size} today",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        Text(
                            "Come back tomorrow for a new set of questions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            } else {
                val q = questions[current]
                Text(
                    "Question ${current + 1} of ${questions.size} · ${q.category}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    q.question,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 10.dp, bottom = 20.dp)
                )

                val chosen = answers[current]
                q.options.forEachIndexed { idx, option ->
                    val isCorrect = idx == q.correctIndex
                    val bg = when {
                        chosen == -1 -> MaterialTheme.colorScheme.surfaceVariant
                        isCorrect -> Color(0xFF2E7D4F)
                        idx == chosen -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Card(
                        onClick = { choose(idx) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = bg)
                    ) {
                        Text(
                            option,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                if (chosen != -1 && current < questions.size - 1) {
                    Button(
                        onClick = { current += 1 },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Next question")
                    }
                }
            }
        }
    }
}
