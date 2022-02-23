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

import static xyz.zedler.patrick.studi.GlobalState.TAG;

import android.content.Context;
import android.util.Log;
import com.hstrobel.lsfplan.Constants;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import xyz.zedler.patrick.studi.R;

public class NetUtil {

  private static String getBaseUrl(Context context) {
    return context.getString(R.string.misc_baseUrl_HTWG);
  }

  public static String getLoginUrl(Context c) {
    return getBaseUrl(c) + c.getString(R.string.misc_personalLoginURL);
  }

  public static String getPersonalPlanUrl(Context c) {
    return getBaseUrl(c) + c.getString(R.string.misc_personalPlanURL);
  }

  public static String getCoursesOverviewUrl(Context c) {
    return getBaseUrl(c) + c.getString(R.string.misc_coursesOverviewURL);
  }

  public static Connection setupAppConnection(String url, Context c) {
    return Jsoup.connect(url)
        .sslSocketFactory(generateSocketFactory(c))
        .userAgent(Constants.NETWORK_USERAGENT)
        .timeout(Constants.NETWORK_TIMEOUT);
  }

  public static SSLSocketFactory generateSocketFactory(Context c) {
    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");

      InputStream is = c.getResources().openRawResource(R.raw.cert_chain);
      BufferedInputStream bis = new BufferedInputStream(is);
      ArrayList<Certificate> caList = new ArrayList<>();

      while (bis.available() > 0) {
        Certificate cert = cf.generateCertificate(bis);
        Log.i(TAG, "ca=" + ((X509Certificate) cert).getSubjectDN());
        caList.add(cert);
      }
      bis.close();

      // Create a KeyStore containing our trusted CAs
      String keyStoreType = KeyStore.getDefaultType();
      KeyStore keyStore = KeyStore.getInstance(keyStoreType);
      keyStore.load(null, null);
      for (Certificate certificate : caList) {
        keyStore.setCertificateEntry(certificate.getPublicKey().toString(), certificate);
      }

      // Create a TrustManager that trusts the CAs in our KeyStore
      String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
      tmf.init(keyStore);

      // Create an SSLContext that uses our TrustManager
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(null, tmf.getTrustManagers(), null);

      return context.getSocketFactory();
    } catch (Exception ex) {
      Log.e(TAG, "SSL failed ", ex);
      return null;
    }
  }
}
