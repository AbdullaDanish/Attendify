package com.abdulla.nsspda.semester.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import com.abdulla.nsspda.ui.theme.AppAnimation
import com.abdulla.nsspda.ui.theme.AppSpacing
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassSelectionField(
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    options: List<String>,
    selectedOption: String,
    enabled: Boolean,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    customOptionLabel: String = "Custom"
) {
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    var isCustomSelected by rememberSaveable {
        mutableStateOf(false)
    }

    var customValue by rememberSaveable {
        mutableStateOf("")
    }

    val focusRequester = remember {
        FocusRequester()
    }

    val keyboardController =
        LocalSoftwareKeyboardController.current

    LaunchedEffect(isCustomSelected) {
        if (isCustomSelected) {
            delay(100)
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(AppSpacing.Small)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                if (enabled) {
                    expanded = !expanded
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = when {
                    isCustomSelected &&
                            customValue.isNotBlank() -> customValue

                    isCustomSelected -> customOptionLabel

                    else -> selectedOption
                },
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                singleLine = true,
                label = {
                    Text(label)
                },
                placeholder = {
                    Text(placeholder)
                },
                leadingIcon = {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                shape = MaterialTheme.shapes.medium,
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
                    val isSelected =
                        !isCustomSelected &&
                                option.equals(
                                    selectedOption,
                                    ignoreCase = true
                                )

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                maxLines = 1,
                                overflow =
                                    TextOverflow.Ellipsis
                            )
                        },
                        trailingIcon = {
                            if (isSelected) {
                                Icon(
                                    imageVector =
                                        Icons.Default.Check,
                                    contentDescription = null,
                                    tint =
                                        MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        onClick = {
                            isCustomSelected = false
                            customValue = ""

                            onOptionSelected(
                                option.trim()
                            )

                            expanded = false
                            keyboardController?.hide()
                        }
                    )
                }

                HorizontalDivider()

                DropdownMenuItem(
                    text = {
                        Text(customOptionLabel)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector =
                                Icons.Default.Edit,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (isCustomSelected) {
                            Icon(
                                imageVector =
                                    Icons.Default.Check,
                                contentDescription = null,
                                tint =
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    onClick = {
                        isCustomSelected = true
                        customValue = ""
                        onOptionSelected("")
                        expanded = false
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = isCustomSelected,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = AppAnimation.Normal
                )
            ) + expandVertically(
                animationSpec = tween(
                    durationMillis = AppAnimation.Normal
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = AppAnimation.Fast
                )
            ) + shrinkVertically(
                animationSpec = tween(
                    durationMillis = AppAnimation.Fast
                )
            )
        ) {
            OutlinedTextField(
                value = customValue,
                onValueChange = { value ->
                    val uppercaseValue =
                        value.uppercase()

                    customValue = uppercaseValue

                    onOptionSelected(
                        uppercaseValue.trim()
                    )
                },
                enabled = enabled,
                singleLine = true,
                label = {
                    Text(customOptionLabel)
                },
                placeholder = {
                    Text("Type your value")
                },
                leadingIcon = {
                    Icon(
                        imageVector =
                            Icons.Default.Edit,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization =
                        KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        }
    }
}