package os.system;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.os.*;
import android.widget.*;
import android.content.SharedPreferences;
import java.io.*;
import java.util.zip.*;
import java.net.*;

public class TharedDownload extends AsyncTask<String, Integer, String>{

    private Context context;
    private PowerManager.WakeLock mWakeLock;
    private ProgressDialog mProgessDialog;

    public TharedDownload(Context context) {
        this.context = context;
        mProgessDialog = new ProgressDialog(context);
        mProgessDialog.setMessage("tes download");
        mProgessDialog.setIndeterminate(true);
        mProgessDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgessDialog.setCancelable(true);
    }

    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
       
        try {
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP "+ connection.getResponseCode()+" "+connection.getResponseMessage();
            }

            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream("/sdcard/file_name.extension");
            byte data[] = new byte[4096];
            long total = 0;
            int count;

            while ( (count=input.read(data)) != -1 ) {
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;

                if (fileLength > 0) 
                    //publishProgress((int) (total*100/fileLength));
                output.write(data, 0, count);
            }

        } catch (IOException e) {
            //return e.toString();
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            }
            catch (IOException ioe) {

            }
            if (connection != null) connection.disconnect();
        }
        return null;
    }

   
    @Override
    public void onPreExecute() {
        super.onPreExecute();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire();
        mProgessDialog.show();
    }

    public void onProgressUpdate(Integer... progress) {
        mProgessDialog.setIndeterminate(false);
        mProgessDialog.setMax(100);
        mProgessDialog.setProgress(progress[0]);
    }


    public void onPostExecute(String result) {
        mWakeLock.release();
        mProgessDialog.dismiss();
        if (result != null) {
            Toast t = Toast.makeText(context, "download error", Toast.LENGTH_LONG);
        } else {
            Toast t = Toast.makeText(context, "File download", Toast.LENGTH_LONG);
        }
    }

    public void onClick(DialogInterface p1, int p2) {

    }
}
