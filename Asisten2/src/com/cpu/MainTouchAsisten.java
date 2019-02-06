package com.cpu;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.net.*;
import android.content.*;
import android.content.pm.*;
import android.os.IBinder;
import android.widget.AdapterView.OnItemLongClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import java.lang.reflect.Method;
import java.io.*;
import java.util.*;

public class MainTouchAsisten extends Activity
{
	ArrayList<String> Listname = new ArrayList<>();
	ArrayList<String> Listpaket = new ArrayList<>();
    ArrayList<String> ListpackageList = new ArrayList<>();
    ArrayList<Drawable> Listlogo = new ArrayList<>();

    public static boolean setIndex = true;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		if (setIndex) index();
		else {
			setIndex = true;
			apkManagerUser();
		}
	}

	public void index() {
		setContentView(R.layout.main_touch);
		Button btn_home = (Button)findViewById(R.id.touch_home);
		btn_home.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v){
				Intent intent = new Intent(Intent.ACTION_MAIN);
        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		intent.addCategory(Intent.CATEGORY_HOME);
        		startActivity(intent);
			}
		});

		Button btn_recent = (Button)findViewById(R.id.touch_recent);
		btn_recent.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v){
				doRecentAction();
			}
		});

		Button btn_apk = (Button)findViewById(R.id.touch_app);
		btn_apk.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v){
				apkManagerUser();
			}
		});
	}

	public void apkManagerUser() {
		setContentView(R.layout.main_apk_user);
		List apps = (List) getInstalledApplicationsUser();
		ArrayAdapterItem appsAdapter = new ArrayAdapterItem(this, R.layout.list_row_apk_user, apps);
		ListView appsView = (ListView) findViewById(R.id.apk_user_listView);

		appsView.setAdapter(appsAdapter);
		appsView.setOnItemLongClickListener(getLongPressListener());
		appsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				Context context = view.getContext(); // Get a context for further usages.
		        String packageName = ((TextView) view.findViewById(R.id.apk_user_textViewPackageItem)).getText().toString();

		        MainTouchAsisten.this.finish();

		        Toast.makeText(MainTouchAsisten.this, packageName+new MainPaket().apkMana(packageName, "open", MainTouchAsisten.this), Toast.LENGTH_LONG).show();
			}
        });
	}

	public void apkManager() {
		setContentView(R.layout.main_apk);
		ListView lsview = (ListView) findViewById(R.id.apk_listview);
		
		PackageManager packageManager = getPackageManager();

		for(ApplicationInfo applicationInfo:packageManager.getInstalledApplications(0))
		{
			Listname.add(applicationInfo.loadLabel(packageManager).toString());
			ListpackageList.add(applicationInfo.sourceDir);
			Listpaket.add(applicationInfo.packageName);
			Listlogo.add(applicationInfo.loadIcon(packageManager));
		}

		customAdapter d = new customAdapter(MainTouchAsisten.this, Listname, ListpackageList, Listlogo, Listpaket);
		lsview.setAdapter(d);
        lsview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            	Context context = view.getContext();
            	String[] aksi = {"Extract...", "Users app", "Open"};
            	String paket = ((TextView)view.findViewById(R.id.apk_txtPaket)).getText().toString();
        			
            	AlertDialog.Builder builderIndex1 = new AlertDialog.Builder(MainTouchAsisten.this);
        		builderIndex1.setTitle(paket);
        		builderIndex1.setItems(aksi, new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int item) 
            		{
            			if (item == 0) {
            				if ( ! MainTouchAsisten.isSDCardPresent() ) { // Check for SD Card
            					new AlertDialog.Builder( context) // Build a dialog
									.setTitle( "SD Card is not available" ) // Here's the title
									.setMessage( "SD Card isn't available. We can't continue." ) // And the content
									.setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {
									public void onClick( DialogInterface dialog, int which ) {
										// Do nothing...
									}
								}).show(); // Show it
            					return; // Exit the function.
        					}

        					ExtractOperation operation = new ExtractOperation( context ); // Initialize the operation
        					operation.execute( paket ); // Execute it!
            			}
            			else if (item == 1) {
            				apkManagerUser();
            			}
            			else if (item == 2) {
		        			MainTouchAsisten.this.finish();
            				Toast.makeText(MainTouchAsisten.this, paket+new MainPaket().apkMana(paket, "open", MainTouchAsisten.this), Toast.LENGTH_LONG).show();
            			}
            		}
            	});
				builderIndex1.create().show();
            }
        });
	}

	private OnItemLongClickListener getLongPressListener() {
        return new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, final View view, int arg2, long arg3) 
            {
            	Context context = view.getContext(); // Get a context for further usages.
		        String packageName = ((TextView) view.findViewById(R.id.apk_user_textViewPackageItem)).getText().toString();

            	String[] aksi = {"Extract...", "Systems app"};
        			
            	AlertDialog.Builder builderIndex1 = new AlertDialog.Builder(MainTouchAsisten.this);
        		builderIndex1.setTitle(packageName);
        		builderIndex1.setItems(aksi, new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int item) 
            		{
            			if (item == 0) {
            				if ( ! MainTouchAsisten.isSDCardPresent() ) { // Check for SD Card
            					new AlertDialog.Builder( context) // Build a dialog
									.setTitle( "SD Card is not available" ) // Here's the title
									.setMessage( "SD Card isn't available. We can't continue." ) // And the content
									.setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {
									public void onClick( DialogInterface dialog, int which ) {
										// Do nothing...
									}
								}).show(); // Show it
            					return; // Exit the function.
        					}

        					ExtractOperation operation = new ExtractOperation( context ); // Initialize the operation
        					operation.execute( packageName ); // Execute it!
            			}
            			else if (item == 1) {
            				apkManager();
            			}
            		}
            	});
				builderIndex1.create().show();
                return true;
            }

        };
    }

	public static void copyFile(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		byte[] buff = new byte[1024];
		int len;
		while ((len = in.read(buff)) > 0) {
			out.write(buff, 0, len);
		}

		in.close();
		out.close();
	}

	public static boolean isSDCardPresent() {
		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}

	public static boolean ExtractAll(Context context) {
		new BackupApps(context);
		return true;
	}

	public static ExtractResults ExtractPackage( Context context, String packageName ) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);
        mainIntent.setFlags(ApplicationInfo.FLAG_ALLOW_BACKUP);
        final List pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        for (Object object : pkgAppsList) {
            ResolveInfo info = (ResolveInfo) object;
            if ( info.activityInfo.applicationInfo.packageName == null ) {
                new AlertDialog.Builder( context)
					.setTitle( "Wrong package" )
					.setMessage( "Package isn't available for extracting." )
					.setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick( DialogInterface dialog, int which ) {

						}
					})
					.show();
            }
            File file = new File(info.activityInfo.applicationInfo.publicSourceDir);
            File dest = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/YourApps/" + info.activityInfo.applicationInfo.packageName + ".apk");
            File parent = dest.getParentFile();
            if ( parent != null ) parent.mkdirs();

            try {
                copyFile(file, dest);
            } catch (IOException e) {
                new AlertDialog.Builder( context)
					.setTitle( "Exception detected" )
					.setMessage( "Exception detected: " + e.getMessage() )
					.setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick( DialogInterface dialog, int which ) {

						}
					})
					.show();
            }

            ExtractResults res = new ExtractResults( true );
            res.setFile( dest );
            return res;
        }

        return new ExtractResults( false );
    }

    /* Old function :
	 public List<PackageItem> getInstalledApplicationsUser(){

	 PackageManager appInfo = getPackageManager();
	 final Intent mainIntent = new Intent( Intent.ACTION_MAIN, null );
	 mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
	 List<ApplicationInfo> listInfo = appInfo.getInstalledApplications( 0 );
	 Collections.sort(listInfo, new ApplicationInfo.DisplayNameComparator(appInfo));

	 List<PackageItem> data = new ArrayList<PackageItem>();

	 for (int index = 0; index < listInfo.size(); index++) {
	 try {
	 ApplicationInfo content = listInfo.get(index);
	 if ( ( ( content.flags & ApplicationInfo.FLAG_SYSTEM) != 0 ) && content.enabled) {
	 if (content.icon != 0) {
	 PackageItem item = new PackageItem();
	 Log.d( "APP", content.packageName );
	 item.setName(getPackageManager().getApplicationLabel(content).toString());
	 item.setPackageName(content.packageName);
	 item.setIcon(getPackageManager().getDrawable(content.packageName, content.icon, content));
	 data.add(item);
	 }
	 }
	 } catch (Exception e) {

	 }
	 }
	 return data;
	 }*/

    public List<PackageItem> getInstalledApplicationsUser() {
        final Intent mainIntent = new Intent( Intent.ACTION_MAIN, null );
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List AppsList = getPackageManager().queryIntentActivities( mainIntent, 0 );
        Collections.sort( AppsList, new ResolveInfo.DisplayNameComparator( getPackageManager() ) );

        List<PackageItem> data = new ArrayList<PackageItem>();
        for( Object object : AppsList ) {
            try {
                ResolveInfo info = (ResolveInfo) object;
                if (info.activityInfo.applicationInfo.icon != 0 && ( ( info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM ) == 0 ) ) {
                    PackageItem item = new PackageItem();
                    item.setName(getPackageManager().getApplicationLabel(info.activityInfo.applicationInfo).toString());
                    item.setPackageName(info.activityInfo.applicationInfo.packageName);
                    item.setIcon(info.activityInfo.applicationInfo.loadIcon(getPackageManager()));

                    File file = new File(info.activityInfo.applicationInfo.publicSourceDir);
                    item.setApkSize( bytesToMB( file.length() ) );
                    data.add(item);
                }
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }

        return data;
    }

    public String bytesToMB( long bytes ) {
        String res = "";
        Integer num = 0;
        if ( bytes < 1000000 ) {
            // In kilobytes
            num = ( ( int ) Math.ceil( bytes / 1000 ) );
            res = num.toString() + " KB";
        } else {
            // In megabytes
            num = ( ( int ) Math.ceil( bytes / 1000000 ) );
            res = num.toString() + " MB";
        }

        return res;
    }

    private class ExtractOperation extends AsyncTask<String, Integer, Boolean> {
        File mApp;
        Context mContext;
        ProgressDialog mDialog;

        public ExtractOperation( Context context ) {
            this.mContext = context;
        }

        protected void onPreExecute() {
            // Display a progress dialog before start the task.
            ProgressDialog dialog = new ProgressDialog( this.mContext );
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setMessage( "Extracting APK in /YourApps/ at SD Card..." );

            if ( ! dialog.isShowing() ) {
                dialog.show();
                this.mDialog = dialog; // Set a global variable to handle this later.
            }
        }

        @Override
        protected Boolean doInBackground( String... params ) {
            String packageName = params[0];
            ExtractResults res = MainTouchAsisten.ExtractPackage( this.mContext, packageName );

            if ( res.result ) {
                this.mApp = res.file; // This will be used for sharing intent.
                return true;
            } else {
                return false;
            }
        }

        protected void onPostExecute( Boolean result ) {
            this.mDialog.dismiss(); // Completed, so hide the progress dialog.
            if ( result ) {
                Toast.makeText(this.mContext, "Extracted to /YourApps/ directory on SD Card", Toast.LENGTH_LONG).show(); // Make a toast
                Intent share = new Intent( Intent.ACTION_SEND ); // Make a share intent
                share.setType( "application/vnd.android.package-archive" ); // Set the type for APK

                share.putExtra( Intent.EXTRA_STREAM, Uri.fromFile( this.mApp ) ); // Send the file to sharing intent.
                this.mContext.startActivity(Intent.createChooser(share, "Share the application" ) ); // Start the sharing intent.
            } else {
                Toast.makeText(this.mContext, "A problem occurred.", Toast.LENGTH_SHORT).show(); // Show a toast that says it's failed.
            }
        }
    }

	public void doRecentAction() {
		try {
			Class ServiceManager = Class.forName("android.os.ServiceManager");
			Method getService = ServiceManager
					.getMethod("getService", new Class[]{String.class});
			Object[] statusbarObj = new Object[]{"statusbar"};
			IBinder binder = (IBinder) getService.invoke(ServiceManager,
					statusbarObj);
			Class IStatusBarService = Class.forName(
					"com.android.internal.statusbar.IStatusBarService")
					.getClasses()[0];
			Method asInterface = IStatusBarService.getMethod("asInterface",
					new Class[]{IBinder.class});
			Object obj = asInterface.invoke(null, new Object[]{binder});
			IStatusBarService.getMethod("toggleRecentApps", new Class[0]).invoke(
					obj, new Object[0]);
		} catch (Exception e) {
			Toast.makeText(MainTouchAsisten.this, "ERR: "+e, Toast.LENGTH_LONG).show();
		}
	}
}

