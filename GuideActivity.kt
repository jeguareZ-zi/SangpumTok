package com.neonloop.sangpumtok

import android.app.Activity
import android.os.Bundle
import android.widget.Button

class GuideActivity : Activity() {

    private lateinit var btnGuideBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        btnGuideBack = findViewById(R.id.btnGuideBack)
    }

    private fun setupClickListeners() {
        btnGuideBack.setOnClickListener {
            finish()
        }
    }
}