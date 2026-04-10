package com.example.edupath_invest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class CicloActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ciclo)

        BottomNavHelper.setup(this, "cycle")
    }
}