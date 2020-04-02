package com.study.mqtt.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.study.mqtt.Constrat;
import com.study.mqtt.MainActivity;
import com.study.mqtt.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 点击主界面订阅字段时，切换的订阅主题并接收消息碎片
 */
public class SubcribeFragment extends Fragment {

    //********订阅主题碎片的控件*********//
    private Spinner mSubcribeSpinner;
    private EditText mSubcribeEditText;
    private TextView mSubcribeTopicSum;
    //********订阅主题碎片的控件*********//

    //***********订阅主题数的ListView***********//
    private ListView mSubcribeSumListView;
    private List<String> mSubcribeSumList = new ArrayList<>();
    private ArrayAdapter<String> mSubcribeTopicAdapter;
    //***********订阅主题数的ListView***********//

    //***********收到主题消息的ListView***********//
    private ListView mMessageListView;
    private List<String> mMessageList = new ArrayList<>();
    private ArrayAdapter<String> mMessageAdapter;
    //***********收到主题消息的ListView***********//

    //********订阅需要的参数***********//
    private int qos;
    private String topic;
    //********订阅需要的参数***********//

    private List<String> subcribeTopic;//订阅的主题

    private LocalBroadcastManager mLocalBroadcastManager;

    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.subrcibe_fragment, container, false);
        initUI();
        return view;
    }

    private void initUI() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getActivity());// 获得本地广播发送器
        subcribeTopic = new ArrayList<>();// 初始化订阅的主题
        mSubcribeSpinner = view.findViewById(R.id.subcribe_spinner);// 下拉选项框
        /**
         * 给Spinner下拉选项框，设置监听器，
         * 使下拉选项框每次变化时，下拉选项框的值qos得以随之改变
         */
        mSubcribeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                qos = Integer.parseInt(mSubcribeSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSubcribeEditText = view.findViewById(R.id.subcribe_topic);// 用户填写的订阅的主题内容
        initSubcribeSumList();
        initMessageList();
    }

    /**
     * 初始化订阅主题操作需要的列表以及显示的数量变化控件，
     * 并设置事件触发
     */
    private void initSubcribeSumList() {
        mSubcribeTopicSum = view.findViewById(R.id.subcribe_topic_sum);// 用户订阅的主题数
        mSubcribeSumListView = view.findViewById(R.id.subcribe_topic_list);// 订阅的主题数列表
        /**
         *点击订阅的主题列表的点击事件，
         * 为了再次确认用户是否确认取消订阅该主题
         */
        mSubcribeSumListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                topic = mSubcribeSumList.get(position).replace("     ->Qos：", "");
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());// 再次确认是否取消订阅
                dialog.setTitle("是否取消订阅这个主题");
                dialog.setMessage("取消订阅这个主题后，将无法收到这个主题的消息");
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent("com.study.mqtt.UNSUBRIBE");
                        intent.putExtra("topicSelected", topic);
                        mLocalBroadcastManager.sendBroadcast(intent);
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.show();
            }
        });
        mSubcribeTopicAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mSubcribeSumList);// 单行的字符串适配器
        mSubcribeSumListView.setAdapter(mSubcribeTopicAdapter);
    }

    /**
     * 初始化收到订阅的主题的消息操作需要的列表
     */
    private void initMessageList() {
        mMessageListView = view.findViewById(R.id.message);// 收到的消息列表
        mMessageAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mMessageList);// 单行的字符串适配器
        mMessageListView.setAdapter(mMessageAdapter);
    }

    /**
     * 每当点击订阅按钮时，刷新订阅的主题topic的值，由此外部可获得当前用户输入的topic
     */
    private void refreshTopic() {
        topic = mSubcribeEditText.getText().toString();
    }

    /**
     * 获取订阅的主题
     * @return 1.如果topic为空，返回null   2.如果不为空，返回topic值
     */
    public String getTopic() {
        refreshTopic();// 获取前刷新topic的值
        if (TextUtils.isEmpty(topic) ) {
            return null;
        }else {
            return topic;
        }
    }

    /**
     * 设置topic内容为空
     */
    public void setTopicNull() {
        topic = null;
    }

    /**
     *获取下拉选项框选择的值
     * @return 当前qos的值
     */
    public int getQos() {
        return qos;
    }

    /**
     * 取消订阅时，设置本类里的qos变量为取消订阅主题的Qos等级，方便取消订阅成功时，将其移除列表
     * @param qos
     */
    public void setQos(int qos) {
        this.qos = qos;
    }

    /**
     * 订阅主题前检查是否主题是否已经订阅
     * @param topic 要检查的主题
     * @return true：已经订阅过。 false：未订阅
     */
    public boolean isTopicSubcribe(String topic) {
        return subcribeTopic.contains(topic);
    }

    /**
     * 每当订阅或取消订阅成功,调用此方法，将订阅主题加入或移除于列表
     * @param option 当 option为 add时加入列表。  当 option为 del时，移除出列表
     */
    public void setSubcribeTopic(String option) {
        if ( option == "add" ){
            subcribeTopic.add(topic);
            mSubcribeTopicSum.setText(String.valueOf(subcribeTopic.size()));// 刷新总订阅数
        }else {
            subcribeTopic.remove(topic.substring(0, topic.length()-1));// 由于传过来的字符串尾部带Qos等级，应裁剪字符串后才可正常移除
            mSubcribeSumList.remove(topic.substring(0, topic.length()-1) + "     ->Qos：" + getQos());// 同上
            mSubcribeTopicAdapter.notifyDataSetChanged();
            mSubcribeTopicSum.setText(String.valueOf(subcribeTopic.size()));// 刷新总订阅数
        }
    }

    /**
     * 当订阅主题成功后，将主题添加进订阅主题列表
     */
    public void addSubcribeTopicList() {
        mSubcribeSumList.add(getTopic() + "     ->Qos：" +getQos());// 加上->标识，指示Qos等级，后续取消订阅时，也应删除此标识符
        mSubcribeTopicAdapter.notifyDataSetChanged();
    }

    /**
     * 当收到订阅的主题的消息后，将消息添加进消息列表
     * @param content 要添加进消息列表的字符串
     */
    public void addMessageList(String content) {
        mMessageList.add(content);
        mMessageAdapter.notifyDataSetChanged();
    }

    /**
     * 清空消息列表
     */
    public void clsMessage() {
        mMessageList.clear();
        mMessageAdapter.notifyDataSetChanged();
    }

}
