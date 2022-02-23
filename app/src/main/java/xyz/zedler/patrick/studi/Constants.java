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

package xyz.zedler.patrick.studi;

public final class Constants {

  private static final String TAG = Constants.class.getSimpleName();

  public static final class PREF {

    public static final String USE_ALT_DL = "alt_download_method";
    public static final String SKIP_WEEKEND = "skip_weekend";

    public static final String ICS_FILE = "ics_file";
    public static final String ICS_DATE = "ics_date";
    public static final String ICS_URL = "ics_url";

    public static final String LANGUAGE = "language";
    public static final String THEME = "app_theme";
    public static final String MODE = "mode";

    public static final String LAST_VERSION = "last_version";
    public static final String FEEDBACK_POP_UP_COUNT = "feedback_pop_up_count";
  }

  public static final class DEF {

    public static final boolean USE_ALT_DL = false;
    public static final boolean SKIP_WEEKEND = true;

    public static final String LANGUAGE = null;
    public static final String THEME = "";
    public static final int MODE = Constants.THEME.MODE.AUTO;
  }

  public static final class EXTRA {

    public static final String RUN_AS_SUPER_CLASS = "run_as_super_class";
    public static final String INSTANCE_STATE = "instance_state";
    public static final String SCROLL_POSITION = "scroll_position";
  }

  public static final class THEME {

    public static final class MODE {

      public static final int AUTO = 0;
      public static final int LIGHT = 1;
      public static final int DARK = 2;
    }

    public static final String DYNAMIC = "dynamic";
    public static final String RED = "red";
    public static final String YELLOW = "yellow";
    public static final String GREEN = "green";
    public static final String BLUE = "blue";
    public static final String GOOGLE = "google";
    public static final String PURPLE = "purple";
    public static final String AMOLED = "amoled";
  }
}
