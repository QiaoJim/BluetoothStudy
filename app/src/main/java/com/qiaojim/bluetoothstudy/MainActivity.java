package com.qiaojim.bluetoothstudy;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";

    TabLayout tabLayout;
    ViewPager viewPager;
    MyPagerAdapter pagerAdapter;
    String[] titleList=new String[]{"设备列表","数据传输"};
    List<Fragment> fragmentList=new ArrayList<>();

    DeviceListFragment deviceListFragment;
    DataTransFragment dataTransFragment;

    BluetoothAdapter bluetoothAdapter;

    Handler uiHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case Params.MSG_REV_A_CLIENT:
                    Log.e(TAG,"--------- uihandler set device name, go to data frag");
                    BluetoothDevice clientDevice = (BluetoothDevice) msg.obj;
                    dataTransFragment.receiveClient(clientDevice);
                    viewPager.setCurrentItem(1);
                    break;
                case Params.MSG_CONNECT_TO_SERVER:
                    Log.e(TAG,"--------- uihandler set device name, go to data frag");
                    BluetoothDevice serverDevice = (BluetoothDevice) msg.obj;
                    dataTransFragment.connectServer(serverDevice);
                    viewPager.setCurrentItem(1);
                    break;
                case Params.MSG_SERVER_REV_NEW:
                    String newMsgFromClient = msg.obj.toString();
                    dataTransFragment.updateDataView(newMsgFromClient, Params.REMOTE);
                    break;
                case Params.MSG_CLIENT_REV_NEW:
                    String newMsgFromServer = msg.obj.toString();
                    dataTransFragment.updateDataView(newMsgFromServer, Params.REMOTE);
                    break;
                case Params.MSG_WRITE_DATA:
                    String dataSend = msg.obj.toString();
                    dataTransFragment.updateDataView(dataSend, Params.ME);
                    deviceListFragment.writeData(dataSend);
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();

        initUI();
    }


    /**
     * 返回 uiHandler
     * @return
     */
    public Handler getUiHandler(){
        return uiHandler;
    }

    /**
     * 初始化界面
     */
    private void initUI() {
        tabLayout= (TabLayout) findViewById(R.id.tab_layout);
        viewPager= (ViewPager) findViewById(R.id.view_pager);

        tabLayout.addTab(tabLayout.newTab().setText(titleList[0]));
        tabLayout.addTab(tabLayout.newTab().setText(titleList[1]));

        deviceListFragment=new DeviceListFragment();
        dataTransFragment=new DataTransFragment();
        fragmentList.add(deviceListFragment);
        fragmentList.add(dataTransFragment);

        pagerAdapter=new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * ViewPager 适配器
     */
    public class MyPagerAdapter extends FragmentPagerAdapter{

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleList[position];
        }
    }

    /**
     * Toast 提示
     */
    public void toast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}
