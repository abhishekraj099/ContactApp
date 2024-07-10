package com.example.newcontactapp.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.newcontactapp.data.database.Contact


data class ContactState(
    val contacts: List<Contact> = emptyList(),
    val id: MutableState<Int> = mutableStateOf(0),
    val name: MutableState<String> = mutableStateOf(""),
    val number: MutableState<String> = mutableStateOf(""),
    val email: MutableState<String> = mutableStateOf(""),
    val dateOfCreation: MutableState<Long> = mutableStateOf(0),
    val image: MutableState<ByteArray?> = mutableStateOf(null),
    val searchQuery: MutableState<String> = mutableStateOf(""),
)