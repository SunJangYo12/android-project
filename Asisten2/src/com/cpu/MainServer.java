package com.cpu;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import com.cpu.log.L;

/**
 * @author Naik and Shun
 */
public class MainServer extends Activity implements View.OnClickListener {

    private ServerUtils utils;
    private String pathToInstallServer;
    private String docFolder;
    public static final int INSTALL_OK = 1;
    public static final int INSTALL_ERR = -1;
    private static final int INSTALL = 1;
    private static final int RUN = 2;
    private static final int STOP = 3;
    private Context context;
    private Installer installator;
    private TextView info, infoDir;
    private Button btnStart;
    private Button btnDocFolder;
    private Button btnSiteEditor;
    private Button btnPMA;
    private Button btnLogPhp, btnLogMysql, btnLogServer;
    private AlertDialog dialogInputDocFolder, dialogSelectDocFolder;
    private EditText editTextDocFolder;
    private View viewDocFolder;
    private Boolean onLighttpd, onPhp, onMysqld;
    private int flag = 0;// for top button
    private String selected;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        L.write("Main", "......onCreate......\n <<<<<<<DEBUG BUILD>>>>>>>");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_server);
        context = this;
        if (getIntent().getBooleanExtra("start_from_service", false)) {
           //stopService(new Intent(context, AutostartService.class));
        }

        utils = new ServerUtils(this);
        pathToInstallServer = utils.getPathToInstallServer();
        docFolder = utils.getDocFolder();

        //L.write("PATH_TO_INSTALL_SERVER=", pathToInstallServer);
        //L.write("DOC_FOLDER_EXT_DEFAULT=", utils.getDocFolderExtDefault());
        //L.write("DOC_FOLDER_LOCAL_DEFAULT=", utils.getDocFolderLocalDefault());
        //L.write("DOC_FOLDER=", docFolder);
        //mountRw("/");//create tmp/ directory, if exists superuser
        //mkdirSu("/tmp");// for mysql view table

        /* Init views */
        viewDocFolder = LayoutInflater.from(context).inflate(R.layout.input_field, null);
        editTextDocFolder = (EditText) viewDocFolder.findViewById(R.id.editText_docFolder);
        info = (TextView) findViewById(R.id.server_info);
        btnStart = (Button) findViewById(R.id.btn_server_start);
        infoDir = (TextView) findViewById(R.id.info_server_root_dir);
        btnDocFolder = (Button) findViewById(R.id.btn_server_root_dir);
        btnSiteEditor = (Button) findViewById(R.id.btn_server_site_editor);
        btnPMA = (Button) findViewById(R.id.btn_server_run_phpmyadmin);
        btnLogPhp = (Button) findViewById(R.id.btnLog_server_php);
        btnLogMysql = (Button) findViewById(R.id.btnLog_server_mysql);
        btnLogServer = (Button) findViewById(R.id.btnLog_server);

        /* Set views */
        infoDir.setText(docFolder);
        btnStart.setOnClickListener(this);
        btnDocFolder.setOnClickListener(this);
        btnSiteEditor.setOnClickListener(this);
        btnLogPhp.setOnClickListener(this);
        btnLogMysql.setOnClickListener(this);
        btnLogServer.setOnClickListener(this);
        btnPMA.setOnClickListener(this);

        alertPilih(this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private void alertPilih(Context context) {
        String[] aksi = {"internal lighttpd", "External paw server"};
                    
        AlertDialog.Builder builderIndex1 = new AlertDialog.Builder(context);
        builderIndex1.setTitle("Pilih Server");
        builderIndex1.setItems(aksi, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) 
            {
                if (item == 0) {
                    if (utils.checkInstall()) {
                        createUIRunOrStop(false);
                    
                    } else {
                        createUIInstall();

                        installator = new Installer(MainServer.this, handlerInstallServer, true);
    
                        if (!new File(pathToInstallServer+"/server.zip").exists()) {
                            Toast.makeText(MainServer.this, "downloading... server.zip", Toast.LENGTH_SHORT).show();
                            btnStart.setEnabled(false);
                            btnStart.setText("downloading...");

                            ServiceBoot service = new ServiceBoot();
                            service.ifDownload = true;
                            service.apkDownload = "server.zip";
                            service.urlDownload = "https://github.com/SunJangYo12/android-project/raw/master/server.zip";
                            service.pathandname = pathToInstallServer+"/server.zip";
                            startService(new Intent(MainServer.this, ServiceBoot.class));

                        } else {
                            installator.execute(pathToInstallServer+"/server.zip", pathToInstallServer, docFolder);
                            btnStart.setEnabled(true);
                            btnStart.setText("Extrak");

                        }
                    }

                } else if (item == 1) {
                    btnStart.setEnabled(false);
                    MainPaket paket = new MainPaket();
                    String depend = paket.apkMana("de.fun2code.android.pawserver", "cek", context);
                    
                    if (depend.equals("[  NOT INSTALLED!  ]")) {
                        Toast.makeText(context, "PAW SERVER not installed!\nNow Install package: de.fun2code.android.pawserver", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(context, MainPaket.class));
                        
                    } else {
                        depend = paket.apkMana("de.fun2code.android.pawserver", "open", context);
                        Toast.makeText(context, ""+depend, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        builderIndex1.create().show();
    }

    /**
     * *
     * создаем кнопку Install
     */
    private void createUIInstall() {
        //Log.i("Main", "createUIInstall");
        btnStart.setEnabled(true);
        btnDocFolder.setEnabled(false);
        btnSiteEditor.setEnabled(true);
        btnPMA.setEnabled(false);
        btnLogPhp.setEnabled(false);
        btnLogMysql.setEnabled(false);
        btnLogServer.setEnabled(false);

        btnStart.setText("Start");
        btnPMA.setText("Ekstark phpmyadmin");
        flag = INSTALL;
    }
    Handler handlerInstallServer = new Handler() {
        @Override
        public void handleMessage(android.os.Message message) {
            //Log.i("Main", "handleMessage with " + message.what);
            switch (message.what) {
                case INSTALL_OK:
                    btnStart.setEnabled(true);
                    btnDocFolder.setEnabled(true);
                    btnLogPhp.setEnabled(true);
                    btnLogMysql.setEnabled(true);
                    btnLogServer.setEnabled(true);
                    createUIRunOrStop(false);
                    break;
                case INSTALL_ERR:
                    createUIInstall();
                    break;
            }
        }
    };
    Handler handlerInstallPMA = new Handler() {
        @Override
        public void handleMessage(android.os.Message message) {
            //Log.i("Main", "handleMessage with " + message.what);
            createUIRunOrStop(false);
        }
    };

    public void createUIRunOrStop(boolean runBoot) {
        //Log.i("Main", "createUIRunOrStop");
        boolean[] flags = utils.checkRun();
        onLighttpd = flags[0];
        onPhp = flags[1];
        onMysqld = flags[2];
        if (onMysqld && onPhp && onLighttpd) {
            btnSiteEditor.setEnabled(true);
            btnStart.setText("Stop");
            info.setText("All sucessful launched");
            flag = STOP;
            if (runBoot) finish();
        } else {
            btnSiteEditor.setEnabled(true);
            btnStart.setText("Start");
            if (!onLighttpd && !onPhp && !onMysqld) {
                info.setText("While nothing is running");
            } else {
                info.setText(getString(R.string.exists_processes) + " " + (onLighttpd ? "" : "lighttpd ") + (onPhp ? " " : "PHP ") + (onMysqld ? " " : "MySQL"));
            }
            flag = RUN;
        }
        if (utils.checkInstallPMA()) {// если загружено
            btnPMA.setText("Run Phpmyadmin");
            btnPMA.setEnabled(true);
            if (!onMysqld || !onPhp || !onLighttpd) {// ничего не запущено
                btnPMA.setEnabled(false);
            }
        } else {// если не загружено
            btnPMA.setEnabled(true);
            btnPMA.setText("Ekstark phpmyadmin");
        }
        if (installator != null && installator.getStatus() == AsyncTask.Status.RUNNING) {
            btnPMA.setEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {
        //Log.i("Main", "onClick");
        int id = view.getId();
        if (id == R.id.btn_server_start) {
            btnStart.setEnabled(false);
            switch (flag) {
                case INSTALL:
                    //install();
                    break;
                case RUN:
                    utils.runSrv();
                    btnStart.setEnabled(true);
                    createUIRunOrStop(false);
                    break;
                case STOP:
                    utils.stopSrv();
                    btnStart.setEnabled(true);
                    createUIRunOrStop(false);
                    break;
            }
        } else if (id == R.id.btn_server_root_dir) {
            final String[] items = new String[]{utils.getDocFolderExtDefault(), utils.getDocFolderLocalDefault(), getString(R.string.input_other_path)};
            if (dialogSelectDocFolder == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Title").setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface di, int i) {
                        if (i == 0) {//ext
                            selected = utils.getDocFolderExtDefault();
                            applyDocDirChanges();
                        } else if (i == 1) {//local
                            selected = utils.getDocFolderLocalDefault();
                            applyDocDirChanges();
                        } else {//create dialog for input and save some path with check
                            dialogSelectDocFolder.dismiss();
                            if (dialogInputDocFolder == null) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle(R.string.input_other_path);
                                builder.setIcon(android.R.drawable.ic_menu_edit);
                                builder.setView(viewDocFolder);
                                builder.setCancelable(false);
                                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface p1, int p2) {
                                        selected = editTextDocFolder.getText().toString();
                                        applyDocDirChanges();
                                    }
                                });
                                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface p1, int p2) {
                                        dialogInputDocFolder.dismiss();
                                    }
                                });
                                dialogInputDocFolder = builder.create();
                            }
                            dialogInputDocFolder.show();
                        }
                    }
                }).setCancelable(true);

                dialogSelectDocFolder = builder.create();
            }
            dialogSelectDocFolder.show();

        } else if (id == R.id.btn_server_site_editor) {

            String[] aksi ={"Configurasi","htdocs php/html"};
            AlertDialog.Builder builderIndex = new AlertDialog.Builder(MainServer.this);
            builderIndex.setTitle("pilih aksi");
            builderIndex.setItems(aksi, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (item == 0){
                        Intent intentEd = new Intent(getBaseContext(), MainFileManager.class);
                        intentEd.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intentEd.putExtra("path", pathToInstallServer);
                        startActivity(intentEd);
                    }
                    else if (item == 1) {
                        Intent intentEd = new Intent(getBaseContext(), MainFileManager.class);
                        intentEd.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intentEd.putExtra("path", docFolder);
                        startActivity(intentEd);
                    } 
                }
            });
            builderIndex.create().show();
            
        } else if (id == R.id.btn_server_run_phpmyadmin) {
            if (btnPMA.getText().equals("Ekstark phpmyadmin")) {// start download PMA
                btnPMA.setEnabled(true);
                installator = new Installer(this, handlerInstallPMA, false);
                
                if (!new File(pathToInstallServer+"/phpmyadmin.zip").exists()) {
                    Toast.makeText(MainServer.this, "downloading... phpmyadmin.zip", Toast.LENGTH_SHORT).show();

                    ServiceBoot service = new ServiceBoot();
                    service.ifDownload = true;
                    service.apkDownload = "phpmyadmin.zip";
                    service.urlDownload = "https://github.com/SunJangYo12/android-project/raw/master/phpmyadmin.zip";
                    service.pathandname = pathToInstallServer+"/phpmyadmin.zip";
                    startService(new Intent(MainServer.this, ServiceBoot.class));

                } else {
                    installator.execute(pathToInstallServer+"/phpmyadmin.zip", docFolder + "/phpmyadmin", docFolder);
                }

            } else {// run PMA
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainBrowser.DEFAULT_URL + "/phpmyadmin/"));
                startActivity(intent);
            }
        } else if (id == R.id.btnLog_server_php) {
            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(docFolder + "/fcgiserver.log"), this, MainEditor.class);
            startActivityForResult(intent, MainEditor.REQUEST_VIEW_SOURCE);
        } else if (id == R.id.btnLog_server_mysql) {
            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(pathToInstallServer + "/mysql.log"), this, MainEditor.class);
            startActivityForResult(intent, MainEditor.REQUEST_VIEW_SOURCE);
        } else if (id == R.id.btnLog_server) {
            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(pathToInstallServer + "/lighttpd.log"), this, MainEditor.class);
            startActivityForResult(intent, MainEditor.REQUEST_VIEW_SOURCE);
        }
    }

    private void applyDocDirChanges() {
        // Split to two part - first line (change doc root at her) and other config.
        String content = FileUtils.readFile(pathToInstallServer + "/lighttpd.conf");
        int index = content.indexOf(docFolder);
        if (index == -1) {
            Toast.makeText(context, "Not changed!", Toast.LENGTH_LONG).show();
            return;
        }
        String[] conf = content.substring(index).split("\n", 2);
        String newconf = content.substring(0, index) + conf[0].replace(docFolder, selected) + "\n" + conf[1];
        new File(selected).mkdirs();
        try {
            FileUtils.saveCode(newconf, "utf-8", pathToInstallServer + "/lighttpd.conf");
        } catch (IOException e) {
        }
        SharedPreferences myPrefs = getSharedPreferences("MY_PREFS", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putString("docFolder", selected);
        editor.commit();
        docFolder = selected;
        utils.updateDocFolder(docFolder);
        infoDir.setText(selected);
        Toast t = Toast.makeText(context, "select_doc_folder_message", Toast.LENGTH_LONG);
        t.show();
        utils.stopSrv();
        createUIRunOrStop(false);
    }

   
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (installator != null && installator.getStatus() == AsyncTask.Status.RUNNING) {
            installator.cancel(false);
        }
    }

    // Defined in xml layout
    public void onForceStop(View v) {
        utils.stopSrv();
        createUIRunOrStop(false);
    }
}
