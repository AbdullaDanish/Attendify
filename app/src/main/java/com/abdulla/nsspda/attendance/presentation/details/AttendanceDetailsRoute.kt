package com.abdulla.nsspda.attendance.presentation.details

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@Composable
fun AttendanceDetailsRoute(
    navController: NavController,
    viewModel: AttendanceDetailsViewModel = hiltViewModel()
) {
    val uiState by
    viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val context = LocalContext.current
    BackHandler(
        enabled = !uiState.isBusy
    ) {
        viewModel.onIntent(
            AttendanceDetailsIntent.BackClicked
        )
    }

    LaunchedEffect(
        viewModel,
        context
    ) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AttendanceDetailsEffect.NavigateBack -> {
                    navController.navigateUp()
                }

                is AttendanceDetailsEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message
                    )
                }

                is AttendanceDetailsEffect
                .ShareExportedFile -> {

                    val shareIntent =
                        Intent(Intent.ACTION_SEND).apply {
                            type =
                                effect.mimeType

                            putExtra(
                                Intent.EXTRA_STREAM,
                                effect.uri
                            )

                            putExtra(
                                Intent.EXTRA_TITLE,
                                effect.fileName
                            )

                            addFlags(
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )

                            clipData =
                                android.content.ClipData.newUri(
                                    context.contentResolver,
                                    effect.fileName,
                                    effect.uri
                                )
                        }

                    val chooser =
                        Intent.createChooser(
                            shareIntent,
                            "Share attendance report"
                        )

                    runCatching {
                        context.startActivity(
                            chooser
                        )
                    }.onFailure {
                        snackbarHostState.showSnackbar(
                            message =
                                "No compatible app was found to share the report."
                        )
                    }
                }
            }
        }
    }

    AttendanceDetailsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent
    )
}