package com.abdulla.nsspda.student.data.imports

internal object ExcelHeaderDetector {

    private val nameAliases = setOf(
        "name",
        "student name",
        "studentname",
        "name of student",
        "student full name",
        "full name",
        "candidate name",
        "learner name",
        "participant name",
        "pupil name"
    )

    private val usnAliases = setOf(
        "usn",
        "student usn",
        "university seat number",
        "university serial number",
        "registration number",
        "registration no",
        "registration no.",
        "reg number",
        "reg no",
        "reg no.",
        "roll number",
        "roll no",
        "roll no.",
        "student id",
        "student identifier",
        "enrollment number",
        "enrolment number"
    )

    fun scoreNameHeader(value: String): Float {
        return scoreHeader(
            rawValue = value,
            aliases = nameAliases
        )
    }

    fun scoreUsnHeader(value: String): Float {
        return scoreHeader(
            rawValue = value,
            aliases = usnAliases
        )
    }

    private fun scoreHeader(
        rawValue: String,
        aliases: Set<String>
    ): Float {
        val normalized = normalizeHeader(rawValue)

        if (normalized.isBlank()) {
            return 0f
        }

        if (normalized in aliases) {
            return 1f
        }

        val compact = normalized.replace(" ", "")

        if (
            aliases.any {
                it.replace(" ", "") == compact
            }
        ) {
            return 0.95f
        }

        if (
            aliases.any {
                normalized.contains(it) ||
                        it.contains(normalized)
            }
        ) {
            return 0.75f
        }

        return when {
            normalized.contains("name") &&
                    !normalized.contains("father") &&
                    !normalized.contains("mother") -> 0.65f

            normalized.contains("usn") -> 0.95f

            normalized.contains("roll") &&
                    normalized.contains("no") -> 0.75f

            normalized.contains("registration") -> 0.70f

            normalized.contains("enrol") ||
                    normalized.contains("enroll") -> 0.70f

            else -> 0f
        }
    }

    fun normalizeHeader(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace(
                regex = Regex("[_\\-./]+"),
                replacement = " "
            )
            .replace(
                regex = Regex("[^a-z0-9 ]"),
                replacement = ""
            )
            .replace(
                regex = Regex("\\s+"),
                replacement = " "
            )
    }
}