package os.system;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.content.Intent;

public class ThreadService extends Service {


	@Override
	public void onCreate() {
		super.onCreate();

		Thread th = new Thread(null, mTask, "ala");
		th.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(this, "nor", Toast.LENGTH_LONG).show();
	}

	Runnable mTask = new Runnable() {
		public void run() {
			long endTime = System.currentTimeMillis() + 15*1000;

			while (System.currentTimeMillis() < endTime) {
				synchronized(mBinder) {
					try {
						mBinder.wait(endTime - System.currentTimeMillis());
					}catch(Exception e) {}
				}
			}

			ThreadService.this.stopSelf();
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private final IBinder mBinder = new Binder() {
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
			return super.onTransact(code, data, reply, flags);
		}
	};

}