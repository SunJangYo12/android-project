package com.cpu;

import android.app.*;
import android.widget.*;
import android.content.*;
import android.os.*;
import com.cpu.init.*;
import java.util.*;
import com.cpu.input.*;
import android.speech.*;
import android.speech.tts.*;
import java.text.*;
import android.media.*;
import com.cpu.init.*;
import com.tools.*;
import android.view.*;
import com.status.*;
import android.graphics.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.*;
import android.util.*;
import android.content.pm.*;
import com.cpu.memori.*;
import android.speech.tts.*;
import java.util.*;
import android.database.sqlite.*;
import android.database.*;
import android.net.*;

public class MainAsisten extends Activity implements TextToSpeech.OnInitListener
{ 
	public String text;
	private SpeechRecognizerManager mSpeechManager;
	public String help = "WAJIB INSTALL GOOGLE SEARCH TERBARU\n"+
						" LALU BUKA PENGATURAN/BAHASA&MASUKAN/PENGETIKAN GOOGLE VOICE/PENGENALAN UCAPAN OFFLINE/INSTALL BAHASA INDONESIA\n\n\n\n"+
						"1. waktu:\n -cuaca -> (di) misal wonosobo (refresh) untuk perbarui data cuaca (hari -> (ini) (besok) )"+
									"-hari ngomong hari ini\n"+
									"-tanggal hari ini\n"+
									"-jam ngomong jam sekarang\n\n\n"+
						 "2. hemat:\n on/off pendengaran, jika dipanggil maka off dan jika dipanggil lagi on\n\n\n"+
						 "3. ngobrol\n: -halo untuk ngtes\n"+
						 			" -bantuan lihat bantuan\n"+
						 			" -al memenggil nama komputer dan akan menyala\n"+
						 			" -istirahat untuk mematikan sistem pendengaran dan penglihatan\n"+
						 			" -buku untuk membuka buku terutama apliaski kiwik\n"+
						 			" -musik untuk membuka aplikasi musik\n"+
						 			" -upload untuk backup file hasil download ke server local (text:paste text dari cliboard\n"+
						 			" -web untuk mencari informasi\n"+
						 			" -catatan -> (masukkan:buat catatan) (tampilakan:lihat catatan) (hapus:tahan list untuk menghapus catatan)\n\n\n"+
						 "4. alat:\n   -senter -> (berkedip:senter akan kedip2)\n"+
						 			" -kompas membuka kompas\n\n\n"+
						 "5. status:\n -status -> (proses:lihat persen cpu) (aplikasi:list semua aplikasi) (baterai:(tegangan) (arus) (suhu) )\n\n\n"+
						 "6. file:\n   -file -> (cari: (bernama) (format) (external/internal) )\n\n\n"+
						 "7. pengaturan:\n configure semua\n\n\n"+
						 "8. install:\n install paket penting\n\n\n"+
						 "9. apkmanager:\n copy/buka apk";
	
	Intent intent, intentMic, intentTTS;
	public String dataSuara;
	Vibrator mVibrator;
	private static String[] dataSpeech;
	private String dataSpeech1;
	private static String temp;
	SharedPreferences settings;
	SharedPreferences.Editor addSettings;
	
	ServiceTTS sertt;
	TextView txt;
	AutoCompleteTextView edt;
	String ulangi, ngobrol; 
	protected AudioManager mAudioManager;
	
	int judul = 0;
	
	private String mode = "";
	private static int noDB = 0;
	private boolean initialized;
	private String queuedText;
	private TextToSpeech tts;
	private String kata;
	private int no;
	protected Cursor cursor;
	private ReceiverBoot rece;
	private Installer installator;
	private ServerUtils utils;
	private String installApp;
	private String installExApp;
	public static final int INSTALL_OK = 1;
    public static final int INSTALL_ERR = -1;
    protected MainMemori memori;

