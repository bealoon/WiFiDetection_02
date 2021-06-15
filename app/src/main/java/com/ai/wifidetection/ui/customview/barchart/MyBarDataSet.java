package com.ai.wifidetection.ui.customview.barchart;

import com.ai.wifidetection.MyBuffer;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.List;

public class MyBarDataSet extends BarDataSet {

    private List<BarEntry> mYVal;

    public MyBarDataSet(List<BarEntry> yVals, String label) {
        super(yVals, label);
        this.mYVal = yVals;
    }

    @Override
    public int getColor(int index) {
        //此处根据自己的需求填写相应的代码
//        if (getEntryForIndex(index).getY() > ) {
//            return mColors.get(1);
//        }else {
//            return mColors.get(0);
//        }
        return mColors.get(MyBuffer.INSTANCE.getDet_status());
    }
}