package com.cpu.memori;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import com.cpu.*;
import android.view.*;
import android.content.*;
import android.widget.*;

public class MemoriActivity extends Activity
{
	String[] daftar;
    ListView ListView01;
    protected Cursor cursor;
    DBHelper dbhelper;
    EditText ed;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_riwayat);
		
		dbhelper = new DBHelper(this);
		init();
	}
	
	public void init(){
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM otak",null);
        daftar = new String[cursor.getCount()];

        cursor.moveToFirst();
        for (int cc=0; cc < cursor.getCount(); cc++){
            cursor.moveToPosition(cc);
            daftar[cc] = cursor.getString(2).toString();
        }
        ListView01 = (ListView)findViewById(R.id.riwList);
        ListView01.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, daftar));
        ListView01.setSelected(true);
        ListView01.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {
					final String selection = daftar[arg2]; //.getItemAtPosition(arg2).toString();
					final CharSequence[] dialogitem = {"Edit", "Hapus"};
					AlertDialog.Builder builder = new AlertDialog.Builder(MemoriActivity.this);
					builder.setTitle("Pilihan");
					builder.setItems(dialogitem, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								switch(item){
									case 0 :
										SQLiteDatabase db = dbhelper.getReadableDatabase();
										cursor = db.rawQuery("SELECT * FROM otak WHERE riwayat = '" +selection+ "'",null);
										cursor.moveToFirst();
										if (cursor.getCount()>0)
										{
											cursor.moveToPosition(0);
											Toast.makeText(MemoriActivity.this,""+cursor.getString(2).toString(),Toast.LENGTH_LONG).show();
										}
										
										AlertDialog.Builder builder1 = new AlertDialog.Builder(MemoriActivity.this);
										builder1.setTitle("       Edit          ");
										builder1.setCancelable(true);

										LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE );
										View layout = inflater.inflate(R.layout.alert_memori, null);

										ed = (EditText) layout.findViewById(R.id.amEdit);
										ed.setText(""+cursor.getString(2).toString());
										Button bt = (Button) layout.findViewById(R.id.amButton);
										bt.setOnClickListener(new View.OnClickListener()
										{
											public void onClick(View v)
											{
												SQLiteDatabase dbs = dbhelper.getWritableDatabase();
												dbs.execSQL("update otak set riwayat='"+ed.getText().toString()+"'");
												Toast.makeText(getApplicationContext(), "Berhasil", Toast.LENGTH_LONG).show();
												
											}
										});
										builder1.setView(layout);

	                                	AlertDialog alert11 = builder1.create();
	                                	alert11.show();
										break;
									case 1 :
										SQLiteDatabase dbf = dbhelper.getWritableDatabase();
										dbf.execSQL("delete from otak where riwayat = '"+selection+"'");
										init();
										break;
								}
							}
						});
					builder.create().show();
				}});
        ((ArrayAdapter)ListView01.getAdapter()).notifyDataSetInvalidated();
    }
	
}
