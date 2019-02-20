package com.tools;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.DateFormat;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;

import java.util.List; 
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.cpu.R;
import com.cpu.init.*;
import android.app.*;
import android.widget.*;
import android.content.*;
import android.view.*;
import android.widget.AdapterView.*;
import android.net.*;
import com.cpu.*;

public class FileExploler extends ListActivity
{

    private File currentDir;
    private FileArrayAdapter adapter;
	private ShellExecuter exe;
	SharedPreferences settings;
	Button alertNext, alertPrev, btnCari;
	EditText edCari;
	AlertDialog alert11;
	int position = 0;
	Item o;
	static String[] hasilFind;
	private static String hasil;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		settings = getSharedPreferences("Settings", 0);	
		
		currentDir = new File("/storage");
		fill(currentDir);
		exe = new ShellExecuter();
	
		try{
			o = adapter.getItem(position);
			String folder ="sdcard0";
			try{
				if (getIntent().getStringExtra("memori").equals("ex"))
				{
					folder = "sdcard1";
				}
				if (getIntent().getStringExtra("memori").equals("in"))
				{
					folder = "sdcard0";
				}
			}
			catch(Exception j){
				folder ="sdcard0";
			}
			
			if(getIntent().getStringExtra("index").equals("cari format")){
				String command = exe.executer("find /storage/"+folder+"/ -name *."+getIntent().getStringExtra("format"));
				hasilFind = command.split("\n");
				
				//ngomong("pencarian file format"+getIntent().getStringExtra("format")+", selesai", 0.8f);
					
				outCari("format "+getIntent().getStringExtra("format"));
				
			}
			else if (getIntent().getStringExtra("index").equals("cari nama")){
				String command = exe.executer("find /storage/"+folder+"/ -iname *"+getIntent().getStringExtra("isi")+"*");
				hasilFind = command.split("\n");
				
				//ngomong("pencarian file bernama"+getIntent().getStringExtra("isi")+", selesai", 0.8f);
					
			    outCari("nama "+getIntent().getStringExtra("isi"));
			}
		}catch(Exception g){}
    }
	
	public void ngomong(String data, float cepat)
	{
		ServiceTTS sertt = new ServiceTTS();
		sertt.cepat = cepat;
		sertt.str = data;
		startService(new Intent(this, ServiceTTS.class));
	}
    private void fill(File f)
    {
        File[]dirs = f.listFiles();
		this.setTitle("Current Dir: "+f.getName());
		List<Item>dir = new ArrayList<Item>();
		List<Item>fls = new ArrayList<Item>();
		try{
			for(File ff: dirs)
			{
				Date lastModDate = new Date(ff.lastModified());
				DateFormat formater = DateFormat.getDateTimeInstance();
				String date_modify = formater.format(lastModDate);
				if(ff.isDirectory()){


					File[] fbuf = ff.listFiles();
					int buf = 0;
					if(fbuf != null){
						buf = fbuf.length;
					}
					else buf = 0;
					String num_item = String.valueOf(buf);

					if(buf == 0) num_item = num_item + " item";
					else num_item = num_item + " items";

					String formated = lastModDate.toString();
					dir.add(new Item(ff.getName(),num_item,date_modify,ff.getAbsolutePath(),"directory_icon"));
				}
				else
				{
					fls.add(new Item(ff.getName(),ff.length() + " Byte", date_modify, ff.getAbsolutePath(),"file_icon"));
				}
			}
		}catch(Exception e)
		{   

		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		if(!f.getName().equalsIgnoreCase("sdcard"))
			dir.add(0,new Item("..","Parent Directory","",f.getParent(),"directory_up"));
		adapter = new FileArrayAdapter(FileExploler.this,R.layout.main_fileexploler,dir);
		this.setListAdapter(adapter);
    }
	
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		this.position = position;
		
		alertFile();
	}
    
	
	public void alertFile()
	{
		o = adapter.getItem(position);
		
		AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
		builder1.setTitle(o.getPath()+"                               ");
		builder1.setCancelable(true);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE );
		View layout = inflater.inflate(R.layout.alert_file, null);
		
		edCari = (EditText)layout.findViewById(R.id.alert_edCari);
		
		btnCari = (Button)layout.findViewById(R.id.alert_cariOke);
		btnCari.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v)
			{
				String[] aksi ={"Directory","Format","Nama","ukuran"};
				AlertDialog.Builder builderIndex = new AlertDialog.Builder(FileExploler.this);
				builderIndex.setTitle("pilih aksi");
				builderIndex.setItems(aksi, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							if(item == 0){
								if (edCari.getText().toString().equals("")){
									Toast.makeText(getBaseContext(),"edit Kosong",Toast.LENGTH_LONG).show();
								}
								else {
									String command = exe.executer("find "+o.getPath()+"/ -name "+edCari.getText().toString());
									hasilFind = command.split("\n");

									outCari("directori");
								}
							}
							else if (item == 1){
								if (edCari.getText().toString().equals("")){
									Toast.makeText(getBaseContext(),"Kosong",Toast.LENGTH_LONG).show();
								}
								else {
									String command = exe.executer("find "+o.getPath()+"/ -name *."+edCari.getText().toString());
									hasilFind = command.split("\n");
									outCari("format");
								}
							}
							else if (item == 2){
								if (edCari.getText().toString().equals("")){
									Toast.makeText(getBaseContext(),"Kosong",Toast.LENGTH_LONG).show();
								}
								else {
									String command = exe.executer("find "+o.getPath()+"/ -iname *"+edCari.getText().toString()+"*");
									hasilFind = command.split("\n");
									outCari("nama");
								}
							}
							else if (item == 3){
								if (edCari.getText().toString().equals("")){
									Toast.makeText(getBaseContext(),"Kosong",Toast.LENGTH_LONG).show();
								}
								else {
									Toast.makeText(getBaseContext(),"contoh -size +10M",Toast.LENGTH_LONG).show();
		
									String command = exe.executer("find "+o.getPath()+"/ "+edCari.getText().toString());
									hasilFind = command.split("\n");
									outCari("size");
								}
							}
						}
					});
				builderIndex.create().show();
				
			}
		});
		
		alertNext = (Button)layout.findViewById(R.id.alert_maju);
		alertNext.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Item o = adapter.getItem(position);
				
				if (o.getImage().equalsIgnoreCase("directory_icon")){
					currentDir = new File(o.getPath());
					fill(currentDir);
				}
				else {
					bukaText(o);
				}
				alert11.cancel();
			}
		});
		alertPrev = (Button)layout.findViewById(R.id.alert_mundur);
		alertPrev.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					Item o = adapter.getItem(position);
					if (o.getImage().equalsIgnoreCase("directory_up")){
						currentDir = new File(o.getPath());
						fill(currentDir);
					}
					else {
						bukaText(o);
					}
					alert11.cancel();
				}
			});
		String[] daftar ={"Text","Intent"};
		ListView li = (ListView)layout.findViewById(R.id.alert_list);
		li.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, daftar));
		li.setSelected(true);
        li.setOnItemClickListener(new OnItemClickListener()
		{
				public void onItemClick(AdapterView arg0, View arg1, int item, long arg3)
				{
					Item o = adapter.getItem(position);
					
					if (item == 0){
						bukaText(o);
					}
					if (item == 1){
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.parse("file://"+o.getPath()), "*/*");
						startActivity(intent);
					}
					
				}});
        ((ArrayAdapter)li.getAdapter()).notifyDataSetInvalidated();
		builder1.setView(layout);
		alert11 = builder1.create();
		alert11.show();
		
	}
	
	private void outCari(String title)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(FileExploler.this);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE );
		View layoutR = inflater.inflate(R.layout.alert_file_find_result, null);

		ListView li = (ListView)layoutR.findViewById(R.id.alert_file_find_r);
		li.setAdapter(new ArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_1, hasilFind));
		li.setSelected(true);
		li.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView arg0, View arg1, int item, long arg3)
				{
					String format = hasilFind[item];
					pilihClick(format);
				}});
		((ArrayAdapter)li.getAdapter()).notifyDataSetInvalidated();
		builder.setTitle("hasil cari dengan : "+title);
		builder.setView(layoutR);
		builder.create().show();
	}
	
	private void pilihClick(String result)
    {
		hasil = result;
		String[] index ={"Text","Intent"};

		AlertDialog.Builder builder1 = new AlertDialog.Builder(FileExploler.this);
		builder1.setTitle("Pilih Aksi");
		builder1.setCancelable(true);
		builder1.setItems(index, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item) {
					if (item == 0){
						String command = exe.executer("cat "+hasil);
						String[] hasil = command.split("\n\n");
						AlertDialog.Builder builder = new AlertDialog.Builder(FileExploler.this);

						builder.setTitle("Hasil Buka Text");
						builder.setItems(hasil, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int item) {
								}
							});
						builder.create().show();
					}
					if (item == 1){
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.parse("file://"+hasil), "*/*");
						startActivity(intent);
					}

				}
			});
		builder1.create().show();
    }
	
	// output
	private void bukaText(Item o)
    {
		Toast.makeText(this, ""+o.getPath(),Toast.LENGTH_LONG).show();
		String command = exe.executer("cat "+o.getPath());
		String[] hasil = command.split("\n\n");
		AlertDialog.Builder builder1 = new AlertDialog.Builder(FileExploler.this);
		builder1.setCancelable(true);
		builder1.setTitle("Hasil Buka Text");
		builder1.setItems(hasil, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
				}
			});
		builder1.create().show();
    }
	
}

