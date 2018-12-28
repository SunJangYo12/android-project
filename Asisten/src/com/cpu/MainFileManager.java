/*
 * This is a file browser window in SiteTools
 */
package com.cpu;

import android.app.Activity;
import android.content.*;
import android.net.*;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.*;
import android.view.LayoutInflater;
import java.io.IOException;
import java.util.*;

public class MainFileManager extends Activity implements AdapterView.OnItemClickListener {

    private ListView listView;
    private TextView fullPath;
    ArrayList<Item> items;
    private static final String tag = "MainFileManager";
    private String currPath, prevPath;
    private Map<String, Integer> mapExt = new HashMap<String, Integer>();
    private boolean chooseFile = false;
    private SharedPreferences settings;
    private AlphabeticComparator alphabeticComparator;
    private Map<String, Integer> supportedFiles = new HashMap<String, Integer>() {
        {
            put(".php", MainEditor.PHP);
            put(".sh", MainEditor.PHP);
            put(".xml", MainEditor.PHP);
            put(".java", MainEditor.PHP);
            put(".c", MainEditor.PHP);
            put(".cpp", MainEditor.PHP);
            put(".js", MainEditor.PHP);
            put(".htm", MainEditor.PHP);
            put(".html", MainEditor.PHP);
            put(".css", MainEditor.PHP);
            put(".config", MainEditor.PHP);
            put(".conf", MainEditor.PHP);
            put(".cfg", MainEditor.PHP);
            put(".ini", MainEditor.PHP);
            put(".txt", MainEditor.PHP);
			//put(".json");
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main_filemanager);
        settings = getSharedPreferences("Settings", 0);

        try {
            Intent intent = getIntent();
            String url = intent.getDataString();
            String[] surl = url.split("file://");

            String ext = getExtension(surl[1]);
            Intent iedit = new Intent(Intent.ACTION_EDIT, Uri.parse(surl[1]), this, MainEditor.class);
            if (settings.getBoolean("text editor color",false)) {
                iedit.putExtra(MainEditor.CODE_TYPE, supportedFiles.get(ext));
            }
            startActivityForResult(iedit, MainEditor.REQUEST_VIEW_SOURCE);
            finish();
        }
        catch (Exception e) {
            try {
                String url = getIntent().getStringExtra("path");
                currPath = url;
            }
            catch (Exception ef) {}
        } 

        //L.write(tag, "onCreate started with " + currPath);
        if (currPath == null) {
            currPath = "/sdcard";
            //L.write(tag, "in onCreate currPath was obtained as null, set /");
        }
        prevPath = calcBackPath();
        fullPath = (TextView) findViewById(R.id.full_path);
        listView = (ListView) findViewById(R.id.file_list);
        listView.setOnItemClickListener(this);
        initMapExt();
        alphabeticComparator = new AlphabeticComparator();

    }

    @Override
    public void onResume() {
        super.onResume();
        //L.write(tag, "onResume()");
        readFolder(currPath);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        //L.write(tag, "onRestart()");
    }
    @Override
    public void onBackPressed()
    {
        // TODO: Implement this method
        super.onBackPressed();
        finish();
    }

