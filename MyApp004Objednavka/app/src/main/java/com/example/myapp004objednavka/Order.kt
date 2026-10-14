package com.example.myapp004objednavka

import android.content.Context

// jeden zaznam objednavky v databazi
// bike: 1, 2 nebo 3 (podle zvoleneho kola), doplnky jako true/false
data class Order(
    val id: Long = 0,
    val bike: Int,
    val fork: Boolean,
    val saddle: Boolean,
    val handlebar: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

// nazev kola v aktualnim jazyce aplikace
fun Order.bikeName(context: Context): String = context.getString(
    when (bike) {
        2 -> R.string.rbbike2
        3 -> R.string.rbbike3
        else -> R.string.rbbike1
    }
)

// doplnky oddelene carkou, nebo " ..." kdyz zadne nejsou
fun Order.extrasText(context: Context): String {
    val selectedOptions = mutableListOf<String>()
    if (fork) selectedOptions.add(context.getString(R.string.lepsi_vidlice))
    if (saddle) selectedOptions.add(context.getString(R.string.lepsdi_sedlo))
    if (handlebar) selectedOptions.add(context.getString(R.string.handle))

    return if (selectedOptions.isNotEmpty()) {
        selectedOptions.joinToString(", ")
    } else {
        context.getString(R.string.no_opts)
    }
}
