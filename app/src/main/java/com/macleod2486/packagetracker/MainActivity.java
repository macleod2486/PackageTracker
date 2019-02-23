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

package com.macleod2486.packagetracker;

import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.ListenableFuture;
import com.macleod2486.packagetracker.fragments.Main;
import com.macleod2486.packagetracker.tools.TrackingUpdater;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
{
    FragmentManager manager;
    Fragment Main;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = getSupportFragmentManager();
        Main = new Main();

        manager.beginTransaction().replace(R.id.main, Main, "Main").commit();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        Log.i("Start","On start");

        WorkManager manager = WorkManager.getInstance();
        ListenableFuture<List<WorkInfo>> list = manager.getWorkInfosByTag("PackageTrackerUpdater");

        if(list == null)
        {
            long minutes = 10;

            PeriodicWorkRequest scheduledWorkRequest;
            PeriodicWorkRequest.Builder scheduledWorkRequestBuild = new PeriodicWorkRequest.Builder(TrackingUpdater.class, minutes, TimeUnit.MINUTES);
            scheduledWorkRequestBuild.addTag("PackageTrackerUpdater");
            scheduledWorkRequest = scheduledWorkRequestBuild.build();
            manager.enqueue(scheduledWorkRequest);
        }
    }
}
