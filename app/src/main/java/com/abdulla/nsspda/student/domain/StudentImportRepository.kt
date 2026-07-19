package com.abdulla.nsspda.student.domain

import android.net.Uri
import com.abdulla.nsspda.student.data.imports.HeaderDetectionResult
import com.abdulla.nsspda.student.data.imports.StudentImportMapping
import com.abdulla.nsspda.student.data.imports.StudentImportPreview

interface StudentImportRepository {

    suspend fun analyzeWorkbook(
        uri: Uri
    ): Result<List<HeaderDetectionResult>>

    suspend fun createPreview(
        uri: Uri,
        mapping: StudentImportMapping
    ): Result<StudentImportPreview>
}