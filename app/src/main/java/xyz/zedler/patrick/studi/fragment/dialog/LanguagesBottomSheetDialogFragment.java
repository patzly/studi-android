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

package xyz.zedler.patrick.studi.fragment.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.Locale;
import java.util.Objects;
import xyz.zedler.patrick.studi.Constants;
import xyz.zedler.patrick.studi.activity.MainActivity;
import xyz.zedler.patrick.studi.adapter.LanguageAdapter;
import xyz.zedler.patrick.studi.databinding.FragmentBottomsheetLanguagesBinding;
import xyz.zedler.patrick.studi.fragment.SettingsFragment;
import xyz.zedler.patrick.studi.model.Language;
import xyz.zedler.patrick.studi.util.LocaleUtil;
import xyz.zedler.patrick.studi.util.SystemUiUtil;

public class LanguagesBottomSheetDialogFragment extends BaseBottomSheetDialogFragment
    implements LanguageAdapter.LanguageAdapterListener {

  private static final String TAG = "LanguagesBottomSheet";

  private FragmentBottomsheetLanguagesBinding binding;
  private MainActivity activity;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    binding = FragmentBottomsheetLanguagesBinding.inflate(
        inflater, container, false
    );

    activity = (MainActivity) requireActivity();
    String selectedCode = getSharedPrefs().getString(
        Constants.PREF.LANGUAGE, Constants.DEF.LANGUAGE
    );

    binding.recyclerLanguages.setLayoutManager(
        new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
    );
    binding.recyclerLanguages.setAdapter(
        new LanguageAdapter(LocaleUtil.getLanguages(activity), selectedCode, this)
    );

    return binding.getRoot();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  @Override
  public void onItemRowClicked(Language language) {
    String previousCode = getSharedPrefs().getString(Constants.PREF.LANGUAGE, null);
    String selectedCode = language != null ? language.getCode() : null;

    if (Objects.equals(previousCode, selectedCode)) {
      return;
    } else if (previousCode == null || selectedCode == null) {
      Locale localeDevice = LocaleUtil.getNearestSupportedLocale(
          activity, LocaleUtil.getDeviceLocale()
      );
      String codeToCompare = previousCode == null ? selectedCode : previousCode;
      if (Objects.equals(localeDevice.toString(), codeToCompare)) {
        SettingsFragment fragment = (SettingsFragment) activity.getCurrentFragment();
        fragment.setLanguage(language);
        dismiss();
      } else {
        dismiss();
        activity.restartToApply(150);
      }
    } else {
      dismiss();
      activity.restartToApply(150);
    }

    getSharedPrefs().edit().putString(Constants.PREF.LANGUAGE, selectedCode).apply();
  }

  @Override
  public void applyBottomInset(int bottom) {
    binding.recyclerLanguages.setPadding(
        0, SystemUiUtil.dpToPx(requireContext(), 8),
        0, SystemUiUtil.dpToPx(requireContext(), 8) + bottom
    );
  }

  @NonNull
  @Override
  public String toString() {
    return TAG;
  }
}