class FileArrayAdapter extends ArrayAdapter<Item>{

	private Context c;
	private int id;
	private List<Item>items;

	public FileArrayAdapter(Context context, int textViewResourceId,
							List<Item> objects) {
		super(context, textViewResourceId, objects);
		c = context;
		id = textViewResourceId;
		items = objects;
	}
	public Item getItem(int i)
	{
		return items.get(i);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(id, null);
		}

		/* create a new view of my layout and inflate it in the row */
		//convertView = ( RelativeLayout ) inflater.inflate( resource, null );

		final Item o = items.get(position);
		if (o != null) {
			TextView t1 = (TextView) v.findViewById(R.id.TextView01);
			TextView t2 = (TextView) v.findViewById(R.id.TextView02);
			TextView t3 = (TextView) v.findViewById(R.id.TextViewDate);

			/* Take the ImageView from layout and set the city's image */
			ImageView imageCity = (ImageView) v.findViewById(R.id.fd_Icon1);
			String uri = "drawable/" + o.getImage();
			int imageResource = c.getResources().getIdentifier(uri, null, c.getPackageName());
			Drawable image = c.getResources().getDrawable(imageResource);
			imageCity.setImageDrawable(image);

			if(t1!=null)
				t1.setText(o.getName());
			if(t2!=null)
				t2.setText(o.getData());
			if(t3!=null)
				t3.setText(o.getDate());
		}
		return v;
	}
}

class Item implements Comparable<Item>{
	private String name;
	private String data;
	private String date;
	private String path;
	private String image;

	public Item(String n,String d, String dt, String p, String img)
	{
		name = n;
		data = d;
		date = dt;
		path = p;
		image = img;           
	}
	public String getName()
	{
		return name;
	}
	public String getData()
	{
		return data;
	}
	public String getDate()
	{
		return date;
	}
	public String getPath()
	{
		return path;
	}
	public String getImage() {
		return image;
	}
	public int compareTo(Item o) {
		if(this.name != null)
			return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
		else
			throw new IllegalArgumentException();
	}
}
