package com.shelfie.core.database.converter

import androidx.room3.ColumnTypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * Room type converters for storing complex types in the database.
 */
internal class StringListConverter {

    private val moshi: Moshi = Moshi.Builder().build()
    private val listAdapter: JsonAdapter<List<String>> = moshi.adapter(
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    @ColumnTypeConverter
    fun fromStringList(value: List<String>?): String {
        return listAdapter.toJson(value ?: emptyList())
    }

    @ColumnTypeConverter
    fun toStringList(value: String): List<String> {
        return listAdapter.fromJson(value) ?: emptyList()
    }
}
