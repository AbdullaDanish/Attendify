package com.abdulla.nsspda.semester.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.unit.dp
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableDropdownMenuWithLabel(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select an option",
    customOptionLabel: String = "Enter a custom option",
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null
) {
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    var customOption by rememberSaveable {
        mutableStateOf("")
    }

    val keyboardController =
        LocalSoftwareKeyboardController.current

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (enabled) {
                expanded = !expanded
            }
        },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = {
                Text(label)
            },
            placeholder = {
                Text(placeholder)
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            isError = isError,
            supportingText = {
                supportingText?.let {
                    Text(text = it)
                }
            },
            colors =
                ExposedDropdownMenuDefaults
                    .outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(
                    type =
                        MenuAnchorType.PrimaryNotEditable,
                    enabled = enabled
                )
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            maxLines = 1,
                            overflow =
                                TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        onOptionSelected(option.trim())
                        customOption = ""
                        expanded = false
                        keyboardController
                            ?.hide()
                    },
                    leadingIcon = {
                        if (
                            option.equals(
                                selectedOption,
                                ignoreCase = true
                            )
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Default.Check,
                                contentDescription = null
                            )
                        }
                    }
                )
            }

            HorizontalDivider()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = customOptionLabel,
                    style =
                        MaterialTheme.typography
                            .labelLarge
                )

                OutlinedTextField(
                    value = customOption,
                    onValueChange = {
                        customOption = it
                    },
                    label = {
                        Text("Custom value")
                    },
                    singleLine = true,
                    enabled = enabled,
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization =
                                KeyboardCapitalization
                                    .Characters,
                            imeAction =
                                ImeAction.Done
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                val normalizedValue =
                                    customOption
                                        .trim()

                                if (
                                    normalizedValue
                                        .isNotEmpty()
                                ) {
                                    onOptionSelected(
                                        normalizedValue
                                    )

                                    customOption = ""
                                    expanded = false
                                    keyboardController
                                        ?.hide()
                                }
                            }
                        ),
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val normalizedValue =
                            customOption.trim()

                        if (
                            normalizedValue
                                .isNotEmpty()
                        ) {
                            onOptionSelected(
                                normalizedValue
                            )

                            customOption = ""
                            expanded = false
                            keyboardController
                                ?.hide()
                        }
                    },
                    enabled =
                        enabled &&
                                customOption
                                    .trim()
                                    .isNotEmpty(),
                    modifier =
                        Modifier.align(
                            Alignment.End
                        )
                ) {
                    Text("Use value")
                }
            }
        }
    }
}