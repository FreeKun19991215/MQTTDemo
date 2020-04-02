package com.study.mqtt.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.study.mqtt.R;

/**
 * 滑动控件的碎片
 * 由于对碎片组件，理解不够导致，没有在此界面进行此碎片控件的配置
 * 此控件的配置全部都在主界面配置，后续可重新配置控件回此碎片方法中，
 * 由外部主界面调用
 */
public class DrawerLayoutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.drawer_layout_fragment, container, false);
        return view;
    }

}
