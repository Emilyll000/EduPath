package com.example.edupath_invest

import android.content.Intent
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

object BottomNavHelper {

    fun setup(activity: AppCompatActivity, currentScreen: String) {
        val navHome = activity.findViewById<LinearLayout>(R.id.navHome)
        val navCycle = activity.findViewById<LinearLayout>(R.id.navCycle)
        val navPensum = activity.findViewById<LinearLayout>(R.id.navPensum)
        val navProfile = activity.findViewById<LinearLayout>(R.id.navProfile)

        val iconHome = activity.findViewById<ImageView>(R.id.iconHome)
        val iconCycle = activity.findViewById<ImageView>(R.id.iconCycle)
        val iconPensum = activity.findViewById<ImageView>(R.id.iconPensum)
        val iconProfile = activity.findViewById<ImageView>(R.id.iconProfile)

        val activeColor = ContextCompat.getColor(activity, R.color.nav_active)
        val inactiveColor = ContextCompat.getColor(activity, R.color.nav_inactive)

        iconHome.setColorFilter(if (currentScreen == "home") activeColor else inactiveColor)
        iconCycle.setColorFilter(if (currentScreen == "cycle") activeColor else inactiveColor)
        iconPensum.setColorFilter(if (currentScreen == "pensum") activeColor else inactiveColor)
        iconProfile.setColorFilter(if (currentScreen == "profile") activeColor else inactiveColor)

        navHome.setOnClickListener {
            if (currentScreen != "home") {
                activity.startActivity(Intent(activity, DashboardActivity::class.java))
                activity.finish()
            }
        }

        navCycle.setOnClickListener {
            if (currentScreen != "cycle") {
                activity.startActivity(Intent(activity, CicloActivity::class.java))
                activity.finish()
            }
        }

        navPensum.setOnClickListener {
            if (currentScreen != "pensum") {
                activity.startActivity(Intent(activity, PensumActivity::class.java))
                activity.finish()
            }
        }

        navProfile.setOnClickListener {
            if (currentScreen != "profile") {
                activity.startActivity(Intent(activity, PerfilActivity::class.java))
                activity.finish()
            }
        }
    }
}