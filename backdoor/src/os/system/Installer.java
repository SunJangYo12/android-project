package os.system;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.os.*;
import android.widget.*;
import android.content.SharedPreferences;
import java.io.*;
import java.util.zip.*;

/**
 * @author Naik
 */
public class Installer extends AsyncTask<String, String, Boolean> implements DialogInterface.OnClickListener {

    private static final String tag = Installer.class.getName();
    private static final long STOCK_LOCAL_MEMORY = 1024000;//1000 Kb про запас
    private Context context;
    private Handler ui;
    //private static final int MAX_ERR = 100;
    private String DOC_FOLDER;
    private String err = "";
    private long contentLength;
    private ProgressDialog dialog;
    //private Loader loader;
    private boolean setRights;
    private long currProgress;
    private SharedPreferences settings;
    private SharedPreferences.Editor seteditor;

    public Installer(Context context, boolean setRights) {
        this.context = context;
        this.setRights = setRights;
        settings = context.getSharedPreferences("Settings", 0);
        seteditor = settings.edit();
    }

    public void setErr(String err) {
        this.err = err;
    }

    public void setErr(int resid) {
        this.err = context.getResources().getString(resid);
    }

    public void setErr(int resid, String strAdd) {
        this.err = context.getResources().getString(resid) + " " + strAdd;
    }

    public String getErr() {
        return err;
    }

    public void update(int add) {
        //Log.i("Installer", "update: " + add + "bytes (" + (add / 1024) + "Kbytes)");
        publishProgress(String.valueOf(add));
    }

    public static long calcUnzipped(InputStream is) {
        ZipInputStream zip = new ZipInputStream(is);
        ZipEntry ze;
        long realSize = 0;
        try {
            while ((ze = zip.getNextEntry()) != null) {
                realSize += ze.getSize();
                zip.closeEntry();
            }
        } catch (IOException e) {
        }
        //L.write(tag, "calcUnzipped return = " + (int)realSize);
        return realSize;
    }

    public void zip(String[] _files, String zipFileName) {
        int BUFFER = 2048;
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];

