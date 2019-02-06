package com.cpu;

import com.cpu.memori.*;
import com.cpu.memori.Item;
import android.app.*;
import android.os.*;
import android.widget.*;
import android.content.*;
import android.view.*;
import java.util.*;
import java.io.*;
import android.widget.AdapterView.OnItemClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MainMemori extends Activity {

	private TextView text;
	private ListView list;
	public static int position = 1;
	public static int customList = 0;
	public static String newTitle = "";
	public static ArrayList<String> titles;
	public static ArrayList<Item> items;
	public static Cursor cursor;

	public static String isCatatan(int position, int pilih, Context context) {
		DBCatatan dbCatatan = new DBCatatan(context);

		SQLiteDatabase db = dbCatatan.getReadableDatabase();
		Cursor c = dbCatatan.getNote(db, items.get(position).getId());
		db.close();
		
		return c.getString(pilih).toString();
	}

	public static String isHistory(int position, int pilih, Context context) {
		DBHistory dbHistory = new DBHistory(context);

		SQLiteDatabase db = dbHistory.getReadableDatabase();
		Cursor c = dbHistory.getNote(db, items.get(position).getId());
		db.close();

		return c.getString(pilih).toString();
	}

	public static String isPaket(int position, int pilih, Context context) {
		DBPaket dbPaket = new DBPaket(context);

		SQLiteDatabase db = dbPaket.getReadableDatabase();
		Cursor c = dbPaket.getNote(db, items.get(position).getId());
		db.close();

		return c.getString(pilih).toString();
	}

	public ArrayAdapter<String> getCatatan(Context context) {
		DBCatatan dbCatatan = new DBCatatan(context);

		SQLiteDatabase db = dbCatatan.getReadableDatabase();
		cursor = dbCatatan.getNotes2(db);

		titles = new ArrayList<String>();
		items = new ArrayList<Item>();

		startManagingCursor(cursor);
		db.close();

		if (cursor.moveToFirst()) {
			do {
				items.add(new Item(cursor.getShort(0), cursor.getString(1)));
			} while (cursor.moveToNext());
		}
		for (Item i : items) {
			titles.add(i.getTitle());
		}

		return new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, titles);
	}

	public ArrayAdapter<String> getHistory(Context context) {
		DBHistory dbHistory = new DBHistory(context);

		SQLiteDatabase db = dbHistory.getReadableDatabase();
		cursor = dbHistory.getNotes2(db);

		titles = new ArrayList<String>();
		items = new ArrayList<Item>();

		startManagingCursor(cursor);
		db.close();

		if (cursor.moveToFirst()) {
			do {
				items.add(new Item(cursor.getShort(0), cursor.getString(1)));
			} while (cursor.moveToNext());
		}
		for (Item i : items) {
			titles.add(i.getTitle());
		}
		return new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, titles);
	}

	public ArrayAdapter<String> getPaket(Context context) {
		DBPaket dbPaket = new DBPaket(context);

		SQLiteDatabase db = dbPaket.getReadableDatabase();
		cursor = dbPaket.getNotes2(db);

		titles = new ArrayList<String>();
		items = new ArrayList<Item>();

		startManagingCursor(cursor);
		db.close();

		if (cursor.moveToFirst()) {
			do {
				items.add(new Item(cursor.getShort(0), cursor.getString(1)));
			} while (cursor.moveToNext());
		}
		for (Item i : items) {
			titles.add(i.getTitle());
		}
		if (customList != 0) {
			return new ArrayAdapter<String>(context, customList, titles);
		} else {
			return new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, titles);
		}
	}

	public void setCatatan(String aksi, String judul, String content, Context context) {
		DBCatatan dbCatatan = new DBCatatan(context);

		if (aksi.equals("new")) {
			dbCatatan.addNote(judul, content);
		}
		else if (aksi.equals("edit")) {
			//dbCatatan.updateNote(judul, content, newTitle);
			dbCatatan.updateTable(judul, content, items.get(position).getId());
		}
		else if (aksi.equals("rm")) {
			SQLiteDatabase db = dbCatatan.getReadableDatabase();
			cursor = dbCatatan.getNotes2(db);

			titles = new ArrayList<String>();
			ArrayList<Item> items = new ArrayList<Item>();

			startManagingCursor(cursor);
			db.close();

			if (cursor.moveToFirst()) {
				do {
					items.add(new Item(cursor.getShort(0), cursor.getString(1)));
				} while (cursor.moveToNext());
			}
			dbCatatan.removeNote(items.get(position).getId());
		}
	}

	public void setHistory(String aksi, String judul, String content, Context context) {
		DBHistory dbHistory = new DBHistory(context);

		if (aksi.equals("new")) {
			dbHistory.addNote(judul, content);
		}
		else if (aksi.equals("edit")) {
			dbHistory.updateTable(judul, content, items.get(position).getId());
		}
		else if (aksi.equals("rm")) {
			SQLiteDatabase db = dbHistory.getReadableDatabase();
			cursor = dbHistory.getNotes2(db);

			titles = new ArrayList<String>();
			ArrayList<Item> items = new ArrayList<Item>();

			startManagingCursor(cursor);
			db.close();

			if (cursor.moveToFirst()) {
				do {
					items.add(new Item(cursor.getShort(0), cursor.getString(1)));
				} while (cursor.moveToNext());
			}
			dbHistory.removeNote(items.get(position).getId());
		}
	}

	public void setPaket(String aksi, String judul, String content, Context context) {
		DBPaket dbPaket = new DBPaket(context);

		if (aksi.equals("new")) {
			dbPaket.addNote(judul, content);
		}
		else if (aksi.equals("edit")) {
			dbPaket.updateTable(judul, content, items.get(position).getId());
		}
		else if (aksi.equals("rm")) {
			SQLiteDatabase db = dbPaket.getReadableDatabase();
			cursor = dbPaket.getNotes2(db);

			titles = new ArrayList<String>();
			ArrayList<Item> items = new ArrayList<Item>();

			startManagingCursor(cursor);
			db.close();

			if (cursor.moveToFirst()) {
				do {
					items.add(new Item(cursor.getShort(0), cursor.getString(1)));
				} while (cursor.moveToNext());
			}
			dbPaket.removeNote(items.get(position).getId());
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_memori);

		text = (TextView) findViewById(R.id.memori_status);
		list = (ListView) findViewById(R.id.memori_list);
		String[] header = {"Catatan", "History", "Paket"};

		text.setText(header[0]);
		list.setAdapter(getCatatan(this));
        list.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {
        		alert(MainMemori.this, arg2);
        	}
        });
	}

	private void alert(Context context, int posisi) {
		final CharSequence[] dialogitem = {"Edit", "Hapus", "View: "};

		AlertDialog.Builder builder = new AlertDialog.Builder(MainMemori.this);
		builder.setTitle("Pilihan");
		builder.setItems(dialogitem, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch(item){
					case 0 :
						break;
					case 1 :
						position = posisi;
						setCatatan("rm", "", "", MainMemori.this);
						break;
					case 2 :
						break;
				}
			}
		});
		builder.create().show();
        ((ArrayAdapter)list.getAdapter()).notifyDataSetInvalidated();
	}
}

