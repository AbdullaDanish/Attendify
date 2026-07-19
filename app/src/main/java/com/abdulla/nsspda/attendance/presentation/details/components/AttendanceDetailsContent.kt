package com.abdulla.nsspda.attendance.presentation.details.components
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.abdulla.nsspda.attendance.presentation.details.AttendanceDetailsIntent
import com.abdulla.nsspda.attendance.presentation.details.AttendanceDetailsUiState

@Composable
fun AttendanceDetailsContent(
    uiState: AttendanceDetailsUiState,
    contentPadding: PaddingValues,
    onIntent: (AttendanceDetailsIntent) -> Unit
) {
    when {
        uiState.isLoading -> {
            AttendanceDetailsLoading(
                contentPadding = contentPadding
            )
        }

        uiState.errorMessage != null -> {
            AttendanceDetailsError(
                message = uiState.errorMessage,
                contentPadding = contentPadding,
                onRetry = {
                    onIntent(
                        AttendanceDetailsIntent.RetryClicked
                    )
                }
            )
        }

        uiState.isEmpty -> {
            AttendanceDetailsEmpty(
                contentPadding = contentPadding
            )
        }

        else -> {
            AttendanceDetailsList(
                uiState = uiState,
                contentPadding = contentPadding,
                onIntent = onIntent
            )
        }
    }
}