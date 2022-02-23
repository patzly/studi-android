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

package xyz.zedler.patrick.studi.net;

import android.content.Context;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import xyz.zedler.patrick.studi.util.CalendarUtil;
import xyz.zedler.patrick.studi.util.NetUtil;

public class IcsFileDownloader implements Runnable {

  private static final String TAG = IcsFileDownloader.class.getSimpleName();

  private final String url;
  private final Context context;
  private final Runnable withEndAction;
  private String file;

  public IcsFileDownloader(Context context, String url, Runnable withEndAction) {
    this.context = context;
    this.url = url;
    this.withEndAction = withEndAction;
  }

  @Override
  public void run() {
    try {
      Log.i(TAG, "run: " + url);
      HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
      con.setSSLSocketFactory(NetUtil.generateSocketFactory(context));
      InputStream fileStream = con.getInputStream();

      ByteArrayOutputStream result = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int length;
      while ((length = fileStream.read(buffer)) != -1) {
        result.write(buffer, 0, length);
      }
      file = CalendarUtil.validateEvents(result.toString("UTF-8"));

      withEndAction.run();
    } catch (Exception e) {
      Log.e(TAG, "run: download failed", e);
      file = null;
    }
  }

  public String getUrl() {
    return url;
  }

  public String getFile() {
    return file;
  }

  public boolean isFileValid() {
    return file != null && file.startsWith("BEGIN:VCALENDAR");
  }
}