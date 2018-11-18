package com.tools;

import android.hardware.Camera;

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
