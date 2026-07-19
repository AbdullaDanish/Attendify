package com.abdulla.nsspda.attendance.data.export

import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook

internal class AttendanceWorkbookStyles(
    workbook: XSSFWorkbook
) {

    val titleStyle: XSSFCellStyle =
        workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER

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
                    color = IndexedColors.WHITE.index
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

    val presentStyle: XSSFCellStyle =
        workbook.createCellStyle().apply {
            alignment =
                HorizontalAlignment.CENTER

            verticalAlignment =
                VerticalAlignment.CENTER

            fillForegroundColor =
                IndexedColors.LIGHT_GREEN.index

            fillPattern =
                FillPatternType.SOLID_FOREGROUND

            setFont(
                workbook.createFont().apply {
                    bold = true
                }
            )

            addBorders()
        }

    val absentStyle: XSSFCellStyle =
        workbook.createCellStyle().apply {
            alignment =
                HorizontalAlignment.CENTER

            verticalAlignment =
                VerticalAlignment.CENTER

            fillForegroundColor =
                IndexedColors.ROSE.index

            fillPattern =
                FillPatternType.SOLID_FOREGROUND

            setFont(
                workbook.createFont().apply {
                    bold = true
                }
            )

            addBorders()
        }

    private fun XSSFCellStyle.addBorders() {
        borderTop = BorderStyle.THIN
        borderBottom = BorderStyle.THIN
        borderLeft = BorderStyle.THIN
        borderRight = BorderStyle.THIN
    }
}