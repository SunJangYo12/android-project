package com.cpu;

import android.app.Service;
import android.speech.tts.*;
import android.os.*;
import android.content.*;
import android.util.*;
import java.util.*;
import android.widget.*;

public class ServiceTTS extends Service implements TextToSpeech.OnInitListener
{

	public static String str;
	public static float cepat = 0.5f;
	private TextToSpeech mTts;
	
	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}


	@Override
	public void onCreate() {

		mTts = new TextToSpeech(this,this);
		mTts.setSpeechRate(cepat);
	
		super.onCreate();
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if (mTts != null) {
           // mTts.stop();
            //mTts.shutdown();
        }
        super.onDestroy();
		Toast.makeText(this,"TTS hancur",Toast.LENGTH_LONG).show();
	}

	@Override
	public void onStart(Intent intent, int startId) {

		sayHello(str);
		super.onStart(intent, startId);
	}
	
	@Override
	public void onInit(int status) 
	{
		if (status == TextToSpeech.SUCCESS)
		{
            int result = mTts.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
            }
			else {
                sayHello(str);

            }
        } 
		else {
           // Log.v(TAG, "Could not initialize TextToSpeech.");
        }
	}
	private void sayHello(String str) {
		mTts.speak(str, TextToSpeech.QUEUE_FLUSH, null);
	}

}
