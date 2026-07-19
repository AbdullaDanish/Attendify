package com.abdulla.nsspda.student.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.student.data.local.Student


@Composable
fun StudentList(
    students: List<Student>,
    onDeleteStudent: (Student) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            bottom = 104.dp
        ),
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = students,
            key = { student ->
                student.id
            }
        ) { student ->
            StudentCard(
                student = student,
                onDelete = {
                    onDeleteStudent(student)
                }
            )
        }
    }
}