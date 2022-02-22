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
import xyz.zedler.patrick.studi.databinding.FragmentNoDataBinding;
import xyz.zedler.patrick.studi.util.ResUtil;
import xyz.zedler.patrick.studi.util.ViewUtil;

public class NoDataFragment extends BaseFragment implements OnClickListener {

  private FragmentNoDataBinding binding;
  private MainActivity activity;
  private ViewUtil viewUtilLogo;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
  ) {
    binding = FragmentNoDataBinding.inflate(inflater, container, false);
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

    viewUtilLogo = new ViewUtil(1010);

    SystemBarBehavior systemBarBehavior = new SystemBarBehavior(activity);
    systemBarBehavior.setAppBar(binding.appBarOverview);
    systemBarBehavior.setScroll(binding.scrollOverview, binding.linearOverviewContainer);
    systemBarBehavior.setUp();

    new ScrollBehavior(activity).setUpScroll(
        binding.appBarOverview, binding.scrollOverview, true
    );

    binding.toolbarOverview.setOnMenuItemClickListener(item -> {
      int id = item.getItemId();
      if (id == R.id.action_share) {
        ResUtil.share(activity, R.string.msg_share);
        performHapticClick();
        return true;
      } else if (id == R.id.action_feedback) {
        performHapticClick();
        getNavController().navigate(NoDataFragmentDirections.actionNoDataToFeedbackDialog());
        return true;
      } else {
        return false;
      }
    });

    boolean startedFromLauncher = activity.getIntent() != null
        && activity.getIntent().hasCategory("android.intent.category.LAUNCHER");

    /*if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
      binding.appBarOverview.setOnClickListener(v -> {
        if (viewUtilLogo.isClickEnabled()) {
          ViewUtil.startIcon(binding.imageOverviewLogo);
          performHapticClick();
        }
      });
    }*/

    ViewUtil.setOnClickListeners(
        this,
        binding.cardOverviewInfo,
        binding.linearOverviewAbout
    );
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.card_overview_info && getViewUtil().isClickEnabled()) {
      NoDataFragmentDirections.ActionOverviewToTextDialog action
          = NoDataFragmentDirections.actionOverviewToTextDialog();
      action.setFile(R.raw.information);
      action.setTitle(R.string.title_info);
      getNavController().navigate(action);
      performHapticClick();
    } else if (id == R.id.linear_overview_about && getViewUtil().isClickEnabled()) {
      getNavController().navigate(NoDataFragmentDirections.actionNoDataToAbout());
      performHapticClick();
    }
  }
}