package com.eventexperts.eventandticketmanagementsystem

import android.app.Application
import com.eventexperts.eventandticketmanagementsystem.di.AppContainer

class EventApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
