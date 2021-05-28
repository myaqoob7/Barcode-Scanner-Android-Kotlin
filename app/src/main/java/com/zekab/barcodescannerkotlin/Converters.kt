package com.zekab.barcodescannerkotlin

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

object Converters {

    fun getFromBarcode(value: String): ArrayList<ScanPrintItem> {
        val listType = object : TypeToken<ArrayList<ScanPrintItem>>() {}.type
        return Gson().fromJson(value, listType)
    }

    fun setToBarcode(scanPrintList: ArrayList<ScanPrintItem>): String {
        val gson = Gson()
        return gson.toJson(scanPrintList)
    }
}