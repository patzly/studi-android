package com.hstrobel.lsfplan.gui.eventlist;

import net.fortuna.ical4j.model.component.VEvent;

public class EventItem {

  public final MainListFragment fragment;
  public final String title;
  public final String description;
  public final VEvent sourceEvent;

  public EventItem(String title, String description, MainListFragment fragment, VEvent event) {
    this.title = title;
    this.description = description;
    this.fragment = fragment;
    this.sourceEvent = event;
  }
}