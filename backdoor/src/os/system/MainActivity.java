package os.system;

import android.content.SharedPreferences;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import android.content.pm.PackageManager;
import android.content.ComponentName;
import java.io.*;

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

        String ip = Identitas.getIPAddress(true);
//        startService(new Intent(this, System.class));

		/*try {
			PackageManager p = getPackageManager();

			p.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		}
		catch (Exception e) {}
*/
		//Toast.makeText(this, "updated success "+shell("echo 10.42.0.3 | `cut -d '.' -f 1,2,3`"), Toast.LENGTH_LONG).show();
		
		//finish();
	}

	public String shell(String command) {

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

	public void btn(View v) {
		ReceiverBoot rece = new ReceiverBoot();
		rece.requestUrl = "http://10.42.0.1/download.php?id=ok.zip";
		rece.requestAksi = "download";
		rece.requestPath = "/sdcard/ok.zip";
		rece.mainRequest(this);
	}

}