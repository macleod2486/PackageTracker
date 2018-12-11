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

import android.content.Intent;
import android.support.v4.app.JobIntentService;

import com.macleod2486.packagetracker.R;

public class TrackingUpdater extends JobIntentService
{
    @Override
    protected void onHandleWork(Intent intent)
    {
        String userId = getApplicationContext().getResources().getString(R.string.USPSApiUserID);
        String api = getApplicationContext().getResources().getString(R.string.USPSAPI);;
        USPSApi apiTool = new USPSApi(userId, api, getApplicationContext());


        //Update the entries once completed.

    }
}
