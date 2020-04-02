package com.study.mqtt.Fragment;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.study.mqtt.R;

/**
 * 发布消息碎片界面
 */
public class PublishFragment extends Fragment {

    //************发布碎片界面的控件*******//
    private Spinner mPublishSpinner;
    private EditText mPublishEditText;
    private EditText getmPublishContentEditText;
    //************发布碎片界面的控件*******//

    //********发布消息需要的参数********//
    private String topic;
    private int qos = 0;
    private byte[] bytes;
    //********发布消息需要的参数********//

    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.publish_fragment, container, false);
        initUI();
        return view;
    }

    private void initUI() {
        mPublishSpinner = view.findViewById(R.id.publish_spinner);// 下拉选项框
        /**
         * 给Spinner下拉选项框，设置监听器，
         * 使下拉选项框每次变化时，下拉选项框的值qos得以随之改变
         */
        mPublishSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                qos = Integer.parseInt(mPublishSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mPublishEditText = view.findViewById(R.id.publish_topic);// 用户填写的发布的主题内容
        getmPublishContentEditText = view.findViewById(R.id.publish_content);// 用户填写的发布的消息的内容
    }

    /**
     * 刷新发布的主题topic的值
     */
    private void refreshTopic() {
        topic = mPublishEditText.getText().toString();
    }

    /**
     * 刷新发布的内容bytes的值
     */
    private void refreshBytes() {
        bytes = getmPublishContentEditText.getText().toString().getBytes();
    }

    /**
     * 获取发布的主题
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
     * @return
     */
    public int getQos() {
        return qos;
    }

    /**
     * 获取发布碎片界面的消息内容
     * @return 返回消息内容
     */
    public byte[] getBytes() {
        refreshBytes();// 获取前刷新消息内容
        return bytes;
    }

}
