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

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class CalendarUtil {

  public static String validateEvents(String file) throws IOException {
    /*
    TO IGNORE
    BEGIN:VEVENT
    DTSTART;TZID=Europe/Berlin:T094500
    DTEND;TZID=Europe/Berlin:T111500
    RRULE:FREQ=WEEKLY;UNTIL=20160129T235900Z;INTERVAL=1;BYDAY=TU
    EXDATE:
    LOCATION:F - 109
    DTSTAMP:20151115T174812Z
    UID:150582264882
    DESCRIPTION:
    SUMMARY:14220920 - Rechnerarchitekturen
    CATEGORIES:Vorlesung/Übung
    END:VEVENT*/

    BufferedReader reader = new BufferedReader(new StringReader(file));
    StringBuilder builder = new StringBuilder();
    StringBuilder eventBuilder = null;
    boolean valid = true;

    String line;
    while ((line = reader.readLine()) != null) {
      if (eventBuilder != null) {
        eventBuilder.append(line).append("\n");
        if (line.startsWith("END:VEVENT")) {
          //event end
          if (valid) {
            builder.append(eventBuilder);
          } else {
            Log.d("LSF", "Ignored event");
          }
          eventBuilder = null;
          valid = true;
        } else if (line.startsWith("DTSTART;TZID=Europe/Berlin:T")) {
          valid = false;
        } else if (line.startsWith("DTEND;TZID=Europe/Berlin:T")) {
          valid = false;
        }
      } else if (line.startsWith("BEGIN:VEVENT")) {
        eventBuilder = new StringBuilder();
        eventBuilder.append(line).append("\n");
      } else {
        builder.append(line).append("\n");
      }
    }
    return builder.toString();
  }
}
