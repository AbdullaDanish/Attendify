package com.abdulla.nsspda.student.presentation

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

@Composable
fun StudentRoute(
    onNavigateBack: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val uiState by
    viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState =
        remember {
            SnackbarHostState()
        }

    val filePicker =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .OpenDocument()
        ) { uri ->
            if (uri == null) {
                viewModel.onIntent(
                    StudentIntent
                        .ImportFileSelectionCancelled
                )
            } else {
                viewModel.onIntent(
                    StudentIntent
                        .ImportFileSelected(uri)
                )
            }
        }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is StudentEffect.ShowMessage -> {
                    snackbarHostState
                        .showSnackbar(
                            message =
                                effect.message
                        )
                }

                StudentEffect
                    .LaunchExcelFilePicker -> {
                    filePicker.launch(
                        arrayOf(
                            "application/vnd.ms-excel",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "application/octet-stream"
                        )
                    )
                }
            }
        }
    }

    BackHandler(
        enabled = uiState.isSelectionMode
    ) {
        viewModel.onIntent(
            StudentIntent.SelectionModeCancelled
        )
    }
    StudentScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}