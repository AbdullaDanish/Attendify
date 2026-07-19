package com.abdulla.nsspda.semester.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSemesterSheet(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        semester: String,
        branch: String,
        subject: String
    ) -> Unit
) {
    var selectedSemester by rememberSaveable {
        mutableStateOf("")
    }

    var selectedBranch by rememberSaveable {
        mutableStateOf("")
    }

    var selectedSubject by rememberSaveable {
        mutableStateOf("")
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val semesters = remember {
        listOf(
            "3A", "3B",
            "4A", "4B",
            "5A", "5B",
            "6A", "6B",
            "7A", "7B",
            "8A", "8B"
        )
    }

    val branches = remember {
        listOf(
            "CSE",
            "ECE",
            "ME",
            "CSD",
            "AIML"
        )
    }

    val subjects = remember {
        listOf(
            "NSS",
            "PE",
            "Yoga"
        )
    }

    val canSubmit =
        selectedSemester.isNotBlank() &&
                selectedBranch.isNotBlank() &&
                selectedSubject.isNotBlank() &&
                !isSubmitting

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            if (!isSubmitting) {
                onDismiss()
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        contentWindowInsets = {
            WindowInsets(0)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    start = AppSpacing.ScreenHorizontal,
                    end = AppSpacing.ScreenHorizontal,
                    bottom = AppSpacing.Section
                ),
            verticalArrangement =
                Arrangement.spacedBy(AppSpacing.Large)
        ) {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(AppSpacing.Small)
            ) {
                Text(
                    text = "Create class",
                    style =
                        MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text =
                        "Select the semester, branch and subject for this class.",
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ClassSelectionField(
                label = "Semester and section",
                placeholder = "Select semester and section",
                leadingIcon = Icons.Default.School,
                options = semesters,
                selectedOption = selectedSemester,
                enabled = !isSubmitting,
                customOptionLabel = "Enter custom semester",
                onOptionSelected = {
                    selectedSemester = it
                }
            )

            ClassSelectionField(
                label = "Branch",
                placeholder = "Select branch",
                leadingIcon = Icons.Default.AccountTree,
                options = branches,
                selectedOption = selectedBranch,
                enabled = !isSubmitting,
                customOptionLabel = "Enter custom branch",
                onOptionSelected = {
                    selectedBranch = it
                }
            )

            ClassSelectionField(
                label = "Subject",
                placeholder = "Select subject",
                leadingIcon = Icons.Default.MenuBook,
                options = subjects,
                selectedOption = selectedSubject,
                enabled = !isSubmitting,
                customOptionLabel = "Enter custom subject",
                onOptionSelected = {
                    selectedSubject = it
                }
            )

            Button(
                enabled = canSubmit,
                onClick = {
                    onSubmit(
                        selectedSemester,
                        selectedBranch,
                        selectedSubject
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color =
                            MaterialTheme.colorScheme.onPrimary
                    )
                } else {
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
    }
}