class OnItemClickListenerListViewItem implements AdapterView.OnItemClickListener {
    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
        
    }

    
}

class ArrayAdapterItem extends ArrayAdapter<PackageItem> {
    Context mContext;
    int layoutResourceId;
    List data = null;

    public ArrayAdapterItem(Context mContext, int layoutResourceId, List data) {
        super( mContext, layoutResourceId, data );

        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        if ( convertView == null ) {
            LayoutInflater inflater = ( (Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate( layoutResourceId, parent, false );
        }

        PackageItem packageItem = (PackageItem) data.get( position );

        TextView textViewNameItem = (TextView) convertView.findViewById(R.id.apk_user_textViewNameItem);
        TextView textViewPackageItem = (TextView) convertView.findViewById(R.id.apk_user_textViewPackageItem);
        TextView textViewSize = (TextView) convertView.findViewById( R.id.apk_user_textViewSize );
        ImageView appIcon = (ImageView) convertView.findViewById(R.id.apk_user_appIcon);

        textViewNameItem.setText( packageItem.getName() );
        textViewPackageItem.setText( packageItem.getPackageName() );
        textViewSize.setText( packageItem.getApkSize() );
        appIcon.setImageDrawable( packageItem.getIcon() );

        return convertView;
    }
}

class BackupApps {
    public BackupApps( Context mContext ) {
        if ( ! MainTouchAsisten.isSDCardPresent() ) { // Check for SD Card
            new AlertDialog.Builder( mContext) // Build a dialog
				.setTitle( "SD Card is not available" ) // Here's the title
				.setMessage( "SD Card isn't available. We can't continue." ) // And the content
				.setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick( DialogInterface dialog, int which ) {
						// Do nothing...
					}
				})
				.show(); // Show it
            return; // Exit the function.
        }
        BackupOperation bOperation = new BackupOperation( mContext );
        bOperation.execute();

    }

