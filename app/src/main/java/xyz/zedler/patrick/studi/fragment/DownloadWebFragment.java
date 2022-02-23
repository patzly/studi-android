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

package xyz.zedler.patrick.studi.fragment;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import xyz.zedler.patrick.studi.R;
import xyz.zedler.patrick.studi.activity.MainActivity;
import xyz.zedler.patrick.studi.behavior.ScrollBehavior;
import xyz.zedler.patrick.studi.behavior.SystemBarBehavior;
import xyz.zedler.patrick.studi.databinding.FragmentDownloadWebBinding;
import xyz.zedler.patrick.studi.net.IcsFileDownloader;
import xyz.zedler.patrick.studi.util.NetUtil;
import xyz.zedler.patrick.studi.util.ResUtil;

public class DownloadWebFragment extends BaseFragment {

  private static final String TAG = DownloadWebFragment.class.getSimpleName();

  private FragmentDownloadWebBinding binding;
  private MainActivity activity;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
  ) {
    binding = FragmentDownloadWebBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    activity = (MainActivity) requireActivity();

    SystemBarBehavior systemBarBehavior = new SystemBarBehavior(activity);
    systemBarBehavior.setAppBar(binding.appBarDownloadWeb);
    systemBarBehavior.setContainer(binding.linearDownloadWebContainer);
    systemBarBehavior.setUp();

    new ScrollBehavior(activity).setUpScroll(
        binding.appBarDownloadWeb, null, true
    );

    binding.toolbarDownloadWeb.setOnMenuItemClickListener(item -> {
      int id = item.getItemId();
      if (id == R.id.action_share) {
        ResUtil.share(activity, R.string.msg_share);
        performHapticClick();
        return true;
      } else if (id == R.id.action_feedback) {
        performHapticClick();
        navigate(AboutFragmentDirections.actionAboutToFeedbackDialog());
        return true;
      } else {
        return false;
      }
    });

    binding.webDownloadWeb.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.i(TAG, "shouldOverrideUrlLoading: " + url);
        if (url.startsWith(
            "https://lsf.htwg-konstanz.de/qisserver/rds?state=verpublish&status=transform")) {
          activity.showSnackbar(R.string.msg_downloading);
        }
        return false;
      }

      @Override
      public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return shouldOverrideUrlLoading(view, request.getUrl().toString());
      }
    });
    binding.webDownloadWeb.setDownloadListener(
        (url, userAgent, contentDisposition, mimetype, contentLength) -> {
          IcsFileDownloader icsDownloader = activity.getIcsDownloader();
          icsDownloader = new IcsFileDownloader(activity, url, () -> {
          try {
            Looper.prepare();

            if (activity.getIcsDownloader().isFileValid()) {
              //not a ics file
              activity.showSnackbar(R.string.msg_file_invalid);
              return;
            }

            activity.setCalendar();

            activity.showSnackbar(R.string.msg_file_loaded);
            activity.navigateUp();
          } catch (Exception e) {
            Log.e(TAG, "onViewCreated: download failed", e);
            activity.showSnackbar(R.string.msg_download_failed);
          }
        });
        new Thread(icsDownloader).start();
    });
    binding.webDownloadWeb.getSettings().setBuiltInZoomControls(true);
    binding.webDownloadWeb.getSettings().setDisplayZoomControls(false);

    String url = NetUtil.getCoursesOverviewUrl(activity);
    binding.webDownloadWeb.loadUrl(url);
  }
}