package com.cpu;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.os.*;
import android.widget.*;
import com.cpu.log.*;

import java.io.*;
import java.util.zip.*;

class Unzip {

    private static final String tag = Unzip.class.getName();

    private static final long STOCK_LOCAL_MEMORY = 1024000;//1000 Kb про запас

    /*
     * Щитает размер архива, если его разархивировать
     */
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

    public static boolean unzip(InputStream is, File folderToUnzip, Installer inst, boolean setRights) {
        //L.write(tag, "method unzip started");
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(is));
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
                                L.write(tag, "in zip.read(bytes) task was cancelled");
                                return false;
                            }
                            inst.update(c);
                            fos.write(bytes, 0, c);
                        }
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        L.write(tag, "IOErr in readFromStream (zip.read(bytes)): " + e);
                    }
                }
                zip.closeEntry();
                if (setRights) {
                    if (fileName.equals("lighttpd") || fileName.equals("mysqld") || fileName.equals(ServerUtils.PHP_BINARY)) {
                        Runtime.getRuntime().exec("chmod 777 " + outputFile.getAbsolutePath());
                    } else {
                        Runtime.getRuntime().exec("chmod 600 " + outputFile.getAbsolutePath());
                    }
                    /*if (fileName.equals("my.ini")) {
                     Runtime.getRuntime().exec("chmod 644 " + outputFile.getAbsolutePath());
                     } else {
                     Runtime.getRuntime().exec("chmod 777 " + outputFile.getAbsolutePath());
                     }*/
                }
            }
            //Runtime.getRuntime().exec("chmod 644 " + "/data/data/" + Const.MY_PACKAGE_NAME + "/my.ini");
        } catch (IOException ioe) {
            L.write(tag, "IOErr in unzip (nextEntry, closeEntry or other): " + ioe);
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
}

public class Installer extends AsyncTask<String, String, Boolean> implements DialogInterface.OnClickListener {

    private static final String tag = Installer.class.getName();

    private Context context;
    private Handler ui;
    //private static final int MAX_ERR = 100;
    private String DOC_FOLDER;
    private String err = "";
    private long contentLength;
    private ProgressDialog dialog;
    private Handler h;
    //private Loader loader;
    private boolean setRights;
    private long currProgress;

    public Installer(Context context, Handler h, boolean setRights) {
        this.context = context;
        this.h = h;
        this.setRights = setRights;
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

    protected Boolean doInBackground(String[] p1) {
        final String nameInAssets = p1[0];
        final String urlToInstall = p1[1];
        DOC_FOLDER = p1[2];
        //final String saveAs = p1[3];
        //L.write("Installer", "doInBackground started");

        /*ЭТАП I - ПОДГОТОВКА ФАЙЛОВОЙ СИСТЕМЫ*/
        File folderInstall = new File(urlToInstall);// сюда распаковываем
        File fileDocFolder = new File(DOC_FOLDER);//корневая папка сервера (сюда загружаем архив)
        //File fileArchive = new File(fileDocFolder, saveAs);//загружаемый файл
        if (!folderInstall.exists()) {
            folderInstall.mkdirs();
        }
        if (!fileDocFolder.exists()) {
            fileDocFolder.mkdirs();
        }
        /*if (!fileArchive.exists()) {
         try {
         fileArchive.createNewFile();
         } catch (IOException e) {
         //L.write("Installer", "Error in create new file (archive)" + e.getLocalizedMessage());
         setErr(R.string.err_create_new_file);
         return false;
         }
         }*/

        /*ЭТАП II - ЗАГРУЗКА АРХИВА*/
        // Создаем загрузчик
        //loader = new Loader(context, this, nameInAssets, fileArchive);
        //contentLength = loader.getLength();
        //L.write("Installer", "contentLength() = " + contentLength);
        //if (contentLength == -1) {
        //L.write("Installer", "contentLength() return -1");
        //setErr(R.string.not_have_internet, getErr());
        //return false;
        //}
        // Пропускем уже загруженное, если есть
        //long skipThis = fileArchive.length();
        //L.write("Installer", "long skipThis = " + skipThis);
        //loader.setReaded(skipThis);
        // проверяем достаточно ли памяти на флешке для архива
        //long free = fileDocFolder.getFreeSpace();
        //if (free < (contentLength - skipThis)) {
        //L.write("Installer", "free = " + free + " b, " + free / 1024576 + " Mb");
        //setErr(R.string.out_of_memory_htdocs, (contentLength - skipThis - free) / 1024576 + " Mb");
        //return false;
        //}
        // Собственно сама загрузка
        //dialog.setMax((int) contentLength);
        //dialog.setProgress((int) skipThis);
        //int countErr = 0;//колличество попыток перезапуска загрузки
        //while (!loader.load()) {
        //if (isCancelled()) {
        //return false;
        //} else if (countErr > MAX_ERR) {
        //setErr(R.string.copy_error);
        //return false;
        //} else {
        //countErr++;
        //}
        //}
        //L.write("Installer", "loaded with " + countErr + " error(s)");

        /* ЭТАП III - РАСПАКОВКА ЗАГРУЖЕННОГО АРХИВА*/
        // Свободная локальная память контроллируется при распаковке в классе Unzip
        try {
            long maxBytes = Unzip.calcUnzipped(context.getAssets().open(nameInAssets));
            final int maxKb = (int) (maxBytes / 1024L);

            ui.post(new Runnable() {
                @Override
                public void run() {
                    dialog.setMax(maxKb);
                    dialog.setIndeterminate(false);
                    dialog.setProgress(0);
                }
            });

            if (!Unzip.unzip(context.getAssets().open(nameInAssets), folderInstall, this, setRights)) {
                return false;
            } else {
                // OK, replace paths in configs to actual in this ROM.
                //fileArchive.delete();
                ServerUtils utils = new ServerUtils(context);
                final String conf = FileUtils.readFile(utils.getPathToInstallServer() + "/lighttpd.conf");
                final String newconf = conf.replaceFirst("server\\.document-root.*\\n", "server.document-root = \"" + utils.getDocFolder() + "\"\n");
                new File(utils.getDocFolderExtDefault()).mkdirs();
                try {
                    FileUtils.saveCode(newconf, "utf-8", utils.getPathToInstallServer() + "/lighttpd.conf");
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
        dialog.setTitle("Extrackting...");
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Stop", this);
        dialog.setMessage(context.getResources().getString(R.string.progress_dialog_message));
        dialog.setIndeterminate(true);
        dialog.show();
    }

    @Override
    public void onPostExecute(Boolean result) {
        //Log.i("Installer", "onPostExecute with: " + result);
        if (result) {
            Toast t = Toast.makeText(context, "Extrack complete", Toast.LENGTH_LONG);
            t.show();
            dialog.dismiss();
            if (!new File(DOC_FOLDER + "/index.php").exists()) {
                try {
                    FileUtils.saveCode("<?php phpinfo(); ?>", "utf-8", DOC_FOLDER + "/index.php");
                } catch (IOException e) {
                }
            }
            h.sendEmptyMessage(MainServer.INSTALL_OK);
        } else {
            Toast t = Toast.makeText(context, getErr().replace("annimon", "pentagon"), Toast.LENGTH_LONG);
            t.show();
            dialog.dismiss();
            setErr("");
            h.sendEmptyMessage(MainServer.INSTALL_ERR);
        }
        //L.write("Installer", "onPostExecuted");
    }

    public void onClick(DialogInterface p1, int p2) {
        //L.write("Installer", "calcel task in onClick()");
        setErr("install calcel");
        this.cancel(false);
        onPostExecute(false);

    }
}
