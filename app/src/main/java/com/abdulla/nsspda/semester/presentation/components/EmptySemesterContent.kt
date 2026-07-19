package com.abdulla.nsspda.semester.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.ui.theme.AppSpacing

@Composable
fun EmptySemesterContent(
    onAddClassClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = AppSpacing.ScreenHorizontal,
                vertical = AppSpacing.Large
            ),
        verticalArrangement =
            Arrangement.spacedBy(AppSpacing.Section)
    ) {
        AcadenceDashboardHeader()

        EmptyClassSection(
            onAddClassClick = onAddClassClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AcadenceDashboardHeader(
    modifier: Modifier = Modifier
) {
    val greeting = rememberGreeting()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(AppSpacing.Medium)
    ) {
        GreetingWithWavingHand(
            greeting = greeting.text,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Ready to organize today’s academic work?",
            style = MaterialTheme.typography.bodyLarge,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        LiveAcademicMessage()
    }
}

@Composable
private fun EmptyClassSection(
    onAddClassClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = AppSpacing.Medium
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(96.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color =
                MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint =
                        MaterialTheme.colorScheme
                            .onSecondaryContainer
                )
            }
        }

        Text(
            text = "No classes yet",
            modifier = Modifier.padding(
                top = AppSpacing.XLarge
            ),
            style =
                MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color =
                MaterialTheme.colorScheme.onBackground
        )

        Text(
            text =
                "Create a class to add students, record attendance and view progress.",
            modifier = Modifier.padding(
                top = AppSpacing.Small,
                start = AppSpacing.Large,
                end = AppSpacing.Large
            ),
            style =
                MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onAddClassClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = AppSpacing.Section
                )
                .heightIn(
                    min = 52.dp
                ),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )

            Text(
                text = "Create class",
                modifier = Modifier.padding(
                    start = AppSpacing.Small
                ),
                style =
                    MaterialTheme.typography.labelLarge
            )
        }
    }
}