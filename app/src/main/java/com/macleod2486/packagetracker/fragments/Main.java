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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import com.macleod2486.packagetracker.PackageTrackerApplication;
import com.macleod2486.packagetracker.R;
import com.macleod2486.packagetrackerusps.USPSManager;

public class Main extends Fragment
{
    View main;
    ArrayList<String> entries;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        main = inflater.inflate(R.layout.content_main, container, false);

        Button add = main.findViewById(R.id.add);
        add.setOnClickListener((View view) ->
        {
            PackageTrackerApplication.navController.navigate(R.id.action_main2_to_USPS2);
        });

        USPSManager manager = new USPSManager(getContext(), "USPS", null,1);
        entries = manager.getEntries();
        manager.close();
        if(entries.size() == 0) entries.add("No current entries");

        ListView entryList = main.findViewById(R.id.entries);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1, entries);
        entryList.setAdapter(arrayAdapter);
        entryList.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
        {
            if(!entries.contains("No current entries"))
            {
                Bundle bundle = new Bundle();
                bundle.putString("trackingnumber", entries.get(position));
                PackageTrackerApplication.navController.navigate(R.id.action_main2_to_USPSDetail, bundle);
            }

        });

        entryList.setOnItemLongClickListener((AdapterView<?> parent, View view, int position, long id) ->
        {
            if(!entries.contains("No current entries"))
            {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                alertBuilder.setMessage("Delete "+entries.get(position));
                alertBuilder.setPositiveButton("Yes", ((DialogInterface dialog, int which) ->
                {
                    USPSManager tempManager = new USPSManager(getContext(), "USPS", null,1);
                    tempManager.deleteEntryAndHistory(entries.get(position));
                    ArrayList<String> tempEntries = tempManager.getEntries();
                    if(tempEntries.size() == 0) tempEntries.add("No current entries");
                    this.entries = tempEntries;
                    tempManager.close();
                    ArrayAdapter<String> tempAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1, tempEntries);
                    entryList.setAdapter(tempAdapter);
                    tempManager.close();
                }));

                alertBuilder.setNegativeButton("No", ((DialogInterface dialog, int which) ->
                {

                }));

                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            }
            return true;
        });

        return main;
    }
}
