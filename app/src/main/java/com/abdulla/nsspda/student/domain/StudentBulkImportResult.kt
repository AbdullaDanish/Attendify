package com.abdulla.nsspda.student.domain

data class StudentBulkImportResult(
    val requestedCount: Int,
    val importedCount: Int,
    val existingStudentCount: Int
) {
    val wasAnythingImported: Boolean
        get() = importedCount > 0
}