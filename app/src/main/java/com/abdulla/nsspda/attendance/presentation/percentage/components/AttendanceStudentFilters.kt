package com.abdulla.nsspda.attendance.presentation.percentage.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.domain.model.AttendanceRiskLevel
import com.abdulla.nsspda.attendance.presentation.percentage.AttendancePercentageIntent
import com.abdulla.nsspda.attendance.presentation.percentage.AttendancePercentageUiState
import com.abdulla.nsspda.attendance.presentation.percentage.AttendanceStudentSort

@Composable
fun AttendanceStudentFilters(
    uiState: AttendancePercentageUiState,
    isSortMenuExpanded: Boolean,
    onSortMenuExpandedChange: (Boolean) -> Unit,
    onIntent: (AttendancePercentageIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { query ->
                    onIntent(
                        AttendancePercentageIntent.SearchChanged(
                            query = query
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("Search students")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        rememberScrollState()
                    ),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                RiskFilterChip(
                    label = "All",
                    selected =
                        uiState.selectedRiskLevel == null,
                    onClick = {
                        onIntent(
                            AttendancePercentageIntent
                                .RiskFilterSelected(null)
                        )
                    }
                )

                AttendanceRiskLevel.entries
                    .filter {
                        it != AttendanceRiskLevel.NO_DATA
                    }
                    .forEach { riskLevel ->
                        RiskFilterChip(
                            label =
                                riskLevel.displayName(),
                            selected =
                                uiState.selectedRiskLevel ==
                                        riskLevel,
                            onClick = {
                                onIntent(
                                    AttendancePercentageIntent
                                        .RiskFilterSelected(
                                            riskLevel
                                        )
                                )
                            }
                        )
                    }
            }

            Box {
                TextButton(
                    onClick = {
                        onSortMenuExpandedChange(true)
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.size(8.dp)
                    )

                    Text(
                        "Sort: ${uiState.sort.displayName()}"
                    )
                }

                DropdownMenu(
                    expanded = isSortMenuExpanded,
                    onDismissRequest = {
                        onSortMenuExpandedChange(false)
                    }
                ) {
                    AttendanceStudentSort.entries.forEach {
                            sort ->

                        DropdownMenuItem(
                            text = {
                                Text(sort.displayName())
                            },
                            onClick = {
                                onSortMenuExpandedChange(false)

                                onIntent(
                                    AttendancePercentageIntent
                                        .SortSelected(sort)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RiskFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(label)
        },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector =
                        Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            null
        }
    )
}