package com.ch.custom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.ch.custom.view.RangeBarView;

public class MainActivity extends AppCompatActivity {

    private TextView tvLeftValue, tvRightValue;
    private RangeBarView rangeBarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLeftValue = (TextView) findViewById(R.id.left_value_tv);
        tvRightValue = (TextView) findViewById(R.id.right_value_tv);
        rangeBarView = (RangeBarView) findViewById(R.id.view_range_bar);

        int minValue = 0;
        int maxValue = 100;
        int sliceValue = 20;
        tvLeftValue.setText(minValue+"");
        tvRightValue.setText(maxValue+"");
        rangeBarView.setDatas(minValue, maxValue, sliceValue, new RangeBarView.OnMoveValueListener() {
            @Override
            public void onMoveValue(int leftValue, int rightValue) {
                tvLeftValue.setText("左边值为：" + leftValue + "--> "+leftValue);
                tvRightValue.setText("右边值为：" + rightValue + "--> "+rightValue);
            }
        });
    }
}
