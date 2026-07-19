package com.abdulla.nsspda.attendance.data.export

import com.abdulla.nsspda.attendance.domain.model.AttendanceRiskLevel
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook

internal class AnalyticsWorkbookStyles(
    private val workbook: XSSFWorkbook
) {

    val titleStyle: XSSFCellStyle =
        workbook.createCellStyle().apply {
            alignment =
                HorizontalAlignment.CENTER

            verticalAlignment =
                VerticalAlignment.CENTER

            setFont(
                workbook.createFont().apply {
                    bold = true
                    fontHeightInPoints = 18
                }
            )
        }

    val infoLabelStyle: XSSFCellStyle =
        workbook.createCellStyle().apply {
            fillForegroundColor =
                IndexedColors.GREY_25_PERCENT.index

            fillPattern =
                FillPatternType.SOLID_FOREGROUND

            setFont(
                workbook.createFont().apply {
                    bold = true
                }
            )

            addBorders()
        }

    val infoValueStyle: XSSFCellStyle =
        workbook.createCellStyle().apply {
            addBorders()
        }

    val headerStyle: XSSFCellStyle =
        workbook.createCellStyle().apply {
            fillForegroundColor =
                IndexedColors.DARK_BLUE.index

            fillPattern =
                FillPatternType.SOLID_FOREGROUND

            alignment =
                HorizontalAlignment.CENTER

            verticalAlignment =
                VerticalAlignment.CENTER

            setFont(
                workbook.createFont().apply {
                    bold = true
                    color =
                        IndexedColors.WHITE.index
                }
            )

            addBorders()
        }

    val bodyStyle: XSSFCellStyle =
        workbook.createCellStyle().apply {
            verticalAlignment =
                VerticalAlignment.CENTER

            wrapText = true

            addBorders()
        }

    val bodyCenteredStyle: XSSFCellStyle =
        workbook.createCellStyle().apply {
            alignment =
                HorizontalAlignment.CENTER

            verticalAlignment =
                VerticalAlignment.CENTER

            addBorders()
        }

    val percentageStyle: XSSFCellStyle =
        workbook.createCellStyle().apply {
            alignment =
                HorizontalAlignment.CENTER

            verticalAlignment =
                VerticalAlignment.CENTER

            dataFormat =
                workbook.creationHelper
                    .createDataFormat()
                    .getFormat("0.0%")

            addBorders()
        }

    private val excellentStyle:
            XSSFCellStyle =
        createRiskStyle(
            backgroundColor =
                IndexedColors.LIGHT_GREEN
        )

    private val goodStyle:
            XSSFCellStyle =
        createRiskStyle(
            backgroundColor =
                IndexedColors.LIGHT_CORNFLOWER_BLUE
        )

    private val warningStyle:
            XSSFCellStyle =
        createRiskStyle(
            backgroundColor =
                IndexedColors.LIGHT_YELLOW
        )

    private val criticalStyle:
            XSSFCellStyle =
        createRiskStyle(
            backgroundColor =
                IndexedColors.ROSE
        )

    private val noDataStyle:
            XSSFCellStyle =
        createRiskStyle(
            backgroundColor =
                IndexedColors.GREY_25_PERCENT
        )

    fun riskStyle(
        riskLevel: AttendanceRiskLevel
    ): XSSFCellStyle {
        return when (riskLevel) {
            AttendanceRiskLevel.EXCELLENT ->
                excellentStyle

            AttendanceRiskLevel.GOOD ->
                goodStyle

            AttendanceRiskLevel.WARNING ->
                warningStyle

            AttendanceRiskLevel.CRITICAL ->
                criticalStyle

            AttendanceRiskLevel.NO_DATA ->
                noDataStyle
        }
    }

    private fun createRiskStyle(
        backgroundColor: IndexedColors
    ): XSSFCellStyle {
        return workbook.createCellStyle().apply {
            alignment =
                HorizontalAlignment.CENTER

            verticalAlignment =
                VerticalAlignment.CENTER

            fillForegroundColor =
                backgroundColor.index

            fillPattern =
                FillPatternType.SOLID_FOREGROUND

            setFont(
                workbook.createFont().apply {
                    bold = true
                }
            )

            addBorders()
        }
    }

    private fun XSSFCellStyle.addBorders() {
        borderTop = BorderStyle.THIN
        borderBottom = BorderStyle.THIN
        borderLeft = BorderStyle.THIN
        borderRight = BorderStyle.THIN
    }
}