    private class BackupOperation extends AsyncTask<Void, String, Boolean> {
        Context mContext;
        ProgressDialog mDialog;
        Integer iApps = 0;
        Integer iConverted = 0;
        List mApps;

        public BackupOperation( Context mAContext ) { this.mContext = mAContext; }

        protected void onPreExecute() {
            try {
                ProgressDialog pDialog = new ProgressDialog(this.mContext);
                pDialog.setTitle("Extracting all apps...");
                pDialog.setMessage("Initializing... Be patient.");
                pDialog.setIndeterminate(true);
                pDialog.setCancelable(false);
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                if ( ! pDialog.isShowing() ) {
                    pDialog.show();
                    this.mDialog = pDialog;
                }

                final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                final List AppsList = this.mContext.getPackageManager().queryIntentActivities(mainIntent, 0);
                Collections.sort(AppsList, new ResolveInfo.DisplayNameComparator(this.mContext.getPackageManager()));
                mApps = AppsList;
                for( Object object : mApps ) {
                    try {
                        ResolveInfo info = (ResolveInfo) object;

                        if (info.activityInfo.applicationInfo.icon != 0 && ( info.activityInfo.applicationInfo.packageName != null ) && ( ( info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM ) == 0 ) ) {
                            this.iApps++;
                        }
                    } catch ( Exception e ) {
                        e.printStackTrace();
                    }
                }
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }

        @Override
        protected Boolean doInBackground( Void... params ) {
            String packageName;
            for( Object object : mApps ) {
                try {
                    ResolveInfo info = (ResolveInfo) object;
                    if ( info.activityInfo.applicationInfo.packageName == null )
                        continue;
                    if (info.activityInfo.applicationInfo.icon != 0 && ( ( info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM ) == 0 ) ) {
                        packageName = info.activityInfo.applicationInfo.packageName;
                        publishProgress( packageName );

                        ExtractResults res = MainTouchAsisten.ExtractPackage(this.mContext, packageName);
                        if (res.result) {
                            iConverted++;
                        } else {
                            Toast.makeText(mContext, packageName + " extraction failed", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch ( Exception e ) {
                    e.printStackTrace();
                    publishProgress( "error" );
                }
            }

            return ( iApps == iConverted );
        }

        @Override
        protected void onProgressUpdate( String... values ) {
            if ( values[0].equals( "error" ) ) {
                this.mDialog.dismiss();
                Toast.makeText(this.mContext, "Something bad occurred!", Toast.LENGTH_SHORT).show();
            }
            this.mDialog.setMessage( "Extracting app : " + values[0] );
            super.onProgressUpdate( values );
        }

        @Override
        protected void onPostExecute( Boolean converted ) {
            this.mDialog.dismiss();
            if ( converted ) {
                new AlertDialog.Builder( this.mContext )
					.setTitle( "Successful" )
					.setMessage( "" + iConverted.toString() + "/" + iApps.toString() + " applications extracted to /YourApps/ at SD Card." )
					.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							// Perfect!
						}
					}).show();
            } else {
                new AlertDialog.Builder( this.mContext)
					.setTitle( "Unsuccessful" )
					.setMessage( "" + iConverted.toString() + "/" + iApps.toString() + " applications extracted to /YourApps/ at SD Card." )
					.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							// Perfect!
						}
					}).show();
            }
        }
    }
}

