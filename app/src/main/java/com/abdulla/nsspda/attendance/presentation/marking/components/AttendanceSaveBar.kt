package com.abdulla.nsspda.attendance.presentation.marking.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.presentation.marking.AttendanceMarkingUiState
import com.abdulla.nsspda.ui.theme.AppSpacing

@Composable
fun AttendanceSaveBar(
    uiState: AttendanceMarkingUiState,
    onSave: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = AppSpacing.ScreenHorizontal,
                    vertical = AppSpacing.Medium
                ),
            verticalArrangement =
                Arrangement.spacedBy(AppSpacing.Small)
        ) {
            if (
                uiState.hasExistingAttendance &&
                uiState.hasUnsavedChanges
            ) {
                Text(
                    text = "You have unsaved changes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                enabled = uiState.canSave,
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color =
                            MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.size(
                            AppSpacing.Small
                        )
                    )

                    Text(
                        text =
                            if (uiState.hasExistingAttendance) {
                                "Update attendance"
                            } else {
                                "Save attendance"
                            },
                        style =
                            MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}