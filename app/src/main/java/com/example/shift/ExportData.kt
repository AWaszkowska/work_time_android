package com.example.shift
import com.alibaba.excel.annotation.ExcelProperty

data class ExportData(
    @ExcelProperty("ID")
    var id: Long? = null,

    @ExcelProperty("Username")
    var username: String? = null,

    @ExcelProperty("Localization")
    var localization: String? = null,

    @ExcelProperty("Timestamp")
    var timestamp: String? = null
)
