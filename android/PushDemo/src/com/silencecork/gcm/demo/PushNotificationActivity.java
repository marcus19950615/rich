package com.silencecork.gcm.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;

public class PushNotificationActivity extends Activity {
	
	private PowerManager mPowerManager;
	private WakeLock mWakeLock = null;
	
	private TextView mTitle;
	
	private static Handler mHandler = new Handler() ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		
		setContentView(R.layout.push_layout);
		
		mTitle = (TextView) findViewById(R.id.title);
		String title = getIntent().getStringExtra("message");
		mTitle.setText(title);
		
		View openButton = findViewById(R.id.btn_open);
		openButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				releaseScreen();
				Intent intent = new Intent(PushNotificationActivity.this, MainActivity.class);
				startActivity(intent);
				
				// if you want to open your activity directly without lock screen
				// use this line to unlock the lock screen
				//getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
				
				finish();
			}
		});
		
		View closeButton = findViewById(R.id.btn_close);
		closeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				releaseScreen();
				finish();
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		String title = intent.getStringExtra("message");
		mTitle.setText(title);
		
		if (!mPowerManager.isScreenOn()) {
			setScreenOn();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		releaseScreen();
	}
	
	/**
	 * Open Screen
	 */
	@SuppressWarnings("deprecation")
	public synchronized void setScreenOn(){
		
		int mode =PowerManager.FULL_WAKE_LOCK | 
				PowerManager.ON_AFTER_RELEASE | 
				PowerManager.ACQUIRE_CAUSES_WAKEUP;
		
		mHandler.removeCallbacks(mReleaseWakeLock);
		
		releaseWakeLock();
		
		mWakeLock = mPowerManager.newWakeLock(mode,
				PushNotificationActivity.class.getName());
		mWakeLock.setReferenceCounted(false);
		mWakeLock.acquire();
		mHandler.postDelayed(mReleaseWakeLock, 5000);
		
	}
	
	/**
	 * release wakelock, if release is not working directly, wait for 5 seconds and retry
	 */
	public synchronized void releaseScreen(){
		mHandler.removeCallbacks(mReleaseWakeLock);
		releaseWakeLock();
	}

	private synchronized void releaseWakeLock(){
		if(mWakeLock!=null && mWakeLock.isHeld()){
			mWakeLock.release();
			mWakeLock = null;
		}
	}
	
	private Runnable mReleaseWakeLock = new Runnable() {
		
		@Override
		public void run() {
			releaseWakeLock();
		}
	};

}
