package com.status;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.TextView;

public class PowerMon {
	public static String mOutput = null;
	public static int mLevel = 0;
	
	BroadcastReceiver mBattReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {

			context.unregisterReceiver(this);
			int rawlevel = intent.getIntExtra("level", -1);
			int scale = intent.getIntExtra("scale", -1);
			//int status = intent.getIntExtra("status", -1);
			//int health = intent.getIntExtra("health", -1);
			int level = -1;  // percentage, or -1 for unknown
			if (rawlevel >= 0 && scale > 0) {
				level = (rawlevel * 100) / scale;
			}
			
			if (mOutput != null) {
				mOutput = Integer.toString(level)+"%";
			}
			mLevel = level;
		}
	};
	
	public PowerMon(Context context) {
		IntentFilter battFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		context.registerReceiver(mBattReceiver, battFilter);

	}
}
