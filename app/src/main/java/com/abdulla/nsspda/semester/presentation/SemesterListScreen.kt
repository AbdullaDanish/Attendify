package com.abdulla.nsspda.semester.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import com.abdulla.nsspda.semester.presentation.components.AddSemesterSheet
import com.abdulla.nsspda.semester.presentation.components.DeleteSemesterDialog
import com.abdulla.nsspda.semester.presentation.components.GreetingWithWavingHand
import com.abdulla.nsspda.semester.presentation.components.SemesterContent
import com.abdulla.nsspda.semester.presentation.components.rememberGreeting
import com.abdulla.nsspda.ui.theme.AppSpacing

private const val PRIVACY_POLICY_URL =
    "https://sites.google.com/view/acadence/home"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterListScreen(
    uiState: SemesterUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (SemesterIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val greeting = rememberGreeting()
    val uriHandler = LocalUriHandler.current

    var isMenuExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor =
            MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(
                                AppSpacing.XSmall
                            )
                    ) {
                        Text(
                            text = "Acadence",
                            style =
                                MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color =
                                MaterialTheme.colorScheme.onSurface
                        )

                        if (uiState.isEmpty) {
                            Text(
                                text = "Academic attendance",
                                style =
                                    MaterialTheme.typography
                                        .labelMedium,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        } else {
                            GreetingWithWavingHand(
                                greeting = greeting.text,
                                style =
                                    MaterialTheme.typography
                                        .labelMedium
                            )
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = {
                                isMenuExpanded = true
                            }
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Default.MoreVert,
                                contentDescription =
                                    "More options"
                            )
                        }

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = {
                                isMenuExpanded = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text("Privacy policy")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector =
                                            Icons.Default.PrivacyTip,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    isMenuExpanded = false

                                    uriHandler.openUri(
                                        PRIVACY_POLICY_URL
                                    )
                                }
                            )
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface,
                        scrolledContainerColor =
                            MaterialTheme.colorScheme.surface
                    )
            )
        },
        floatingActionButton = {
            if (
                !uiState.isLoading &&
                !uiState.isEmpty
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        onIntent(
                            SemesterIntent.AddClassClicked
                        )
                    },
                    containerColor =
                        MaterialTheme.colorScheme.primary,
                    contentColor =
                        MaterialTheme.colorScheme.onPrimary,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = "Add class",
                            style =
                                MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }
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
        AddSemesterSheet(
            isSubmitting = uiState.isSubmitting,
            onDismiss = {
                onIntent(
                    SemesterIntent.AddDialogDismissed
                )
            },
            onSubmit = {
                    semester,
                    branch,
                    subject ->

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