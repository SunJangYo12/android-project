package com.cpu;

import com.cpu.memori.*;
import android.app.*;
import android.os.*;
import android.widget.*;
import android.content.*;
import android.view.*;
import android.net.*;
import android.widget.AdapterView.OnItemClickListener;
import java.util.*;
import java.io.*;


public class MainCatatan extends Activity {

	private EditText edit_alert;
	private ListView list;
	private MainMemori memori;
	private int position = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_catatan);
		init();
	}

	private void init() {
		list = (ListView) findViewById(R.id.list_catatan);
		memori = new MainMemori();

		try {
			list.setAdapter(memori.getCatatan(MainCatatan.this));
			list.setOnItemClickListener(new OnItemClickListener() {
        		public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {
        			position = arg2;
        			alert();
        		}
        	});
        } catch(Exception e) {
        	MainCatatan.this.finish();
        }
	}

	private void alert() {
		final CharSequence[] dialogitem = {"Open...", "Edit", "Hapus", "View: "};

		AlertDialog.Builder builder = new AlertDialog.Builder(MainCatatan.this);
		builder.setTitle("Pilihan: "+memori.isCatatan(position, 0, MainCatatan.this));
		builder.setItems(dialogitem, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (item == 0) {
					alertOpen();
				}
				else if (item == 1) {
					String[] rinci = {"Nama: \n"+memori.isCatatan(position, 0, MainCatatan.this),
                					 "Content: \n"+memori.isCatatan(position, 1, MainCatatan.this),
                					 "Date: \n"+memori.isCatatan(position, 2, MainCatatan.this)
                	};
                	AlertDialog.Builder builderIndex1 = new AlertDialog.Builder(MainCatatan.this);
					builderIndex1.setTitle("Rincian: "+position);
					builderIndex1.setItems(rinci, new DialogInterface.OnClickListener() {
            			public void onClick(DialogInterface dialog, int item) 
            			{
            				if (item == 0) {
								alertEdit(rinci[0], 0);
            				}
            				else if (item == 1) {
								alertEdit(rinci[1], 1);            					
            				}
            				else if (item == 2) {
								alertEdit(rinci[2], 2);            					
            				}
            			}
            		});
            		builderIndex1.create().show();

				}
				else if (item == 2) {
					memori.position = position;
					memori.setCatatan("rm", "", "", MainCatatan.this);
					init();
				}
				else if (item == 3) {

				}
			}
		});
		builder.create().show();
        ((ArrayAdapter)list.getAdapter()).notifyDataSetInvalidated();
	}

	private void alertEdit(String inEdit, int index) {
		AlertDialog.Builder builder1 = new AlertDialog.Builder(MainCatatan.this);
		builder1.setTitle("Edit: "+inEdit);
		builder1.setCancelable(true);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE );
		View layout = inflater.inflate(R.layout.alert_memori, null);

		edit_alert = (EditText) layout.findViewById(R.id.amEdit);
		edit_alert.setText(memori.isCatatan(position, index, MainCatatan.this));

		Button bt = (Button) layout.findViewById(R.id.amButton);
		bt.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				String edit = edit_alert.getText().toString();
				if (index == 0) {
					memori.position = position;
					memori.setCatatan("edit", edit, memori.isCatatan(position, 1, MainCatatan.this), MainCatatan.this);
				}
				if (index == 1) {
					memori.position = position;
					memori.setCatatan("edit", memori.isCatatan(position, 0, MainCatatan.this), edit, MainCatatan.this);
				}
				if (index == 2) {
					Toast.makeText(MainCatatan.this, "Auto edit", Toast.LENGTH_LONG).show();
				}
				init();
			}
		});
		builder1.setView(layout);
		AlertDialog alert11 = builder1.create();
		alert11.show();
	}

	private void alertOpen() {
		final CharSequence[] dialogitem = {"Google.com", "bing.com"};

		AlertDialog.Builder builder = new AlertDialog.Builder(MainCatatan.this);
		builder.setTitle("Pilihan");
		builder.setItems(dialogitem, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				init();
				String data = memori.titles.get(position);

				if (item == 0) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/m?hl=in&q="+data+"&source=android-browser-type")));

					//startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?safe=strict&client=firefox-b-ab&ei=BRpWXKm1F9DprQGN6p3oCA&q=zzz&oq=zzz&gs_l=psy-ab.3..0l2j0i131l2j0l6.7384.8817..9380...0.0..0.213.723.0j2j2......0....1..gws-wiz.....0..0i131i67.65XSM2uBm9c")));
				}
				else if (item == 1) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://http-www-bing-com.0.freebasics.com/search?iorg_service_id_internal=803478443041409%3BAfrEX0ng8fF-69Ni&iorgbsid=AZwOf5p9ZGHdo4ma-_4xLROJiPP57wR4JxMMMfZYMk2RHTXt0k_suZhZX4ELlv0Xo8d0A99ibKz2Zk2OYsINpLd4&q="+data+"&go=Search&qs=ds&form=QBRE&pc=FBIO")));
				}
			}
		});
		builder.create().show();

	}
}