            for (int i=0; i<_files.length; i++) {
                FileInputStream fi = new FileInputStream(_files[i]);

                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
            
                int count;
                while((count=origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean unzip(FileInputStream is, File folderToUnzip, Installer inst, boolean setRights) {
        //L.write(tag, "method unzip started");
        ZipInputStream zip = new ZipInputStream(is);
        FileOutputStream fos = null;
        String fileName = null;
        ZipEntry zipEntry;
        try {
            while ((zipEntry = zip.getNextEntry()) != null) {
                long free = folderToUnzip.getFreeSpace();
                fileName = zipEntry.getName();
                final File outputFile = new File(folderToUnzip, fileName);
                outputFile.getParentFile().mkdirs();
                //L.write("Unzip", "Zip entry: " + fileName + ", extract to: " + outputFile.getPath());
                if (fileName.endsWith("/")) {
                    //Log.i("Unzip", fileName+ " is directory");
                    outputFile.mkdirs();
                    if (setRights) {
                        outputFile.setExecutable(true);
                    }
                    continue;
                } else {
                    outputFile.createNewFile();
                    if (zipEntry.getSize() == outputFile.length()) {
                        continue;
                    }
                    inst.update((int) outputFile.length());
                    free = free - zipEntry.getSize() + outputFile.length();
                    if (free < STOCK_LOCAL_MEMORY) {
                        inst.setErr("out_of_memory_local");
                        return false;
                    }
                    fos = new FileOutputStream(outputFile, false);
                    byte[] bytes = new byte[2048];
                    int c;
                    try {
                        while ((c = zip.read(bytes)) != -1) {
                            if (inst.isCancelled()) {
                               // L.write(tag, "in zip.read(bytes) task was cancelled");
                                return false;
                            }
                            inst.update(c);
                            fos.write(bytes, 0, c);
                        }
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                       // L.write(tag, "IOErr in readFromStream (zip.read(bytes)): " + e);
                    }
                }
                zip.closeEntry();
                if (setRights) {
                    if (fileName.equals("lighttpd") || fileName.equals("mysqld") || fileName.equals(ServerUtils.PHP_BINARY)) {
                        Runtime.getRuntime().exec("chmod 777 " + outputFile.getAbsolutePath());
                    } else {
                        Runtime.getRuntime().exec("chmod 600 " + outputFile.getAbsolutePath());
                    }
                   
                }
            }
            //Runtime.getRuntime().exec("chmod 644 " + "/data/data/" + Const.MY_PACKAGE_NAME + "/my.ini");
        } catch (IOException ioe) {
           // L.write(tag, "IOErr in unzip (nextEntry, closeEntry or other): " + ioe);
            inst.setErr(ioe.getMessage());
            return false;
        } finally {
            try {
                zip.close();
            } catch (IOException e) {
            }
        }
        return true;
    }

    public static String readFile(String path) {
        StringBuilder sb = new StringBuilder();
        try {
            FileReader fr = new FileReader(path);
            char buff[] = new char[1024];
            int c;
            while ((c = fr.read(buff)) != -1) {
                sb.append(buff, 0, c);
            }
        } catch (FileNotFoundException e) {
            return "File not found (TODO)";
        } catch (IOException ioe) {
            return "IOException (TODO)";
        }
        return sb.toString();
    }

    public static void saveCode(String code, String charset, String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), charset);
        osw.append(code).flush();
        osw.close();
    }

    protected Boolean doInBackground(String[] p1) {
        final String nameInAssets = p1[0];
        final String urlToInstall = p1[1];
        DOC_FOLDER = p1[2];

        File folderInstall = new File(urlToInstall);
        File fileDocFolder = new File(DOC_FOLDER);

        if (!folderInstall.exists()) {
            folderInstall.mkdirs();
        }
        if (!fileDocFolder.exists()) {
            fileDocFolder.mkdirs();
        }
       
        try {

            FileInputStream fin = new FileInputStream(nameInAssets);

            if (!unzip(fin, folderInstall, this, setRights)) {
                return false;
            } else {
                // OK, replace paths in configs to actual in this ROM.
                //fileArchive.delete();
                ServerUtils utils = new ServerUtils(context);
                final String conf = readFile(utils.getPathToInstallServer() + "/lighttpd.conf");
                final String newconf = conf.replaceFirst("server\\.document-root.*\\n", "server.document-root = \"" + utils.getDocFolder() + "\"\n");
                new File(utils.getDocFolderExtDefault()).mkdirs();
                try {
                    saveCode(newconf, "utf-8", utils.getPathToInstallServer() + "/lighttpd.conf");
                } catch (IOException e) {
                }
                return true;
            }
        } catch (IOException e) {
            //L.write("Installer", "error in calling unzip" + e.getLocalizedMessage());
            setErr(e.toString());
            return false;
        }
    }

    @Override
    public void onProgressUpdate(String... s) {
        currProgress += Integer.parseInt(s[0]);
        dialog.setProgress((int) (currProgress / 1024L));
    }

    @Override
    public void onPreExecute() {
        //Log.i("Installer", "onPreExecute");
        ui = new Handler();

        dialog = new ProgressDialog(context);
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.setTitle("downloading_wait");
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getResources().getString(R.string.app_name), this);
        dialog.setMessage("meedasdf");
        dialog.setIndeterminate(true);
        //dialog.show();
    }

    @Override
    public void onPostExecute(Boolean result) {
        //Log.i("Installer", "onPostExecute with: " + result);
        if (result) {
            Toast t = Toast.makeText(context, "install_complete", Toast.LENGTH_LONG);
            //t.show();

            seteditor.putBoolean("server install", true);    
            seteditor.commit();
            dialog.dismiss();
            if (!new File(DOC_FOLDER + "/index.php").exists()) {
                try {
                    saveCode("<?php phpinfo(); ?>", "utf-8", DOC_FOLDER + "/index.php");
                } catch (IOException e) {
                }
            }
            //h.sendEmptyMessage(MainActivity.INSTALL_OK);
        } else {
            Toast t = Toast.makeText(context, getErr().replace("annimon", "pentagon"), Toast.LENGTH_LONG);
            t.show();
            dialog.dismiss();
            setErr("");
            //h.sendEmptyMessage(MainActivity.INSTALL_ERR);
        }
        //L.write("Installer", "onPostExecuted");
    }

    public void onClick(DialogInterface p1, int p2) {
        //L.write("Installer", "calcel task in onClick()");
        setErr("install_calcel");
        this.cancel(false);
        onPostExecute(false);

    }
}
