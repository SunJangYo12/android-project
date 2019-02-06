package com.tools;

import android.hardware.Camera;
import com.cpu.ReceiverBoot;
import android.graphics.*;
import android.os.*;


public class Senter
{
	public static final String TOGGLE_SENTER = "TOGGLE_SENTER";
	public static boolean NYALA = false;
	public static Camera camera;

	public void runingKu(){
		NYALA = !NYALA;
		if (NYALA){
			//on
			if (camera==null) {
				camera = Camera.open();
				Camera.Parameters params = camera.getParameters();
				params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
				camera.setParameters(params);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					try {
						camera.setPreviewTexture(new SurfaceTexture(0));
					}catch(Exception e) {}
				}
				camera.startPreview();

			}
		}
		else {
			//off
			if (camera!=null) {
				camera.stopPreview();
				camera.release();
				camera = null;
			}
		}
	}

}
