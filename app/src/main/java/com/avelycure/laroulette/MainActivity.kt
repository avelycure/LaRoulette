package com.avelycure.laroulette

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    lateinit var roulette: Roulette
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        roulette = findViewById(R.id.roulette)
        roulette.setData(listOf("Action", "Drama", "Comedy", "Horror", "TvShow", "Cartoon", "War", "History"))
    }
}