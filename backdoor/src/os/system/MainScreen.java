package os.system;

import android.app.*;
import android.os.*;
import android.view.WindowManager.LayoutParams;

public class MainScreen extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		this.getWindow().addFlags(LayoutParams.FLAG_TURN_SCREEN_ON | LayoutParams.FLAG_DISMISS_KEYGUARD);
		
		CountDownTimer hitungMundur = new CountDownTimer(1500, 100)
		{
			public void onTick(long millisUntilFinished){
			}
			public void onFinish()
			{
				finish();
			}
		}.start();
	}
	
}
