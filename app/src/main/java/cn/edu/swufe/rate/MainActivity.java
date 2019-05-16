package cn.edu.swufe.rate;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements Runnable{
    private final String TAG = "Rate";
    private float dollarRate = 0.0f;
    private float euroRate = 0.0f;
    private float wonRate = 0.0f;
    private String updateDate = "";

    EditText rmb;
    TextView show;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rmb=findViewById(R.id.rmb);
        show=findViewById(R.id.showOut);
        SharedPreferences sharedPreferences = getSharedPreferences("myrate",Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        dollarRate = sharedPreferences.getFloat("dollar_rate",0.0f);
        euroRate = sharedPreferences.getFloat("euro_rate",0.0f);
        wonRate = sharedPreferences.getFloat("won_rate",0.0f);
        updateDate = sharedPreferences.getString("update_date","");
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final String todayStr = sdf.format(today);


        Log.i(TAG,"onCreate: sp dollar_rate="+dollarRate);
        Log.i(TAG,"onCreate: sp euroRate="+euroRate);
        Log.i(TAG,"onCreate: sp wonRate="+wonRate);
        Log.i(TAG,"onCreate: sp updateDate="+updateDate);
        Log.i(TAG,"onCreate: todayStr"+todayStr);

        if(!todayStr.equals(updateDate)){
            Log.i(TAG,"onCreate: 需要更新");


            Thread t = new Thread(this);
            t.start();
        }else{
            Log.i(TAG,"onCreate: 不需要更新");

        }
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==5){
                    Bundle bdl= (Bundle) msg.obj;
                    dollarRate=bdl.getFloat("dollar-rate");
                    euroRate=bdl.getFloat("euro-rate");
                    wonRate=bdl.getFloat("won-rate");
                    Log.i(TAG,"handleMessage: dollarRate"+dollarRate);
                    Log.i(TAG,"handleMessage: euroRate"+euroRate);
                    Log.i(TAG,"handleMessage: wonRate"+wonRate);
                    SharedPreferences sharedPreferences = getSharedPreferences("myrate",Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putFloat("dollar_rate",dollarRate);
                    editor.putFloat("euro_rate",euroRate);
                    editor.putFloat("won_rate",wonRate);
                    editor.putString("update_date",todayStr);
                    editor.apply();


                    Toast.makeText(MainActivity.this,"汇率已更新",Toast.LENGTH_SHORT);
                }
                super.handleMessage(msg);
            }
        };
    }
    public void onClick(View btn){
        Log.i(TAG,"onClick:");
        String str= rmb.getText().toString();
        Log.i(TAG,"onClick: get str="+str);
        float r = 0;
        if(rmb.length()>0){
         r= Float.parseFloat(str);}else{
            Toast.makeText(this,"请输入内容", Toast.LENGTH_SHORT).show();
        }
        Log.i(TAG,"onClick: r="+r);
         if(btn.getId()==R.id.btn_dollar){
            show.setText(String.format("%.2f",r*dollarRate));
         } else if(btn.getId()==R.id.btn_euro){
            show.setText(String.format("%.2f",r*euroRate));
         } else{
             show.setText(String.format("%.2f",r*wonRate));

         }


    }
    public void openOne(View btn){
        Intent config = new Intent(this,configActivity.class);
        config.putExtra("dollar_rate_key",dollarRate);
        config.putExtra("euro_rate_key",euroRate);
        config.putExtra("won_rate_key",wonRate);
        Log.i(TAG,"openOne: dollarRate=" + dollarRate);
        Log.i(TAG,"openOne: euroRate=" + euroRate);
        Log.i(TAG,"openOne: wonRate=" + wonRate);

        startActivity(config);
        startActivityForResult(config,1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rate,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId()==R.id.menu_set){
            openConfig();
        }else if(item.getItemId()==R.id.open_list){
            Intent list = new Intent(this,MyListActivity.class);
            startActivity(list);

        }
        return super.onOptionsItemSelected(item);
    }

    private void openConfig() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        if(requestCode==1 && resultCode==2){
            Bundle bundle = data.getExtras();
            dollarRate = bundle.getFloat("key_dollar",0.1f);
            euroRate = bundle.getFloat("key_euro",0.1f);
            wonRate = bundle.getFloat("key_won",0.1f);
            Log.i(TAG, "onActivityResult: dollarRate" +dollarRate);
            Log.i(TAG, "onActivityResult: euroRate" +euroRate);
            Log.i(TAG, "onActivityResult: wonRate" +wonRate);
            SharedPreferences sharedPreferences = getSharedPreferences("myrate",Activity.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putFloat("dollar_rate",dollarRate);
            edit.putFloat("euro_rate",euroRate);
            edit.putFloat("won_rate",wonRate);
            edit.commit();
            Log.i(TAG,"onActivityResult:数据已保存到sharedPreferences");


        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void run() {
        Log.i(TAG,"run: run().......");
        for(int i=1; i<6; i++){
            Log.i(TAG,"run: i="+i);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Bundle bundle = new Bundle();

        /*URL url = null;
        try {
            url = new URL("www.usd-cny.com/bank of china.htm");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            InputStream in = http.getInputStream();
            String html = inputStream2String(in);
            Log.i(TAG,"run:html="+html);
            Document doc=Jsoup.parse(html);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        bundle = getFromBOC();
        Message msg = handler.obtainMessage();
        msg.what = 5;
        //msg.obj = "Hello from run()";
        msg.obj = bundle;
        handler.sendMessage(msg);
    }

    private Bundle getFromBOC( ){
        Bundle bundle = new Bundle();
        Document doc = null;
        try {
            doc = Jsoup.connect("http://www.boc.cn/http://www.boc.cn/").get();
           // doc=Jsoup.parse(html);
            Log.i(TAG,"run:"+doc.title());
            Elements tables = doc.getElementsByTag("table");
            /*for(Element table: tables){
                Log.i(TAG,"run: table[+i+]="+table);
                i++;

            }*/
            Element table6=tables.get(5);
          //  Log.i(TAG,"run; table6="+ table6);
            Elements tds = table6.getElementsByTag("td");
            for(int i = 1;i<tds.size();i+=8){
                Element td1 = tds.get(i);
                Element td2 = tds.get(i+5);
                String str1 = td1.text();
                //String val = td2.text();
                Log.i(TAG,"run:td="+td1.text()+"==>"+td2.text());
                if("美元".equals(str1)){
                    bundle.putFloat("dollar-rate",100f/Float.parseFloat(td2.text()));
                }else if("欧元".equals(str1)){
                    bundle.putFloat("euro-rate",100f/Float.parseFloat(td2.text()));
                }else if("韩国元".equals(str1)){
                    bundle.putFloat("won-rate",100f/Float.parseFloat(td2.text()));
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bundle;
    }
    private Bundle getFromUsdCny( ){
        Bundle bundle = new Bundle();
        Document doc = null;
        try {
            doc = Jsoup.connect("http://www.boc.cn/sourcedb/whpj/\n").get();
            // doc=Jsoup.parse(html);
            Log.i(TAG,"run:"+doc.title());
            Elements tables = doc.getElementsByTag("table");
            /*for(Element table: tables){
                Log.i(TAG,"run: table[+i+]="+table);
                i++;

            }*/
            Element table6=tables.get(5);
            //  Log.i(TAG,"run; table6="+ table6);
            Elements tds = table6.getElementsByTag("td");
            for(int i = 1;i<tds.size();i+=8){
                Element td1 = tds.get(i);
                Element td2 = tds.get(i+5);
                String str1 = td1.text();
                Log.i(TAG,"run:td="+td1.text()+">=="+td2.text());
                if("美元".equals(str1)){
                    bundle.putFloat("dollar-rate",100f/Float.parseFloat(td2.text()));
                }else if("欧元".equals(str1)){
                    bundle.putFloat("euro-rate",100f/Float.parseFloat(td2.text()));
                }else if("韩国元".equals(str1)){
                    bundle.putFloat("won-rate",100f/Float.parseFloat(td2.text()));
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bundle;
    }

    private String inputStream2String(InputStream inputStream) throws IOException {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(inputStream, "gb2312");
        for (; ; ) {
            int rsz;
            rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0)
                break;
                out.append(buffer,0,rsz);

        }
        return out.toString();
    }
}
