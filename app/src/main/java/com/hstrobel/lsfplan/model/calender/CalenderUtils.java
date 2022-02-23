package com.hstrobel.lsfplan.model.calender;

import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.NoSuchElementException;
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.filter.Rule;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * Created by Henry on 10.11.2015.
 */
public class CalenderUtils {

  private static final boolean DEBUG = false;

  private static final Comparator<VEvent> comparator = (c1, c2) -> (timeWithoutDate(c1)
      .compareTo(timeWithoutDate(c2)));

  public static List<VEvent> getEventsForDay(Calendar myCal, java.util.Calendar date) {
    if (DEBUG) {
      Log.d(GlobalState.TAG,
          "getEventsForDay: " + SimpleDateFormat.getDateTimeInstance().format(date.getTime()));
    }
    return getEvents(myCal, date, new Dur(1, 0, 0, 0));
  }

  private static List<VEvent> getEvents(Calendar myCal, java.util.Calendar date, Dur duration) {
    DateTime startDate = new DateTime(date.getTime());

    Period period = new Period(startDate, duration);
    Filter filter = new Filter(new Rule[]{new PeriodRule(period)}, Filter.MATCH_ANY);

    Collection eventsTodayC = filter.filter(myCal.getComponents(Component.VEVENT));
    return convertComponentCollection(eventsTodayC);
  }


  private static List<VEvent> convertComponentCollection(Collection eventsTodayC) {
    List<VEvent> result = new ArrayList<>();
    for (Object comp : eventsTodayC) {
      if (comp instanceof VEvent) {
        result.add((VEvent) comp);
      }
    }
    return result;
  }

  private static java.util.Calendar timeWithoutDate(VEvent c1) {
    java.util.Calendar d = java.util.Calendar.getInstance();
    d.setTimeInMillis(c1.getStartDate().getDate().getTime());
    d.set(0, 0, 0);
    return d;
  }

  public static DateTime dateWithOutTime(VEvent c1) {
    java.util.Calendar cal = new GregorianCalendar();
    cal.setTimeInMillis(c1.getStartDate().getDate().getTime());
    cal.set(java.util.Calendar.MINUTE, 0);
    cal.set(java.util.Calendar.HOUR, 0);
    return new DateTime(cal.getTimeInMillis());
  }

  public static List<VEvent> sortEvents(List<VEvent> events) {
    Collections.sort(events, comparator);
    return events;// use the comparator as much as u want
  }

  public static DateTime getNextRecuringStartDate(VEvent event) {
    //start from now, function is for notifications only
    return getNextRecuringStartDate(event, new DateTime());
  }

  public static DateTime getNextRecuringStartDate(VEvent event, DateTime start) {
    Period period = new Period(start, new Dur(7, 0, 0, 0)); //1w
    PeriodList r = event.calculateRecurrenceSet(period);

    Period closest = null;
    for (Object objectP : r) {
      Period p = (Period) objectP;
      //System.out.println(" - " + p.toString());
      if (closest == null) {
        closest = p;
      } else {
        if (p.compareTo(closest) < 0) {
          closest = p;
        }
      }
    }

    if (closest == null) {
      throw new NoSuchElementException(event.getSummary().getValue());
    }
    return closest.getStart();
  }
}
