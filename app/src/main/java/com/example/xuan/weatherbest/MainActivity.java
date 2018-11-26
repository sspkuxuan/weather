package com.example.xuan.weatherbest;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import weatherbest.bean.TodayWeather;
import weatherbest.util.NetUtil;
import weatherbest.viewPage.ViewPagerAdapter;

public class MainActivity extends Activity implements View.OnClickListener ,ViewPager.OnPageChangeListener{//定义一个MainActivity并且继承于Activity，然后设置一个Click监听器
    private static final int UPDATE_TODAY_WEATHER = 1;
    private ImageView mUpdateBtn;
    private ImageView mCitySelect;//定义两个私有ImageView属性
    //定义一个ProgressBar控件，ProgressBar是一个进度条组件
    private ProgressBar mUpdateProgressBar;
    private ViewPagerAdapter vpAdapter;
    private ViewPager vp;
    private List<View> views;
    private ImageView[] dots;
    private int[] ids = {R.id.iv1,R.id.iv2};
    private TextView week_today,temperature,climate,wind,week_today1,temperature1,climate1,wind1 ,week_today2,temperature2,climate2,wind2;
    private TextView week_today3,temperature3,climate3,wind3,week_today4,temperature4,climate4,wind4,week_today5,temperature5,climate5,wind5;


