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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import xyz.zedler.patrick.studi.R;
import xyz.zedler.patrick.studi.activity.MainActivity;
import xyz.zedler.patrick.studi.behavior.ScrollBehavior;
import xyz.zedler.patrick.studi.behavior.SystemBarBehavior;
import xyz.zedler.patrick.studi.databinding.FragmentWelcomeBinding;
import xyz.zedler.patrick.studi.util.ResUtil;
import xyz.zedler.patrick.studi.util.ViewUtil;

public class DownloadNativeFragment extends BaseFragment implements OnClickListener {

  private FragmentWelcomeBinding binding;
  private MainActivity activity;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
  ) {
    binding = FragmentWelcomeBinding.inflate(inflater, container, false);
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
    systemBarBehavior.setAppBar(binding.appBarWelcome);
    systemBarBehavior.setContainer(binding.linearWelcomeContainer);
    systemBarBehavior.setUp();

    new ScrollBehavior(activity).setUpScroll(binding.appBarWelcome, null, true);

    binding.toolbarWelcome.setOnMenuItemClickListener(item -> {
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

    ViewUtil.setOnClickListeners(
        this,
        binding.buttonWelcomeDownload
    );
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.button_welcome_download && getViewUtil().isClickEnabled()) {
      performHapticClick();
      navigate(AboutFragmentDirections.actionAboutToChangelogDialog());
    }
  }
}