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

import android.os.AsyncTask;
import android.util.Log;
import com.hstrobel.lsfplan.gui.download.NativeSelector;
import org.jsoup.Connection;

/**
 * Created by Henry on 07.12.2015.
 */


public class LoginTask extends AsyncTask<String, String, String> {

  private static final String TAG = "LSF";
  private NativeSelector context = null;

  public LoginTask(NativeSelector c) {
    context = c;
  }

  //1. post, 2. handle answer (cookie or a fail message), 3.return cookie?
    /*
    POST /qisserver/rds?state=user&type=1&category=auth.login&re=last&startpage=portal.vm&breadCrumbSource=portal HTTP/1.1
    Host: lsf.htwg-konstanz.de
    Connection: keep-alive
    Content-Length: 45
    Cache-Control: max-age=0
    Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,;q=0.8
Origin: https://lsf.htwg-konstanz.de
Upgrade-Insecure-Requests: 1
User-Agent: Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.23 Safari/537.36
Content-Type: application/x-www-form-urlencoded
Referer: https://lsf.htwg-konstanz.de/qisserver/rds?state=wlogin&login=in&breadCrumbSource=
Accept-Encoding: gzip, deflate
Accept-Language: de-DE,de;q=0.8,en-US;q=0.6,en;q=0.4
Cookie: JSESSIONID=2A21ACB9733CC49DE90888A6BEF91DFD.lsf; download-complete=true; JSESSIONID=C4382B1EAC218C0D8532FAB32C7DB724.lsf

DATA:
asdf:henstrob
fdsa:xx_pw_xx
submit:Anmelden

Successfull response
HTTP/1.1 302 Found
Date: Mon, 07 Dec 2015 18:40:39 GMT
Server: Apache/2.2.22 (Ubuntu)
Strict-Transport-Security: 86400; includeSubDomains
Set-Cookie: JSESSIONID=35611D98B400C611BAA52972FA250B2A.lsf; Path=/qisserver/; Secure; HttpOnly
Location: https://lsf.htwg-konstanz.de/qisserver/rds?state=wplan&act=stg&pool=stg&show=plan&P.vx=kurz&r_zuordabstgv.semvonint=1&r_zuordabstgv.sembisint=1&missing=allTerms&k_parallel.parallelid=&k_abstgv.abstgvnr=4511&r_zuordabstgv.phaseid=&chco=y
Content-Length: 0
Keep-Alive: timeout=5, max=99
Connection: Keep-Alive



*/


  @Override
  protected String doInBackground(String... params) {
    try {
      String user = params[0];
      String pw = params[1];

      String url = Utils.getLoginUrl(context, GlobalState.getInstance().getCollege());
      Connection connection = Utils.setupAppConnection(url, context)
          .data("asdf", user)
          .data("fdsa", pw)
          .data("submit", "Anmelden");
      connection.post();
      Connection.Response response = connection.response();
      Log.d(TAG, String.valueOf(response.method()));

      //get is successfull because it will redirect. if it fails we get a modified post response.
      if (response.method() == Connection.Method.GET) {
        //yay
        for (String key : response.cookies().keySet()) {
          Log.d("LSF", String.format("%s : %s", key, response.cookie(key)));
          if (key.equals("JSESSIONID")) {
            return response.cookie(key);
          }
        }
      }

      return null;

    } catch (Exception ex) {
      Log.e(TAG, "Login failed ", ex);
    }
    return null;
  }

  @Override
  protected void onPostExecute(String result) {
    super.onPostExecute(result);
    context.loginCallback(result);
  }

}