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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;

import com.macleod2486.packagetracker.R;

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
                Log.i("MainActivity","Selected "+vendorsDropdown.getSelectedItem().toString());
            }
        });

        ArrayList<String> statuses = new ArrayList<String>();
        statuses.add("No current entries");

        ListView statusList = main.findViewById(R.id.status);
        statusList.setAdapter(new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, statuses)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View view = super.getView(position, convertView, parent);
                return view;
            }
        });

        return main;
    }

}
