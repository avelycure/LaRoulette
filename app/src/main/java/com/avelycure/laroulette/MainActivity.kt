package com.avelycure.laroulette

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.laroullete.Roulette

class MainActivity : AppCompatActivity() {
    lateinit var roulette: Roulette
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        roulette = findViewById(R.id.roulette)
        roulette.setData(listOf("Action", "Drama", "Comedy", "Horror", "TvShow", "Cartoon", "War", "History"))

        Log.d("mytag", mainLooper.hashCode().toString())
    }
}