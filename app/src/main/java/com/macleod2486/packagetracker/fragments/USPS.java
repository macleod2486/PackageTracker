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

package com.macleod2486.packagetracker.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.macleod2486.packagetracker.R;
import com.macleod2486.packagetracker.tools.USPSApi;

public class USPS extends Fragment
{
    String api;
    Button addUSPSTracking;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View uspsView = inflater.inflate(R.layout.fragment_usps, container, false);
        api = uspsView.getResources().getString(R.string.USPSAPI);

        final EditText text = uspsView.findViewById(R.id.trackingEntry);

        addUSPSTracking = uspsView.findViewById(R.id.addUSPS);
        addUSPSTracking.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Main main = new Main();
                final FragmentManager manager = getActivity().getSupportFragmentManager();

                String userId = uspsView.getResources().getString(R.string.USPSApiUserID);
                final String trackingIDs = text.getText().toString();
                final USPSApi apiTool = new USPSApi(userId, api);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String result = apiTool.getTrackingInfo(trackingIDs);
                        Log.i("USPS","Result: "+result);
                        manager.beginTransaction().replace(R.id.main, main, "Main").commit();
                    }
                }).start();
            }
        });

        Log.i("USPS","Api "+api);

        return uspsView;
    }
}
