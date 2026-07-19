package com.abdulla.nsspda.attendance.presentation.marking

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.abdulla.nsspda.attendance.presentation.marking.components.AttendanceMarkingDialogs
import com.abdulla.nsspda.attendance.presentation.marking.components.AttendanceMarkingEmpty
import com.abdulla.nsspda.attendance.presentation.marking.components.AttendanceMarkingError
import com.abdulla.nsspda.attendance.presentation.marking.components.AttendanceMarkingList
import com.abdulla.nsspda.attendance.presentation.marking.components.AttendanceMarkingLoading
import com.abdulla.nsspda.attendance.presentation.marking.components.AttendanceSaveBar
import com.abdulla.nsspda.common.presentation.components.AppDatePickerDialog

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AttendanceMarkingScreen(
    uiState: AttendanceMarkingUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (AttendanceMarkingIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Mark attendance",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (uiState.subject.isNotBlank()) {
                            Text(
                                text = uiState.subject,
                                style =
                                    MaterialTheme.typography.labelMedium,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        enabled = !uiState.isSaving,
                        onClick = {
                            onIntent(
                                AttendanceMarkingIntent.BackClicked
                            )
                        }
                    ) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                colors = TopAppBarDefaults
                    .centerAlignedTopAppBarColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface
                    )
            )
        },
        bottomBar = {
            AttendanceSaveBar(
                uiState = uiState,
                onSave = {
                    onIntent(
                        AttendanceMarkingIntent.SaveClicked
                    )
                }
            )
        }
    ) { innerPadding ->
        AttendanceMarkingContent(
            uiState = uiState,
            contentPadding = innerPadding,
            onSelectDate = {
                showDatePicker = true
            },
            onIntent = onIntent
        )
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDate = uiState.selectedDate,
            onDismissRequest = {
                showDatePicker = false
            },
            onDateSelected = { date ->
                showDatePicker = false

                onIntent(
                    AttendanceMarkingIntent.DateSelected(
                        date = date
                    )
                )
            }
        )
    }

    AttendanceMarkingDialogs(
        uiState = uiState,
        onIntent = onIntent
    )
}


@Composable
private fun AttendanceMarkingContent(
    uiState: AttendanceMarkingUiState,
    contentPadding: PaddingValues,
    onSelectDate: () -> Unit,
    onIntent: (AttendanceMarkingIntent) -> Unit
) {
    when {
        uiState.isLoading -> {
            AttendanceMarkingLoading(
                contentPadding = contentPadding
            )
        }

        uiState.errorMessage != null -> {
            AttendanceMarkingError(
                message = uiState.errorMessage,
                contentPadding = contentPadding,
                onRetry = {
                    onIntent(
                        AttendanceMarkingIntent.RetryClicked
                    )
                }
            )
        }

        uiState.isEmpty -> {
            AttendanceMarkingEmpty(
                contentPadding = contentPadding
            )
        }

        else -> {
            AttendanceMarkingList(
                uiState = uiState,
                contentPadding = contentPadding,
                onSelectDate = onSelectDate,
                onIntent = onIntent
            )
        }
    }
}