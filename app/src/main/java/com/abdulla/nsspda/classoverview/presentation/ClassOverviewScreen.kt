package com.abdulla.nsspda.classoverview.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassOverviewScreen(
    uiState: ClassOverviewUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ClassOverviewIntent) -> Unit,
    modifier: Modifier = Modifier
) {
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
                            text = "Class overview",
                            maxLines = 1
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
                        enabled = !uiState.isLoading,
                        onClick = {
                            onIntent(
                                ClassOverviewIntent.BackClicked
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
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface
                    )
            )
        }
    ) { innerPadding ->
        ClassOverviewContent(
            uiState = uiState,
            contentPadding = innerPadding,
            onIntent = onIntent
        )
    }
}

@Composable
private fun ClassOverviewContent(
    uiState: ClassOverviewUiState,
    contentPadding: PaddingValues,
    onIntent: (ClassOverviewIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(
                rememberScrollState()
            )
            .padding(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {
        ClassInformationCard(
            semester = uiState.semester,
            branch = uiState.branch,
            subject = uiState.subject
        )

        when {
            uiState.isLoading -> {
                ClassOverviewLoadingCard()
            }

            uiState.errorMessage != null -> {
                ClassOverviewErrorCard(
                    message = uiState.errorMessage,
                    onRetry = {
                        onIntent(
                            ClassOverviewIntent.RetryClicked
                        )
                    }
                )
            }

            else -> {
                StudentSummaryCard(
                    uiState = uiState,
                    onClick = {
                        onIntent(
                            ClassOverviewIntent
                                .ManageStudentsClicked
                        )
                    }
                )

                AttendanceSummaryCard(
                    enabled = uiState.canOpenAttendance,
                    hasStudents = uiState.hasStudents,
                    onClick = {
                        onIntent(
                            ClassOverviewIntent
                                .AttendanceHistoryClicked
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ClassInformationCard(
    semester: String,
    branch: String,
    subject: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = subject.ifBlank {
                    "Class"
                },
                style =
                    MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color =
                    MaterialTheme.colorScheme
                        .onPrimaryContainer
            )

            HorizontalDivider(
                color =
                    MaterialTheme.colorScheme
                        .onPrimaryContainer
                        .copy(alpha = 0.16f)
            )

            ClassInformationRow(
                label = "Semester",
                value = semester
            )

            ClassInformationRow(
                label = "Branch",
                value = branch
            )
        }
    }
}

@Composable
private fun ClassInformationRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style =
                MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme
                    .onPrimaryContainer
                    .copy(alpha = 0.75f),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value.ifBlank {
                "Not available"
            },
            style =
                MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color =
                MaterialTheme.colorScheme
                    .onPrimaryContainer
        )
    }
}


@Composable
private fun StudentSummaryCard(
    uiState: ClassOverviewUiState,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color =
                        MaterialTheme.colorScheme
                            .secondaryContainer
                ) {
                    Icon(
                        imageVector = if (uiState.hasStudents) {
                            Icons.Default.Person
                        } else {
                            Icons.Default.Add
                        },
                        contentDescription = null,
                        tint =
                            MaterialTheme.colorScheme
                                .onSecondaryContainer,
                        modifier = Modifier.padding(13.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Students",
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = uiState.studentCountLabel,
                        style =
                            MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color =
                            MaterialTheme.colorScheme.primary
                    )
                }

                FilledTonalIconButton(
                    onClick = onClick
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.ChevronRight,
                        contentDescription =
                            uiState.studentActionLabel
                    )
                }
            }

            Text(
                text = if (uiState.hasStudents) {
                    "View the roster, add students, import an Excel file or remove students."
                } else {
                    "No students have been added to this class. Add or import students before taking attendance."
                },
                style =
                    MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
            ) {
                Icon(
                    imageVector = if (uiState.hasStudents) {
                        Icons.Default.Person
                    } else {
                        Icons.Default.Add
                    },
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                Text(uiState.studentActionLabel)
            }
        }
    }
}


@Composable
private fun AttendanceSummaryCard(
    enabled: Boolean,
    hasStudents: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = (if (enabled) {
                    "Open attendance history"
                } else {
                    "Attendance unavailable. Add students first."
                    disabled()
                }) as String
            }
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (enabled) {
                1.dp
            } else {
                0.dp
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (enabled) {
                        MaterialTheme.colorScheme
                            .tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme
                            .surfaceVariant
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = if (enabled) {
                            MaterialTheme.colorScheme
                                .onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                        },
                        modifier = Modifier.padding(13.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Attendance",
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = if (hasStudents) {
                            "History, marking and analytics"
                        } else {
                            "Add students to continue"
                        },
                        style =
                            MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }

                Icon(
                    imageVector =
                        Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                    }
                )
            }

            Text(
                text = if (hasStudents) {
                    "View previous attendance sessions, take new attendance and calculate student percentages."
                } else {
                    "Attendance becomes available after at least one student is added to this class."
                },
                style =
                    MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            OutlinedButton(
                enabled = enabled,
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                Text("Open attendance history")
            }
        }
    }
}

@Composable
private fun ClassOverviewLoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator()

            Text(
                text = "Loading class information…",
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ClassOverviewErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Unable to load class",
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color =
                    MaterialTheme.colorScheme
                        .onErrorContainer
            )

            Text(
                text = message,
                style =
                    MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onErrorContainer
            )

            TextButton(
                onClick = onRetry
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                Text("Try again")
            }
        }
    }
}