    private void readFolder(String folderStr) {
        //L.write(tag, "read : " + folderStr);
        String[] lsOutputDet;
        String[] names;
        String error;
        try {
            java.lang.Process proc = new ProcessBuilder().command("ls", "-l", "-a", folderStr + "/").start();
            lsOutputDet = ServerUtils.readFromProcess(proc, false).split("\n");
            error = ServerUtils.readFromProcess(proc, true);
            names = ServerUtils.readFromProcess(new ProcessBuilder().command("ls", "-a", folderStr + "/").start(), false).split("\n");
            if (!error.equals("")) {
                
                currPath = prevPath;
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (IOException e) {
            //L.write(tag, "read ls" + e.getLocalizedMessage());
            return;
        }
        items = new ArrayList<Item>();
        ArrayList<Item> listFolder = new ArrayList<Item>();
        ArrayList<Item> listFile = new ArrayList<Item>();
        StringBuilder subheader = new StringBuilder();
        if (!currPath.equals("")) {
            items.add(new Item(R.drawable.folder_in, "..", "Parent folder", 3));
        }
        if (names[0].equals("")) {//если папка пустая
            listView.setAdapter(new MyAdapter(this, items));
            fullPath.setText(currPath);
            return;
        }
        int j = 0;//счетчик для names
        for (String str : lsOutputDet) {
            String arr[] = str.split("\\s+");
            char id = arr[0].charAt(0);
            if (id != '-' && id != 'd' && id != 'l') {
                /*Если не файл, не папка, не ссылка,
                 *а какая-то фигня, то от греха подальше, пропускаем
                 */
                //L.write(tag, id + " not known");
                continue;
            }
            subheader.delete(0, subheader.length()).append(' ');//cls subheader
            subheader.append(arr[0].substring(1)).append(' ');//add permissions to subheader
            if (id == 'd' || id == 'l') {//если папка или ссылка
                subheader.append(arr[3]).append(' ').append(arr[4]);//date folder
                listFolder.add(new Item(R.drawable.folder, names[j], subheader.toString(), 1));
            } else {//если файл
                subheader.append(arr[4]).append(' ').append(arr[5]);//date file
                subheader.append(' ').append(calcSize(Long.parseLong(arr[3])));
                String ext = getExtension(names[j]);// get extension from name
                int iconId = R.drawable.file;
                if (mapExt.containsKey(ext)) {
                    iconId = mapExt.get(ext);
                }
                listFile.add(new Item(iconId, names[j], subheader.toString(), 2));
            }
            j++;
        }
        Collections.sort(listFolder, alphabeticComparator);
        Collections.sort(listFile, alphabeticComparator);
        items.addAll(listFolder.subList(0, listFolder.size()));
        items.addAll(listFile.subList(0, listFile.size()));
        //Collections.sort(items, alphabeticComparator);
        listView.setAdapter(new MyAdapter(this, items));
        fullPath.setText(currPath);
    }

    /*
     * calc file size in b, Kb or Mb
     */
    private String calcSize(long length) {
        if (length < 1024) {
            return String.valueOf(length).concat(" b");
        } else if (length < 1048576) {
            return String.valueOf(round((float) length / 1024f)).concat(" Kb");
        } else {
            return String.valueOf(round((float) length / 1048576f)).concat(" Mb");
        }
    }

    /* 
     * rounded to two decimal places
     */
    public static float round(float sourceNum) {
        int temp = (int) (sourceNum / 0.01f);
        return temp / 100f;
    }

    private void initMapExt() {
        mapExt.put(".php", R.drawable.icon_php);
        mapExt.put(".html", R.drawable.icon_html);
        mapExt.put(".txt", R.drawable.icon_txt);
        mapExt.put(".cfg", R.drawable.icon_config);
        mapExt.put(".conf", R.drawable.icon_config);
        mapExt.put(".config", R.drawable.icon_config);
        mapExt.put(".ini", R.drawable.icon_config);
        mapExt.put(".sh", R.drawable.icon_config);
        mapExt.put(".css", R.drawable.icon_css);
        mapExt.put(".mp3", R.drawable.icon_music);
        mapExt.put(".amr", R.drawable.icon_music);
        mapExt.put(".wav", R.drawable.icon_music);
        mapExt.put(".mid", R.drawable.icon_music);
        mapExt.put(".midi", R.drawable.icon_music);
        mapExt.put(".ogg", R.drawable.icon_music);
        mapExt.put(".mp4", R.drawable.icon_video);
        mapExt.put(".3gp", R.drawable.icon_video);
        mapExt.put(".apk", R.drawable.icon_apk);
        mapExt.put(".sql", R.drawable.icon_db);
        mapExt.put(".doc", R.drawable.icon_doc);
        mapExt.put(".docx", R.drawable.icon_doc);
        mapExt.put(".ico", R.drawable.icon_image);
        mapExt.put(".jpg", R.drawable.icon_image);
        mapExt.put(".bmp", R.drawable.icon_image);
        mapExt.put(".gif", R.drawable.icon_image);
        mapExt.put(".png", R.drawable.icon_image);
        mapExt.put(".pdf", R.drawable.icon_pdf);
        mapExt.put(".ppt", R.drawable.icon_ppt);
        mapExt.put(".zip", R.drawable.icon_zip);
        mapExt.put(".rar", R.drawable.icon_zip);
        mapExt.put(".tar", R.drawable.icon_zip);
        mapExt.put(".7z", R.drawable.icon_zip);
        mapExt.put(".jar", R.drawable.icon_zip);
    }

    @Override
    public void onItemClick(AdapterView<?> p1, View p2, int sel, long p4) {
        prevPath = currPath;
        Item it = items.get(sel);
        switch (it.getType()) {
            case 1:
                currPath = currPath + "/" + it.getHeader();// build URL
                readFolder(currPath);
                break;
            case 3:
                currPath = calcBackPath();
                readFolder(currPath);
                break;
            case 2:
                selectAction(currPath + '/' + it.getHeader());// build URL
                break;
        }
    }

    private String calcBackPath() {
        try {
            return currPath.substring(0, currPath.lastIndexOf('/'));
        } catch (IndexOutOfBoundsException ex) {
            return "";
        }
    }

    private void selectAction(String path) {
        if (chooseFile) {
            Intent intent = getIntent();
            intent.setData(Uri.parse("file://" + path));
            setResult(RESULT_OK, intent);
            finish();
            return;
        }
        String mimeType;
        String ext = getExtension(path);
        if (supportedFiles.containsKey(ext)) {
			Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(path), this, MainEditor.class);
			intent.putExtra(MainEditor.CODE_TYPE, supportedFiles.get(ext));
			
            startActivityForResult(intent, MainEditor.REQUEST_VIEW_SOURCE);
            return;
        }
        if (ext != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            mimeType = mime.getMimeTypeFromExtension(ext.substring(1));
            if (mimeType != null) {
                //Log.d(tag, mimeType);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + path), mimeType);
                intent.putExtra("data", path);
                intent.putExtra(Intent.EXTRA_TITLE, "Что использовать?");
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                }
            }
            else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://"+path), "*/*");
                startActivity(intent);
            }
        }
        
    }

    private static String getExtension(String path) {
        if (path.contains(".")) {
            return path.substring(path.lastIndexOf(".")).toLowerCase();
        }
        return null;
    }
}


