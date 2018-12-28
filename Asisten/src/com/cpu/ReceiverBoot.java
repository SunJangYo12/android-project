package com.cpu;

import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Notification;
import android.widget.Toast;
import com.cpu.init.ShellExecuter;
import android.os.CountDownTimer;
import android.os.BatteryManager;
import com.tools.Kompas;
import android.widget.RemoteViews;
import android.view.View;
import android.widget.TextView;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.Gravity;
import android.net.Uri;
import com.cpu.input.MicHelper;
import android.os.*;
import com.status.*;
import com.tools.*;
import android.app.*;
import android.media.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.os.AsyncTask;
import android.util.Log;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.cpu.memori.*;

public class ReceiverBoot extends BroadcastReceiver
{
	private static String TAG = "asisten";

	Context context;
	public NotificationManager notificationManager;
	private Toast toast;
	Intent cuai, komi, maini;
	PendingIntent cuap, kompp, mainp;
	SharedPreferences settings;
	protected AudioManager mAudioManager;
	
	public static String dataTemp = "";
	public static String dataVolt = "";
	public static String dataAmp = "";
	public static String dataCpu = "";
	public static String wifiStatus = "";
	public static String dataServer = "halo";
	public static boolean btnServer = true;
	public static boolean senter = false;
	

	@Override
	public void onReceive(Context context, Intent intent)
	{
		this.context = context;
		dbCatatan(context);

		if (btnServer) {
			//inServer(context, "http://10.42.0.1/client.php?main="+dataServer);
		}
		settings = context.getSharedPreferences("Settings", 0);	
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
		{
			if (senter) {
				senter = false;
				new Senter().runingKu();
			}
		}
		if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
		{
			CountDownTimer hitungMundur = new CountDownTimer(5000, 100){
				public void onTick(long millisUntilFinished){
					senter = true;
				}
				public void onFinish()
				{
					senter = false;
				}
			}.start();

			if (settings.getBoolean("mode hemat", false)){
				Intent mIntent = new Intent(context, MainAsisten.class);
				mIntent.putExtra("layar","on");
				mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(mIntent);
			}
		}
	
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
		{
			context.startService(new Intent(context, ServiceBoot.class));
			context.startService(new Intent(context, ServiceStatus.class));
			
			ServiceTTS sertts = new ServiceTTS();
			sertts.cepat = 1.0f;
			sertts.str = "";
			context.startService(new Intent(context, ServiceTTS.class));

			ServerUtils utils = new ServerUtils(context);

			if (utils.checkInstall()) {
				utils.runSrv();
			} else {
				Intent mIntent = new Intent(context, MainServer.class);
				mIntent.putExtra("server","runboot");
				mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(mIntent);
			}
		}
		if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED))
		{
			ShellExecuter exe = new ShellExecuter();
			String amp = exe.Executer("cat /sys/class/power_supply/battery/current_now");
			String[] a = amp.split("(?<=\\G.{1})");
			String[] b = amp.split("(?<=\\G.{3})");
			
			if (a[0].equals("-")){
				b = amp.split("(?<=\\G.{4})");
			}
			
			float BatteryTemp = (float)(intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0))/10;
			float voltase     = (float)(intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0))/100;
			
			dataTemp = ""+BatteryTemp+(char)0x00B0+"C";
			dataVolt = ""+voltase+" V";
			dataAmp  = ""+b[0];
			if (dataAmp.equals("")) {
				dataAmp = "Error";
			}

			notifiBoot(context, dataVolt, dataTemp, dataAmp+" mA");
			
			if (voltase <= 34.5){
				toastText(context, dataVolt, Color.YELLOW, Gravity.TOP | Gravity.LEFT);
			}
			else {
				if (BatteryTemp >= 36.5){
				    toastText(context, dataTemp, Color.RED, Gravity.TOP | Gravity.RIGHT);
			    }
			}
		
		}
		
	}
	

	public void notifiBoot(Context context, String volt, String temp, String amp)
	{
		MainAsisten asisten = new MainAsisten();
		String ledMic = "OFF";
		if (settings.getBoolean("mode hemat",true)){
			ledMic = "ON";
		}
		cuai = new Intent(Intent.ACTION_VIEW, Uri.parse("https://https-m-accuweather-com.0.freebasics.com/id/id/wonosobo/202805/weather-forecast/202805?iorg_service_id_internal=398041573691256%3BAfriwooZeY5Vtpkj"));
	    komi = new Intent(context, Kompas.class);
		maini = new Intent(context, MainAsisten.class);
		maini.putExtra("destroy","hancur");

	    cuap = PendingIntent.getActivity(context, 0, cuai, PendingIntent.FLAG_UPDATE_CURRENT);
	    kompp = PendingIntent.getActivity(context, 0, komi, PendingIntent.FLAG_UPDATE_CURRENT);
	    mainp = PendingIntent.getActivity(context, 0, maini, PendingIntent.FLAG_UPDATE_CURRENT);

		RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notifi);
		contentView.setImageViewResource(R.id.image, R.drawable.setting);
		contentView.setImageViewResource(R.id.alat, R.drawable.compas);
		contentView.setImageViewResource(R.id.main, R.drawable.iris);
		contentView.setTextViewText(R.id.title, asisten.getWeton(4));
		contentView.setTextViewText(R.id.text, new StringBuilder().append(volt+"\n")
									.append(amp+"\n")
									.append(temp+"  proses : "+dataCpu+"\n"));
		contentView.setOnClickPendingIntent(R.id.image, mainp);
		contentView.setOnClickPendingIntent(R.id.alat, kompp);
		contentView.setOnClickPendingIntent(R.id.main, cuap);

		int libur = R.drawable.trans;
		if(asisten.getWeton(0).equals("Minggu")){
			libur = R.drawable.transm;
		}
	
		Notification.Builder mBuilder = new Notification.Builder(context)
			.setSmallIcon(libur)
			.setPriority(Notification.PRIORITY_MAX)
			.setContent(contentView);

		Notification notification = mBuilder.build();
		notification.flags |= Notification.FLAG_NO_CLEAR;

		NotificationManager notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
		notificationManager.notify(7, notification);
	}

	public void toastText(Context context, String data, int warna, int letak)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE );
		View layout = inflater.inflate(R.layout.toast, null);

    	TextView text = (TextView) layout.findViewById(R.id.toast);
		text.setText(data);
		text.setTextColor(Color.BLUE);
		text.setTextSize(13);
		text.setGravity(Gravity.CENTER);

		toast = new Toast(context.getApplicationContext());
		toast.setGravity(letak, 0, 0);
		toast.setView(text);
		toast.setView(layout);

		View toastView = toast.getView();
		toastView.setBackgroundColor(warna);

    	CountDownTimer hitungMundur = new CountDownTimer(3000, 100)
		{
			public void onTick(long millisUntilFinished)
			{
				toast.show();
			}
			public void onFinish()
			{
				toast.cancel();
			}
		}.start();
	}

	public void inServer(Context context, String url) {
		CallWebPageTask task = new CallWebPageTask();
		task.applicationContext = context;
		task.execute(new String[] { url });
	}
	
	//Method untuk Mengirimkan data keserver
	public String getRequest(String Url){
		String sret;
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(Url);
        try{
            HttpResponse response = client.execute(request);
            sret= request(response);
        }
		catch(Exception ex){
			sret= "Failed Connect to Server!";
        }
        return sret;

    }
	// Method untuk Menerima data dari server
	public static String request(HttpResponse response){
        String result = "";
        try{
            InputStream in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null){
                str.append(line + "\n");
            }
            in.close();
            result = str.toString();
        }
		catch(Exception ex){
            result = "Error split text";
        }
        return result;
    }
	// Class untuk implementasi class AscyncTask
	private class CallWebPageTask extends AsyncTask<String, Void, String> {

		protected Context applicationContext;

		// connecting...
		@Override
		protected void onPreExecute() {

		}

	    @Override
	    protected String doInBackground(String... urls) {
			String response = "";
			response = getRequest(urls[0]);
			return response;
	    }

	    // berhasil
	    @Override
	    protected void onPostExecute(String result) {
	    	String[] a = result.split("-_-");
	    	if (a[0].equals("gawa file pora")) {
				Intent mIntent = new Intent(applicationContext, MainAsisten.class);
				mIntent.putExtra("main","SERVER_LOCAL");
				mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				applicationContext.startActivity(mIntent);
	    	}
		}
	}

	private void notifCatatan(Context context, String data) {
		Intent intent = new Intent(context, MemoriActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

		Notification notification = new Notification.Builder(context)
			.setSmallIcon(R.drawable.catatan)
			.setContentTitle(data)
			.setTicker("ada Catatan")
			.setOngoing(true)
			.setLights(Color.RED, 500, 500)
			.setContentIntent(pIntent)
			.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
		notificationManager.notify(8, notification);

	}
	
	private void dbCatatan(Context context) {
		//Log.i(TAG, "receiver oke");

		DBHelper dbhelper = new DBHelper(context);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM otak",null);
        String[] daftar = new String[cursor.getCount()];

        cursor.moveToFirst();
        for (int cc=0; cc < cursor.getCount(); cc++){
            cursor.moveToPosition(cc);
            daftar[cc] = cursor.getString(2).toString();
        }
        for (int i=0; i<daftar.length; i++) {
        	notifCatatan(context, daftar[i]);
			//Log.i(TAG, "database : "+daftar[i]);
        }
	}

}
