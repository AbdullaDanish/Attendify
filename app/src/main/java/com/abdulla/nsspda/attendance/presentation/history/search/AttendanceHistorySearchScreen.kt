package com.abdulla.nsspda.attendance.presentation.history.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.presentation.history.components.AttendanceSessionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHistorySearchScreen(
    uiState: AttendanceHistorySearchUiState,
    onIntent: (AttendanceHistorySearchIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = uiState.query,
                        onQueryChange = { query ->
                            onIntent(
                                AttendanceHistorySearchIntent
                                    .QueryChanged(query)
                            )
                        },
                        onSearch = {
                            focusManager.clearFocus()
                        },
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = {
                            Text(
                                "Search date, day, month or year"
                            )
                        },
                        leadingIcon = {
                            IconButton(
                                onClick = onNavigateBack
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Go back"
                                )
                            }
                        },
                        trailingIcon = {
                            if (uiState.query.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        onIntent(
                                            AttendanceHistorySearchIntent
                                                .QueryCleared
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector =
                                            Icons.Default.Close,
                                        contentDescription =
                                            "Clear search"
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector =
                                        Icons.Default.Search,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                },
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
            ) {}
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.Center
                ) {
                    Text(
                        text = "Unable to search attendance",
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = uiState.errorMessage,
                        modifier = Modifier.padding(
                            top = 8.dp
                        ),
                        style =
                            MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    TextButton(
                        onClick = {
                            onIntent(
                                AttendanceHistorySearchIntent
                                    .RetryClicked
                            )
                        }
                    ) {
                        Text("Try again")
                    }
                }
            }

            uiState.hasNoResults -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    Text(
                        text = "No attendance found",
                        modifier = Modifier.padding(
                            top = 12.dp
                        ),
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text =
                            "Try another date, day, month or year.",
                        modifier = Modifier.padding(
                            top = 4.dp
                        ),
                        style =
                            MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 12.dp,
                        end = 16.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    item(
                        key = "result-count"
                    ) {
                        Text(
                            text = when {
                                uiState.query.isBlank() ->
                                    "All recorded sessions"

                                uiState.filteredSessions.size == 1 ->
                                    "1 result"

                                else ->
                                    "${uiState.filteredSessions.size} results"
                            },
                            style =
                                MaterialTheme.typography.labelLarge,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }

                    items(
                        items = uiState.filteredSessions,
                        key = { session ->
                            session.date.toEpochDay()
                        }
                    ) { session ->
                        AttendanceSessionCard(
                            item = session,
                            onClick = {
                                onIntent(
                                    AttendanceHistorySearchIntent
                                        .SessionClicked(
                                            session.date
                                        )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}