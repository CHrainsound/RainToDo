// User.kt
package com.example.raintodo.repository

data class User(
    val id: Int = 0,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val createdAt: Long = System.currentTimeMillis()
)