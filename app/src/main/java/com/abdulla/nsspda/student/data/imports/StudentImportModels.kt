package com.abdulla.nsspda.student.data.imports

data class SpreadsheetMetadata(
    val displayName: String,
    val sizeBytes: Long?,
    val mimeType: String?
)

data class SheetSummary(
    val index: Int,
    val name: String,
    val firstRowIndex: Int,
    val lastRowIndex: Int
)

data class SpreadsheetColumn(
    val index: Int,
    val displayName: String
)

data class ColumnCandidate(
    val columnIndex: Int,
    val headerValue: String,
    val confidence: Float
)

data class HeaderDetectionResult(
    val sheetIndex: Int,
    val sheetName: String,
    val headerRowIndex: Int,
    val availableColumns: List<SpreadsheetColumn>,
    val nameColumn: ColumnCandidate?,
    val usnColumn: ColumnCandidate?,
    val confidence: Float
) {
    val isAutomaticMappingReliable: Boolean
        get() {
            return nameColumn != null &&
                    usnColumn != null &&
                    nameColumn.columnIndex != usnColumn.columnIndex &&
                    confidence >= 0.80f
        }
}

data class StudentImportMapping(
    val sheetIndex: Int,
    val headerRowIndex: Int,
    val nameColumnIndex: Int,
    val usnColumnIndex: Int,
    val detectionConfidence: Float = 0f
)

data class ParsedStudentRow(
    val sourceRowNumber: Int,
    val studentName: String,
    val usn: String
)

enum class InvalidStudentReason {
    MISSING_NAME,
    MISSING_USN,
    INVALID_NAME,
    INVALID_USN,
    DUPLICATE_USN_IN_FILE
}
data class InvalidStudentRow(
    val sourceRowNumber: Int,
    val rawName: String,
    val rawUsn: String,
    val reason: InvalidStudentReason
)

data class StudentImportPreview(
    val metadata: SpreadsheetMetadata,
    val sheets: List<SheetSummary>,
    val mapping: StudentImportMapping,
    val detectedConfidence: Float,
    val validRows: List<ParsedStudentRow>,
    val invalidRows: List<InvalidStudentRow>
) {
    val previewRows: List<ParsedStudentRow>
        get() = validRows.take(10)

    val duplicateInsideFileCount: Int
        get() = invalidRows.count {
            it.reason ==
                    InvalidStudentReason.DUPLICATE_USN_IN_FILE
        }

    val invalidDataCount: Int
        get() = invalidRows.size - duplicateInsideFileCount
}