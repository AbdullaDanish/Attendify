package com.abdulla.nsspda.student.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddStudentDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        studentName: String,
        usn: String
    ) -> Unit
) {
    var studentName by rememberSaveable {
        mutableStateOf("")
    }

    var usn by rememberSaveable {
        mutableStateOf("")
    }

    var hasAttemptedSubmit by rememberSaveable {
        mutableStateOf(false)
    }

    val focusManager = LocalFocusManager.current

    val trimmedName = studentName.trim()
    val normalizedUsn = usn
        .trim()
        .uppercase()
        .filterNot(Char::isWhitespace)

    val isNameInvalid =
        hasAttemptedSubmit &&
                trimmedName.isBlank()

    val isUsnInvalid =
        hasAttemptedSubmit &&
                normalizedUsn.isBlank()

    val canSubmit =
        trimmedName.isNotBlank() &&
                normalizedUsn.isNotBlank() &&
                !isSubmitting

    fun submit() {
        hasAttemptedSubmit = true

        if (!canSubmit) {
            return
        }

        focusManager.clearFocus()

        onSubmit(
            trimmedName,
            normalizedUsn
        )
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) {
                onDismiss()
            }
        },
        title = {
            Text("Add student")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = studentName,
                    onValueChange = { value ->
                        studentName = value
                            .replace(
                                regex = Regex("\\s{2,}"),
                                replacement = " "
                            )
                    },
                    enabled = !isSubmitting,
                    label = {
                        Text("Student name")
                    },
                    placeholder = {
                        Text("Enter full name")
                    },
                    singleLine = true,
                    isError = isNameInvalid,
                    supportingText = {
                        if (isNameInvalid) {
                            Text(
                                "Student name is required."
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization =
                            KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(
                                FocusDirection.Down
                            )
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = usn,
                    onValueChange = { value ->
                        usn = value
                            .uppercase()
                            .filterNot(Char::isWhitespace)
                    },
                    enabled = !isSubmitting,
                    label = {
                        Text("USN")
                    },
                    placeholder = {
                        Text("For example, 2PD21CS001")
                    },
                    singleLine = true,
                    isError = isUsnInvalid,
                    supportingText = {
                        if (isUsnInvalid) {
                            Text("USN is required.")
                        } else {
                            Text(
                                "USN must be unique within this class."
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization =
                            KeyboardCapitalization.Characters,
                        keyboardType =
                            KeyboardType.Ascii,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            submit()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = {
                    submit()
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