    private TextView cityTv,timeTv,humidityTv,weekTv,pmDataTv,pmQualityTv,wenduTv,
           temperatureTv,climateTv,windTv,city_name_Tv;//定义私有TextView属性
    private ImageView weatherImg,pmImg;//定义相关的控件对象
    /*
    通过消息机制，将解析的天气对象，通过消息发送给主线程，主线程接收到消息数
据后，调用updateTodayWeather函数，更新UI界面上的数据。
     */
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {//判断接收消息的what类型
                case UPDATE_TODAY_WEATHER:
                    //更新天气信息
                    updateTodayWeather((TodayWeather) msg.obj);
                    //关闭进度条，开启旋转图片
                        //隐藏刷新按钮
                        mUpdateBtn.setVisibility(View.VISIBLE);
                        //开启进度条progresbar
                        mUpdateProgressBar.setVisibility(View.GONE);

                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//调用基类中的onCreate方法
        setContentView(R.layout.weather_info);
       //刷新设置监听器
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);//刷新图标按钮赋值给mUpdateBtn
        mUpdateBtn.setOnClickListener(this);//在MainActivity类里设置事件处理的监听器
        //设置一个刷新旋转监听器
        mUpdateProgressBar = (ProgressBar)findViewById(R.id.title_update_progress);
        //为选择城市按钮设置监听器
        mCitySelect=(ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);
        if (NetUtil.getNetworkState(this) == NetUtil.NETWORN_NONE) {//调用NetUtil类里的getNetworkState方法，判断网络是否连接
            Log.d("myInternet", "网络挂了");
            Toast.makeText(MainActivity.this,"网络挂了！", Toast.LENGTH_LONG).show();
        }else if (NetUtil.getNetworkState(this) == NetUtil.NETWORN_WIFI)
        {
            Log.d("myInternet", "网络OK，WIFI连接");
            Toast.makeText(MainActivity.this,"WIFI连接！", Toast.LENGTH_LONG).show();
        }
        else
        {
            Log.d("myInternet", "网络OK，手机数据连接");
            Toast.makeText(MainActivity.this,"手机数据连接！", Toast.LENGTH_LONG).show();
        }
        //初始化
        initViews();
        initDots();
        initView();

    }
    @Override
    public void onClick(View view){//谁触发的onClick方法，view参数就是谁

        //选择城市按钮处理事件
        if(view.getId()==R.id.title_city_manager){
            Intent i=new Intent(this,SelectCity.class);//建立SelectCity和MainAcitivity的连接通信
            startActivityForResult(i,1);
        }


        //刷新按钮处理事件
        if(view.getId()==R.id.title_update_btn){
                //隐藏刷新按钮
                mUpdateBtn.setVisibility(View.GONE);
                //开启进度条progresbar
                mUpdateProgressBar.setVisibility(View.VISIBLE);

            //获取SharedPreferences实例（因为是接口，所以不能直接new,系统为我们提供了方法）
            //String name 保存的文件名
            //int mode  操作文件的模式,下面是四种操作模式的详解
            //Context.MODE_PRIVATE：为默认操作模式，代表该文件是私有数据，只能被应用本身访问，在该模式下，写入的内容会覆盖原文件的内容，如果想把新写入的内容追加到原文件中。可以使用Context.MODE_APPEND
            //Context.MODE_APPEND：模式会检查文件是否存在，存在就往文件追加内容，否则就创建新文件。
            //Context.MODE_WORLD_READABLE和Context.MODE_WORLD_WRITEABLE用来控制其他应用是否有权限读写该文件。
            //MODE_WORLD_READABLE：表示当前文件可以被其他应用读取；
            //MODE_WORLD_WRITEABLE：表示当前文件可以被其他应用写入。
            SharedPreferences sharedPreferences=getSharedPreferences("config",MODE_PRIVATE);
            //系统默认值
            String cityCode=sharedPreferences.getString("main_city_code","101010100");
            Log.d("myWeather",cityCode);
            if (NetUtil.getNetworkState(this) == NetUtil.NETWORN_NONE) {//调用NetUtil类里的getNetworkState方法，判断网络是否连接
                Log.d("myInternet", "网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了！", Toast.LENGTH_LONG).show();
            }else if (NetUtil.getNetworkState(this) == NetUtil.NETWORN_WIFI)
            {
                Log.d("myInternet", "网络OK，WIFI连接");
                Toast.makeText(MainActivity.this,"WIFI连接！", Toast.LENGTH_LONG).show();
                queryWeatherCode(cityCode);
            }
            else
            {
                Log.d("myInternet", "网络OK，手机数据连接");
                Toast.makeText(MainActivity.this,"手机数据连接！", Toast.LENGTH_LONG).show();
                queryWeatherCode(cityCode);
            }
        }
    }


    /**
     onActivityResult code
     **/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode= data.getStringExtra("cityCode");
            Log.d("myWeather", "选择的城市代码为"+newCityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(newCityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
    }
    /**
     *
     * ueryWeatherCode 更新城市天气信息
     */
    private void queryWeatherCode(String cityCode) {


        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;//定义网址地址
        Log.d("myWeather", address);
        new Thread(new Runnable() {//开启线程发送网络请求
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather=null;
                try {
                    URL url = new URL(address);//传入目标网址
                    con = (HttpURLConnection) url.openConnection();//打开网址
                    con.setRequestMethod("GET");//从服务器获取数据
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);//连接超时和读取超时时间，设为8秒
                    InputStream in = con.getInputStream();//获取从服务器返回的输入流
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));//构造一个BufferedReader，里面存放输入的字节转换后成的字符。
                    StringBuilder response = new StringBuilder();//定义 response对象处理字符串
                    String str;
                    while ((str = reader.readLine()) != null) {//从BufferedReader对象中读取一行的内容
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather", responseStr);
                    todayWeather = parseXML(responseStr);
                    if (todayWeather != null) {
                        Log.d("myWeather", todayWeather.toString());
                        Message msg =new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj=todayWeather;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    /**
     * 初始化界面
     */
    void initView(){
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        wenduTv=(TextView)findViewById(R.id.nowtemperature);


        week_today=views.get(0).findViewById(R.id.week_today);
        temperature=views.get(0).findViewById(R.id.temperature);
        climate=views.get(0).findViewById(R.id.climate);
        wind=views.get(0).findViewById(R.id.wind);

        week_today1=views.get(0).findViewById(R.id.week_today1);
        temperature1=views.get(0).findViewById(R.id.temperature1);
        climate1=views.get(0).findViewById(R.id.climate1);
        wind1=views.get(0).findViewById(R.id.wind1);

        week_today2=views.get(0).findViewById(R.id.week_today2);
        temperature2=views.get(0).findViewById(R.id.temperature2);
        climate2=views.get(0).findViewById(R.id.climate2);
        wind2=views.get(0).findViewById(R.id.wind2);

        week_today3=views.get(1).findViewById(R.id.week_today);
        temperature3=views.get(1).findViewById(R.id.temperature);
        climate3=views.get(1).findViewById(R.id.climate);
        wind3=views.get(1).findViewById(R.id.wind);

        week_today4=views.get(1).findViewById(R.id.week_today1);
        temperature4=views.get(1).findViewById(R.id.temperature1);
        climate4=views.get(1).findViewById(R.id.climate1);
        wind4=views.get(1).findViewById(R.id.wind1);

        week_today5=views.get(1).findViewById(R.id.week_today2);
        temperature5=views.get(1).findViewById(R.id.temperature2);
        climate5=views.get(1).findViewById(R.id.climate2);
        wind5=views.get(1).findViewById(R.id.wind2);


        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
        wenduTv.setText("N/A");

        week_today.setText("N/A");
        temperature.setText("N/A");
        climate.setText("N/A");
        wind.setText("N/A");

       //多了一次 已经删除

        week_today1.setText("N/A");
        temperature1.setText("N/A");
        climate1.setText("N/A");
        wind1.setText("N/A");

        week_today2.setText("N/A");
        temperature2.setText("N/A");
        climate2.setText("N/A");
        wind2.setText("N/A");

        week_today3.setText("N/A");
        temperature3.setText("N/A");
        climate3.setText("N/A");
        wind3.setText("N/A");

        week_today4.setText("N/A");
        temperature4.setText("N/A");
        climate4.setText("N/A");
        wind4.setText("N/A");

        week_today5.setText("N/A");
        temperature5.setText("N/A");
        climate5.setText("N/A");
        wind5.setText("N/A");


    }
    /*
    更新天气信息
     */

    /**
     *
     * 初始化小圆点
     */
    void initDots(){
        dots = new ImageView[views.size()];
        for(int i =0;i<views.size();i++){
            dots[i]=(ImageView)findViewById(ids[i]);
        }
    }
    /**
     *    初始化viewpage,动态加载要在viewpage显示的视图
     */
    private void initViews(){
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.sixday1,null));
        views.add(inflater.inflate(R.layout.sixday2,null));
        vpAdapter = new ViewPagerAdapter(views,this);
        vp = (ViewPager) findViewById(R.id.viewpager);
        vp.setAdapter(vpAdapter);
        //设置监听事件
        vp.addOnPageChangeListener(this);//setOnPageChangeListener过时了
    }

    /**
     * 更新天气信息
     * @param todayWeather
     */
    void updateTodayWeather(TodayWeather todayWeather){
        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+ "发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getLow()+"~"+todayWeather.getHigh());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());
        wenduTv.setText("当前温度"+todayWeather.getWendu());
        //新增六日天气信息
        week_today.setText(todayWeather.getWeek_today());
        temperature.setText(todayWeather.getTemperatureL()+"~"+todayWeather.getTemperatureH());
        climate.setText(todayWeather.getClimate());
        wind.setText(todayWeather.getWind());

        week_today1.setText(todayWeather.getWeek_today1());
        temperature1.setText(todayWeather.getTemperatureL1()+"~"+todayWeather.getTemperatureH1());
        climate1.setText(todayWeather.getClimate());
        wind1.setText(todayWeather.getWind());

        week_today2.setText(todayWeather.getWeek_today2());
        temperature2.setText(todayWeather.getTemperatureL2()+"~"+todayWeather.getTemperatureH2());
        climate2.setText(todayWeather.getClimate2());
        wind2.setText(todayWeather.getWind2());

        week_today3.setText(todayWeather.getWeek_today3());
        temperature3.setText(todayWeather.getTemperatureL3()+"~"+todayWeather.getTemperatureH3());
        climate3.setText(todayWeather.getClimate3());
        wind3.setText(todayWeather.getWind3());

        week_today4.setText(todayWeather.getWeek_today4());
        temperature4.setText(todayWeather.getTemperatureL4()+"~"+todayWeather.getTemperatureH4());
        climate4.setText(todayWeather.getClimate4());
        wind4.setText(todayWeather.getWind4());

        week_today5.setText(todayWeather.getWeek_today5());
        temperature5.setText(todayWeather.getTemperatureL5()+"~"+todayWeather.getTemperatureH5());
        climate5.setText(todayWeather.getClimate5());
        wind5.setText(todayWeather.getWind5());

        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
    }


    /**
     * 解析XML文件
     * @param xmldata
     * @return
     */
        private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather=null;
        int fengxiangCount=0;
        int fengliCount =0;
        int dateCount=0;
        int highCount =0;
        int lowCount=0;
        int typeCount =0;
        try{
            XmlPullParserFactory fac=XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser=fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType=xmlPullParser.getEventType();
            Log.d("myWeather","parseXML");
            while (eventType!=xmlPullParser.END_DOCUMENT){
                switch (eventType){
                    //判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather= new TodayWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime( xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount==0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang( xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                todayWeather.setWind1(xmlPullParser.getText());
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWind2(xmlPullParser.getText());
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWind3(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWind4(xmlPullParser.getText());
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWind5(xmlPullParser.getText());
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("fl_1")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWind(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                todayWeather.setWeek_today1(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWeek_today2(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWeek_today3(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWeek_today4(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWeek_today5(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("date_1") ) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWeek_today(xmlPullParser.getText());
                            }  else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                todayWeather.setTemperatureH1(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }
                            else if (xmlPullParser.getName().equals("high") && highCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureH2(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }
                            else if (xmlPullParser.getName().equals("high") && highCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureH3(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }
                            else if (xmlPullParser.getName().equals("high") && highCount ==3 ) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureH4(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }
                            else if (xmlPullParser.getName().equals("high") && highCount ==4 ) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureH5(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }
                            else if (xmlPullParser.getName().equals("high_1")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureH(xmlPullParser.getText().substring(2).trim());
                            }
                            else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                todayWeather.setTemperatureL1(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureL2(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("low") && lowCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureL3(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("low") && lowCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureL4(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("low") && lowCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureL5(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("low_1")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureL(xmlPullParser.getText().substring(2).trim());
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                todayWeather.setClimate1(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setClimate2(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setClimate3(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setClimate4(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setClimate5(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type_1") ) {
                                eventType = xmlPullParser.next();
                                todayWeather.setClimate(xmlPullParser.getText());
                            }
                        }
                        break;
                    //判断当前事件是否为元素结束事件
                    case XmlPullParser.END_TAG:
                        break;


                }
                //进入下一个元素并触发事件
                eventType=xmlPullParser.next();


            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return todayWeather;
    }


    @Override
    public void onPageScrolled(int position,float positionOffset, int positionOffsetPixels){

    }

    @Override
    public void onPageSelected(int position) {
        for (int a = 0;a<ids.length;a++){
            if(a==position){
                dots[a].setImageResource(R.drawable.page_indicator_focused);
            }else {
                dots[a].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
