package com.abdulla.nsspda.attendance.presentation.percentage

import android.content.ClipData
import android.content.Intent
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
fun AttendancePercentageRoute(
    navController: NavController,
    viewModel: AttendancePercentageViewModel = hiltViewModel()
) {
    val uiState by
    viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val context = LocalContext.current
    LaunchedEffect(
        viewModel,
        context
    ) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AttendancePercentageEffect.NavigateBack -> {
                    navController.navigateUp()
                }

                is AttendancePercentageEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message
                    )
                }

                is AttendancePercentageEffect
                .ShareExportedFile -> {

                    val shareIntent =
                        Intent(Intent.ACTION_SEND).apply {
                            type = effect.mimeType

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
                                ClipData.newUri(
                                    context.contentResolver,
                                    effect.fileName,
                                    effect.uri
                                )
                        }

                    val chooser =
                        Intent.createChooser(
                            shareIntent,
                            "Share attendance analytics"
                        )

                    runCatching {
                        context.startActivity(
                            chooser
                        )
                    }.onFailure {
                        snackbarHostState.showSnackbar(
                            message =
                                "No compatible application was found."
                        )
                    }
                }
            }
        }
    }

    AttendancePercentageScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent
    )
}