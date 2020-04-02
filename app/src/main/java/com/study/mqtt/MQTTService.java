package com.study.mqtt;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * MQTT服务
 * 控制MQTT所有行为
 */
public class MQTTService extends Service {

    //**********连接服务器需要的参数**********//
    private String brokerAddressPort;// MQTT服务器地址
    private String clientID;//唯一的客户端识别码
    private String userName;//服务器用户名
    private String password;//服务器密码
    //**********连接服务器需要的参数**********//

    private MqttClient mMqttClient;//客户端的操作对象

    private MQTTController mMQTTController = new MQTTController();// Binder子类

    private LocalBroadcastManager mLocalBroadcastManager;

    /**
     * 初始化需要的变量
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);// 获得本地广播发送器
    }

    /**
     * 当开启服务时，首先调用此方法，
     * 通过Intent获得用户输入的所有服务器连接相关参数
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            String content = intent.getStringExtra("content");
            String[] str = content.split("#");// 分割获得的服务器连接参数的信息
            // 依次赋值给个参数
            brokerAddressPort = "tcp://" + str[0];
            clientID = str[1];
            userName = str[2];
            password = str[3];
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * MQTT功能控制器
     */
    class MQTTController extends Binder {

        /**
         * 连接服务器
         */
        public void connect() {
            try {
                MemoryPersistence persistence = new MemoryPersistence();// 内存储存的方式
                mMqttClient = new MqttClient(brokerAddressPort, clientID, persistence);
                MqttConnectOptions connOpts = new MqttConnectOptions();// 连接服务器需要的参数对象
                connOpts.setCleanSession(true);// 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
                connOpts.setUserName(userName);// 设置连接需要的服务器登录用户名
                connOpts.setPassword(password.toCharArray());// 设置连接需要的服务器登录密码
                Log.d(Constrat.TAG, "MQTTClientCreated");
                mMqttClient.connect(connOpts);// 连接服务器
                Log.d(Constrat.TAG, "MQTTConnected");
            } catch (Exception e) {
                Log.d(Constrat.TAG, "connect->" + e.getMessage());
                e.printStackTrace();
            }
        }

        /**
         * 断开连接
         */
        public void disconnect() {
            try {
                mMqttClient.disconnect();
                Log.d(Constrat.TAG,"MQTTDisconnect");
            } catch (MqttException e) {
                Log.d(Constrat.TAG,"disconnect->" + e.getMessage());
                e.printStackTrace();
            }
        }

        /**
         * 获得MQTT服务器的连接状态
         * @return true 已连接 false 未连接
         */
        public boolean isConnected() {
            return mMqttClient.isConnected();
        }

        /**
         * 关闭客户端
         */
        public void close() {
            try {
                mMqttClient.close();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        /**
         * 发布指定主题（topic）的消息
         * @param mqttMessage 发布的消息
         */
        public void publish(String topic, MqttMessage mqttMessage) {
            try {
                    mqttMessage.setRetained(false);// 是否为保留消息
                    mMqttClient.publish(topic, mqttMessage);
                    Log.d(Constrat.TAG, "publish");
            } catch (MqttException e) {
                Log.d(Constrat.TAG, "publish->"+e.getStackTrace());
            }
        }

        /**
         *订阅指定主题的消息
         */
        public void subscribe(String topic, int qos) {
            try {
                Log.d(Constrat.TAG, "subscribe");
                mMqttClient.subscribe(topic, qos, new IMqttMessageListener() {
                    @Override
                    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                        Log.d(Constrat.TAG, "messageArrived");
                        Intent intent = new Intent("com.study.mqtt.MESSAGEARRIVED");
                        intent.putExtra("arrivedmessage", mqttMessage.toString() + "->topic: " + s);// s为topic
                        mLocalBroadcastManager.sendBroadcast(intent);
                    }
                });
                Intent intent = new Intent("com.study.mqtt.SUBCRIBESUCCESSD");
                mLocalBroadcastManager.sendBroadcast(intent);
            } catch (MqttException e) {
                Log.d(Constrat.TAG, "subscribe->" + e.getMessage());
                Intent intent = new Intent("com.study.mqtt.SUBCRIBEFAILED");
                mLocalBroadcastManager.sendBroadcast(intent);
            }
        }

        /**
         * 取消订阅的主题
         * @param topic 要取消订阅的主题
         */
        public void unSubcribe(String topic) {
            try {
                mMqttClient.unsubscribe(topic);
                Intent intent = new Intent("com.study.mqtt.UNSUBCRIBESUCCESSD");
                mLocalBroadcastManager.sendBroadcast(intent);
            } catch (MqttException e) {
                Log.d(Constrat.TAG, "unsubscribe->" + e.getMessage());
                Intent intent = new Intent("com.study.mqtt.UNSUBCRIBEFAILED");
                mLocalBroadcastManager.sendBroadcast(intent);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMQTTController;
    }

}
