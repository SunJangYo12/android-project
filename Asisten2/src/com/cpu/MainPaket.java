package com.cpu;

import android.util.*;
import android.app.*;
import android.os.*;
import android.net.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.content.pm.*;
import java.io.*;
import java.util.*;
import com.cpu.init.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.view.View;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class MainPaket extends Activity {

	public static int tmpEdit = 0;
	public static String doc = "";
	public static String[] apk = {
				"com.UCMobile.intl",
				"com.gmail.heagoo.apkeditor.pro",
				"com.google.android.apps.translate",
				"com.mxtech.videoplayer.ad",
				"ru.zdevs.zarchiver",
				"jackpal.androidterm",
				"com.speedsoftware.rootexplorer",
				"ua.naiksoftware.phprunner",
				"com.aide.ui",
				"de.fun2code.android.lite.webdrive",
				"de.fun2code.android.pawserver",
				"app.greyshirts.firewall"
			};
	private EditText edit_alert, edit_alertU;
	private TextView text;
	private ListView list;
	private String[] aksiList;
	private int position = 0;
	private static int iList = 10;
	private static String url = "http://github.com";
	private static String titleAlert = "Pilih aksi";
	private MainMemori memori;
	private ReceiverBoot receAction;
	private ServerUtils utils;
	private ServiceBoot service;

	private Handler mHandler = new Handler();
	private Runnable mRefresh = new Runnable() {
		public void run() {
			Toast.makeText(MainPaket.this, "size: "+new ReceiverBoot().sizedownload, Toast.LENGTH_LONG).show();
			mHandler.postDelayed(mRefresh, 1000);
		}
	};

	//https://github.com/SunJangYo12/android_smali_project/raw/master/app.greyshirts.firewall.apk

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_paket);
        memori = new MainMemori();
        receAction = new ReceiverBoot();
        utils = new ServerUtils(this);
        service = new ServiceBoot();

		text = (TextView) findViewById(R.id.paket_status);
		list = (ListView) findViewById(R.id.paket_list);
        
		doc = utils.getDocFolder();
        //mHandler.postDelayed(mRefresh, 1000);
		init();

		if (memori.cursor.getCount() == 0) {
			for (int i=0; i<apk.length; i++) {
				//memori.setPaket("new", apk[i], "http://10.42.0.1/download.php?id=/root/Dokumen/android/apk/"+apk[i]+".apk", MainPaket.this);
				memori.setPaket("new", apk[i], "https://github.com/SunJangYo12/android_smali_project/raw/master/"+apk[i]+".apk", MainPaket.this);
			}
			init();
		}

		try {
			String apkf = getIntent().getStringExtra("apkf");
			if (apkf.equals("")) {

			}else {
				String[] aksi = {"Install", "Cancel Download"};
        			
            	AlertDialog.Builder builderIndex1 = new AlertDialog.Builder(MainPaket.this);
        		builderIndex1.setTitle("Buka Dengan...");
        		builderIndex1.setItems(aksi, new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int item) 
            		{
            			if (item == 0) {
            				Intent intent = new Intent(Intent.ACTION_VIEW);
        					intent.setDataAndType(Uri.fromFile(new File(doc+"/"+apkf)), "application/vnd.android.package-archive");
        					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        					startActivity(intent);
            				
            			} else if (item == 1) {
							receAction.sizedownload = 100;
							Toast.makeText(MainPaket.this, "CANCEL>>klik notifi untuk hapus notifikasi", Toast.LENGTH_LONG).show();
            			}
            		}
            	});
				builderIndex1.create().show();
				

				Toast.makeText(MainPaket.this, apkf, Toast.LENGTH_LONG).show();
			}
		}catch(Exception e) {}
        
	}
	@Override
	public void onPause() {
		super.onPause();
		//MainPaket.this.finish();
	}

	@Override
	public void onResume() {
		super.onResume();
		setContentView(R.layout.main_paket);
        memori = new MainMemori();
        receAction = new ReceiverBoot();
        utils = new ServerUtils(this);
        service = new ServiceBoot();

		text = (TextView) findViewById(R.id.paket_status);
		list = (ListView) findViewById(R.id.paket_list);
        
		doc = utils.getDocFolder();
        //mHandler.postDelayed(mRefresh, 1000);
		init();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//mHandler.removeCallbacks(mRefresh);
	}

	private void init() {
		try {
			memori = new MainMemori();

			list.setAdapter(memori.getPaket(MainPaket.this));
        	list.setSelected(true);
        	list.setOnItemLongClickListener(getLongPressListener());
        	list.setOnItemClickListener(new OnItemClickListener() {
        		public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {
        			position = arg2;
        			alertCek();
        		}
        	});
        	((ArrayAdapter)list.getAdapter()).notifyDataSetInvalidated();

        } catch(Exception e) {
        	MainPaket.this.finish();
        	Toast.makeText(MainPaket.this, "List kosong", Toast.LENGTH_LONG).show();
        }
	}

	private OnItemLongClickListener getLongPressListener() {
        return new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, final View view, int arg2, long arg3) 
            {
            	position = arg2;
            	alertAksi();

                return true;
            }

        };
    }

    private void alertCek() {
    	ArrayAdapter<String> exe = memori.getPaket(MainPaket.this);

		AlertDialog.Builder builderIndex = new AlertDialog.Builder(MainPaket.this);
		String depend = apkMana(memori.isPaket(position, 0, MainPaket.this), "cek", MainPaket.this);
		builderIndex.setTitle("~~~~"+depend+"~~~~");
		
		if (depend.equals("[  NOT INSTALLED!  ]")) {
			String[] aksi = {"Download"};
			builderIndex.setItems(aksi, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int item) 
            	{
            		String[] aksi = {"Browser Intent", "Download AL"};
        			
            		AlertDialog.Builder builderIndex1 = new AlertDialog.Builder(MainPaket.this);
        			builderIndex1.setTitle("Buka Dengan...");
        			builderIndex1.setItems(aksi, new DialogInterface.OnClickListener() {
            			public void onClick(DialogInterface dialog, int item) 
            			{
            				
            				if (item == 0) {
            					String apkDownload = memori.isPaket(position, 1, MainPaket.this)+"/"+memori.isPaket(position, 0, MainPaket.this)+".apk";
            					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(apkDownload)));
            				
            				} else if (item == 1) {
            					service.ifDownload = true;
            					service.apkDownload = memori.isPaket(position, 0, MainPaket.this)+".apk";
            					service.urlDownload = memori.isPaket(position, 1, MainPaket.this);
            					service.pathandname = doc+"/"+memori.isPaket(position, 0, MainPaket.this)+".apk";
            					
            					startService(new Intent(MainPaket.this, ServiceBoot.class));

            				}
            			}
            		});
					builderIndex1.create().show();
            	}
            });
			builderIndex.create().show();
		
		} else {
			builderIndex.create().show();
		}
    }

	private void alertAksi() {
		
    	ArrayAdapter<String> exe = memori.getPaket(MainPaket.this);

		String[] aksi = {"Download", "Rincian", "Add paket", "Delete! paket", "All Download"};
		AlertDialog.Builder builderIndex = new AlertDialog.Builder(MainPaket.this);
        
        builderIndex.setTitle(titleAlert+": "+memori.isPaket(position, 0, MainPaket.this));
        builderIndex.setItems(aksi, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) 
            {
                if (item == 0){
                	String[] aksi = {"Browser Intent", "Download AL"};
        			builderIndex.setTitle("Buka Dengan...");
        			builderIndex.setItems(aksi, new DialogInterface.OnClickListener() {
            			public void onClick(DialogInterface dialog, int item) 
            			{
            				if (item == 0) {
            					String apkDownload = memori.isPaket(position, 1, MainPaket.this)+"/"+memori.isPaket(position, 0, MainPaket.this)+".apk";
            					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(apkDownload)));
            				
            				} else if (item == 1) {
            					service.ifDownload = true;
            					service.apkDownload = memori.isPaket(position, 0, MainPaket.this)+".apk";
            					service.urlDownload = memori.isPaket(position, 1, MainPaket.this);
            					service.pathandname = doc+"/"+memori.isPaket(position, 0, MainPaket.this)+".apk";
            					
            					startService(new Intent(MainPaket.this, ServiceBoot.class));

            				}
            			}
            		});
        			builderIndex.create().show();

                } else if (item == 1) {
                	String[] rinci = {"Nama: \n"+memori.isPaket(position, 0, MainPaket.this),
                					 "URL: \n"+memori.isPaket(position, 1, MainPaket.this),
                					 "Status: \n"+apkMana(memori.isPaket(position, 0, MainPaket.this), "cek", MainPaket.this), 
                					 "Date: "+memori.isPaket(position, 2, MainPaket.this)
                	};
                	AlertDialog.Builder builderIndex1 = new AlertDialog.Builder(MainPaket.this);
					builderIndex1.setTitle("Rincian: "+position);
					builderIndex1.setItems(rinci, new DialogInterface.OnClickListener() {
            			public void onClick(DialogInterface dialog, int item) 
            			{
            				if (item == 0) {
								alertEdit(rinci[0], 0);
            				}
            				else if (item == 1) {
								alertEdit(rinci[1], 1);            					
            				}
            				else if (item == 2) {
								alertEdit(rinci[2], 2);            					
            				}
            			}
            		});
            		builderIndex1.create().show();

                } else if (item == 2) { //add
					alertAdd();
					init();

                } else if (item == 3) {
                    Toast.makeText(MainPaket.this, "Hapus: "+memori.titles.get(position), Toast.LENGTH_LONG).show();

                    memori.position = position;
                	memori.setPaket("rm", "", "", MainPaket.this);
					init();
                
                } else if (item == 4) {
                	String[] pilih = {"Semua", "Tidak terinstall"};
                	
                	AlertDialog.Builder builderIndex1 = new AlertDialog.Builder(MainPaket.this);
					builderIndex1.setTitle("Pilih aksi");
					builderIndex1.setItems(pilih, new DialogInterface.OnClickListener() {
            			public void onClick(DialogInterface dialog, int item) 
            			{
            				if (item == 0) {
								
            				}
            				else if (item == 1) {
            				}
            				
            			}
            		});
            		builderIndex1.create().show();
                }
            }
        });
        builderIndex.create().show();
	}

	private void alertEdit(String inEdit, int index) {
		AlertDialog.Builder builder1 = new AlertDialog.Builder(MainPaket.this);
		builder1.setTitle("Edit: "+inEdit);
		builder1.setCancelable(true);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE );
		View layout = inflater.inflate(R.layout.alert_memori, null);

		edit_alert = (EditText) layout.findViewById(R.id.amEdit);
		edit_alert.setText(memori.isPaket(position, index, MainPaket.this));

		Button bt = (Button) layout.findViewById(R.id.amButton);
		bt.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				String edit = edit_alert.getText().toString();
				if (index == 0) {
					memori.position = position;
					memori.setPaket("edit", edit, memori.isPaket(position, 1, MainPaket.this), MainPaket.this);
				}
				if (index == 1) {
					memori.position = position;
					memori.setPaket("edit", memori.isPaket(position, 0, MainPaket.this), edit, MainPaket.this);
				}
				if (index == 2) {
					Toast.makeText(MainPaket.this, "Auto edit", Toast.LENGTH_LONG).show();
				}
				if (index == 3) {
					Toast.makeText(MainPaket.this, "Auto edit", Toast.LENGTH_LONG).show();
				}
				init();
			}
		});
		builder1.setView(layout);
		AlertDialog alert11 = builder1.create();
		alert11.show();
	}

	private void alertAdd() {
		AlertDialog.Builder builder1 = new AlertDialog.Builder(MainPaket.this);
		AlertDialog alert11 = builder1.create();
		
		builder1.setTitle("                  Edit                ");
		builder1.setCancelable(true);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE );
		View layout = inflater.inflate(R.layout.alert_paket, null);

		edit_alert = (EditText) layout.findViewById(R.id.alert_paket_nama);
		edit_alertU = (EditText) layout.findViewById(R.id.alert_paket_url);
		edit_alert.setHint("Nama apk contoh com.google.translete");
		edit_alertU.setHint("URL apk, pastikan url yang langsung men-download dan nama harus cocok");

		Button bt = (Button) layout.findViewById(R.id.alert_paket_button);
		bt.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				memori.setPaket("new", edit_alert.getText().toString(), edit_alert.getText().toString(), MainPaket.this);
				init();
			}
		});
		builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) 
            {
            }
        });
		builder1.setView(layout);
		alert11.show();
	}

	public boolean ping(Context context) {
		if (cekConnection(context)) {
        	text.setText("ping... ["+url+" ]");

			HttpParams httpParams = new BasicHttpParams();
	    	HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
	    	HttpConnectionParams.setSoTimeout(httpParams, 10000);

        	HttpClient httpClient = new DefaultHttpClient(httpParams);
        	HttpGet request = new HttpGet(url);
        	try{
        		text.setText("ping... ["+url+" ]");
        	    HttpResponse response = httpClient.execute(request);
        		text.setText("Sukses server ["+url+"]");
        		return true;
			}
			catch(Exception e) {
				e.printStackTrace();
				text.setText("Not Connected to server! SERVER DOWN!");
				return false;
			}
		} else {
			text.setText("Offline! Please enable Celluler/Wifi");
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

	public void downloadFile(String downurl, String pathDname, Context context) {
		
	}

	public String apkMana(String packageName, String pilih, Context context) {
		PackageManager manager = context.getPackageManager();

		if (pilih.equals("open")) {
			try {
				Intent i = manager.getLaunchIntentForPackage(packageName);
				if (i == null) {
					return "[ FAILED! ]";
				}
				i.addCategory(Intent.CATEGORY_LAUNCHER);
				context.startActivity(i);
				return "[ SUKSES ]";
			} 
			catch (Exception e) {
				return "[ ERR!: ]"+e;
			}
		}
		else if (pilih.equals("cek")) {
			try {
				manager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
				return "[  INSTALLED  ]";
			}catch(PackageManager.NameNotFoundException e) {
				return "[  NOT INSTALLED!  ]";
			}
		} 
		else if (pilih.equals("pull")) {
			final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			mainIntent.setPackage(packageName);
			mainIntent.setFlags(ApplicationInfo.FLAG_ALLOW_BACKUP);
			final List pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
			for (Object object : pkgAppsList) {
				ResolveInfo info = (ResolveInfo) object;
				if ( info.activityInfo.applicationInfo.packageName == null ) {
					return "[ APK NOT FOUND! ]";
				}

				File file = new File(info.activityInfo.applicationInfo.publicSourceDir);
				File dest = new File("/storage" + info.activityInfo.applicationInfo.packageName + ".apk");
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

 					return "[ SUKSES_PULL ]";
				} 
				catch (IOException e) {
					return "[ FAILED_PULL! ]";
				}
			}
		}
		return null;
	}

}