class MyAdapter extends BaseAdapter {

    private ArrayList<Item> list = new ArrayList<Item>();
    private Context context;
    private LayoutInflater li;

    public MyAdapter(Context context, ArrayList<Item> arr) {
        if (arr != null) {
            list = arr;
        }
        this.context = context;
        li = LayoutInflater.from(context);
    }

    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = li.inflate(R.layout.list_row, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) view.findViewById(R.id.list_image);
            holder.header = (TextView) view.findViewById(R.id.list_header);
            holder.subheader = (TextView) view.findViewById(R.id.list_subheader);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        Item item = list.get(position);
        

        holder.imageView.setImageResource(item.getImageId());
        holder.header.setText(item.getHeader());
        holder.subheader.setText(item.getSubheader());
        return view;
    }
    
    private static class ViewHolder {
        ImageView imageView;
        TextView header, subheader;
    }
}


class Item implements SortItem {

    private int imageId, type;
    private String header, subheader;

    public Item(int imageId_, String header_, String subheader_, int type_) {
        imageId = imageId_;
        header = header_;
        subheader = subheader_;
        type = type_;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

    public void setSubheader(String subheader) {
        this.subheader = subheader;
    }

    public String getSubheader() {
        return subheader;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getImageId() {
        return imageId;
    }

    public String getSortField() {
        return header;
    }
}

interface SortItem {

    public String getSortField();
}

class AlphabeticComparator implements Comparator<SortItem> {

    public int compare(SortItem p1, SortItem p2) {
        return p1.getSortField().compareToIgnoreCase(p2.getSortField());
    }
}

