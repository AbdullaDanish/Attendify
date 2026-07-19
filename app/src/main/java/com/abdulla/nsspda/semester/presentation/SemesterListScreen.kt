package com.abdulla.nsspda.semester.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.abdulla.nsspda.semester.data.Semester
import com.abdulla.nsspda.semester.presentation.components.AddSemesterDialog
import com.abdulla.nsspda.semester.presentation.components.DeleteSemesterDialog
import com.abdulla.nsspda.semester.presentation.components.SemesterContent
import com.abdulla.nsspda.ui.theme.NSSPDATheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterListScreen(
    uiState: SemesterUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (SemesterIntent) -> Unit,
    onNavigateBack: () -> Unit,
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
            TopAppBar(
                title = {
                    Text("ATTENDIFY")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack
                    ) {
                        Icon(
                            imageVector =
                                Icons.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    onIntent(
                        SemesterIntent.AddClassClicked
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text("Add class")
                }
            )
        }
    ) { paddingValues ->
        SemesterContent(
            uiState = uiState,
            onIntent = onIntent,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }

    if (uiState.isAddDialogVisible) {
        AddSemesterDialog(
            isSubmitting = uiState.isSubmitting,
            onDismiss = {
                onIntent(
                    SemesterIntent.AddDialogDismissed
                )
            },
            onSubmit = { semester, branch, subject ->
                onIntent(
                    SemesterIntent.AddSemesterSubmitted(
                        semester = semester,
                        branch = branch,
                        subject = subject
                    )
                )
            }
        )
    }

    uiState.semesterToDelete?.let { semester ->
        DeleteSemesterDialog(
            semester = semester,
            isSubmitting = uiState.isSubmitting,
            onConfirm = {
                onIntent(
                    SemesterIntent.DeleteConfirmed
                )
            },
            onDismiss = {
                onIntent(
                    SemesterIntent.DeleteDismissed
                )
            }
        )
    }
}

private data class SemesterDisplayValue(
    val number: String,
    val section: String
)

private fun Semester.toDisplayValue(): SemesterDisplayValue {
    val normalized = semester.trim()

    return SemesterDisplayValue(
        number = normalized.firstOrNull()
            ?.toString()
            .orEmpty(),
        section = normalized
            .drop(1)
            .ifBlank { "Not specified" }
    )
}


@Preview(showBackground = true, showSystemUi = true, apiLevel = 33, device = "id:pixel_7")
@Composable
private fun SemesterListScreenPreview() {
    NSSPDATheme {
        SemesterListScreen(
            uiState = SemesterUiState(
                isLoading = false,
                semesters = listOf(
                    Semester(
                        id = 1,
                        semester = "5A",
                        branch = "CSE",
                        subject = "NSS"
                    )
                )
            ),
            snackbarHostState =
                remember { SnackbarHostState() },
            onIntent = {},
            onNavigateBack = {}
        )
    }
}