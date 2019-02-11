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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;

import java.util.concurrent.TimeUnit;

public class UpdaterReceiver extends BroadcastReceiver
{
    public PeriodicWorkRequest scheduledWorkRequest;

    @Override
    public void onReceive(Context arg0, Intent arg1)
    {
        Log.i("UpdaterReceiver","");

        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(arg0.getApplicationContext());

        if(arg1.toString().contains(Intent.ACTION_BOOT_COMPLETED))
        {
            long minutes = 5;

            PeriodicWorkRequest.Builder scheduledWorkRequestBuild = new PeriodicWorkRequest.Builder(TrackingUpdater.class, minutes, TimeUnit.MINUTES);
            scheduledWorkRequest = scheduledWorkRequestBuild.build();
            WorkManager.getInstance().enqueue(scheduledWorkRequest);

            Log.i("UpdaterReceiver","PackageTracker Service started");
        }
    }
}