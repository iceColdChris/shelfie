package com.shelfie.feature.books.data.remote

import com.shelfie.feature.books.data.remote.dto.OpenLibraryResponseDto
import com.shelfie.feature.books.data.remote.dto.WorkDetailDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface for the Open Library API.
 */
internal interface OpenLibraryApi {

    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "key,title,author_name,cover_i,subject"
    ): OpenLibraryResponseDto

    @GET("works/{workId}.json")
    suspend fun getWorkDetail(@Path("workId") workId: String): WorkDetailDto
}
