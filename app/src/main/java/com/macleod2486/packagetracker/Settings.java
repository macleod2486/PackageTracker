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

package com.macleod2486.packagetracker;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.Preference;

public class Settings extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener
{
    private ListPreference frequency;

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey)
    {
        addPreferencesFromResource(R.xml.settings);
        frequency = findPreference("freq");
        frequency.setOnPreferenceChangeListener(this);
        frequency.setSummary(frequency.getEntry());
    }

    public boolean onPreferenceChange(Preference preference, Object object)
    {
        int target = frequency.findIndexOfValue(object.toString());
        CharSequence[] frequencies = frequency.getEntries();

        frequency.setSummary(frequencies[target]);

        return true;
    }

}
