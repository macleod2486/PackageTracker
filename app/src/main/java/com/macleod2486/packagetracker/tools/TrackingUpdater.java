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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.macleod2486.packagetracker.R;

import java.util.ArrayList;
import java.util.HashMap;

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
        USPSApi apiTool = new USPSApi(userId, getApplicationContext());

        ArrayList<String> trackingNumbers = apiTool.getTrackingNumbers();

        for(String trackingNumber: trackingNumbers)
        {
            apiTool.updateHistory(trackingNumber);
        }

        HashMap<String, ArrayList<String>> newEntries = apiTool.getNewEntries();

        if(newEntries != null && newEntries.size() > 0)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
                CharSequence name = getApplicationContext().getString(R.string.NotificationCategoryName);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                String channel_id = TrackingUpdater.class.toString();
                NotificationChannel channel = new NotificationChannel(channel_id, name, importance);
                notificationManager.createNotificationChannel(channel);

                for(String trackingNumber: trackingNumbers)
                {
                    ArrayList<String> entryDetails = newEntries.get(trackingNumber);

                    if(entryDetails != null && entryDetails.size() > 0)
                    {
                        Notification.InboxStyle style = new Notification.InboxStyle();
                        for(String entryDetail: entryDetails)
                        {
                            Log.i("TrackingUpdater", entryDetail.split(",")[3]);
                            style.addLine(entryDetail.split(",")[3]);
                        }

                        Notification notification = new Notification.Builder(getApplicationContext(), channel_id).setSmallIcon(R.mipmap.ic_launcher_round)
                                .setContentTitle(trackingNumber)
                                .setStyle(style).build();

                        notificationManager.notify(channel_id, 0, notification);
                    }

                }

            }

        }

        apiTool.closeDatabase();

        return Result.success();
    }
}
