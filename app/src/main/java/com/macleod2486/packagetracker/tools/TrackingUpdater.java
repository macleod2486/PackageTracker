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

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.macleod2486.packagetracker.R;

import java.util.ArrayList;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class TrackingUpdater extends Worker
{
    public TrackingUpdater(@NonNull Context context, @NonNull WorkerParameters params)
    {
        super(context, params);
    }

    @Override
    public Result doWork()
    {
        Log.i("TrackingUpdater","Doing work");

        String userId = getApplicationContext().getResources().getString(R.string.USPSApiUserID);
        String api = getApplicationContext().getResources().getString(R.string.USPSAPI);
        USPSApi apiTool = new USPSApi(userId, api, getApplicationContext());

        ArrayList<String> trackingNumbers = apiTool.getTrackingNumbers();

        for(String trackingNumber: trackingNumbers)
        {
            apiTool.updateHistory(trackingNumber);
        }

        return Result.success();
    }
}
