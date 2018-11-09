package com.example.xuan.weatherbest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weatherbest.app.MyApplication;
import weatherbest.bean.City;

public class SelectCity extends Activity implements View.OnClickListener{
    private ImageView mBackBtn;
    private TextView mTestBtn;
    private ListView listView = null;
    private TextView cityselected = null;
    private List<City> listcity = MyApplication.getInstance().getCityList();
    private int listSize = listcity.size();
    private String[] city = new String[listSize];
    private ArrayList<String> mSearchResult = new ArrayList<>(); //搜索结果
    private Map<String,String> nameToCode = new HashMap<>();//城市名到编码
    private Map<String,String> nameToPinyin = new HashMap<>(); //城市名到拼音
    private EditText mSearch;
    String returnCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        mBackBtn=(ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);//给mBackBtn创建监听
        mTestBtn = (TextView) findViewById(R.id.title_name);
        mTestBtn.setOnClickListener(this);
        cityselected = (TextView)findViewById(R.id.title_name);

        for(int i = 0;i < listSize;i++){
            city [i] = listcity.get(i).getCity();
        }
        //建立映射
        String strName;
        String strNamePinyin;
        String strCode;

        for(City city1:listcity) {
            strCode = city1.getNumber();
            strName = city1.getCity();
            strNamePinyin = city1.getFirstPY();
            nameToCode.put(strName,strCode);
            nameToPinyin.put(strName,strNamePinyin);
            mSearchResult.add(strName);
        }
        final ArrayAdapter<String>adapter=new ArrayAdapter<String>(SelectCity.this,android.R.layout.simple_list_item_1,mSearchResult );

        listView=findViewById(R.id.list_view);//定义一个listview对象
        listView.setAdapter(adapter);
        // listView.setAdapter(arrayAdapter);
        /*
        对listview的内容点击事件处理
         */




        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String returnCityName = mSearchResult.get(i);

                Toast.makeText(SelectCity.this,"你单击了:"+city[i],Toast.LENGTH_SHORT).show();
                //cityselected.setText("当前城市："+ city[i]);
                //int position = listView.getCheckedItemPosition();

                returnCode =nameToCode.get(returnCityName);
                Intent j = new Intent();
                j.putExtra("cityCode",  returnCode);//把城市的code信息传回MainActivity
                setResult(RESULT_OK, j);
                finish();

            }
        });
        mSearch = (EditText)findViewById(R.id.search_city);
        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSearchResult.clear();
                if (s!=null){
                    for(City city1:listcity){
                        if(city1.getCity().toString().contains(s)){
                            nameToCode.put(city1.getCity(),city1.getNumber());
                            nameToPinyin.put(city1.getCity(),city1.getFirstPY());
                            mSearchResult.add(city1.getCity());
                        }
                    }
                }
                else {
                    for (City city1 : listcity) {
                        nameToCode.put(city1.getCity(), city1.getNumber());
                        nameToPinyin.put(city1.getCity(), city1.getFirstPY());
                        mSearchResult.add(city1.getCity());
                    }
                }

                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }




    /*
    返回处理
     */
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back:
                // Intent i = new Intent();
                //  i.putExtra("cityCode", "101220101");
                //setResult(RESULT_OK, i);
                finish();
                break;
            default:
                break;
        }
    }


}
