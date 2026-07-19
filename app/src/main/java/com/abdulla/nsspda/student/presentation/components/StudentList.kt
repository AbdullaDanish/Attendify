package com.abdulla.nsspda.student.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.student.data.local.Student
import com.abdulla.nsspda.ui.theme.AppSpacing

@Composable
fun StudentList(
    students: List<Student>,
    isSelectionMode: Boolean,
    selectedStudentIds: Set<Long>,
    onStudentSelected: (Student) -> Unit,
    onDeleteStudent: (Student) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            bottom = 104.dp
        ),
        verticalArrangement =
            Arrangement.spacedBy(AppSpacing.Medium)
    ) {
        items(
            items = students,
            key = Student::id
        ) { student ->
            StudentCard(
                student = student,
                isSelectionMode = isSelectionMode,
                isSelected =
                    student.id in selectedStudentIds,
                onClick = {
                    if (isSelectionMode) {
                        onStudentSelected(student)
                    }
                },
                onDelete = {
                    onDeleteStudent(student)
                }
            )
        }
    }
}