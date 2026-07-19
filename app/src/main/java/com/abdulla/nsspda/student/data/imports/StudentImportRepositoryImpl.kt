package com.abdulla.nsspda.student.data.imports

import android.net.Uri
import com.abdulla.nsspda.student.domain.StudentImportRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class StudentImportRepositoryImpl @Inject constructor(
    private val excelStudentParser: ExcelStudentParser
) : StudentImportRepository {

    override suspend fun analyzeWorkbook(
        uri: Uri
    ): Result<List<HeaderDetectionResult>> {
        return try {
            Result.success(
                excelStudentParser.analyze(uri)
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun createPreview(
        uri: Uri,
        mapping: StudentImportMapping
    ): Result<StudentImportPreview> {
        return try {
            Result.success(
                excelStudentParser.parsePreview(
                    uri = uri,
                    mapping = mapping
                )
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}