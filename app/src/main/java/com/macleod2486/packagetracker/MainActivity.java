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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.macleod2486.packagetracker.tools.TrackingUpdater;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
{
    FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = getSupportFragmentManager();

        getSupportActionBar().setTitle(R.string.app_name);

        PackageTrackerApplication.navHost = (NavHostFragment) manager.findFragmentById(R.id.nav_host_fragment);
        PackageTrackerApplication.navController = PackageTrackerApplication.navHost.getNavController();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        WorkManager manager = WorkManager.getInstance();

        Log.i("MainActivity","Starting scheduled process");

        long minutes = PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String multiplier = preferences.getString("freq", "1");
        minutes = minutes * Integer.parseInt(multiplier);

        PeriodicWorkRequest.Builder scheduledWorkRequestBuild = new PeriodicWorkRequest.Builder(TrackingUpdater.class, minutes, TimeUnit.MILLISECONDS);
        scheduledWorkRequestBuild.addTag("PackageTrackerUpdater");
        PeriodicWorkRequest scheduledWorkRequest = scheduledWorkRequestBuild.build();
        manager.enqueueUniquePeriodicWork("PackageTrackerUpdater", ExistingPeriodicWorkPolicy.KEEP, scheduledWorkRequest);

    }

    @Override
    public void onStop()
    {
        super.onStop();

        WorkManager manager = WorkManager.getInstance();

        Log.i("MainActivity","Starting scheduled process");

        long minutes = PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String multiplier = preferences.getString("freq", "1");
        minutes = minutes * Integer.parseInt(multiplier);

        PeriodicWorkRequest.Builder scheduledWorkRequestBuild = new PeriodicWorkRequest.Builder(TrackingUpdater.class, minutes, TimeUnit.MILLISECONDS);
        scheduledWorkRequestBuild.addTag("PackageTrackerUpdater");
        PeriodicWorkRequest scheduledWorkRequest = scheduledWorkRequestBuild.build();
        manager.enqueueUniquePeriodicWork("PackageTrackerUpdater", ExistingPeriodicWorkPolicy.KEEP, scheduledWorkRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(0, 1, 1, "Settings");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(PackageTrackerApplication.navController.getCurrentDestination().getId() != R.id.settings)
            PackageTrackerApplication.navController.navigate(R.id.action_main2_to_settings);

        return true;
    }

}
