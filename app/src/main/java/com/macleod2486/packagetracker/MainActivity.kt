/*
 *
 *   PackageTracker
 *    a simple application for keeping track of multiple packages from multiple services
 *
 *    Copyright (C) 2018  Manuel Gonzales Jr.
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see [http://www.gnu.org/licenses/].
 *
 */
package com.macleod2486.packagetracker

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.macleod2486.packagetracker.tools.TrackingUpdater
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    var manager: FragmentManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        manager = supportFragmentManager
        supportActionBar!!.setTitle(R.string.app_name)
        PackageTrackerApplication.navHost = manager!!.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        PackageTrackerApplication.navController = PackageTrackerApplication.navHost.navController
    }

    public override fun onStart() {
        super.onStart()
        val manager = WorkManager.getInstance()
        Log.i("MainActivity", "Starting scheduled process")
        var minutes = PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val multiplier = preferences.getString("freq", "1")
        minutes = minutes * multiplier!!.toInt()
        val scheduledWorkRequestBuild = PeriodicWorkRequest.Builder(TrackingUpdater::class.java, minutes, TimeUnit.MILLISECONDS)
        scheduledWorkRequestBuild.addTag("PackageTrackerUpdater")
        val scheduledWorkRequest = scheduledWorkRequestBuild.build()
        manager.enqueueUniquePeriodicWork("PackageTrackerUpdater", ExistingPeriodicWorkPolicy.KEEP, scheduledWorkRequest)
    }

    public override fun onStop() {
        super.onStop()
        val manager = WorkManager.getInstance()
        Log.i("MainActivity", "Starting scheduled process")
        var minutes = PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val multiplier = preferences.getString("freq", "1")
        minutes = minutes * multiplier!!.toInt()
        val scheduledWorkRequestBuild = PeriodicWorkRequest.Builder(TrackingUpdater::class.java, minutes, TimeUnit.MILLISECONDS)
        scheduledWorkRequestBuild.addTag("PackageTrackerUpdater")
        val scheduledWorkRequest = scheduledWorkRequestBuild.build()
        manager.enqueueUniquePeriodicWork("PackageTrackerUpdater", ExistingPeriodicWorkPolicy.KEEP, scheduledWorkRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 1, "Settings")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (PackageTrackerApplication.navController.currentDestination!!.id != R.id.settings) PackageTrackerApplication.navController.navigate(R.id.action_main2_to_settings)
        return true
    }
}