class PackageItem {
    private Drawable icon;
    private String name;
    private String packageName;
    private String apkSize;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setApkSize(String apkSize) { this.apkSize = apkSize; }
    public String getApkSize() { return apkSize; }
}

class ExtractResults {
    public File file;
    public final boolean result;

    public ExtractResults( boolean result ) {
        this.result = result;
    }

    public void setFile( File file ) {
        this.file = file;
    }
}

class customAdapter extends ArrayAdapter {

    ArrayList<String> Listname = new ArrayList<>();
    ArrayList<String> Listpaket = new ArrayList<>();
    ArrayList<String> ListpackageList = new ArrayList<>();
    ArrayList<Drawable> Listlogo = new ArrayList<>();
    Activity activity;
    public customAdapter(Activity activity, ArrayList<String> Listname,
                         ArrayList<String> ListpackageList,
                         ArrayList<Drawable> Listlogo, ArrayList<String> Listpaket) 
    {
        super(activity,R.layout.list_row_apk, Listname);
        this.activity=activity;
        this.Listlogo=Listlogo;
        this.Listname=Listname;
        this.Listpaket=Listpaket;
        this.ListpackageList=ListpackageList;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater=activity.getLayoutInflater();
        View view=inflater.inflate(R.layout.list_row_apk, null);

        //find view's here.
        TextView txtAPKNAME,txtPackageName, txtPaket;
        ImageView imageView;

        txtAPKNAME = (TextView) view.findViewById(R.id.apk_txtName);
        txtPackageName = (TextView) view.findViewById(R.id.apk_txtPackage);
        txtPaket = (TextView) view.findViewById(R.id.apk_txtPaket);
        imageView = (ImageView) view.findViewById(R.id.apk_imageView);

        txtAPKNAME.setText(Listname.get(position));
        txtPaket.setText(Listpaket.get(position));
        txtPackageName.setText(ListpackageList.get(position)); //path
        imageView.setImageDrawable(Listlogo.get(position));

        return view;
    }
}
