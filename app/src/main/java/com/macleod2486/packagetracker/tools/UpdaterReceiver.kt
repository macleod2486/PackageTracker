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
package com.macleod2486.packagetracker.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class UpdaterReceiver : BroadcastReceiver() {
    var scheduledWorkRequest: PeriodicWorkRequest? = null
    override fun onReceive(arg0: Context, arg1: Intent) {
        Log.i("UpdaterReceiver", "")
        if (arg1.toString().contains(Intent.ACTION_BOOT_COMPLETED)) {
            var minutes = PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS
            val preferences = PreferenceManager.getDefaultSharedPreferences(arg0)
            val multiplier = preferences.getString("freq", "1")
            minutes = minutes * multiplier!!.toInt()
            val scheduledWorkRequestBuild = PeriodicWorkRequest.Builder(TrackingUpdater::class.java, minutes, TimeUnit.MILLISECONDS)
            scheduledWorkRequestBuild.addTag("PackageTrackerUpdater")
            scheduledWorkRequest = scheduledWorkRequestBuild.build()
            WorkManager.getInstance().enqueueUniquePeriodicWork("PackageTrackerUpdater", ExistingPeriodicWorkPolicy.REPLACE, scheduledWorkRequest!!)
            Log.i("UpdaterReceiver", "PackageTracker Service started")
        }
    }
}