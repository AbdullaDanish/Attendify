package com.abdulla.nsspda.attendance.presentation.percentage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.presentation.percentage.components.AttendanceAnalyticsEmptyState
import com.abdulla.nsspda.attendance.presentation.percentage.components.AttendanceAnalyticsError
import com.abdulla.nsspda.attendance.presentation.percentage.components.AttendanceAnalyticsInitialState
import com.abdulla.nsspda.attendance.presentation.percentage.components.AttendanceAnalyticsLoading
import com.abdulla.nsspda.attendance.presentation.percentage.components.AttendanceDateRangeCard
import com.abdulla.nsspda.attendance.presentation.percentage.components.AttendanceDistributionCard
import com.abdulla.nsspda.attendance.presentation.percentage.components.AttendanceFilteredEmptyState
import com.abdulla.nsspda.attendance.presentation.percentage.components.AttendanceOverviewSection
import com.abdulla.nsspda.attendance.presentation.percentage.components.AttendanceStudentFilters
import com.abdulla.nsspda.attendance.presentation.percentage.components.AttendanceTrendCard
import com.abdulla.nsspda.attendance.presentation.percentage.components.StudentAttendanceProgressCard
import com.abdulla.nsspda.common.presentation.components.AppDatePickerDialog
import java.time.LocalDate

private enum class PercentageDatePickerTarget {
    START_DATE,
    END_DATE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendancePercentageScreen(
    uiState: AttendancePercentageUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (AttendancePercentageIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var datePickerTarget by rememberSaveable {
        mutableStateOf<PercentageDatePickerTarget?>(null)
    }

    var isSortMenuExpanded by rememberSaveable {
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
                            text = "Attendance analytics",
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
                        enabled =
                            !uiState.isLoading &&
                                    !uiState.isExporting,
                        onClick = {
                            onIntent(
                                AttendancePercentageIntent.BackClicked
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
                actions = {
                    IconButton(
                        enabled =
                            uiState.hasData &&
                                    !uiState.isLoading &&
                                    !uiState.isExporting,
                        onClick = {
                            onIntent(
                                AttendancePercentageIntent.ExportClicked
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription =
                                "Export attendance analytics"
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
        AttendancePercentageContent(
            uiState = uiState,
            contentPadding = innerPadding,
            isSortMenuExpanded = isSortMenuExpanded,
            onSortMenuExpandedChange = {
                isSortMenuExpanded = it
            },
            onStartDateClicked = {
                datePickerTarget =
                    PercentageDatePickerTarget.START_DATE
            },
            onEndDateClicked = {
                datePickerTarget =
                    PercentageDatePickerTarget.END_DATE
            },
            onIntent = onIntent
        )
    }

    when (datePickerTarget) {
        PercentageDatePickerTarget.START_DATE -> {
            AppDatePickerDialog(
                initialDate = uiState.startDate,
                maximumDate = LocalDate.now(),
                onDismissRequest = {
                    datePickerTarget = null
                },
                onDateSelected = { date ->
                    datePickerTarget = null

                    onIntent(
                        AttendancePercentageIntent.StartDateSelected(
                            date = date
                        )
                    )
                }
            )
        }


        PercentageDatePickerTarget.END_DATE -> {
            AppDatePickerDialog(
                initialDate = uiState.endDate,
                maximumDate = LocalDate.now(),
                onDismissRequest = {
                    datePickerTarget = null
                },
                onDateSelected = { date ->
                    datePickerTarget = null

                    onIntent(
                        AttendancePercentageIntent.EndDateSelected(
                            date = date
                        )
                    )
                }
            )
        }

        null -> Unit
    }

    if (uiState.isExporting) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                // Export cannot be cancelled from this dialog.
            },
            title = {
                Text("Creating analytics report")
            },
            text = {
                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )

                    Text(
                        "Preparing summary, student and trend sheets…"
                    )
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun AttendancePercentageContent(
    uiState: AttendancePercentageUiState,
    contentPadding: PaddingValues,
    isSortMenuExpanded: Boolean,
    onSortMenuExpandedChange: (Boolean) -> Unit,
    onStartDateClicked: () -> Unit,
    onEndDateClicked: () -> Unit,
    onIntent: (AttendancePercentageIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top =
                contentPadding.calculateTopPadding() +
                        16.dp,
            end = 16.dp,
            bottom =
                contentPadding.calculateBottomPadding() +
                        24.dp
        ),
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {
        item(
            key = "date-range"
        ) {
            AttendanceDateRangeCard(
                uiState = uiState,
                onStartDateClicked =
                    onStartDateClicked,
                onEndDateClicked =
                    onEndDateClicked,
                onCalculate = {
                    onIntent(
                        AttendancePercentageIntent.CalculateClicked
                    )
                }
            )
        }

        when {
            uiState.isLoading -> {
                item(
                    key = "loading"
                ) {
                    AttendanceAnalyticsLoading()
                }
            }

            uiState.errorMessage != null -> {
                item(
                    key = "error"
                ) {
                    AttendanceAnalyticsError(
                        message = uiState.errorMessage,
                        onRetry = {
                            onIntent(
                                AttendancePercentageIntent.RetryClicked
                            )
                        }
                    )
                }
            }

            !uiState.hasCalculated -> {
                item(
                    key = "initial"
                ) {
                    AttendanceAnalyticsInitialState()
                }
            }

            !uiState.hasData -> {
                item(
                    key = "empty"
                ) {
                    AttendanceAnalyticsEmptyState()
                }
            }

            else -> {
                val analytics =
                    requireNotNull(uiState.analytics)

                item(
                    key = "overview"
                ) {
                    AttendanceOverviewSection(
                        analytics = analytics
                    )
                }

                item(
                    key = "trend"
                ) {
                    AttendanceTrendCard(
                        trend = analytics.trend
                    )
                }

                item(
                    key = "distribution"
                ) {
                    AttendanceDistributionCard(
                        distribution =
                            analytics.distribution
                    )
                }

                item(
                    key = "filters"
                ) {
                    AttendanceStudentFilters(
                        uiState = uiState,
                        isSortMenuExpanded =
                            isSortMenuExpanded,
                        onSortMenuExpandedChange =
                            onSortMenuExpandedChange,
                        onIntent = onIntent
                    )
                }

                if (uiState.filteredStudents.isEmpty()) {
                    item(
                        key = "filtered-empty"
                    ) {
                        AttendanceFilteredEmptyState()
                    }
                } else {
                    item(
                        key = "students-heading"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Student attendance",
                                style =
                                    MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text =
                                    "${uiState.filteredStudents.size} students",
                                style =
                                    MaterialTheme.typography.labelLarge,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }
                    }

                    items(
                        items = uiState.filteredStudents,
                        key = { student ->
                            student.usn
                        }
                    ) { student ->
                        StudentAttendanceProgressCard(
                            student = student
                        )
                    }
                }
            }
        }
    }
}