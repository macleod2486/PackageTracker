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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.macleod2486.packagetracker.MainActivity
import com.macleod2486.packagetracker.R
import com.macleod2486.packagetrackerusps.USPSApi

class TrackingUpdater(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        Log.i("TrackingUpdater", "Doing work")
        val userId = applicationContext.resources.getString(R.string.USPSApiUserID)
        val apiTool = USPSApi(userId, applicationContext)
        val trackingNumbers = apiTool.trackingNumbers
        for (trackingNumber in trackingNumbers) {
            apiTool.updateHistory(trackingNumber)
        }
        val newEntries = apiTool.newEntries
        if (newEntries != null && newEntries.size > 0) {
            for (trackingNumber in trackingNumbers) {
                val entryDetails = newEntries[trackingNumber]
                if (entryDetails != null && entryDetails.size > 0) {
                    val style = NotificationCompat.InboxStyle()
                    for (entryDetail in entryDetails) {
                        Log.i("TrackingUpdater", entryDetail.split(",").toTypedArray()[3])
                        style.addLine(entryDetail.split(",").toTypedArray()[3])
                    }
                    val mainIntent = Intent(applicationContext, MainActivity::class.java)
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val mainPending = PendingIntent.getActivity(applicationContext, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
                        val name: CharSequence = applicationContext.getString(R.string.NotificationCategoryName)
                        val importance = NotificationManager.IMPORTANCE_DEFAULT
                        val channel_id = TrackingUpdater::class.java.toString()
                        val channel = NotificationChannel(channel_id, name, importance)
                        notificationManager.createNotificationChannel(channel)
                        val notification = NotificationCompat.Builder(applicationContext, channel_id).setSmallIcon(R.mipmap.ic_launcher_round)
                                .setContentTitle(trackingNumber)
                                .setStyle(style)
                                .setAutoCancel(true)
                                .setContentIntent(mainPending)
                                .build()
                        notificationManager.notify(channel_id, 0, notification)
                    } else {
                        val channel_id = TrackingUpdater::class.java.toString()
                        val notificationManager = NotificationManagerCompat.from(applicationContext)
                        val notification = NotificationCompat.Builder(applicationContext, channel_id).setSmallIcon(R.mipmap.ic_launcher_round)
                                .setContentTitle(trackingNumber)
                                .setStyle(style)
                                .setAutoCancel(true)
                                .setContentIntent(mainPending)
                                .build()
                        notificationManager.notify(0, notification)
                    }
                }
            }
        }
        apiTool.closeDatabase()
        return Result.success()
    }
}