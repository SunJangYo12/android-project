package com.cpu;

import android.app.Service;
import android.speech.tts.TextToSpeech;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;
import java.util.Locale;
import android.os.*;

public class ServiceBoot extends Service
{
	private BroadcastReceiver receiver;

	@Override
    public IBinder onBind(Intent intent) {
        return null;
    }

	@Override
	public void onCreate()
	{
		// TODO: Implement this method
		super.onCreate();
		
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		
		receiver = new ReceiverBoot();
		registerReceiver(receiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		CountDownTimer hitungMundur = new CountDownTimer(2000, 100)
		{
			public void onTick(long millisUntilFinished){
			}
			public void onFinish()
			{
				startService(new Intent(ServiceBoot.this, ServiceBoot.class));
			}
		}.start();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy()
	{
		// TODO: Implement this method
		super.onDestroy();
		unregisterReceiver(receiver);
		Toast.makeText(this, "destroy", Toast.LENGTH_LONG).show();

	}


}


