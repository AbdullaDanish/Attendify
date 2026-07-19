package com.abdulla.nsspda.student.data.imports

import java.text.Normalizer
import java.util.Locale

internal object ExcelHeaderDetector {

    private val nameAliases = setOf(
        "name",
        "student name",
        "studentname",
        "name of student",
        "name of the student",
        "student full name",
        "full name",
        "candidate name",
        "learner name",
        "participant name",
        "pupil name"
    ).mapTo(mutableSetOf(), ::normalizeHeader)

    private val usnAliases = setOf(
        "usn",
        "student usn",
        "university seat number",
        "university serial number",
        "university seat no",
        "university seat no.",
        "registration number",
        "registration no",
        "registration no.",
        "register number",
        "register no",
        "reg number",
        "reg no",
        "reg no.",
        "roll number",
        "roll no",
        "roll no.",
        "student id",
        "student identifier",
        "enrollment number",
        "enrolment number",
        "admission number",
        "admission no"
    ).mapTo(mutableSetOf(), ::normalizeHeader)

    private val excludedNameTerms = setOf(
        "father",
        "mother",
        "guardian",
        "faculty",
        "teacher",
        "course",
        "subject",
        "department",
        "college",
        "university",
        "institution",
        "signature",
        "remarks"
    )

    fun scoreNameHeader(value: String): Float {
        val normalized = normalizeHeader(value)

        if (normalized.isBlank()) {
            return 0f
        }

        if (excludedNameTerms.any(normalized::contains)) {
            return 0f
        }

        return scoreAgainstAliases(
            normalized = normalized,
            aliases = nameAliases,
            genericScore = when {
                normalized == "name" -> 1f

                normalized.contains("student") &&
                        normalized.contains("name") -> 0.95f

                normalized.contains("candidate") &&
                        normalized.contains("name") -> 0.90f

                normalized.contains("name") -> 0.70f

                else -> 0f
            }
        )
    }

    fun scoreUsnHeader(value: String): Float {
        val normalized = normalizeHeader(value)

        if (normalized.isBlank()) {
            return 0f
        }

        return scoreAgainstAliases(
            normalized = normalized,
            aliases = usnAliases,
            genericScore = when {
                normalized == "usn" -> 1f

                normalized.contains("usn") -> 0.98f

                containsAny(
                    normalized,
                    "university seat",
                    "register number",
                    "registration number",
                    "roll number",
                    "enrollment number",
                    "enrolment number",
                    "student id",
                    "admission number"
                ) -> 0.88f

                normalized.contains("registration") -> 0.75f

                normalized.contains("register") -> 0.72f

                normalized.contains("roll") &&
                        (
                                normalized.contains("number") ||
                                        normalized.contains("no")
                                ) -> 0.72f

                normalized.contains("enrol") ||
                        normalized.contains("enroll") -> 0.72f

                else -> 0f
            }
        )
    }

    private fun scoreAgainstAliases(
        normalized: String,
        aliases: Set<String>,
        genericScore: Float
    ): Float {
        if (normalized in aliases) {
            return 1f
        }

        val compact = normalized.removeSpaces()

        if (aliases.any { it.removeSpaces() == compact }) {
            return 0.97f
        }

        val tokenScore = aliases
            .maxOfOrNull { alias ->
                tokenSimilarity(
                    first = normalized,
                    second = alias
                )
            }
            ?: 0f

        return maxOf(
            genericScore,
            tokenScore
        ).coerceIn(0f, 1f)
    }

    private fun tokenSimilarity(
        first: String,
        second: String
    ): Float {
        val firstTokens = first
            .split(' ')
            .filter(String::isNotBlank)
            .toSet()

        val secondTokens = second
            .split(' ')
            .filter(String::isNotBlank)
            .toSet()

        if (
            firstTokens.isEmpty() ||
            secondTokens.isEmpty()
        ) {
            return 0f
        }

        val intersection =
            firstTokens.intersect(secondTokens).size

        val union =
            firstTokens.union(secondTokens).size

        if (union == 0) {
            return 0f
        }

        val similarity =
            intersection.toFloat() / union.toFloat()

        return when {
            similarity >= 0.80f -> 0.92f
            similarity >= 0.60f -> 0.82f
            similarity >= 0.40f -> 0.68f
            else -> 0f
        }
    }

    fun normalizeHeader(
        value: String
    ): String {
        val withoutAccents = Normalizer
            .normalize(
                value,
                Normalizer.Form.NFD
            )
            .replace(
                Regex("\\p{M}+"),
                ""
            )

        return withoutAccents
            .trim()
            .lowercase(Locale.ROOT)
            .replace(
                Regex("[\\r\\n\\t]+"),
                " "
            )
            .replace(
                Regex("[_\\-./\\\\:()\\[\\]{}]+"),
                " "
            )
            .replace(
                Regex("[^a-z0-9 ]"),
                ""
            )
            .replace(
                Regex("\\s+"),
                " "
            )
            .trim()
    }

    private fun String.removeSpaces(): String {
        return replace(" ", "")
    }

    private fun containsAny(
        value: String,
        vararg terms: String
    ): Boolean {
        return terms.any(value::contains)
    }
}