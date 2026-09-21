package com.spendlimit.ug
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this)
        tv.text = "SpendLimit UG - Loading..."
        tv.textSize = 24f
        tv.setPadding(50,200,50,50)
        setContentView(tv)
    }
}
