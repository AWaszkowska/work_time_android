package com.example.shift

import com.alibaba.excel.EasyExcel
import java.io.File

fun exportResultsToExcel(results: List<Result>, filePath: String) {
    // Prepare data for EasyExcel
    val data = results.map { result ->
        ExportData(
            id = result.id,
            username = result.username,
            localization = result.localization,
            timestamp = result.timestamp
        )
    }

    // Write data to Excel file
    EasyExcel.write(filePath, ExportData::class.java)
        .sheet("Results")
        .doWrite(data)
}
