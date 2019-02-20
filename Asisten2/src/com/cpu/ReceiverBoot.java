package com.cpu;

import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.*;
import android.net.*;
import android.app.*;
import android.widget.*;
import android.media.*;
import android.view.*;
import java.io.*;
import java.util.*;
import java.net.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.*;
import com.cpu.memori.*;
import com.cpu.init.ShellExecuter;
import com.cpu.input.MicHelper;
import com.status.*;
import com.tools.*;

public class ReceiverBoot extends BroadcastReceiver
{
	private static String TAG = "asisten";
	private MainMemori memori;

	Context context;
	public NotificationManager notificationManager;
	private Toast toast;
	private boolean swpaket = true;
	Intent cuai, komi, maini, touchi;
	PendingIntent cuap, kompp, mainp, touchp;
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

	public static String requestAksi = "";
	public static String requestPath = "";
	public static String requestUrl = "https://github.com";
	public static String requestResult = "";
	public static String requestStatus = "";
	public static String requestResultUpload = "";
	public static int sizedownload = 0;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		this.context = context;
		memori = new MainMemori();
		settings = context.getSharedPreferences("Settings", 0);	
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
		{
			
		}
		if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
		{
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
		}
		if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED))
		{
			ShellExecuter exe = new ShellExecuter();
			String amp = exe.executer("cat /sys/class/power_supply/battery/current_now");
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

			if (settings.getBoolean("notif_catatan", false)) {
				notifCatatan(context);
			}

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
	    touchi = new Intent(context, MainTouchAsisten.class);
		maini = new Intent(context, MainAsisten.class);
		maini.putExtra("destroy","hancur");

	    cuap = PendingIntent.getActivity(context, 0, cuai, PendingIntent.FLAG_UPDATE_CURRENT);
	    kompp = PendingIntent.getActivity(context, 0, komi, PendingIntent.FLAG_UPDATE_CURRENT);
	    touchp = PendingIntent.getActivity(context, 0, touchi, PendingIntent.FLAG_UPDATE_CURRENT);
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

		if (settings.getBoolean("touch asisten", false)) {
			contentView.setOnClickPendingIntent(R.id.main, touchp);
		} else {			
			contentView.setOnClickPendingIntent(R.id.main, cuap);
		}

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


	private void notifCatatan(Context context) {
		Intent intent = new Intent(context, MainCatatan.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        String catatan = "";

        try {
        	ArrayAdapter<String> exe = memori.getCatatan(context);
        	catatan = memori.isCatatan(0, 0, context);
        }catch(Exception e) {
        	//Toast.makeText(context, ""+e, Toast.LENGTH_LONG).show();
        }

        Notification notification = new Notification.Builder(context)
			.setSmallIcon(R.drawable.catatan)
			.setContentTitle(catatan)
			.setTicker("ada Catatan")
			.setOngoing(true)
			.setLights(Color.RED, 500, 500)
			.setContentIntent(pIntent)
			.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);

		if (catatan.equals("")) {
			notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
			notificationManager.cancel(8);
		
		} else {
			if (ping(context)) {
				notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
				notificationManager.notify(8, notification);
			}
		}

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

	public boolean ping(Context context) {
		if (cekConnection(context)) {
        	requestStatus = "ping... ["+requestUrl+" ]";

			HttpParams httpParams = new BasicHttpParams();
	    	HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
	    	HttpConnectionParams.setSoTimeout(httpParams, 10000);

        	HttpClient httpClient = new DefaultHttpClient(httpParams);
        	HttpGet request = new HttpGet(requestUrl);
        	try{
        		requestStatus = "ping... ["+requestUrl+" ]";
        	    HttpResponse response = httpClient.execute(request);
        		requestStatus = "Sukses server ["+requestUrl+"]";
        		return true;
			}
			catch(Exception e) {
				e.printStackTrace();
				requestStatus = "Not Connected to server! SERVER DOWN!";
				return false;
			}
		} else {
			requestStatus = "Offline! Please enable Celluler/Wifi";
		}
		return false;
	}

	public static boolean cekConnection(Context context) {
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifinfo    = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobileinfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo active     = cm.getActiveNetworkInfo();
		
		if (wifinfo != null && wifinfo.isConnected()) {
			return true;
		}
		if (mobileinfo != null && mobileinfo.isConnected()) {
			return true;
		}
		if (active != null && active.isConnected()) {
			return true;
		}
		return false;
	}

	public void mainRequest(Context context) {
		CallWebPageTask task = new CallWebPageTask();
		task.applicationContext = context;
		task.main = requestAksi;
		task.execute(new String[] { requestUrl });

	}

	public String requestUpload() {
		String strSDPath = requestPath;
		String strUrlServer = requestUrl;
            
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		int resCode = 0;
		String resMessage = "";

		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "*****";
		String resServer = "";

        	
		try {
			/** Check file on SD Card ***/
			File file = new File(strSDPath);
			if(!file.exists())
			{
				resServer = "{\"StatusID\":\"0\",\"Error\":\"Please check path on SD Card\"}";
				return null;
			}

			FileInputStream fileInputStream = new FileInputStream(new File(strSDPath));

			URL url = new URL(strUrlServer);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");

			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type",
										"multipart/form-data;boundary=" + boundary);

			DataOutputStream outputStream = new DataOutputStream(conn
																	 .getOutputStream());
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"filUpload\";filename=\""+ strSDPath + "\"" + lineEnd);
			outputStream.writeBytes(lineEnd);

			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// Read file
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// Response Code and  Message
			resCode = conn.getResponseCode();
			if(resCode == HttpURLConnection.HTTP_OK)
			{
				InputStream is = conn.getInputStream();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				int read = 0;
				while ((read = is.read()) != -1) {
					bos.write(read);
				}
				byte[] result = bos.toByteArray();
				bos.close();
				resMessage = new String(result);
			}

			Log.d("resCode=",Integer.toString(resCode));
			Log.d("resMessage=",resMessage.toString());

			fileInputStream.close();
			outputStream.flush();
			outputStream.close();

			resServer = resMessage.toString();
			requestResultUpload = resServer;
		
		} catch (Exception ex) {
			// Exception handling
			requestResultUpload = "uploaderror";

			return null;
		}
		return resServer;
	}
	

	public String requestDownload() {
		InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        String error = "";

        try {
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP "+ connection.getResponseCode()+" "+connection.getResponseMessage();
            }

            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(requestPath);
            byte data[] = new byte[4096];
            long total = 0;
            int count;

            while ( (count=input.read(data)) != -1 ) {
                /*if (isCancelled()) {
                    input.close();
                    return null;
                }*/
                total += count;

                if (fileLength > 0) 
                	sizedownload = (int) (total * 100 / fileLength);
                    //publishProgress((int) (total*100/fileLength));
                output.write(data, 0, count);
            }

        } catch (IOException e) {
            error = e.toString();
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            }
            catch (IOException ioe) {

            }
            if (connection != null) connection.disconnect();
        }
        Log.i("yyyy", ""+error);
        return "ok : "+error;
	}
	
	//Mengirimkan data web keserver
	public String requestWeb(){
		String sret = "";
		HttpParams httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(httpParams, 50000);
	    HttpConnectionParams.setSoTimeout(httpParams, 50000);

        HttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpGet request = new HttpGet(requestUrl);
        try{
            HttpResponse response = httpClient.execute(request);

            try { // split result
         	   InputStream in = response.getEntity().getContent();
         	   BufferedReader reader = new BufferedReader(new InputStreamReader(in));
         	   StringBuilder str = new StringBuilder();
         	   String line = null;
         	   while((line = reader.readLine()) != null){
         	       str.append(line);
         	   }
         	   in.close();
         	   sret = str.toString();
        	} catch(Exception ex) {
        	  	Log.i(TAG, "Error split text");
        	}
        }
		catch(Exception ex){
			Log.i(TAG, "Failed rece Connect to Server!");
        }
        return sret;
    }

    private class CallWebPageTask extends AsyncTask<String, Void, String> 
	{
		protected Context applicationContext;
		protected String main = "";
		// connecting...
		@Override
		protected void onPreExecute() {}

	    @Override
	    protected String doInBackground(String... data) {
	    	//data[0] = url
	    	if (main.equals("web")) {
				return requestWeb();
			}
			else if (main.equals("download")){
				return requestDownload();
			}
			else if (main.equals("upload")) {
				return requestUpload();
			}
			return null;
	    }

	    // berhasil
	    @Override
	    protected void onPostExecute(String result) {
	    	if (main.equals("web")) {
		    	requestResult = result;
	    	}
	    	Log.i("yyyy", result);
		}
	}
}
