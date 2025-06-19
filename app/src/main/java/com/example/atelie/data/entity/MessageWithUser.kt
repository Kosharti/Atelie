package com.example.atelie.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation

data class MessageWithUser(
    @Embedded
    val message: Message,
    @ColumnInfo(name = "username")
    val username: String
)