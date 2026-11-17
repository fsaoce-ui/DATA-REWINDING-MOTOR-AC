package com.datarewindingmotorac;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.graphics.Typeface;
import android.graphics.Color;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.text.*;
import java.util.*;

public class MainActivity extends Activity {
    DB db; LinearLayout root, list; EditText search; Spinner filter; boolean favOnly=false;
    final String[] cats={"Umum","Data Motor","Lilitan / Winding","Pengukuran","Kerusakan","Perbaikan","Material"};
    int navy=Color.rgb(20,48,75), teal=Color.rgb(0,121,107);
    public void onCreate(Bundle b){super.onCreate(b); db=new DB(this); build();}
    void build(){
        root=new LinearLayout(this); root.setOrientation(1); root.setPadding(18,16,18,12); root.setBackgroundColor(Color.rgb(245,247,250));
        TextView title=new TextView(this); title.setText("DATA REWINDING MOTOR AC"); title.setTextSize(20); title.setTypeface(null,Typeface.BOLD); title.setTextColor(navy); root.addView(title);
        TextView sub=new TextView(this); sub.setText("Catatan teknis motor listrik • Offline"); sub.setTextColor(Color.DKGRAY); sub.setTextSize(12); root.addView(sub);
        search=new EditText(this); search.setSingleLine(true); search.setHint("Cari judul atau isi catatan..."); search.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_search,0,0,0); root.addView(search);
        LinearLayout tools=new LinearLayout(this); tools.setOrientation(0);
        filter=new Spinner(this); ArrayAdapter<String> a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Semua kategori","Umum","Data Motor","Lilitan / Winding","Pengukuran","Kerusakan","Perbaikan","Material"}); filter.setAdapter(a); tools.addView(filter,new LinearLayout.LayoutParams(0,-2,1));
        Button fav=new Button(this); fav.setText("★ Favorit"); tools.addView(fav); fav.setOnClickListener(v->{favOnly=!favOnly;fav.setText(favOnly?"★ Semua":"★ Favorit");refresh();}); root.addView(tools);
        TextView hint=new TextView(this); hint.setText("Ketuk catatan untuk mengedit • Tekan lama untuk menghapus"); hint.setTextSize(11); hint.setTextColor(Color.GRAY); root.addView(hint);
        ScrollView sc=new ScrollView(this); list=new LinearLayout(this); list.setOrientation(1); sc.addView(list); root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        Button add=new Button(this); add.setText("+  TAMBAH CATATAN"); add.setTextColor(Color.WHITE); add.setBackgroundTintList(android.content.res.ColorStateList.valueOf(teal)); root.addView(add); add.setOnClickListener(v->edit(null));
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int af){} public void onTextChanged(CharSequence s,int st,int before,int count){refresh();} public void afterTextChanged(Editable e){}});
        filter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){} public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){refresh();}});
        setContentView(root); refresh();
    }
    void refresh(){
        if(list==null)return; list.removeAllViews();
        String q=search.getText().toString().trim().toLowerCase(); String cat=filter.getSelectedItemPosition()==0?"":cats[filter.getSelectedItemPosition()-1];
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,title,body,category,favorite FROM notes ORDER BY favorite DESC, updated DESC",null); int count=0;
        while(c.moveToNext()){
            int id=c.getInt(0), fav=c.getInt(4); String t=c.getString(1), body=c.getString(2), category=c.getString(3);
            if(favOnly&&fav==0)continue; if(!cat.isEmpty()&&!cat.equals(category))continue;
            if(!q.isEmpty()&&!t.toLowerCase().contains(q)&&!body.toLowerCase().contains(q))continue;
            count++; LinearLayout card=new LinearLayout(this); card.setOrientation(1); card.setPadding(14,12,14,12); card.setBackgroundColor(Color.WHITE);
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2); cp.setMargins(0,5,0,5); list.addView(card,cp);
            TextView heading=new TextView(this); heading.setText((fav==1?"★  ":"")+t); heading.setTextSize(16); heading.setTypeface(null,Typeface.BOLD); heading.setTextColor(navy); card.addView(heading);
            TextView meta=new TextView(this); meta.setText(category); meta.setTextSize(11); meta.setTextColor(teal); card.addView(meta);
            TextView desc=new TextView(this); desc.setText(body); desc.setTextSize(14); desc.setTextColor(Color.DKGRAY); desc.setMaxLines(3); card.addView(desc);
            card.setOnClickListener(v->edit(id)); card.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle("Hapus catatan?").setMessage(t).setNegativeButton("Batal",null).setPositiveButton("Hapus",(d,w)->{db.getWritableDatabase().delete("notes","id=?",new String[]{""+id});refresh();}).show();return true;});
        }
        c.close(); if(count==0){TextView empty=new TextView(this); empty.setText("Belum ada catatan. Tekan TAMBAH CATATAN untuk mulai."); empty.setTextColor(Color.GRAY); empty.setPadding(12,35,12,20); list.addView(empty);}
    }
    void edit(Integer id){
        String oldTitle="",oldBody="",oldCat="Umum"; boolean oldFav=false;
        if(id!=null){Cursor c=db.getReadableDatabase().rawQuery("SELECT title,body,category,favorite FROM notes WHERE id=?",new String[]{""+id});if(c.moveToFirst()){oldTitle=c.getString(0);oldBody=c.getString(1);oldCat=c.getString(2);oldFav=c.getInt(3)==1;}c.close();}
        LinearLayout form=new LinearLayout(this); form.setPadding(20,8,20,0); form.setOrientation(1);
        EditText title=new EditText(this); title.setSingleLine(true);title.setHint("Judul catatan");title.setText(oldTitle);form.addView(title);
        Spinner category=new Spinner(this); category.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,cats)); for(int i=0;i<cats.length;i++)if(cats[i].equals(oldCat))category.setSelection(i);form.addView(category);
        EditText body=new EditText(this);body.setHint("Isi catatan / data teknis");body.setText(oldBody);body.setMinLines(5);body.setGravity(Gravity.TOP);form.addView(body);
        CheckBox favorite=new CheckBox(this);favorite.setText("Tandai sebagai favorit");favorite.setChecked(oldFav);form.addView(favorite);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(id==null?"Catatan Baru":"Edit Catatan").setView(form).setNegativeButton("Batal",null).setPositiveButton("Simpan",null).create();
        dialog.setOnShowListener(x->dialog.getButton(-1).setOnClickListener(v->{String t=title.getText().toString().trim(),b=body.getText().toString().trim();if(t.isEmpty()){title.setError("Judul wajib diisi");return;} ContentValues cv=new ContentValues();cv.put("title",t);cv.put("body",b);cv.put("category",cats[category.getSelectedItemPosition()]);cv.put("favorite",favorite.isChecked()?1:0);cv.put("updated",System.currentTimeMillis());if(id==null)db.getWritableDatabase().insert("notes",null,cv);else db.getWritableDatabase().update("notes",cv,"id=?",new String[]{""+id});dialog.dismiss();refresh();}));
        dialog.show();
    }
    static class DB extends SQLiteOpenHelper{
        DB(Context c){super(c,"rewinding_notes.db",null,1);}
        public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE notes(id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT NOT NULL,body TEXT,category TEXT,favorite INTEGER DEFAULT 0,updated INTEGER)");}
        public void onUpgrade(SQLiteDatabase d,int o,int n){}
    }
}