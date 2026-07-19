package com.abdulla.nsspda.student.presentation.components
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StudentInitialAvatar(
    studentName: String,
    modifier: Modifier = Modifier
) {
    val initial = remember(studentName) {
        studentName
            .trim()
            .firstOrNull()
            ?.uppercase()
            ?: "?"
    }

    Surface(
        modifier = modifier.size(46.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme
            .secondaryContainer
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme
                    .onSecondaryContainer
            )
        }
    }
}