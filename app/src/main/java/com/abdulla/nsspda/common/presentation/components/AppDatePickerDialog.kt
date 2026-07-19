package com.abdulla.nsspda.common.presentation.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    maximumDate: LocalDate? = null
) {
    val initialDateMillis = initialDate
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

    val maximumDateMillis = maximumDate
        ?.plusDays(1)
        ?.atStartOfDay(ZoneOffset.UTC)
        ?.toInstant()
        ?.toEpochMilli()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(
                utcTimeMillis: Long
            ): Boolean {
                return maximumDateMillis == null ||
                        utcTimeMillis < maximumDateMillis
            }

            override fun isSelectableYear(
                year: Int
            ): Boolean {
                return maximumDate == null ||
                        year <= maximumDate.year
            }
        }
    )

    DatePickerDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedDateMillis =
                        datePickerState.selectedDateMillis
                            ?: return@TextButton

                    val selectedDate = Instant
                        .ofEpochMilli(selectedDateMillis)
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()

                    onDateSelected(selectedDate)
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = true
        )
    }
}