/*
 * This file is part of Doodle Android.
 *
 * Doodle Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Doodle Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Doodle Android. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2020-2021 by Patrick Zedler
 */

package xyz.zedler.patrick.studi.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.snackbar.Snackbar;
import xyz.zedler.patrick.studi.BuildConfig;
import xyz.zedler.patrick.studi.Constants.PREF;
import xyz.zedler.patrick.studi.R;
import xyz.zedler.patrick.studi.databinding.ActivityMainBinding;
import xyz.zedler.patrick.studi.fragment.dialog.ChangelogBottomSheetDialogFragment;
import xyz.zedler.patrick.studi.fragment.dialog.FeedbackBottomSheetDialogFragment;
import xyz.zedler.patrick.studi.util.HapticUtil;
import xyz.zedler.patrick.studi.util.PrefsUtil;
import xyz.zedler.patrick.studi.util.ViewUtil;

public class MainActivity extends AppCompatActivity {

  private final static String TAG = MainActivity.class.getSimpleName();

  private ActivityMainBinding binding;
  private NavController navController;
  private SharedPreferences sharedPrefs;
  private HapticUtil hapticUtil;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    sharedPrefs = new PrefsUtil(this).checkForMigrations().getSharedPrefs();

    hapticUtil = new HapticUtil(binding.getRoot());

    NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(
        R.id.fragment_main_nav_host
    );
    assert navHost != null;
    navController = navHost.getNavController();

    if (savedInstanceState == null) {
      showChangelogIfUpdated();
      showFeedbackAfterSomeUsage();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  @Override
  protected void onResume() {
    super.onResume();

    hapticUtil.setEnabled(HapticUtil.areSystemHapticsTurnedOn(this));
  }

  private void showSnackbar(@StringRes int resId) {
    showSnackbar(
        Snackbar.make(binding.fragmentMainNavHost, getString(resId), Snackbar.LENGTH_LONG)
    );
  }

  public void showSnackbar(Snackbar snackbar) {
    snackbar.show();
  }

  public NavController getNavController() {
    return navController;
  }

  public SharedPreferences getSharedPrefs() {
    return sharedPrefs;
  }

  public void reset() {
    sharedPrefs.edit().clear().apply();
    new Handler(getMainLooper()).postDelayed(() -> {
      startActivity(new Intent(this, MainActivity.class));
      finish();
      Runtime.getRuntime().exit(0);
    }, 300);
  }

  private void showChangelogIfUpdated() {
    int versionNew = BuildConfig.VERSION_CODE;
    int versionOld = sharedPrefs.getInt(PREF.LAST_VERSION, 0);
    if (versionOld == 0) {
      sharedPrefs.edit().putInt(PREF.LAST_VERSION, versionNew).apply();
    } else if (versionOld != versionNew) {
      sharedPrefs.edit().putInt(PREF.LAST_VERSION, versionNew).apply();
      ViewUtil.showBottomSheet(this, new ChangelogBottomSheetDialogFragment());
    }
  }

  private void showFeedbackAfterSomeUsage() {
    int feedbackCount = sharedPrefs.getInt(PREF.FEEDBACK_POP_UP_COUNT, 1);
    if (feedbackCount > 0) {
      if (feedbackCount < 5) {
        sharedPrefs.edit().putInt(PREF.FEEDBACK_POP_UP_COUNT, feedbackCount + 1).apply();
      } else {
        ViewUtil.showBottomSheet(this, new FeedbackBottomSheetDialogFragment());
      }
    }
  }

  public void performHapticClick() {
    hapticUtil.click();
  }

  public void performHapticHeavyClick() {
    hapticUtil.heavyClick();
  }
}
