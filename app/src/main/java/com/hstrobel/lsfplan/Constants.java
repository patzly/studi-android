package com.hstrobel.lsfplan;

/**
 * Created by Henry on 27.03.2017.
 */

public class Constants {

  public static final String TAG = "LSF";

  //IDs
  public static final String INTENT_UPDATE_LIST = "INTENT_UPDATE_LIST";
  public static final String INTENT_EXTRA_REFRESH = "INTENT_EXTRA_REFRESH";
  public static final int SYNC_SERVICE_ID = 133742;
  public static final int TIMEDEVENT_SERVICE_ID = 133723;
  public static final String NOTIFICATION_CHANNEL_ALARMS_ID = "lsf_alarms";
  public static final String NOTIFICATION_CHANNEL_REMOTE_ID = "lsf_remote";

  //Settings
  public static final String PREF_DEV_NOTIFY = "debugNotify";
  public static final String PREF_DEV_SYNC = "debugSync";

  //Model stuff, misc
  public static final String MAGIC_WORD_LOGIN = "#LOGIN#";
  public static final int NETWORK_TIMEOUT = 10 * 1000;
  public static final String NETWORK_USERAGENT = "Android_lsfapp";
  public static final int NOTIFY_BRIEFING_COUNT = 2;
}
