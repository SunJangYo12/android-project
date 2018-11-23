package com.cpu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.TreeSet;
import com.cpu.editor.CodeChangedListener;
import com.cpu.editor.SourceEditor;
import com.cpu.editor.VerticalNumsLine;
import widget.IconSpinnerAdapter;
import android.widget.ImageButton;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Button;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;


public class MainEditor extends Activity implements View.OnClickListener {

    private static final String tag = "MainEditor";
    public static final String CODE = "code";
    public static final String CODE_TYPE = "code_type";

    public static final int CONF = 5;
    public static final int CSS = 4;
    public static final int JS = 3;
    public static final int PHP = 2;
    public static final int HTML = 1;
    public static final int NONE = 0;

    public static final int REQUEST_VIEW_SOURCE = 1;
    private static final String FILES_ID = "files";
	private static final int MAX_OPENED = 10;
    private static int defaultTextSize = 25;
    private String code, url;
    private String[] surl;
    private Spinner spinnerFiles;
    private TreeSet<String> fileSet = new TreeSet<String>();
    private SourceEditor editorView;
    private VerticalNumsLine vertLine;
    private Toast toastSavedOk;
    private boolean saved, exists;
    private int codeType;
    private int defaultTextColorLine = 0xffcccc11;
    private SharedPreferences prefs;
    private EditText ednf;
    private Intent intent;
    private String act;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();

        act = intent.getAction();
        url = intent.getDataString();
        surl = url.split("file://");
        codeType = intent.getIntExtra(CODE_TYPE, NONE);
        exists = true;
        saved = true;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        fileSet = new TreeSet<String>(prefs.getStringSet(FILES_ID, new TreeSet<String>()));
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_editor);

        editorView = (SourceEditor) findViewById(R.id.editor_editCode);
        vertLine = (VerticalNumsLine) findViewById(R.id.verticalNumsLine);
        toastSavedOk = Toast.makeText(this, "saved", Toast.LENGTH_SHORT);
        
        ImageButton btnMenu = (ImageButton) findViewById(R.id.editor_btnMenu);
        btnMenu.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                String[] aksi ={"Open","New File","Save as", "zoom+ : "+defaultTextSize, "zoom- : "+defaultTextSize};
                AlertDialog.Builder builderIndex = new AlertDialog.Builder(MainEditor.this);
                builderIndex.setTitle("Pilih Aksi");
                builderIndex.setItems(aksi, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0){
                            Intent ib = new Intent(getBaseContext(), MainFileManager.class);
                            ib.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                             getBaseContext().startActivity(ib);
                        }
                        else if (item == 1) {
                            AlertDialog.Builder builderNewFile = new AlertDialog.Builder(MainEditor.this);
                            builderNewFile.setTitle("                  File Baru                ");
                            builderNewFile.setCancelable(true);
                            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE );
                            View layout = inflater.inflate(R.layout.alert_newfile, null);
                            
                            ednf = (EditText)layout.findViewById(R.id.alert_nfed);
                            ednf.setHint(surl[1]);
                            Button btnnf = (Button)layout.findViewById(R.id.alert_btnnf);
                            btnnf.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v)
                                {
                                    String dest = surl[1]+"/"+ednf.getText().toString();
                                    if (FileUtils.newFile(dest).equals("")) {
                                        Toast.makeText(MainEditor.this, "saved : "+dest, Toast.LENGTH_LONG).show();
                                        code = FileUtils.readFile(dest);
                                    }
                                }
                            });
                            builderNewFile.setView(layout);
                            builderNewFile.create().show();
                        } 
                        else if (item == 2) {
                            saveCode();
                        }
                        else if (item == 3) {
                            defaultTextSize+=5;
                            init();
                        } 
                        else if (item == 4) {
                            defaultTextSize-=5;
                            init();
                        } 
                        
                    }
                });
                builderIndex.create().show();
            }
        });

        init();
    }

    private void init() {
        if (act.equals(Intent.ACTION_VIEW)) {
            code = intent.getStringExtra(CODE);
            if (code == null) {
                code = "";
            }
            url = surl[1];
            code = FileUtils.readFile(surl[1]);
            fileSet.add(url);
            if (fileSet.size() > MAX_OPENED) {
                fileSet.pollFirst();
            }
        } else if (act.equals(Intent.ACTION_EDIT)) {// load code from URL
            if (url == null || !new File(url).exists()) {
                Toast.makeText(this, "not found file" + url, Toast.LENGTH_LONG).show();
                finish();
            }
            code = FileUtils.readFile(url);
            fileSet.add(url);
            if (fileSet.size() > MAX_OPENED) {
                fileSet.pollFirst();
            }
        } else {
            throw new RuntimeException("Invalid action for MainEditor, "
                               + "must be ACTION_VIEW or ACTION_EDIT!");
        }

        spinnerFiles = (Spinner) findViewById(R.id.spinnerListFiles);
        spinnerFiles.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_item, fileSet.toArray(new String[]{})));
        ((ImageButton)findViewById(R.id.editor_btnRemove)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.editor_btnPrev)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.editor_btnNext)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.editor_btnSave)).setOnClickListener(this);

        //fileNameView.setText(url);
        setCode(code);
        editorView.setCodeChangedListener(new CodeChangedListener() {
            public void codeChanged() {
                //fileNameView.setText("*" + url);
                vertLine.setLines(editorView.getLineCount());
            }
        });
        code = null;
    }

    private void setCode(String code) {
        editorView.setTextHighlighted(code, codeType);
        int textSize = defaultTextSize;// TODO: get from prefs
        int textColor = defaultTextColorLine;//... 
        vertLine.setTextSize(textSize);
        vertLine.setTextColor(textColor);
        editorView.setTextSize(textSize);
        editorView.setTextColor(0xffcccccc);
        editorView.getPaint().set(vertLine.getPaint());
        editorView.post(new Runnable() {

				public void run() {
					vertLine.setLines(editorView.getLineCount());
				}
			});
    }

    private void saveCode() {
        if (!exists) {
            // pick path
            return;
        }
        try {
            FileUtils.saveCode(editorView.getText().toString(), "utf-8", url);
        } catch (IOException e) {
            Toast.makeText(this, "Error IO: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        //fileNameView.setText(url);
        toastSavedOk.show();
    }

 
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.editor_btnRemove:
				finish();
				break;
            case R.id.editor_btnPrev:
                finish();
                break;
            case R.id.editor_btnNext:
                finish();
                break;
            case R.id.editor_btnSave:
                saveCode();
                break;
		}
	}

    @Override
    public void onBackPressed() {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setTitle("Alert!");
        build.setMessage("Apakah mau simpan");
        build.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int i) {
                saveCode();
                finish();
            }
        });
        build.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int i) {
                finish();
                Toast.makeText(MainEditor.this, "tidak tersimpan", Toast.LENGTH_LONG).show();
            }
        });
        build.create().show();
    }

	@Override
	protected void onDestroy() {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putStringSet(FILES_ID, fileSet);
		editor.commit();
		super.onDestroy();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, 1, "Browser")
            .setIcon(R.drawable.icon_html);
        menu.add(Menu.FIRST, 2, 1, "File manager")
            .setIcon(R.drawable.folder);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1: {
                    startActivity(new Intent(this, MainBrowser.class));
                    break;
                }
            case 2: {
                    startActivity(new Intent(this, MainFileManager.class));
                }
        }
        return true;
    }
}
