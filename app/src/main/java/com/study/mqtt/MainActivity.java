package com.study.mqtt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.study.mqtt.Fragment.PublishFragment;
import com.study.mqtt.Fragment.SubcribeFragment;

import org.eclipse.paho.client.mqttv3.MqttMessage;


/**
 *  连接MQTT服务器，并实现相关功能
 * 1.发布消息
 * 2.订阅主题
 *由于开发时间有限，遇到的问题超出可控范围
 * 滑动切换发布和订阅碎片界面暂未开发，后续可添加回来
 * Created by freeKun on 2020/3/20
 */
public class MainActivity extends AppCompatActivity {

    public MQTTService.MQTTController mMQTTController = null;// MQTT相关方法控制器

    //**********DrawerLayout界面控件*********//
    private DrawerLayout mDrawerLayout;
    private EditText mBrokerAddressEditText;
    private EditText mPortEditText;
    private EditText mClientIDEditText;
    private EditText mUserName;
    private EditText mPassword;
    //**********DrawerLayout界面控件*********//

    //*********主界面控件*************//
    private TextView mBrokerIDText;
    private ImageView mImageView;
    private TextView mPulishText;
    private TextView mSubcribeText;
    //*********主界面控件*************//

    //*********发布和订阅的界面控件**********//
    private int isWhatFragemt = 0;
    private PublishFragment mPublishFragment;
    private SubcribeFragment mSubcribeFragment;
    //*********发布和订阅的界面控件**********//

    //*********本地广播***********//
    private IntentFilter mIntentFiler;
    private LocalBroadcastManager mLocalBroadcastManager;
    //*********本地广播***********//

