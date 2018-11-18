package com.cpu;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.*;
import android.content.Context;
import android.content.Intent;
import android.content.ClipboardManager;
import android.graphics.Bitmap;
import android.net.Uri;

public class MainBrowser extends Activity {
	private EditText edHasil;
	private EditText edUrl;
	private Button btnSubmit;
	private String surl = "https://sunjangyo12.000webhostapp.com/login.php/";

	public static final String DEFAULT_URL = "http://localhost:8080";
    public static final int FILE_CHOOSER_RESULT = 0x01;
    private static final String tag = "MainBrowser";
    private static final int DELAY = 1, ADD_DELAY = 2, CLEAR_DELAY = 3;
    private static final long DISPLAY_TIME = 4000L;
    private static final String GET_HTML = "GET_HTML";
    private WebView webView;
    private ProgressBar urlLoading;
    private ImageView favicon;
    private TextView htmlTitle;
    private Button btnPrev, btnRefresh, btnNext;
    private LinearLayout webToolsPanel;
    private RelativeLayout webTitlePanel;
    private String url;
    private EditText gotoUrl;
    private boolean isLoading = false;
    private InputMethodManager imm;
    private ValueCallback<Uri> mFileChooserCallback;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_browser);
		
		edHasil = (EditText) findViewById(R.id.browser_hasil);
		edUrl = (EditText) findViewById(R.id.browser_url);

		try {
			if (getIntent().getStringExtra("upload").equals("text")){
				CallWebPageTask task = new CallWebPageTask();
	   			task.applicationContext = MainBrowser.this;

	   			ClipboardManager clip = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
	   			String url = "http://10.42.0.1/client.php?main="+clip.getText().toString();
	   			task.execute(new String[] { url });
				Toast.makeText(this,"Upload text",Toast.LENGTH_LONG).show();

	   		}
		} catch(Exception e1) {}

		edUrl.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                        CallWebPageTask task = new CallWebPageTask();
                        task.applicationContext = MainBrowser.this;
                        String url = surl;
                        if (edUrl.getText().toString() != "") {
                            url = edUrl.getText().toString();
                        }
                        task.execute(new String[] { url });
                        return true;
                    }
                    return false;
                }
            });

		WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());// for access to favicon in WebView
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        try {
            Intent intent = getIntent();
            if (intent.getAction().equals(Intent.ACTION_VIEW)) {
                //стартовал из SiteTools
            }
            url = intent.getDataString();
            if (url == null) {
                url = DEFAULT_URL;
            }
        } catch (Exception e) {}

        gotoUrl = (EditText) findViewById(R.id.goto_url);
        urlLoading = (ProgressBar) findViewById(R.id.progressBar_url_loading);
        favicon = (ImageView) findViewById(R.id.favicon);
        htmlTitle = (TextView) findViewById(R.id.html_title);
        btnPrev = (Button) findViewById(R.id.btnPrev);
        btnRefresh = (Button) findViewById(R.id.btnRefresh);
        btnNext = (Button) findViewById(R.id.btnNext);
        webToolsPanel = (LinearLayout) findViewById(R.id.webToolsPanel);
        webTitlePanel = (RelativeLayout) findViewById(R.id.webTitlePanel);

        webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true); // включаем поддержку JavaScript
        webView.addJavascriptInterface(new MyJavascriptInterface(), GET_HTML);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//		webView.getSettings().setPluginsEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());
        webView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					//Log.d(tag, "onClick");
					if (h.hasMessages(DELAY)) {
						h.sendEmptyMessage(ADD_DELAY);
						return false;
					}
					webTitlePanel.setVisibility(View.VISIBLE);
					h.sendEmptyMessageDelayed(DELAY, DISPLAY_TIME);
					return false;
				}
			});
        webView.loadUrl(url);

        btnRefresh.setOnClickListener(NavListener);
        btnPrev.setOnClickListener(NavListener);
        btnNext.setOnClickListener(NavListener);
        gotoUrl.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
					if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
						webView.loadUrl(gotoUrl.getText().toString());
						return true;
					}
					return false;
				}
			});
	}

	/**
	 * Method untuk Mengirimkan data keserver
	 *
	 */
	public String getRequest(String Url){
		String sret;
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(Url);
        try{
            HttpResponse response = client.execute(request);
            sret= request(response);
        }
		catch(Exception ex){
			sret= "Failed Connect to server!";
        }
        return sret;

    }
	/**
	 * Method untuk Menerima data dari server
	 * @param response
	 * @return
	 */
	public static String request(HttpResponse response){
        String result = "";
        try{
            InputStream in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null){
                str.append(line + "\n");
            }
            in.close();
            result = str.toString();
        }
		catch(Exception ex){
            result = "Error";
        }
        return result;
    }

	/**
	 * Class CallWebPageTask untuk implementasi class AscyncTask
	 */
	private class CallWebPageTask extends AsyncTask<String, Void, String> {

		private ProgressDialog dialog;
		protected Context applicationContext;

		@Override
		protected void onPreExecute() {
			this.dialog = ProgressDialog.show(applicationContext, "request Process", "Please Wait...", true);
		}

	    @Override
	    protected String doInBackground(String... urls) {
			String response = "";
			response = getRequest(urls[0]);
			return response;
	    }

	    @Override
	    protected void onPostExecute(String result) {
	    	this.dialog.cancel();
	    	edHasil.setText(result);
	    }
	}

	private View.OnClickListener NavListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.btnRefresh:
                    if (isLoading) {
                        webView.stopLoading();
                    } else {
                        webView.reload();
                    }
                    break;
                case R.id.btnPrev:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    }
                    break;
                case R.id.btnNext:
                    if (webView.canGoForward()) {
                        webView.goForward();
                    }
                    break;
            }
        }
    };

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            view.loadUrl(url);
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap fav) {
            if (imm != null) {
                imm.hideSoftInputFromWindow(gotoUrl.getWindowToken(), 0);
            }
            urlLoading.setVisibility(View.VISIBLE);
            gotoUrl.setText(url);
            htmlTitle.setText(url);
            favicon.setImageBitmap(fav);
            btnRefresh.setText("✖");
            isLoading = true;
			MainBrowser.this.url = url;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            urlLoading.setVisibility(View.GONE);
            btnRefresh.setText("↺");
            isLoading = false;
        }
    }

    private class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            favicon.setImageBitmap(icon);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            htmlTitle.setText(title);
        }

        //@Override
        public void openFileChooser(ValueCallback<Uri> fileChooserCallback, String acceptType, String capture) {
            // Log.d(tag, "openFileChooser with: acceptType = " + acceptType + " capture = " + capture);
            mFileChooserCallback = fileChooserCallback;
            startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT, Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()), MainBrowser.this, MainFileManager.class), FILE_CHOOSER_RESULT);
        }
    }
    private Handler h = new Handler() {
        private long up = 0;

        @Override
        public void handleMessage(android.os.Message message) {
            //Log.d(tag, "handleMessage");
            switch (message.what) {
                case DELAY:
                    if (up != 0) {
                        sendEmptyMessageDelayed(DELAY, DISPLAY_TIME - (System.currentTimeMillis() - up));
                        up = 0;
                        return;
                    }
                    webTitlePanel.setVisibility(View.GONE);
                    break;
                case ADD_DELAY:
                    up = System.currentTimeMillis();
                    break;
                case CLEAR_DELAY:
                    up = 0;
                    removeMessages(DELAY);
                    webTitlePanel.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //super.onActivityResult(requestCode, resultCode, intent);
        //Log.d(tag, "onActivityResult");
        if (requestCode == FILE_CHOOSER_RESULT) {
            if (mFileChooserCallback == null) {
                //Log.d(tag, "callback null");
                return;
            }
            Uri result = (intent == null || resultCode != RESULT_OK) ? null : intent.getData();
            mFileChooserCallback.onReceiveValue(result);
            mFileChooserCallback = null;
            //Log.d(tag, "callback result: " + result);
        }
    }

    private class MyJavascriptInterface {

        public void getHtml(String html) {
            Intent intent = new Intent(Intent.ACTION_VIEW, null, MainBrowser.this, MainEditor.class);
            intent.putExtra(MainEditor.CODE, html);
            intent.putExtra(MainEditor.CODE_TYPE, MainEditor.HTML);
            startActivityForResult(intent, MainEditor.REQUEST_VIEW_SOURCE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, 1, R.string.view_html)
			.setIcon(R.drawable.ic_menu_html);
		menu.add(Menu.FIRST, 2, 1, R.string.view_cookies)
			.setIcon(R.drawable.ic_menu_cookies);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 1: {
					webView.loadUrl("javascript:window." + GET_HTML + ".getHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
					break;
				}
			case 2: {
					CookieManager cman = CookieManager.getInstance();
                    Intent intent = new Intent(Intent.ACTION_VIEW, null, this, MainEditor.class);
					intent.putExtra(MainEditor.CODE, cman.getCookie(url));
					intent.putExtra(MainEditor.CODE_TYPE, MainEditor.NONE);
					startActivityForResult(intent, MainEditor.REQUEST_VIEW_SOURCE);
				}
		}
        return true;
    }
	
	@Override
	public void onBackPressed(){
		finish();
		//Intent intent = new Intent(this, MainActivity.class);
		//startActivity(intent);
		//Toast.makeText(this, "MainBrowser onBackPressed", Toast.LENGTH_SHORT).show();
	}

}
