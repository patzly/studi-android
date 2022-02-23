package com.hstrobel.lsfplan.gui.eventlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import java.util.List;
import xyz.zedler.patrick.studi.R;

public class EventListAdapter extends ArrayAdapter<EventItem> {

  public EventListAdapter(Context context, List<EventItem> items) {
    super(context, R.layout.listview_item, items);
    setNotifyOnChange(true);
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder = null;

    if (convertView != null) {
      // recycle the already inflated view
      viewHolder = (ViewHolder) convertView.getTag();

      if (viewHolder.isHeader && position != 0) {
        //old header layout as a normal item
        convertView = null;
      } else if (position == 0 && !viewHolder.isHeader) {
        //old list item as header
        convertView = null;
      }
    }

    if (convertView == null) {
      // inflate the GridView item layout
      LayoutInflater inflater = LayoutInflater.from(getContext());
      if (position == 0) {
        convertView = inflater.inflate(R.layout.listview_item_head, parent, false);
      } else {
        convertView = inflater.inflate(R.layout.listview_item, parent, false);
      }

      // initialize the view holder
      viewHolder = new ViewHolder();
      viewHolder.viewTitle = (TextView) convertView.findViewById(R.id.tvTitle);
      viewHolder.viewDescription = (TextView) convertView.findViewById(R.id.tvDescription);
      viewHolder.isHeader = (position == 0);

      convertView.setTag(viewHolder);
    }

    // update the item view
    EventItem item = getItem(position);
    if (item == null) {
      return convertView;
    }

    viewHolder.viewTitle.setText(item.title);
    viewHolder.viewDescription.setText(item.description);

    if (position == 0) {
      if (viewHolder.viewIconLeft != null) {
        viewHolder.viewIconLeft.setOnClickListener(v -> {
          MainListFragment frag = (MainListFragment) v.getTag();
          frag.onDateDec();

        });
      }
      if (viewHolder.viewIconRight != null) {
        viewHolder.viewIconRight.setOnClickListener(v -> {
          MainListFragment frag = (MainListFragment) v.getTag();
          frag.onDateInc();
        });
      }
    }

    return convertView;
  }

  private static class ViewHolder {

    boolean isHeader = false;

    TextView viewTitle;
    TextView viewDescription;
  }
}