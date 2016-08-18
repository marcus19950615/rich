package com.silencecork.gcm.demo;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMIntentService extends IntentService {

	NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	private PowerManager mPowerManager;

	public GCMIntentService() {
		super("GCMIntentService");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
	}



	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		
		String type = GoogleCloudMessaging.getInstance(this).getMessageType(intent);
		if (!GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(type)) {
			return;
		}
		
		if (!extras.isEmpty()) {

			// read extras as sent from server
			String message = extras.getString("message");
			sendNotification("Message: " + message);
			startPushActivity(message);
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GCMBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Notification from GCM")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify((int)System.currentTimeMillis(), mBuilder.build());
	}
	
	@SuppressWarnings("deprecation")
	private void startPushActivity(String message) {
		// if the to activity is PushNotification, do not start new activity
		// or screen is opened.
		if (getTopActivityName().equals(PushNotificationActivity.class.getName()) ||
				!mPowerManager.isScreenOn()) {
			Intent intent = new Intent(this, PushNotificationActivity.class);
			intent.putExtra("message", message);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}
	
	@SuppressWarnings("deprecation")
	public String getTopActivityName() {
		String activityName = "";
		try {
			ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> forGroundActivity = activityManager
					.getRunningTasks(1);
			RunningTaskInfo currentActivity;
			currentActivity = forGroundActivity.get(0);
			activityName = currentActivity.topActivity.getClassName();
		} catch (Exception e) {
		}
		return activityName;
	}

}
