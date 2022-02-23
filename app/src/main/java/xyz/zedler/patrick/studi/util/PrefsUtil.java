/*
 * This file is part of Studi Android.
 *
 * Studi Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Studi Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Studi Android. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2022 by Patrick Zedler
 */

package xyz.zedler.patrick.studi.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import java.util.Objects;

public class PrefsUtil {

  private static final String TAG = PrefsUtil.class.getSimpleName();

  private final SharedPreferences sharedPrefs;

  public PrefsUtil(Context context) {
    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
  }

  public PrefsUtil checkForMigrations() {
    return this;
  }

  public SharedPreferences getSharedPrefs() {
    return sharedPrefs;
  }

  private void migrateString(String keyOld, String keyNew, String def) {
    if (sharedPrefs.contains(keyOld)) {
      SharedPreferences.Editor editor = sharedPrefs.edit();
      try {
        String current = sharedPrefs.getString(keyOld, def);
        if (!Objects.equals(current, def)) {
          editor.remove(keyOld);
          editor.putString(keyNew, current);
        }
      } catch (ClassCastException ignored) {
        editor.remove(keyOld);
      }
      editor.apply();
    }
  }

  private void migrateBoolean(String keyOld, String keyNew, boolean def) {
    if (sharedPrefs.contains(keyOld)) {
      SharedPreferences.Editor editor = sharedPrefs.edit();
      try {
        boolean current = sharedPrefs.getBoolean(keyOld, def);
        if (!Objects.equals(current, def)) {
          editor.remove(keyOld);
          editor.putBoolean(keyNew, current);
        }
      } catch (ClassCastException ignored) {
        editor.remove(keyOld);
      }
      editor.apply();
    }
  }

  private void migrateInteger(String keyOld, String keyNew, int def) {
    if (sharedPrefs.contains(keyOld)) {
      SharedPreferences.Editor editor = sharedPrefs.edit();
      try {
        int current = sharedPrefs.getInt(keyOld, def);
        if (!Objects.equals(current, def)) {
          editor.remove(keyOld);
          editor.putInt(keyNew, current);
        }
      } catch (ClassCastException ignored) {
        editor.remove(keyOld);
      }
      editor.apply();
    }
  }

  private void migrateFloat(String keyOld, String keyNew, float def) {
    if (sharedPrefs.contains(keyOld)) {
      SharedPreferences.Editor editor = sharedPrefs.edit();
      try {
        float current = sharedPrefs.getFloat(keyOld, def);
        if (!Objects.equals(current, def)) {
          editor.remove(keyOld);
          editor.putFloat(keyNew, current);
        }
      } catch (ClassCastException ignored) {
        editor.remove(keyOld);
      }
      editor.apply();
    }
  }

  private void removePreference(String key) {
    if (sharedPrefs.contains(key)) {
      sharedPrefs.edit().remove(key).apply();
    }
  }
}
