package com.abdulla.nsspda.attendance.presentation.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.presentation.history.components.AttendanceHistoryEmpty
import com.abdulla.nsspda.attendance.presentation.history.components.AttendanceHistoryError
import com.abdulla.nsspda.attendance.presentation.history.components.AttendanceHistoryList
import com.abdulla.nsspda.attendance.presentation.history.components.AttendanceHistoryLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHistoryScreen(
    uiState: AttendanceHistoryUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (AttendanceHistoryIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by rememberSaveable {
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
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Attendance history",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (uiState.subject.isNotBlank()) {
                            Text(
                                text = uiState.subject,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme
                                    .onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onIntent(
                                AttendanceHistoryIntent.BackClicked
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = {
                                isMenuExpanded = true
                            },
                            enabled = !uiState.isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = {
                                isMenuExpanded = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text("Attendance analytics")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector =
                                            Icons.Default.Analytics,
                                        contentDescription = null
                                    )
                                },
                                enabled = uiState.isClassValid,
                                onClick = {
                                    isMenuExpanded = false

                                    onIntent(
                                        AttendanceHistoryIntent
                                            .PercentageClicked
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults
                    .centerAlignedTopAppBarColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface
                    )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (
                        uiState.isClassValid &&
                        !uiState.isLoading
                    ) {
                        onIntent(
                            AttendanceHistoryIntent
                                .NewAttendanceClicked
                        )
                    }
                },
                modifier = Modifier
                    .navigationBarsPadding(),
                containerColor =
                    MaterialTheme.colorScheme.primaryContainer,
                contentColor =
                    MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                Text("New attendance")
            }
        }
    ) { innerPadding ->
        AttendanceHistoryContent(
            uiState = uiState,
            onIntent = onIntent,
            contentPadding = innerPadding
        )
    }
}

@Composable
private fun AttendanceHistoryContent(
    uiState: AttendanceHistoryUiState,
    onIntent: (AttendanceHistoryIntent) -> Unit,
    contentPadding: PaddingValues
) {
    when {
        uiState.isLoading -> {
            AttendanceHistoryLoading(
                contentPadding = contentPadding
            )
        }

        uiState.errorMessage != null -> {
            AttendanceHistoryError(
                message = uiState.errorMessage,
                contentPadding = contentPadding,
                onRetry = {
                    onIntent(
                        AttendanceHistoryIntent.RetryClicked
                    )
                }
            )
        }

        uiState.isEmpty -> {
            AttendanceHistoryEmpty(
                semester = uiState.semester,
                branch = uiState.branch,
                subject = uiState.subject,
                contentPadding = contentPadding,
                onCreateAttendance = {
                    onIntent(
                        AttendanceHistoryIntent
                            .NewAttendanceClicked
                    )
                }
            )
        }

        else -> {
            AttendanceHistoryList(
                uiState = uiState,
                contentPadding = contentPadding,
                onSessionClicked = { date ->
                    onIntent(
                        AttendanceHistoryIntent
                            .AttendanceSessionClicked(date)
                    )
                }
            )
        }
    }
}