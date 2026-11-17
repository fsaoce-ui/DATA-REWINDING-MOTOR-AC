package com.datarewindingmotorac;

import android.app.*; import android.os.Bundle; import android.content.*; import android.database.Cursor; import android.database.sqlite.*;
import android.graphics.Color; import android.graphics.Typeface; import android.graphics.drawable.GradientDrawable; import android.view.*; import android.widget.*; import android.text.*; import android.content.res.ColorStateList;

public class MainActivity extends Activity {
 DB db; LinearLayout root,list; EditText search; Spinner filter; boolean favOnly=false;
 final String[] cats={"Umum","Data Motor","Lilitan / Winding","Pengukuran","Kerusakan","Perbaikan","Material"};
 int navy=Color.rgb(8,43,91),blue=Color.rgb(17,103,210),teal=Color.rgb(0,150,136),ink=Color.rgb(28,43,62),muted=Color.rgb(105,119,139);
 int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
 GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 TextView txt(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
 LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
 LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
 public void onCreate(Bundle b){super.onCreate(b);db=new DB(this);build();}
 void build(){
  ScrollView page=new ScrollView(this);page.setFillViewport(true);page.setBackgroundColor(Color.rgb(242,246,252));root=vertical();root.setPadding(dp(16),dp(12),dp(16),dp(22));page.addView(root);
  LinearLayout top=horizontal(); TextView logo=txt("⚙",28,Color.WHITE,true);logo.setGravity(Gravity.CENTER);logo.setBackground(shape(blue,16));top.addView(logo,new LinearLayout.LayoutParams(dp(48),dp(48)));
  LinearLayout brand=vertical();brand.setPadding(dp(12),0,0,0);brand.addView(txt("DATA REWINDING",18,navy,true));brand.addView(txt("MOTOR AC  •  CATATAN TEKNIS",10,muted,true));top.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
  TextView star=txt("★",27,Color.rgb(255,174,0),true);star.setGravity(Gravity.CENTER);top.addView(star);star.setOnClickListener(v->{favOnly=!favOnly;refresh();});root.addView(top);
  LinearLayout hero=vertical();hero.setPadding(dp(18),dp(18),dp(18),dp(18));hero.setBackground(shape(navy,20));LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.topMargin=dp(16);root.addView(hero,hp);
  hero.addView(txt("PUSAT DATA REWINDING",12,Color.rgb(160,210,255),true));TextView ht=txt("Semua catatan motor AC\ntersimpan rapi.",22,Color.WHITE,true);ht.setPadding(0,dp(6),0,dp(5));hero.addView(ht);hero.addView(txt("Simpan • Cari • Kelola  |  100% Offline",12,Color.WHITE,false));
  LinearLayout actions=horizontal();actions.setPadding(0,dp(14),0,dp(4));root.addView(actions);
  tile(actions,"＋","Tambah Data",blue,()->edit(null));tile(actions,"⌕","Cari Data",teal,()->{search.requestFocus();search.postDelayed(()->((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(search,android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT),180);});
  LinearLayout actions2=horizontal();root.addView(actions2);tile(actions2,"▦","Kategori",Color.rgb(239,143,20),()->new AlertDialog.Builder(this).setTitle("Kategori Catatan").setItems(cats,(d,w)->{filter.setSelection(w+1);}).show());tile(actions2,"★","Favorit",Color.rgb(116,70,190),()->{favOnly=!favOnly;refresh();});
  TextView section=txt("CATATAN ANDA",13,navy,true);section.setPadding(dp(2),dp(18),0,dp(7));root.addView(section);
  search=new EditText(this);search.setSingleLine(true);search.setTextSize(14);search.setHint("Cari judul atau isi catatan...");search.setPadding(dp(14),dp(10),dp(14),dp(10));search.setBackground(shape(Color.WHITE,14));root.addView(search,new LinearLayout.LayoutParams(-1,dp(48)));
  filter=new Spinner(this);filter.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Semua kategori","Umum","Data Motor","Lilitan / Winding","Pengukuran","Kerusakan","Perbaikan","Material"}));LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dp(45));fp.topMargin=dp(7);root.addView(filter,fp);
  list=vertical();root.addView(list);Button add=new Button(this);add.setText("+  TAMBAH CATATAN");add.setTextColor(Color.WHITE);add.setBackgroundTintList(ColorStateList.valueOf(blue));LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,dp(52));ap.topMargin=dp(14);root.addView(add,ap);add.setOnClickListener(v->edit(null));
  search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){refresh();}public void afterTextChanged(Editable e){}});
  filter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){refresh();}});
  setContentView(page);refresh();
 }
 void tile(LinearLayout row,String symbol,String label,int color,Runnable action){
  LinearLayout box=vertical();box.setGravity(Gravity.CENTER);box.setPadding(dp(6),dp(12),dp(6),dp(12));box.setBackground(shape(color,16));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(92),1);p.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(box,p);
  box.addView(txt(symbol,25,Color.WHITE,true));box.addView(txt(label,12,Color.WHITE,true));box.setOnClickListener(v->action.run());
 }
 void refresh(){
  if(list==null||search==null||filter==null)return;list.removeAllViews();String q=search.getText().toString().trim().toLowerCase();String cat=filter.getSelectedItemPosition()==0?"":cats[filter.getSelectedItemPosition()-1];
  Cursor c=db.getReadableDatabase().rawQuery("SELECT id,title,body,category,favorite FROM notes ORDER BY favorite DESC,updated DESC",null);int count=0;
  while(c.moveToNext()){int id=c.getInt(0),fav=c.getInt(4);String t=c.getString(1),body=c.getString(2),category=c.getString(3);if(favOnly&&fav==0)continue;if(!cat.isEmpty()&&!cat.equals(category))continue;if(!q.isEmpty()&&!t.toLowerCase().contains(q)&&!body.toLowerCase().contains(q))continue;count++;
   LinearLayout card=vertical();card.setPadding(dp(14),dp(13),dp(14),dp(13));card.setBackground(shape(Color.WHITE,15));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,dp(5),0,dp(5));list.addView(card,cp);
   LinearLayout line=horizontal();TextView heading=txt(t,15,ink,true);line.addView(heading,new LinearLayout.LayoutParams(0,-2,1));line.addView(txt(fav==1?"★":"☆",22,Color.rgb(244,169,0),true));card.addView(line);
   TextView meta=txt("▰  "+category,11,blue,true);meta.setPadding(0,dp(4),0,dp(5));card.addView(meta);TextView desc=txt(body,13,muted,false);desc.setMaxLines(3);card.addView(desc);
   card.setOnClickListener(v->edit(id));card.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle("Hapus catatan?").setMessage(t).setNegativeButton("Batal",null).setPositiveButton("Hapus",(d,w)->{db.getWritableDatabase().delete("notes","id=?",new String[]{""+id});refresh();}).show();return true;});
  }c.close();if(count==0){LinearLayout empty=vertical();empty.setGravity(Gravity.CENTER);empty.setPadding(dp(10),dp(28),dp(10),dp(28));empty.addView(txt("▤",35,muted,true));empty.addView(txt("Belum ada catatan",15,ink,true));empty.addView(txt("Tekan Tambah Data untuk mulai menyimpan data motor.",12,muted,false));list.addView(empty);}
 }
 void edit(Integer id){
  String ot="",ob="",oc="Umum";boolean of=false;if(id!=null){Cursor c=db.getReadableDatabase().rawQuery("SELECT title,body,category,favorite FROM notes WHERE id=?",new String[]{""+id});if(c.moveToFirst()){ot=c.getString(0);ob=c.getString(1);oc=c.getString(2);of=c.getInt(3)==1;}c.close();}
  LinearLayout form=vertical();form.setPadding(dp(18),dp(6),dp(18),0);EditText title=new EditText(this);title.setSingleLine(true);title.setHint("Judul catatan");title.setText(ot);form.addView(title);Spinner category=new Spinner(this);category.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,cats));for(int i=0;i<cats.length;i++)if(cats[i].equals(oc))category.setSelection(i);form.addView(category);EditText body=new EditText(this);body.setHint("Isi catatan / data teknis");body.setText(ob);body.setMinLines(5);body.setGravity(Gravity.TOP);form.addView(body);CheckBox favorite=new CheckBox(this);favorite.setText("Tandai sebagai favorit");favorite.setChecked(of);form.addView(favorite);
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle(id==null?"Tambah Catatan":"Edit Catatan").setView(form).setNegativeButton("Batal",null).setPositiveButton("Simpan",null).create();dialog.setOnShowListener(x->dialog.getButton(-1).setOnClickListener(v->{String t=title.getText().toString().trim(),b=body.getText().toString().trim();if(t.isEmpty()){title.setError("Judul wajib diisi");return;}ContentValues cv=new ContentValues();cv.put("title",t);cv.put("body",b);cv.put("category",cats[category.getSelectedItemPosition()]);cv.put("favorite",favorite.isChecked()?1:0);cv.put("updated",System.currentTimeMillis());if(id==null)db.getWritableDatabase().insert("notes",null,cv);else db.getWritableDatabase().update("notes",cv,"id=?",new String[]{""+id});dialog.dismiss();refresh();}));dialog.show();
 }
 static class DB extends SQLiteOpenHelper{DB(Context c){super(c,"rewinding_notes.db",null,1);}public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE notes(id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT NOT NULL,body TEXT,category TEXT,favorite INTEGER DEFAULT 0,updated INTEGER)");}public void onUpgrade(SQLiteDatabase d,int o,int n){}}
}