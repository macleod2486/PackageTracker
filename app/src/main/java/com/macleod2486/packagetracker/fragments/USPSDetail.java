/*
 *
 *   PackageTracker
 *    a simple application for keeping track of multiple packages from multiple services
 *
 *    Copyright (C) 2019  Manuel Gonzales Jr.
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.macleod2486.packagetracker.PackageTrackerApplication;
import com.macleod2486.packagetracker.R;
import com.macleod2486.packagetrackerusps.USPSManager;

import java.util.ArrayList;

public class USPSDetail extends Fragment
{
    String trackingNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View uspsDetailView = inflater.inflate(R.layout.fragment_usps_detail, container, false);

        trackingNumber = getArguments().getString("trackingnumber");

        if(trackingNumber != null)
        {
            Toolbar toolbar = uspsDetailView.findViewById(R.id.uspsdetailstoolbar);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
            toolbar.setTitle(trackingNumber);
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
            toolbar.setNavigationOnClickListener((View view) ->
                 {
                     PackageTrackerApplication.navController.popBackStack();
                 }
            );

            USPSManager manager  = new USPSManager(getContext(), "USPS", null, 1);
            ArrayList<String> historyList = manager.getHistoryForDisplay(trackingNumber);
            manager.close();
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, historyList);
            ListView statusList = uspsDetailView.findViewById(R.id.history);
            statusList.setAdapter(arrayAdapter);
        }

        return uspsDetailView;
    }
}
