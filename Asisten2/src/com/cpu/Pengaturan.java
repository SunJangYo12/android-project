package com.cpu;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.speech.tts.*;
import java.util.*;
import com.tools.*;
import com.status.*;

public class Pengaturan extends Activity
{
	private Button btnTelinga, aSave, btnMulut, btnDir, btnCuaca, btnServer, btnEditor;
	private Button btnTouch, btnCatatan;
	private Switch swMicControl, swSuaraControl;
	private EditText edCuaca, edCuaca1;
	public TextToSpeech textToSpeech;
	private EditText ed;
	private RadioButton radioB, radioB2;
	private String title, isi;
	private int index;
	private SharedPreferences settings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_pengaturan);
		main();
	}

	public void main() {
		btnTelinga = (Button)findViewById(R.id.btn_Ptelinga);
		btnTelinga.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v){
					telinga(1);
				}
			});
		settings = getSharedPreferences("Settings", 0);

		btnServer = (Button)findViewById(R.id.btn_local);
		btnServer.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v){
					startActivity(new Intent(Pengaturan.this, MainServer.class));
				}
			});
		
		/*swMicControl = (Switch)findViewById(R.id.sw_micService);
		swMicControl.setChecked(settings.getBoolean("mode hemat",false));
		swMicControl.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v){
					SharedPreferences.Editor editor = settings.edit();	
					
					if (swMicControl.isChecked()) {
						editor.putBoolean("mode hemat", true);	
						editor.commit();
						
					}
					else{
						editor.putBoolean("Mode hemat", false);	
						editor.commit();
					}
					Toast.makeText(getBaseContext(), "disimpan "+settings.getBoolean("Mic Control", true), Toast.LENGTH_LONG).show();

				}
			});
		swSuaraControl = (Switch)findViewById(R.id.sw_suara);
		swSuaraControl.setChecked(settings.getBoolean("suara",false));
		swSuaraControl.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v){
					SharedPreferences.Editor editor = settings.edit();	
					
					if (swMicControl.isChecked()) {
						editor.putBoolean("suara", true);	
						editor.commit();
						
					}
					else{
						editor.putBoolean("suara", false);	
						editor.commit();
					}
					Toast.makeText(getBaseContext(), "disimpan "+settings.getBoolean("Mic Control", true), Toast.LENGTH_LONG).show();

				}
			});*/
		btnMulut = (Button)findViewById(R.id.btn_Pmulut);
		btnMulut.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v){
					ServiceTTS sertts = new ServiceTTS();
					sertts.cepat = 1.0f;
					sertts.str = "selamat datang";
					startService(new Intent(Pengaturan.this, ServiceTTS.class));
					
				}
			});
		
		btnDir = (Button)findViewById(R.id.btn_Pdir);
		btnDir.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v){
					//telinga(3);
					startActivity(new Intent(Pengaturan.this, FileExploler.class));

				}
			});
		edCuaca = (EditText)findViewById(R.id.edt_tempatC);
	   	edCuaca1 = (EditText)findViewById(R.id.edt_negaraC);
		
		edCuaca.setText(settings.getString("cuaTempat",""));
		edCuaca1.setText(settings.getString("cuaNegara",""));
		if (settings.getString("cuaTempat","").equals("") && settings.getString("cuaNegara","").equals("")){
			//Toast.makeText(this,"silahkan isikan tempat dan negara cuaca",Toast.LENGTH_LONG).show();
		}
		btnCuaca = (Button)findViewById(R.id.btn_okeC);
		btnCuaca.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v){
					SharedPreferences.Editor editor = settings.edit();	
					editor.putString("cuaNegara", edCuaca1.getText().toString());	
					editor.putString("cuaTempat", edCuaca.getText().toString());	
					Toast.makeText(getApplicationContext(),"Negara : "+settings.getString("cuaNegara","")+"\nTempat : "+settings.getString("cuaTempat",""),Toast.LENGTH_LONG).show();
					editor.commit();
					
				}
			});

		btnEditor = (Button)findViewById(R.id.btn_editor_color);
		btnEditor.setText("Text Editor Color : "+settings.getBoolean("text editor color",false));
		btnEditor.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v){
					SharedPreferences.Editor editor = settings.edit();	

					if (settings.getBoolean("text editor color",false)) {
						editor.putBoolean("text editor color", false);
						editor.commit();
						main();
					}
					else {
						editor.putBoolean("text editor color", true);
						editor.commit();
						main();

					}

					Toast.makeText(getApplicationContext(),"Color : "+settings.getBoolean("text editor color",false),Toast.LENGTH_LONG).show();
				}
			});
		btnTouch = (Button)findViewById(R.id.pengaturan_touch);
		btnTouch.setText("Touch Asisten : "+settings.getBoolean("touch asisten",false));
		btnTouch.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v){
					SharedPreferences.Editor editor = settings.edit();	

					if (settings.getBoolean("touch asisten",false)) {
						editor.putBoolean("touch asisten", false);
						editor.commit();
						main();

					}
					else {
						editor.putBoolean("touch asisten", true);
						editor.commit();
						main();
						
					}

					Toast.makeText(getApplicationContext(),"Touch Service : "+settings.getBoolean("touch asisten", false),Toast.LENGTH_LONG).show();
				}
			});

		btnCatatan = (Button)findViewById(R.id.pengaturan_catatan);
		btnCatatan.setText("Notifi Catatan: "+settings.getBoolean("notif_catatan",false));
		btnCatatan.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v){
					SharedPreferences.Editor editor = settings.edit();	

					if (settings.getBoolean("notif_catatan",false)) {
						editor.putBoolean("notif_catatan", false);
						editor.commit();
						main();

					}
					else {
						editor.putBoolean("notif_catatan", true);
						editor.commit();
						main();
						
					}
				}
			});

	}
	@Override
    public void onBackPressed()
    {
        // TODO: Implement this method
        super.onBackPressed();
        finish();
    }

	public void telinga(int data) {
		index = data;
		if (index == 1) {
			title = "Masukan Spektrum Terkecil\nisi : "+settings.getInt("Spektrum_awal",0);
			isi = "Spektrum_awal";
		}
		else if (index == 2) {
			title = "Sekarang Spektrum Terbesar\nisi : "+settings.getInt("Spektrum_akhir",0);
			isi = "Spektrum_akhir";
		}
		else if (index == 3)	{
			title = "Atur directory";
			isi = "Atur Dir";
		}
		AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
		builder1.setTitle(title);
		builder1.setCancelable(true);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE );
		View layout = inflater.inflate(R.layout.alert_telinga, null);

		ed = (EditText) layout.findViewById(R.id.aEdit);
		aSave = (Button) layout.findViewById(R.id.aButton);
		aSave.setOnClickListener(new View.OnClickListener()
			{
				SharedPreferences settings = getSharedPreferences("Settings", 0);	
				SharedPreferences.Editor editor = settings.edit();	

				public void onClick(View v){
					if (index == 1){
						int ku = 0;
						try{
							ku = Integer.parseInt(ed.getText().toString());
						}
						catch(Exception e){}

						editor.putInt(isi, ku);	
						editor.commit();
						Toast.makeText(getBaseContext(), "rendah : "+ku, Toast.LENGTH_SHORT).show();
						telinga(2);
					}
					else if (index == 2){
						int ku = 0;
						try{
							ku = Integer.parseInt(ed.getText().toString());
						}
						catch(Exception e){}

						editor.putInt(isi, ku);	
						editor.commit();
						Toast.makeText(getBaseContext(), "tinggi : "+ku, Toast.LENGTH_SHORT).show();
					}
					else if (index == 3){
						editor.putString(isi, ed.getText().toString());	
						editor.commit();
						Toast.makeText(getBaseContext(), "disimpan dir : "+settings.getString(isi,""), Toast.LENGTH_LONG).show();
					}
				}
			});
	    radioB = (RadioButton) layout.findViewById(R.id.rg1);
		radioB.setText(settings.getString("Atur dir",""));
        radioB.setOnClickListener(new View.OnClickListener()
			{
				SharedPreferences settings = getSharedPreferences("Settings", 0);	
				SharedPreferences.Editor editor = settings.edit();	

				public void onClick(View v){
					if (index == 1){
						editor.putInt(isi, 500);	
						editor.commit();
						Toast.makeText(getBaseContext(), "disimpan awal : 500", Toast.LENGTH_LONG).show();
						telinga(2);
					}
					else if (index == 2){
						editor.putInt(isi, 500);	
						editor.commit();
						Toast.makeText(getBaseContext(), "disimpan akhir : 500", Toast.LENGTH_LONG).show();
					}
					else if (index == 3){
						editor.putString(isi, "removable/sdcard1/");	
						editor.commit();
						Toast.makeText(getBaseContext(), "disimpan dir : removable/sdcard1/", Toast.LENGTH_LONG).show();
					}
				}
			});
		radioB2 = (RadioButton) layout.findViewById(R.id.rg2);
		radioB2.setText(settings.getString("Atur dir",""));
        radioB2.setOnClickListener(new View.OnClickListener()
			{
				SharedPreferences settings = getSharedPreferences("Settings", 0);	
				SharedPreferences.Editor editor = settings.edit();	

				public void onClick(View v){
					if (index == 1){
						editor.putInt(isi, 800);	
						editor.commit();
						Toast.makeText(getBaseContext(), "disimpan awal : 800", Toast.LENGTH_LONG).show();
						telinga(2);
					}
					else if (index == 2){
						editor.putInt(isi, 800);	
						editor.commit();
						Toast.makeText(getBaseContext(), "disimpan akhir : 800", Toast.LENGTH_LONG).show();
					}
					else if (index == 3){
						editor.putString(isi, "emulated/0/");	
						editor.commit();
						Toast.makeText(getBaseContext(), "disimpan dir : emulated/0/", Toast.LENGTH_LONG).show();
					}
				}
			});

		builder1.setView(layout);

		AlertDialog alert11 = builder1.create();
		alert11.show();
	}

}
