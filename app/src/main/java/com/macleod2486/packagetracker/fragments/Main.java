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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;

import com.macleod2486.packagetracker.R;
import com.macleod2486.packagetracker.tools.USPSManager;

public class Main extends Fragment
{
    View main;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        main = inflater.inflate(R.layout.content_main, container, false);

        final Spinner vendorsDropdown = (Spinner) main.findViewById(R.id.vendorsDropdown);

        ArrayList<String> listOfVendors = new ArrayList<String>();
        listOfVendors.addAll(Arrays.asList(getResources().getStringArray(R.array.vendors)));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line, listOfVendors);

        vendorsDropdown.setAdapter(adapter);

        Button add = (Button)main.findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                String selection = vendorsDropdown.getSelectedItem().toString();

                FragmentManager manager = getActivity().getSupportFragmentManager();

                switch(selection)
                {
                    case "USPS":
                    {
                        Fragment USPS = new USPS();
                        manager.beginTransaction().replace(R.id.main, USPS, "USPS").addToBackStack(null).commit();
                    }
                }

            }
        });

        USPSManager manager = new USPSManager(getContext(), "USPS", null,1);
        ArrayList<String> entries = manager.getEntries();
        manager.close();
        if(entries.size() == 0) entries.add("No current entries");

        ListView entryList = main.findViewById(R.id.entries);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1, entries);
        entryList.setAdapter(arrayAdapter);
        entryList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(entries.size() > 0)
                {
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    Fragment USPSDetail = new USPSDetail();
                    ((USPSDetail) USPSDetail).trackingNumber = entries.get(position);
                    manager.beginTransaction().replace(R.id.main, USPSDetail, "USPSDetail").addToBackStack(null).commit();
                }
            }
        });

        return main;
    }
}