    private Intent intent;

    Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }

    /**
     * 设置ActionBar相关内容，
     * 并初始化滑动组件和主界面各控件
     */
    private void initUI() {
        ActionBar actionBar = getSupportActionBar();
        if ( actionBar != null ) {
            actionBar.setDisplayHomeAsUpEnabled(true);// 使能开启滑动界面的图标
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);// 设置开启滑动界面的图标
        }
        mDrawerLayout = findViewById(R.id.drawer_layout);// 滑动组件
        mBrokerAddressEditText = findViewById(R.id.broker_address);// 服务器的地址
        mPortEditText = findViewById(R.id.port);// tcp连接服务器所用的端口
        mClientIDEditText = findViewById(R.id.client_id);// 用户输入的服务器登录的唯一clientID参数
        mUserName = findViewById(R.id.user_name);// 用户输入的服务器登录的用户参数
        mPassword = findViewById(R.id.password);// 用户输入的服务器登录的密码参数
        mImageView = findViewById(R.id.connectState);// 指示是否连接MQTT服务器的图片
        mBrokerIDText = findViewById(R.id.broker_id);// 显示连接的服务器地址的TextView
        mPulishText = findViewById(R.id.publish_text);// 主界面的发布TextView
        mSubcribeText = findViewById(R.id.subscribe_text);// 主界面的订阅TextView
        mPulishText.setTextColor(Color.BLUE); // 初始化发布TextView字体为蓝色
        mSubcribeText.setTextColor(Color.BLACK);// 初始化订阅TextView字体为黑色
        mPublishFragment = new PublishFragment();// 初始化发布碎片界面
        mSubcribeFragment = new SubcribeFragment();// 初始化订阅碎片界面
        replaceFragment(mPublishFragment);// 初始化一开始的发布和订阅碎片界面
        initLocalBroadcast();
    }

    /**
     * 注册本地广播
     */
    private void initLocalBroadcast() {
        mIntentFiler = new IntentFilter();
        mIntentFiler.addAction("com.study.mqtt.MESSAGEARRIVED");
        mIntentFiler.addAction("com.study.mqtt.SUBCRIBESUCCESSD");
        mIntentFiler.addAction("com.study.mqtt.SUBCRIBEFAILED");
        mIntentFiler.addAction("com.study.mqtt.UNSUBRIBE");
        mIntentFiler.addAction("com.study.mqtt.UNSUBCRIBESUCCESSD");
        mIntentFiler.addAction("com.study.mqtt.UNSUBCRIBEFAILED");
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFiler);
    }

    /**
     * 按钮连接的点击事件
     * 功能：连接服务器
     * @param view
     */
    public void connect(View view) {
        //如果服务器正在连接状态，使此方法无效
        if ( mMQTTController != null && mMQTTController.isConnected() ) {
            showToast("已连接");
        }else {
            /**
             * 如果服务器还为初始化为null，进入此语句块
             * 判断用户参数是否填写完整
             */
            if ( !(TextUtils.isEmpty(mBrokerAddressEditText.getText()) & TextUtils.isEmpty(mPortEditText.getText()) & TextUtils.isEmpty(mClientIDEditText.getText()) & TextUtils.isEmpty(mUserName.getText()) & TextUtils.isEmpty(mPassword.getText())) ) {
                String content = mBrokerAddressEditText.getText() + ":" + mPortEditText.getText() + "#" + mClientIDEditText.getText() + "#" + mUserName.getText() + "#" + mPassword.getText();// 整合用户输入的各服务器连接需要的参数
                intent = new Intent(this, MQTTService.class);
                intent.putExtra("content", content);
                startService(intent);// 先开启服务，目的是调用onStartCommand方法将MQTTSrevice需要的服务器连接参数初始化
                bindService(intent, connection, BIND_AUTO_CREATE);// 在绑定服务，目的回调ServiceConnection方法，获得操作MQTT服务器的Binder类
            }else {
                showToast("请输入完整所有连接相关信息");
            }
        }
    }

    /**
     * 按钮断开连接的点击事件
     * 功能断开服务器连接，并关闭服务器对象且置空
     * @param view
     */
    public void disconnection(View view) {
        // 如果MQTT控制器还为初始化说明，还为连接到服务器，直接跳过此语句块
        if ( mMQTTController != null ) {
            mMQTTController.disconnect();
            if ( mMQTTController.isConnected() == false) {
                mImageView.setImageResource(R.drawable.circle);//当断开连接后，设置显示服务器连接状态的图片更改
                showToast("断开连接成功");
            }else {
                showToast("断开连接失败");
            }
            mMQTTController.close();// 关闭服务器，方便下次更换服务器地址的连接
            mMQTTController = null;// 设置为null，使连接按钮在下次按下时，可以顺利调用connect函数连接服务器
            stopService(intent);// 关闭服务，使重新连接服务器时连接参数得以重新赋值
            unbindService(connection);// 解绑服务，使重新连接服务器时重新获得MQTT控制器
        }
    }

    /**
     * 主界面的订阅TextView的点击事件
     * @param view
     */
    public void replaceFramentToSubcribe(View view) {
        // 通过isWhatFragment变量判断当前界面上处于哪个碎片界面
        if ( isWhatFragemt == 0 ) {
            replaceFragment(mSubcribeFragment);
            isWhatFragemt = 1;
            mPulishText.setTextColor(Color.BLACK);
            mSubcribeText.setTextColor(Color.BLUE);
        }
    }

    /**
     * 主界面的发布TextView的点击事件
     * @param view
     */
    public void replaceFramentToPublish(View view) {
        // 通过isWhatFragment变量判断当前界面上处于哪个碎片界面
        if ( isWhatFragemt == 1 ) {
            replaceFragment(mPublishFragment);
            isWhatFragemt = 0;
            mSubcribeText.setTextColor(Color.BLACK);
            mPulishText.setTextColor(Color.BLUE);
        }
    }

    /**
     * 发布和订阅碎片界面的切换方法
     * @param fragment 要替换的碎片
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.publish_subcribe_fragment, fragment);
        transaction.commit();
    }

    /**
     * 发布碎片界面的发布按钮的点击事件
     * @param view
     */
    public void publish(View view) {
        // 如果未填写发布的主题或未初始化MQTT控制器或服务器未连接，不可发布主题消息
        if ( TextUtils.isEmpty(mPublishFragment.getTopic()) == false && mMQTTController != null && mMQTTController.isConnected() == true ) {
            MqttMessage message = new MqttMessage();// 消息对象
            //message.setRetained(false);// 发布方法内已经设置
            message.setQos(mPublishFragment.getQos());// 设置消息的Qos等级
            message.setPayload(mPublishFragment.getBytes());// 设置消息内容
            mMQTTController.publish(mPublishFragment.getTopic(), message);// 此处忘记开发发布成功弹出Toast，可由MQTT控制器内部发布完成发布成功发布的广播
            mPublishFragment.setTopicNull();// 设置发布碎片内的topic值为空，方便下次发布操作
        }else {
            mPublishFragment.setTopicNull();// 设置发布碎片内的topic值为空，方便下次发布操作
            showToast("请输入完整主题和内容或连接服务器");
        }
    }

    /**
     * 订阅碎片界面的订阅按钮的点击事件
     * @param view
     */
    public void subcribe(View view) {
        // 判断用户输入的主题是否订阅过
        if ( mSubcribeFragment.isTopicSubcribe(mSubcribeFragment.getTopic()) ) {
            showToast("此主题已经订阅过了");
        }else {
            // 如果未填写订阅主题或未初始化MQTT控制器或服务器未连接，不可订阅主题消息
            if ( TextUtils.isEmpty(mSubcribeFragment.getTopic()) == false && mMQTTController != null && mMQTTController.isConnected()) {
                mMQTTController.subscribe(mSubcribeFragment.getTopic(), mSubcribeFragment.getQos());
            }else {
                mSubcribeFragment.setTopicNull();// 设置订阅碎片内的topic值为空，方便下次订阅操作
                if ( TextUtils.isEmpty(mSubcribeFragment.getTopic()) ) {
                    showToast("请输入订阅的主题");
                }else {
                    showToast("请连接服务器");
                }
            }
        }
    }

    /**
     * 清空消息的点击事件触发器
     * @param view
     */
    public void cls(View view) {
        mSubcribeFragment.clsMessage();
    }

    /**
     * 广播接收器
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if ( "com.study.mqtt.MESSAGEARRIVED".equals(action) ) {// 订阅的主题收到消息
                String arrivedMessage = intent.getStringExtra("arrivedmessage");
                mSubcribeFragment.addMessageList(arrivedMessage);
            }else if ( "com.study.mqtt.SUBCRIBESUCCESSD".equals(action) ) {// 订阅主题成功
                mSubcribeFragment.addSubcribeTopicList();// 将成功订阅的主题加入订阅的主题列表
                mSubcribeFragment.setSubcribeTopic("add");// 将订阅的主题加入已订阅列表
                mSubcribeFragment.setTopicNull();// 置空topic，方便下一次订阅主题
                showToast("订阅成功");
            }else if ( "com.study.mqtt.SUBCRIBEFAILED".equals(action) ) {// 订阅主题失败
                mSubcribeFragment.setTopicNull();// 置空topic，方便下一次订阅主题
                showToast("订阅失败");
            }else if ( "com.study.mqtt.UNSUBRIBE".equals(action) ) {
                String str = intent.getStringExtra("topicSelected");// 获得取消订阅的主题和将订阅的主题移除出列表需要的Qos等级参数
                String qos = str.substring(str.length()-1);// 裁剪出Qos等级
                mSubcribeFragment.setQos(Integer.parseInt(qos));// 设置订阅碎片内的qos变量，发布移除操作
                String topic = str.substring(0, str.length()-1);// 裁剪出取消订阅的主题
                mMQTTController.unSubcribe(topic);
            }else if ( "com.study.mqtt.UNSUBCRIBESUCCESSD".equals(action) ) {
                mSubcribeFragment.setSubcribeTopic("del");// 将主题移除出订阅的主题列表
                showToast("取消订阅成功");
            }else if ( "com.study.mqtt.UNSUBCRIBEFAILED".equals(action) ) {
                showToast("取消订阅失败");
            }

        }
    };

    /**
     * 开启滑动界面的点击事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch ( item.getItemId() ) {
            case android.R.id.home:
                if ( mDrawerLayout.isDrawerOpen(GravityCompat.START) ) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 绑定服务时，回调的函数
     */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMQTTController = (MQTTService.MQTTController) service;// 获得MQTT控制器
            Log.d(Constrat.TAG, "onServiceConnected");
            ((MQTTService.MQTTController) service).connect();// 连接服务器
            if ( mMQTTController.isConnected() == false ) {
                showToast("连接失败");
                mMQTTController = null;
            }else {
                showToast("连接成功");
                mImageView.setImageResource(R.drawable.greencircle);// 设置主界面成功连接的标识图片
                mBrokerIDText.setText(mBrokerAddressEditText.getText());// 设置主界面成功连接的服务器IP地址标识
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(Constrat.TAG, "onServiceDisconnected");
            showToast("onServiceDisconnected");
        }
    } ;

    /**
     * 标准化弹窗控件
     * @param str
     */
    private void showToast(String str) {
        if ( mToast == null ) {
            mToast = Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT);
            mToast.show();
        }else {
            mToast.setText(str);
            mToast.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( mMQTTController != null ) {
            mMQTTController.close();
        }
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
    }

}
