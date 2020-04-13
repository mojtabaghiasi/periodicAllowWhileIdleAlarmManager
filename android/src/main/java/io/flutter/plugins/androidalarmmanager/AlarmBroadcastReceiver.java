// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.androidalarmmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;

import androidx.annotation.RequiresApi;

import java.util.Calendar;
import java.util.TimeZone;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
  public static final long milDay = 86400000L;

  /**
   * Invoked by the OS when a timer goes off.
   *
   * <p>The associated timer was registered in {@link AlarmService}.
   *
   * <p>In Android, timer notifications require a {@link BroadcastReceiver} as the artifact that is
   * notified when the timer goes off. As a result, this method is kept simple, immediately
   * offloading any work to {@link AlarmService#enqueueAlarmProcessing(Context, Intent)}.
   *
   * <p>This method is the beginning of an execution path that will eventually execute a desired
   * Dart callback function, as registed by the Dart side of the android_alarm_manager plugin.
   * However, there may be asynchronous gaps between {@code onReceive()} and the eventual invocation
   * of the Dart callback because {@link AlarmService} may need to spin up a Flutter execution
   * context before the callback can be invoked.
   */
  @Override
  public void onReceive(Context context, Intent intent) {
    AlarmService.enqueueAlarmProcessing(context, intent);
    Calendar calendar = getCalendarInstance();
//    calendar.add(Calendar.DAY_OF_YEAR , 1);
//    calendar.set(Calendar.HOUR_OF_DAY , 0);
//    calendar.set(Calendar.MINUTE , 0);
//    calendar.set(Calendar.SECOND , 1);
    calendar.add(Calendar.MINUTE , 1);
    setAlarm(context , calendar , calendar.get(Calendar.SECOND));
  }


  public static Calendar getCalendarInstance() {
    return Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"));
  }


  public void setAlarm(Context context, Calendar calendar, int ID) {
    AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    // Put Reminder ID in Intent Extra
    Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
    intent.putExtra("Reminder_ID", Integer.toString(ID));
    PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    // Calculate notification time
    Calendar c = Calendar.getInstance();
    long currentTime = c.getTimeInMillis();
    long diffTime = calendar.getTimeInMillis() - currentTime;

    // Start alarm using notification time
    if (diffTime > 0) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + diffTime,
                mPendingIntent);
      }else {
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + diffTime,
                mPendingIntent);
      }
    }else{
      Long addTimeToAlarm = milDay + diffTime;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + addTimeToAlarm,
                mPendingIntent);
      }else{
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + addTimeToAlarm,
                mPendingIntent);
      }
    }

    // Restart alarm if device is rebooted
    ComponentName receiver = new ComponentName(context, RebootBroadcastReceiver.class);
    PackageManager pm = context.getPackageManager();
    pm.setComponentEnabledSetting(receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP);
  }

}
