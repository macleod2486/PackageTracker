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

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.macleod2486.packagetracker.R;
import com.macleod2486.packagetracker.tools.USPSApi;

import org.w3c.dom.Document;

public class USPS extends Fragment
{
    Button addUSPSTracking;
    String trackingIDs;
    ProgressBar progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View uspsView = inflater.inflate(R.layout.fragment_usps, container, false);

        Toolbar toolbar = uspsView.findViewById(R.id.uspstoolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle("Add tracking numbers");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setNavigationOnClickListener((View view) ->
                {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
        );

        EditText text = uspsView.findViewById(R.id.trackingEntry);

        addUSPSTracking = uspsView.findViewById(R.id.addUSPS);
        addUSPSTracking.setOnClickListener((View v) ->
        {
            trackingIDs = text.getText().toString();
            new InitalizeEntry().execute();
        });

        progress = uspsView.findViewById(R.id.uspsAddProgress);
        progress.setVisibility(View.INVISIBLE);

        return uspsView;
    }

    private class InitalizeEntry extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void...params)
        {
            getActivity().runOnUiThread(() ->
            {
                progress.setVisibility(View.VISIBLE);
            });

            String userId = getActivity().getResources().getString(R.string.USPSApiUserID);
            USPSApi apiTool = new USPSApi(userId, getContext());
            String ids[] = trackingIDs.split(",");

            for(String id : ids)
            {
                Document result = apiTool.getTrackingInfo(id);
                apiTool.trackingNumber = id;
                apiTool.storeInitial(result);
                apiTool.initialHistory(id);
            }
            apiTool.closeDatabase();

            return null;
        }

        protected void onPostExecute(Void result)
        {
            getActivity().runOnUiThread(() ->
            {
                progress.setVisibility(View.INVISIBLE);
            });

            FragmentManager manager = getActivity().getSupportFragmentManager();
            manager.popBackStack();
        }
    }
}
