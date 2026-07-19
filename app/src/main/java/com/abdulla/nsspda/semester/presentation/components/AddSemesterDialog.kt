package com.abdulla.nsspda.semester.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddSemesterDialog(
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

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) {
                onDismiss()
            }
        },
        title = {
            Text("Add class")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {
                EditableDropdownMenuWithLabel(
                    label =
                        "Semester and section",
                    options = semesters,
                    selectedOption =
                        selectedSemester,
                    onOptionSelected = {
                        selectedSemester = it
                    }
                )

                EditableDropdownMenuWithLabel(
                    label = "Branch",
                    options = branches,
                    selectedOption =
                        selectedBranch,
                    onOptionSelected = {
                        selectedBranch = it
                    }
                )

                EditableDropdownMenuWithLabel(
                    label = "Subject",
                    options = subjects,
                    selectedOption =
                        selectedSubject,
                    onOptionSelected = {
                        selectedSubject = it
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSubmit,
                onClick = {
                    onSubmit(
                        selectedSemester,
                        selectedBranch,
                        selectedSubject
                    )
                }
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Add")
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}