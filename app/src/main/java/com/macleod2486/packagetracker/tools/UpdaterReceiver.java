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

package com.macleod2486.packagetracker.tools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.JobIntentService;
import android.util.Log;

public class UpdaterReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context arg0, Intent arg1)
    {
        Log.i("UpdaterReceiver","");

        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(arg0.getApplicationContext());

        if(arg1.toString().contains(Intent.ACTION_BOOT_COMPLETED))
        {
            //one second * 60 seconds in a minute * minutes
            long interval = 1000*60*5;

            Intent service = new Intent(arg0, UpdaterReceiver.class);
            PendingIntent pendingService = PendingIntent.getBroadcast(arg0, 0, service, 0);
            AlarmManager newsUpdate = (AlarmManager)arg0.getSystemService(arg0.ALARM_SERVICE);

            //Check for the update based on interval
            newsUpdate.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), interval, pendingService);

            Log.i("UpdaterReceiver","PackageTracker Service started");
        }
        else
        {
            //Starting the news update class
            Intent newsUpdate = new Intent(arg0, TrackingUpdater.class);
            JobIntentService.enqueueWork(arg0, TrackingUpdater.class, 1001, newsUpdate);

            Log.i("UpdaterReceiver","Broadcast finished");
        }
    }
}