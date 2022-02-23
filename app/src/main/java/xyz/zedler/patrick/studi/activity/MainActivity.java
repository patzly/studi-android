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

package xyz.zedler.patrick.studi.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.JobIntentService;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.snackbar.Snackbar;
import com.hstrobel.lsfplan.Constants;
import com.hstrobel.lsfplan.gui.download.CourseGroup;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import xyz.zedler.patrick.studi.BuildConfig;
import xyz.zedler.patrick.studi.Constants.DEF;
import xyz.zedler.patrick.studi.Constants.EXTRA;
import xyz.zedler.patrick.studi.Constants.PREF;
import xyz.zedler.patrick.studi.Constants.THEME;
import xyz.zedler.patrick.studi.Constants.THEME.MODE;
import xyz.zedler.patrick.studi.R;
import xyz.zedler.patrick.studi.databinding.ActivityMainBinding;
import xyz.zedler.patrick.studi.fragment.BaseFragment;
import xyz.zedler.patrick.studi.fragment.dialog.ChangelogBottomSheetDialogFragment;
import xyz.zedler.patrick.studi.fragment.dialog.FeedbackBottomSheetDialogFragment;
import xyz.zedler.patrick.studi.net.IcsFileDownloader;
import xyz.zedler.patrick.studi.util.HapticUtil;
import xyz.zedler.patrick.studi.util.LocaleUtil;
import xyz.zedler.patrick.studi.util.PrefsUtil;
import xyz.zedler.patrick.studi.util.ViewUtil;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private ActivityMainBinding binding;
  private NavController navController;
  private SharedPreferences sharedPrefs;
  private HapticUtil hapticUtil;
  private NavHostFragment navHost;
  private Locale localeUser;
  private boolean runAsSuperClass;

  public IcsFileDownloader icsDownloader;
  public String icsFile;
  public boolean initialized = false;
  public boolean updated = false;
  public Calendar calendar;
  public List<CourseGroup> cachedPlans;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    runAsSuperClass = savedInstanceState != null
        && savedInstanceState.getBoolean(EXTRA.RUN_AS_SUPER_CLASS, false);

    if (runAsSuperClass) {
      super.onCreate(savedInstanceState);
      return;
    }

    sharedPrefs = new PrefsUtil(this).checkForMigrations().getSharedPrefs();

    // LANGUAGE

    localeUser = LocaleUtil.getUserLocale(this, sharedPrefs);
    Locale.setDefault(localeUser);

    // NIGHT MODE

    int modeNight;
    int uiMode = getResources().getConfiguration().uiMode;
    switch (sharedPrefs.getInt(PREF.MODE, DEF.MODE)) {
      case MODE.LIGHT:
        modeNight = AppCompatDelegate.MODE_NIGHT_NO;
        uiMode = Configuration.UI_MODE_NIGHT_NO;
        break;
      case MODE.DARK:
        modeNight = AppCompatDelegate.MODE_NIGHT_YES;
        uiMode = Configuration.UI_MODE_NIGHT_YES;
        break;
      default:
        modeNight = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        break;
    }
    AppCompatDelegate.setDefaultNightMode(modeNight);

    // APPLY CONFIG TO RESOURCES

    // base
    Resources resBase = getBaseContext().getResources();
    Configuration configBase = resBase.getConfiguration();
    configBase.setLocale(localeUser);
    configBase.uiMode = uiMode;
    resBase.updateConfiguration(configBase, resBase.getDisplayMetrics());
    // app
    Resources resApp = getApplicationContext().getResources();
    Configuration configApp = resApp.getConfiguration();
    configApp.setLocale(localeUser);
    // Don't set uiMode here, won't let FOLLOW_SYSTEM apply correctly
    resApp.updateConfiguration(configApp, getResources().getDisplayMetrics());

    switch (sharedPrefs.getString(PREF.THEME, DEF.THEME)) {
      case THEME.RED:
        setTheme(R.style.Theme_Doodle_Red);
        break;
      case THEME.YELLOW:
        setTheme(R.style.Theme_Doodle_Yellow);
        break;
      case THEME.GREEN:
        setTheme(R.style.Theme_Doodle_Green);
        break;
      case THEME.BLUE:
        setTheme(R.style.Theme_Doodle_Blue);
        break;
      case THEME.GOOGLE:
        setTheme(R.style.Theme_Doodle_Google);
        break;
      case THEME.PURPLE:
        setTheme(R.style.Theme_Doodle_Purple);
        break;
      case THEME.AMOLED:
        setTheme(R.style.Theme_Doodle_Amoled);
        break;
      default:
        if (DynamicColors.isDynamicColorAvailable()) {
          DynamicColors.applyIfAvailable(this);
        } else {
          setTheme(R.style.Theme_Doodle_Google);
        }
        break;
    }

    Bundle bundleInstanceState = getIntent().getBundleExtra(EXTRA.INSTANCE_STATE);
    super.onCreate(bundleInstanceState != null ? bundleInstanceState : savedInstanceState);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    hapticUtil = new HapticUtil(binding.getRoot());

    navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(
        R.id.fragment_main_nav_host
    );
    assert navHost != null;
    navController = navHost.getNavController();

    if (savedInstanceState == null && bundleInstanceState == null) {
      new Handler(Looper.getMainLooper()).postDelayed(
          this::showInitialBottomSheets, Build.VERSION.SDK_INT >= 31 ? 950 : 0
      );
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

    if (runAsSuperClass) {
      return;
    }

    hapticUtil.setEnabled(HapticUtil.areSystemHapticsTurnedOn(this));
  }

  @Override
  protected void attachBaseContext(Context base) {
    if (runAsSuperClass) {
      super.attachBaseContext(base);
    } else {
      SharedPreferences sharedPrefs = new PrefsUtil(base).checkForMigrations().getSharedPrefs();
      // Language
      Locale userLocale = LocaleUtil.getUserLocale(base, sharedPrefs);
      Locale.setDefault(userLocale);
      // Night mode
      int modeNight;
      int uiMode = base.getResources().getConfiguration().uiMode;
      switch (sharedPrefs.getInt(PREF.MODE, MODE.AUTO)) {
        case MODE.LIGHT:
          modeNight = AppCompatDelegate.MODE_NIGHT_NO;
          uiMode = Configuration.UI_MODE_NIGHT_NO;
          break;
        case MODE.DARK:
          modeNight = AppCompatDelegate.MODE_NIGHT_YES;
          uiMode = Configuration.UI_MODE_NIGHT_YES;
          break;
        default:
          modeNight = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
          break;
      }
      AppCompatDelegate.setDefaultNightMode(modeNight);
      // Apply config to resources
      Resources resources = base.getResources();
      Configuration config = resources.getConfiguration();
      config.setLocale(userLocale);
      config.uiMode = uiMode;
      resources.updateConfiguration(config, resources.getDisplayMetrics());
      super.attachBaseContext(base.createConfigurationContext(config));
    }
  }

  @Override
  public void applyOverrideConfiguration(Configuration overrideConfiguration) {
    if (!runAsSuperClass && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
      overrideConfiguration.setLocale(LocaleUtil.getUserLocale(this, sharedPrefs));
    }
    super.applyOverrideConfiguration(overrideConfiguration);
  }

  @NonNull
  public BaseFragment getCurrentFragment() {
    return (BaseFragment) navHost.getChildFragmentManager().getFragments().get(0);
  }

  public void showSnackbar(@StringRes int resId) {
    showSnackbar(
        Snackbar.make(binding.fragmentMainNavHost, getString(resId), Snackbar.LENGTH_LONG)
    );
  }

  public IcsFileDownloader getIcsDownloader() {
    return icsDownloader;
  }

  public void initCalender()
      throws IOException, ParserException {
    if (!initialized) {
      //Try to load from file
      cachedPlans = null;
      calendar = null;
      icsFile = sharedPrefs.getString(PREF.ICS_FILE, null);
      if (icsFile != null) {
        updated = true;
      }
      initialized = true;
    }

    if (updated) {
      if (icsFile != null) {
        calendar = new CalendarBuilder().build(new StringReader(icsFile));
      }
      updated = false;
    }
  }

  public void setCalendar() throws IOException, ParserException {
    icsFile = icsDownloader.getFile();
    updated = true;
    initCalender();

    SharedPreferences.Editor editor = sharedPrefs.edit();
    editor.putString(PREF.ICS_FILE, icsFile);
    editor.putLong(PREF.ICS_DATE, java.util.Calendar.getInstance().getTimeInMillis());
    editor.putString(PREF.ICS_URL, icsDownloader.getUrl());
    editor.apply();

    //Set to null to show that nothing is download --> used by SyncService
    icsDownloader = null;
  }

  public void sync(Context context, boolean forceRefresh) {
    Intent intent = new Intent();
    intent.putExtra(Constants.INTENT_EXTRA_REFRESH, forceRefresh);

    //Rejecting re-init on previously-failed class java.lang.Class<android.support.v4.app.JobIntentService$JobServiceEngineImpl>: java.lang.NoClassDefFoundError: Failed resolution of: Landroid/app/job/JobServiceEngine;
    //normal on pre oreo
    JobIntentService.enqueueWork(context, SyncService.class, Constants.SYNC_SERVICE_ID, intent);
  }

  public void showSnackbar(Snackbar snackbar) {
    snackbar.show();
  }

  public Snackbar getSnackbar(@StringRes int resId, int duration) {
    return Snackbar.make(binding.fragmentMainNavHost, getString(resId), duration);
  }

  public void navigate(NavDirections directions) {
    if (navController == null || directions == null) {
      Log.e(TAG, "navigate: controller or direction is null");
      return;
    }
    try {
      navController.navigate(directions);
    } catch (IllegalArgumentException e) {
      Log.e(TAG, "navigate: " + directions, e);
    }
  }

  public void navigateUp() {
    if (navController != null) {
      navController.navigateUp();
    } else {
      Log.e(TAG, "navigateUp: controller is null");
    }
  }

  public SharedPreferences getSharedPrefs() {
    return sharedPrefs;
  }

  public Locale getLocale() {
    return localeUser != null ? localeUser : Locale.getDefault();
  }

  public void reset() {
    sharedPrefs.edit().clear().apply();
  }

  public void restartToApply(long delay) {
    restartToApply(delay, new Bundle(), true);
  }

  public void restartToApply(long delay, @NonNull Bundle bundle, boolean restoreState) {
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      if (restoreState) {
        onSaveInstanceState(bundle);
      }
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        finish();
      }
      Intent intent = new Intent(this, MainActivity.class);
      if (restoreState) {
        intent.putExtra(EXTRA.INSTANCE_STATE, bundle);
      }
      startActivity(intent);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        finish();
      }
      overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }, delay);
  }

  private void showInitialBottomSheets() {
    // Changelog
    int versionNew = BuildConfig.VERSION_CODE;
    int versionOld = sharedPrefs.getInt(PREF.LAST_VERSION, 0);
    if (versionOld == 0) {
      sharedPrefs.edit().putInt(PREF.LAST_VERSION, versionNew).apply();
    } else if (versionOld != versionNew) {
      sharedPrefs.edit().putInt(PREF.LAST_VERSION, versionNew).apply();
      ViewUtil.showBottomSheet(this, new ChangelogBottomSheetDialogFragment());
    }

    // Feedback
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