	/*@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(
				WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
		this.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
						);

		super.onAttachedToWindow();
	}*/

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);


		mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		txt = (TextView)findViewById(R.id.main_text);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		edt = (AutoCompleteTextView) findViewById(R.id.main_edit);
		memori = new MainMemori();
		utils = new ServerUtils(this);
		installApp = utils.getPathToInstallServer();
		installExApp = utils.getDocFolder();
		rece = new ReceiverBoot();

		ArrayAdapter<String> utilPaket = memori.getPaket(this);
		MainPaket paket = new MainPaket();

		if (memori.cursor.getCount() == 0) {
			for (int i=0; i<paket.apk.length; i++) {
				//memori.setPaket("new", paket.apk[i], "https://github.com/SunJangYo12/android_smali_project/raw/master/"+paket.apk[i]+".apk", this);
			}
		}

		edt.setAdapter(memori.getHistory(this));

		startService(new Intent(this, ServiceBoot.class));
		startService(new Intent(this, ServiceStatus.class));
		startService(new Intent(this, AudioPreview.class));

		String[] aksi ={"Browser","File Manager","Terminal Emulator","Console... [text]","Console... [speech]"};
		AlertDialog.Builder builderIndex = new AlertDialog.Builder(MainAsisten.this);
		builderIndex.setTitle("pilih aksi");
		builderIndex.setItems(aksi, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (item == 0){
					Intent ib = new Intent(getBaseContext(), MainBrowser.class);
					ib.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getBaseContext().startActivity(ib);
					finish();
				}
				else if (item == 1) {
					Intent ifm = new Intent(getBaseContext(), MainFileManager.class);
					ifm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getBaseContext().startActivity(ifm);
					finish();
				} 
				else if (item == 2) {
					Intent it = new Intent(getBaseContext(), MainTerminal.class);
					it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getBaseContext().startActivity(it);
					finish();
				} 
				else if (item == 3) {
					inputan();
				} 
				else if (item == 4) {
					mode = "speech";
					inputan();
				}
			}
		});
		builderIndex.create().show();
		
		try{
			if (getIntent().getStringExtra("layar").equals("off"))
			{
				if(mSpeechManager==null)
				{
					SetSpeechListener();
				}
				else if(!mSpeechManager.ismIsListening())
				{
					mSpeechManager.destroy();
					SetSpeechListener();
				}
			}
			if (getIntent().getStringExtra("layar").equals("on"))
			{
				finish();
			}
		}
		catch(Exception e){
			try {
				if (getIntent().getStringExtra("main").equals("SERVER_LOCAL")){
					startActivity(new Intent(this, ScreenOn.class));
					
					long[] patern = {0, 200, 500, 200};
					mVibrator.vibrate(patern, 0);
					txt.setTextSize(30);
					txt.setText("server LOCAL connected!\n\nsilahkan masukan upload jika ada file baru\nklik button jika tidak ada!");
					Toast.makeText(this, "tekan button untuk meredam getaran!\nkalo ada file terbaru silahkan masukan upload!", Toast.LENGTH_LONG).show();
				}
			} catch(Exception e1) {}
		}
	}

	@Override
	protected void onPause()
	{
		// TODO: Implement this method
		super.onPause();
		//startService(new Intent(getBaseContext(), ServiceTTS.class));
	}

	@Override
	public void onBackPressed()
	{
		// TODO: Implement this method
		super.onBackPressed();
		if(mSpeechManager!=null)
		{
			Toast.makeText(this,"destroy", Toast.LENGTH_LONG).show();
			mSpeechManager.destroy();
			mSpeechManager = null;
		}
		mVibrator.cancel();
		finish();
	}

	@Override
	protected void onDestroy()
	{
		// TODO: Implement this method
		super.onDestroy();
		
		if(mSpeechManager!=null)
		{
			Toast.makeText(this,"destroy", Toast.LENGTH_LONG).show();
			mSpeechManager.destroy();
			mSpeechManager = null;
		}
		ServiceTTS sertts = new ServiceTTS();
		sertts.cepat = 1.0f;
		sertts.str = "";
		startService(new Intent(this, ServiceTTS.class));
		
		
	}

	Handler handlerInstallServer = new Handler() {
        @Override
        public void handleMessage(android.os.Message message) {
            //Log.i("Main", "handleMessage with " + message.what);
            switch (message.what) {
                case INSTALL_OK:
                    Toast.makeText(MainAsisten.this, "Install OK.", Toast.LENGTH_LONG).show();
                    break;
                case INSTALL_ERR:
                    Toast.makeText(MainAsisten.this, "Install Failed!", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

	public void install() {
		installator = new Installer(this, handlerInstallServer, true);
        installator.execute("data.zip", installApp, installExApp);
	}

	public void inputan() {
		txt.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v){
					keluaran(edt.getText().toString());
				}
			});
		if (mode.equals("speech")){
			mVibrator.cancel();
			rece.btnServer = true;
			rece.dataServer = "ora";

			if(mSpeechManager==null){
				SetSpeechListener();
			} else if (!mSpeechManager.ismIsListening() ) {
				mSpeechManager.destroy();
				SetSpeechListener();
			}
			txt.setTextSize(30);
			txt.setText("Voice siap");
		} else {
			txt.setText("Console Edit");
			edt.setOnKeyListener(new View.OnKeyListener() {
            	public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
                	if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    	mVibrator.cancel();
						rece.btnServer = true;
						rece.dataServer = "ora";
						txt.setTextSize(30);
						txt.setText(edt.getText().toString());
						keluaran(edt.getText().toString());
                    	return true;
                	}
                	return false;
            	}
        	});
		}
	}

	
	private void SetSpeechListener()
    {
        mSpeechManager=new SpeechRecognizerManager(this, new SpeechRecognizerManager.onResultsReady() {
				@Override
				public void onResults(ArrayList<String> results)
				{
					SetSpeechListener();
					if(results!=null && results.size()>0){
						if(results.size()==1){
							mSpeechManager.destroy();
							mSpeechManager = null;
							txt.setText(results.get(0));
							keluaran(results.get(0));
						} else {
							StringBuilder sb = new StringBuilder();
							if (results.size() > 5) {
								results = (ArrayList<String>) results.subList(0, 5);
							}
							for (String result : results) {
								sb.append(result).append("\n");
							}
							txt.setText(sb.toString());
						}
					} else txt.setText("tidak ada hasil");
				}
			});
    }
	
	public void ngomong(String data, float cepat)
	{
		//SharedPreferences.Editor editor = settings.edit();	
		//editor.putBoolean("ctrMic", false);	
		//editor.commit();
		
		//tts.setSpeechRate(cepat);
		//speak(data);
	}
	

	public String getWeton(int index){
		final KalenderKu kal = new KalenderKu();  
		Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);  
        int mMonth = c.get(Calendar.MONTH);  
        int mDay = c.get(Calendar.DAY_OF_MONTH);  

        String[] jawa = kal.MasehiToJawa(mYear, mMonth, mDay);// (mDay, mMonth, mYear);  

		return jawa[index];
	}

	public void keluaran(String data)
	{
		tts = new TextToSpeech(this /* context */, this /* listener */);
		tts.setOnUtteranceProgressListener(mProgressListener);
	
		dataSpeech = data.split(" ");
		dataSpeech1 = data;
		temp = "";
		
		for (int a=0; a<dataSpeech.length; a++)
		{
			outStatus(a);
			outFile(a);
			outAlat(a);
			outNgobrol(a);
			outPengaturan(a);
			outWaktu(a);
		}
		memori.setHistory("new", data, "", MainAsisten.this);
	}
	private void outWaktu(int index)
	{
		if (dataSpeech[index].equals("cuaca"))
		{
			
			for(int a=0; a<dataSpeech.length; a++){
				if (dataSpeech[a].equals("di"))
				{
					SharedPreferences.Editor editor = settings.edit();	
					editor.putString("cuaTempat", dataSpeech1.substring(9, dataSpeech1.length()));	
					editor.commit();
				}
				if (dataSpeech[a].equals("refresh")){
					String negara = settings.getString("cuaNegara","");
					String tempat = settings.getString("cuaTempat","");
					String surl = "https://sunjangyo12.000webhostapp.com/cuaca.php/";
					String url = surl+"?negara="+negara+"&tempat="+tempat;
					
					CallWebPageTask task = new CallWebPageTask();
					task.applicationContext = MainAsisten.this;
					task.execute(new String[] { url });
					
					
					ngomong("baik tuan. cuaca direfresh. silahkan tunggu",0.9f);
				}
				if (dataSpeech[a].equals("hari")){
					for (int b=0; b<dataSpeech.length; b++)
					{
						if (dataSpeech[b].equals("ini")){
							
						}
						if (dataSpeech[b].equals("besok")){

						}
					}
				}
			}	
		}
		if(dataSpeech[index].equals("hari")){
			txt.setText(getWeton(0)+","+getWeton(4));
			txt.setTextColor(Color.WHITE);
			txt.setTextSize(30);

			ngomong(getWeton(0)+","+getWeton(4), 0.8f);
		}
		if(dataSpeech[index].equals("tanggal")){
			String tanggal = new SimpleDateFormat("  ,dd,MMM,yyy").format(new Date());

			txt.setText(getWeton(0)+","+tanggal);
			txt.setTextColor(Color.WHITE);
			txt.setTextSize(30);
			
			ngomong(getWeton(0)+","+tanggal, 0.8f);
		}
		if (dataSpeech[index].equals("jam")){
			String jam = new SimpleDateFormat("HH:mm").format(new Date());
			ngomong(jam,0.8f);
			txt.setText(jam);
		}
	}
	private void outPengaturan(int index)
	{
		settings = getSharedPreferences("Settings", 0);	
		SharedPreferences.Editor editor = settings.edit();	
		
		if (dataSpeech[index].equals("hemat")){
			if(settings.getBoolean("mode hemat",true))
			{
				ngomong("sistem pendengaran di matikan", 0.9f);
				editor.putBoolean("mode hemat", false);	
				editor.commit();
			}
			else{
				editor.putBoolean("mode hemat", true);	
				editor.commit();
				ngomong("melakukan reboot pendengaran", 0.9f);
			}
		}
		else if (dataSpeech[index].equals("pengaturan")) {
			startActivity(new Intent(this, Pengaturan.class));
		}
		else if (dataSpeech[index].equals("install")) {
			startActivity(new Intent(this, MainPaket.class));
		}
		
	}

	private void outNgobrol(int index)
	{
		
		if (dataSpeech[index].equals("halo")){
			ngomong("ya halo selamat malam mas", 0.9f);
		}
		if (dataSpeech[index].equals("bantuan") || dataSpeech[index].equals("help")) {
			txt.setText(help);
			txt.setTextSize(9);

			ngomong("bantuan ditampilkan", 0.9f);
		}
		if (dataSpeech[index].equals("upload")) {
			for (int u=0; u<dataSpeech.length; u++) {
				if (dataSpeech[u].equals("text")) {
					Intent ut = new Intent(getBaseContext(), MainBrowser.class);
					ut.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					ut.putExtra("upload","text");
					getBaseContext().startActivity(ut);
					ngomong("upload file text", 0.9f);
				} else {
					Intent iu = new Intent(Intent.ACTION_VIEW, Uri.parse("http://10.42.0.1/fileman.php"));
					startActivity(iu);
					ngomong("oke siap backup file", 0.9f);
				}

			}
		}
		if (dataSpeech[index].equals("al")){
			ngomong("ya tuan", 0.9f);
			startActivity(new Intent(this, ScreenOn.class));
		}
		if (dataSpeech[index].equals("istirahat")){
			ngomong("sistem stenbay", 0.9f);
			if(mSpeechManager!=null)
			{
				Toast.makeText(this,"destroy", Toast.LENGTH_LONG).show();
				mSpeechManager.destroy();
				mSpeechManager = null;
			}
			finish();
		}
		if (dataSpeech[index].equals("buku")){
			
			if (openApp(this, "org.kiwix.kiwixmobile")){
				
				Toast.makeText(this,"sukses",Toast.LENGTH_LONG).show();
			}
			else{
				Toast.makeText(this,"buku gagal silahkan download kiwix.apk",Toast.LENGTH_LONG).show();
				
			}
		}
		if (dataSpeech[index].equals("musik")){
			Intent intent = new Intent();  
			intent.setAction(android.content.Intent.ACTION_VIEW);  
			intent.setDataAndType(Uri.parse("sdcard1/audio"), "audio/*");  
			startActivity(intent);
		}
		if (dataSpeech[index].equals("web")) {
			for (int w=0; w<dataSpeech.length; w++) {
				if (dataSpeech[w].equals("")) {
					Intent iu = new Intent(Intent.ACTION_VIEW, Uri.parse("https://http-www-bing-com.0.freebasics.com/search?iorg_service_id_internal=803478443041409%3BAfrEX0ng8fF-69Ni&iorgbsid=AZwOf5p9ZGHdo4ma-_4xLROJiPP57wR4JxMMMfZYMk2RHTXt0k_suZhZX4ELlv0Xo8d0A99ibKz2Zk2OYsINpLd4&q=sunjangyo12 github&go=Search&qs=ds&form=QBRE&pc=FBIO"));
					startActivity(iu);
					ngomong("status url adalah bing.com", 0.9f);
				} else {
					Intent iu = new Intent(Intent.ACTION_VIEW, Uri.parse("https://http-www-bing-com.0.freebasics.com/search?iorg_service_id_internal=803478443041409%3BAfrEX0ng8fF-69Ni&iorgbsid=AZwOf5p9ZGHdo4ma-_4xLROJiPP57wR4JxMMMfZYMk2RHTXt0k_suZhZX4ELlv0Xo8d0A99ibKz2Zk2OYsINpLd4&q="+dataSpeech[w]+"&go=Search&qs=ds&form=QBRE&pc=FBIO"));
					startActivity(iu);
					ngomong("status url adalah bing.com mencari "+dataSpeech[w], 0.9f);
				}
			}
		}
		
		if (dataSpeech[index].equals("catatan")){
			for (int a=0; a<dataSpeech.length; a++)
			{
				if (dataSpeech[a].equals("masukkan")){
					String hasil = dataSpeech1.substring(16, dataSpeech1.length());

					ngomong("menulis catatan", 0.9f);
					
					memori.setCatatan("new", hasil, "keterangan", MainAsisten.this);

					Toast.makeText(MainAsisten.this, "catatan dibuat", Toast.LENGTH_LONG).show();
				}
				if (dataSpeech[a].equals("tampilkan")){
					startActivity(new Intent(this, MainCatatan.class));
				}
				if (dataSpeech[a].equals("hapus")){
					ngomong("tahan list list yang akan dihapus", 0.9f);
					
				}
			}
		}
		
	}
	
	private void outAlat(int index)
	{
		if (dataSpeech[index].equals("senter")  ||  dataSpeech[index].equals("berkedip")){
			if (dataSpeech[index].equals("berkedip")){
		    	for (int i=0; i<18; i++)
		    	{
			    	CountDownTimer hitungMundur = new CountDownTimer(200, 100)
			    	{
				    	public void onTick(long millisUntilFinished){
				     	}
				     	public void onFinish()
				    	{
					    	Senter s = new Senter();
					    	s.runingKu();
					    }
			    	}.start();
		    	}
			}
			else{
		    	Senter s = new Senter();
		    	s.runingKu();
			}
		}
		if (dataSpeech[index].equals("kompas")){
			startActivity(new Intent(this, Kompas.class));
		}
		if (dataSpeech[index].equals("apkmanager")){
			new MainTouchAsisten().setIndex = false;
			startActivity(new Intent(this, MainTouchAsisten.class));
		}
	}

	
	private void outStatus(int index)
	{
		if (dataSpeech[index].equals("status")){
			temp = "status";
			String tempStatus = "";
			for (int b=0; b<dataSpeech.length; b++)
			{
				if (dataSpeech[b].equals("proses")){
					tempStatus = "cpu";
					ngomong(new CpuMon().cpuPakai+", mas", 0.8f);
				}
				if (dataSpeech[b].equals("aplikasi")){
					tempStatus = "aplikasi";
					ngomong("ini status aplikasi yang berjalan, mas", 0.8f);
					startActivity(new Intent(this, TaskList.class));
				}
				if (dataSpeech[b].equals("baterai")){
					for (int c=0; c<dataSpeech.length; c++)
					{
						if (dataSpeech[c].equals("tegangan")){
							ngomong("tegangan baterai = "+new ReceiverBoot().dataVolt,0.9f);
						}
						if (dataSpeech[c].equals("arus")){
							ngomong("arus baterai= "+new ReceiverBoot().dataAmp,0.9f);
						}
						if (dataSpeech[c].equals("suhu")){
							ngomong("suhu baterai = "+new ReceiverBoot().dataTemp,0.9f);
						}
					}
				}
			}
			if (tempStatus.equals("")){
				startActivity(new Intent(this, ActivityStatus.class));
			}
		}
	}
	
	private void outFile(int index)
	{
		if(dataSpeech[index].equals("file"))
		{
			temp = "file";
			String tempFile = "";
			String[] format ={"zip","rar","dltemp","mp4","mp3","jpg","wav","mht","txt","pdf","doc"};

			for(int c=0; c<dataSpeech.length; c++)
			{
				if (dataSpeech[c].equals("cari")){
					for (int d=0; d<dataSpeech.length; d++)
					{
						tempFile = "cari";

						if (dataSpeech[d].equals("bernama")){
							Intent inama = new Intent(MainAsisten.this, FileExploler.class);
							txt.setText("Tunggu...");
							txt.setTextColor(Color.RED);
							txt.setTextSize(30);
							
							String ngomongMemori = "";
							for (int e=0; e<dataSpeech.length; e++)
							{
								inama.putExtra("isi",dataSpeech[3]);
							}
							for (int f=0; f<dataSpeech.length; f++)
							{
								if (dataSpeech[f].equals("external")){
									inama.putExtra("memori","ex");
								}
								if (dataSpeech[f].equals("internal")){
									inama.putExtra("memori","in");
								}
							}
							ngomong("file "+dataSpeech[3]+", sedang dicari "+ngomongMemori,0.9f);
							
							inama.putExtra("index","cari nama");
							startActivity(inama);
						}
						else if (dataSpeech[d].equals("format")){
							for (int e=0; e<dataSpeech.length; e++)
							{
								for (int fe=0; fe<format.length; fe++)
								{
									if (dataSpeech[e].equals(format[fe])){
										txt.setText("Tunggu...");
										txt.setTextColor(Color.RED);
										txt.setTextSize(30);
										
										Intent iformat = new Intent(MainAsisten.this, FileExploler.class);
										String ngomongMemori = "";
										
										for (int f=0; f<dataSpeech.length; f++)
										{
											if (dataSpeech[f].equals("external")){
												iformat.putExtra("memori","ex");
											}
											if (dataSpeech[f].equals("internal")){
												iformat.putExtra("memori","in");
											}
										}
										ngomong("file "+format[fe]+", sedang dicari "+ngomongMemori, 1.0f);
										
										iformat.putExtra("format",format[fe]);
										iformat.putExtra("index","cari format");
										startActivity(iformat);
									}
								}
							}

						}
					}

				}
			}

			if (tempFile.equals("")){
				
				startActivity(new Intent(MainAsisten.this, FileExploler.class));
			}
		}// for c
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
			sret= "No Internet !!!";
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
            result = "Error";
        }
        return result;
    }
	// Class untuk implementasi class AscyncTask
	private class CallWebPageTask extends AsyncTask<String, Void, String> {

		private ProgressDialog dialog;
		protected Context applicationContext;

		@Override
		protected void onPreExecute() {
			this.dialog = ProgressDialog.show(applicationContext, "Server Process", "Please Wait...", true);
		}

	    @Override
	    protected String doInBackground(String... urls) {
			String response = "";
			response = getRequest(urls[0]);
			return response;
	    }

	    @Override
	    protected void onPostExecute(String result) {
	    	this.dialog.cancel();
	    	txt.setText(result);
			
			//SQLiteDatabase dbk = dbhelper.getWritableDatabase();
			//dbk.execSQL("INSERT INTO otak (no, cuaca) VALUES ('"+no+"', '"+result+"');");

			Toast.makeText(getApplicationContext(), "Berhasil", Toast.LENGTH_LONG).show();
		}
	}
	
	// mtod app external
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
	
	public void speak(String text) { 

		if (!initialized) {
			queuedText = text;
			return;
		}
		queuedText = null;

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
		tts.speak(text, TextToSpeech.QUEUE_ADD, map);
	}
	
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			initialized = true;
			tts.setLanguage(Locale.getDefault());

			if (queuedText != null) {
				speak(queuedText);
			}
		}
	}
	
	// tts
	private abstract class runnable implements Runnable {
	}
	
	// tts
	private UtteranceProgressListener mProgressListener = new UtteranceProgressListener() {
		@Override
		public void onStart(String utteranceId) {
		} // Do nothing

		@Override
		public void onError(String utteranceId) {
		} // Do nothing.

		@Override
		public void onDone(String utteranceId) {

			new Thread()
			{
				public void run()
				{
					MainAsisten.this.runOnUiThread(new runnable()
						{
							public void run()
							{

								Toast.makeText(getBaseContext(), "TTS Completed", Toast.LENGTH_SHORT).show();
								SharedPreferences.Editor editor = settings.edit();	
								editor.putBoolean("ctrMic", true);	
								editor.commit();
								
								if(mSpeechManager==null)
								{
									SetSpeechListener();
								}
								else if(!mSpeechManager.ismIsListening())
								{
									mSpeechManager.destroy();
									SetSpeechListener();
								}
							}
						});
				}
			}.start();

		}
	}; 
}


