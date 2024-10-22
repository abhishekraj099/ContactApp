package com.example.newcontactapp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Upsert
    suspend fun upsertContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("SELECT * FROM contact_table ORDER BY name ASC")
    fun getContactSortedByName(): Flow<List<Contact>>

    @Query("SELECT * FROM contact_table ORDER BY dateOfCreation DESC")
    fun getContactSortedByDate(): Flow<List<Contact>>

    @Query("SELECT * FROM contact_table WHERE name LIKE :query OR number LIKE :query OR email LIKE :query ORDER BY name ASC")
    fun searchContactsSortedByName(query: String): Flow<List<Contact>>

    @Query("SELECT * FROM contact_table WHERE name LIKE :query OR number LIKE :query OR email LIKE :query ORDER BY dateOfCreation DESC")
    fun searchContactsSortedByDate(query: String): Flow<List<Contact>>
}