class SpeechRecognizerManager {

    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent intent;

    protected boolean mIsListening;
    private boolean mIsStreamSolo; 
	SharedPreferences settings;
	

    public boolean mute=false;
    private final static String TAG="SpeechRecognizerManager";

    private onResultsReady mListener;

	Context context;
    public SpeechRecognizerManager(Context context,onResultsReady listener)
    {
		this.context = context;
		settings = context.getSharedPreferences("Settings", 0);	
		
        try{
            mListener=listener;
        }
        catch(ClassCastException e)
        {
            Log.e(TAG,e.toString());
        }
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
		
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
		
        startListening();

    }

    private void listenAgain()
    {
        if(mIsListening) {
            mIsListening = false;
            mSpeechRecognizer.cancel();
			if(settings.getBoolean("ctrMic",true)){
				startListening();
			}
        
        }
    }


    private void startListening()
    {
        if(!mIsListening)
        {
            mIsListening = true; 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                // turn off beep sound
                
            }
            mSpeechRecognizer.startListening(intent);
        }
    }

    public void destroy()
    {
        if (mSpeechRecognizer != null)
        {
            mSpeechRecognizer.stopListening();
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();
            mSpeechRecognizer=null;
        }

    }

    protected class SpeechRecognitionListener implements RecognitionListener
    {

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onBufferReceived(byte[] buffer)
        {

        }

        @Override
        public void onEndOfSpeech()
        {}

        @Override
        public synchronized void onError(int error)
        {

            if(error==SpeechRecognizer.ERROR_RECOGNIZER_BUSY)
            {
                if(mListener!=null) {
                    ArrayList<String> errorList=new ArrayList<String>(1);
                    errorList.add("ERROR RECOGNIZER BUSY");
                    if(mListener!=null)
						mListener.onResults(errorList);
                }
                return;
            }

            if(error==SpeechRecognizer.ERROR_NO_MATCH)
            {
                if(mListener!=null)
                    mListener.onResults(null);
            }

            if(error==SpeechRecognizer.ERROR_NETWORK)
            {
                ArrayList<String> errorList=new ArrayList<String>(1);
                errorList.add("STOPPED LISTENING");
                if(mListener!=null)
                    mListener.onResults(errorList);
            }
            Log.d(TAG, "error = " + error);
           	new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						listenAgain();
					}
				},100);

        }

        @Override
        public void onEvent(int eventType, Bundle params)
        {

        }

        @Override
        public void onPartialResults(Bundle partialResults)
        {

        }

        @Override
        public void onReadyForSpeech(Bundle params) {}

        @Override
        public void onResults(Bundle results)
        {
            if(results!=null && mListener!=null)
				mListener.onResults(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
            listenAgain();

        }

        @Override
        public void onRmsChanged(float rmsdB) {}

    }

    public boolean ismIsListening() {
        return mIsListening;
    }


    public interface onResultsReady
    {
        public void onResults(ArrayList<String> results);
    }

}
