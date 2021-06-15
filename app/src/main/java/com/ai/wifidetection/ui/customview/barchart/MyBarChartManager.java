package com.ai.wifidetection.ui.customview.barchart;


import android.graphics.Color;

import com.ai.wifidetection.utils.AppTools;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.List;

public class MyBarChartManager {

    private BarChart barChart;
    private BarData barData;
    private BarDataSet barDataSet;
    private List<IBarDataSet> barDataSets = new ArrayList<>();
    List<BarEntry> barEntries = new ArrayList<>();

    public MyBarChartManager(BarChart mBarChart, String name, int color, final List<String> xLabels){
        this.barChart = mBarChart;

        initBarChart(xLabels);
        initBarDataSet(name, color,0);
    }

    public MyBarChartManager(BarChart mBarChart, String name, int color, final List<String> xLabels, int fmtLimit){
        this.barChart = mBarChart;

        initBarChart(xLabels);
        initBarDataSet(name, color,fmtLimit);
    }

    private void initBarChart(final List<String> xLabels){
        barChart.getDescription().setEnabled(false);//设置描述
        barChart.setPinchZoom(false);//设置按比例放缩柱状图
        barChart.setScaleEnabled(false);
        barChart.setDragEnabled(true); //X,Y轴同时缩放，false则X,Y轴单独缩放,默认false
        barChart.setNoDataText("暂无数据"); // 没有数据时的提示文案
        barChart.setTouchEnabled(true); // 所有触摸事件,默认true
        barChart.setDrawBorders(false); ////显示边界
        barChart.getAxisRight().setEnabled(false);//禁用右侧y轴
        barChart.getLegend().setEnabled(false);
        barChart.setExtraBottomOffset(10);//距视图窗口底部的偏移，类似与paddingbottom
//        barChart.setExtraTopOffset(10);//距视图窗口顶部的偏移，类似与paddingtop
        barChart.setFitBars(true);//使两侧的柱图完全显示
        barChart.animateX(500);//数据显示动画，从左往右依次显示

        //图例设置
       /* Legend legend = barChart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);//图例水平居中
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);//图例在图表上方
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);//图例的方向为水平
        legend.setDrawInside(false);//绘制在chart的外侧
        legend.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);//图例中的文字方向

        legend.setForm(Legend.LegendForm.SQUARE);//图例窗体的形状
        legend.setFormSize(0f);//图例窗体的大小
        legend.setTextSize(16f);//图例文字的大小*/
        //legend.setYOffset(-2f);

        //x坐标轴设置
        // IAxisValueFormatter xAxisFormatter = new StringAxisValueFormatter(xAxisValue);//设置自定义的x轴值格式化器
        XAxis xAxis = barChart.getXAxis();//获取x轴
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//设置X轴标签显示位置
        xAxis.setDrawGridLines(false);//不绘制格网线
        xAxis.setDrawAxisLine(true);
//        xAxis.setEnabled(true);
        xAxis.setDrawLabels(false);
        xAxis.setGranularity(1);//设置最小间隔，防止当放大时，出现重复标签。

//        xAxis.setDrawLimitLinesBehindData(true);
        // 显示x轴标签
//        ValueFormatter formatter = new ValueFormatter() {
//
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return xLabels.get((int)value);//xLabels.get(Math.min(Math.max((int) value, 0), xLabels.size() - 1))
//            }
//
//        };
//        xAxis.setValueFormatter(formatter);
        xAxis.setTextSize(10);//设置标签字体大小
        xAxis.setTextColor(Color.LTGRAY);
        xAxis.setAxisLineColor(Color.parseColor("#4cffffff"));//4cffffff
        xAxis.setLabelCount(6);//设置标签显示的个数

        //y轴设置
        YAxis leftAxis = barChart.getAxisLeft();//获取左侧y轴
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);//设置y轴标签显示在外侧
        leftAxis.setAxisMinimum(0);//设置Y轴最小值
        leftAxis.setAxisMaximum(100);//设置Y轴最小值
        leftAxis.setDrawZeroLine(true);//显示第一条，x轴
        leftAxis.setDrawGridLines(true); //绘制网格线 false=不绘制
        leftAxis.setDrawLabels(false);//禁止绘制y轴标签
        leftAxis.setLabelCount(4, false);
        leftAxis.setDrawAxisLine(true);//禁止绘制y轴
        leftAxis.setEnabled(true);//显示x轴线
        leftAxis.setAxisLineColor(Color.parseColor("#4cffffff"));
        leftAxis.setTextColor(Color.LTGRAY);
//        float limitValue = 50;
//        leftAxis.addLimitLine(new LimitLine(limitValue,limitValue+" "));
//        leftAxis.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return String.valueOf(value);//((int) (value * Constant.INSTANCE.getM_FeaUiMaxSize())) + "%";
//            }
//        });

    }

    public void refreshLimitLine(int value){
        YAxis leftAxis = barChart.getAxisLeft();//获取左侧y轴
        leftAxis.removeAllLimitLines();
//        float b = (float)(Math.round(value*10))/10;
        leftAxis.addLimitLine(new LimitLine(value,value+" "));
        barChart.invalidate();
    }

    private int lineColors[] = {Color.YELLOW, Color.RED, Color.LTGRAY};
    private void initBarDataSet(String name, int color, final float fmtLimit){
        barDataSet = new BarDataSet(barEntries, name);
        barDataSet.setValueTextColor(color);
        barDataSet.setColors(lineColors);//设置多个color 当超过某值后 可改变颜色
//        barDataSet.setColor(color);
        barDataSets.add(barDataSet);
        barDataSet.setDrawValues(false);
//        barDataSet.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value) {
//                if (value >= fmtLimit){
//                    return "大动作";
//                }else {
//                    return "";
//                }
//            }
//        });

        //添加一个空的 BarData
        barData = new BarData();
        barData.setDrawValues(false);
        barData.setValueTextSize(10f);
        // 设置bar的宽度，但是点很多少的时候好像没作用，会拉得很宽
        barData.setBarWidth(0.5f);
//        barChart.moveViewToX(95);
        // 设置value值 颜色
        barData.setValueTextColor(Color.RED);
        barChart.setData(barData);
        barChart.invalidate();
    }

    public void addEntry(float dotValue){
        addEntry(dotValue, null);
    }
    public void addEntry(float dotValue, Object obj){
        if (barDataSet.getEntryCount() == 0) {
            barData.addDataSet(barDataSet);
        }
        barChart.setData(barData);
        //避免集合数据过多，及时清空（做这样的处理，并不知道有没有用，但还是这样做了）

        BarEntry entry = new BarEntry(barDataSet.getEntryCount(), dotValue, obj);
        barData.setHighlightEnabled(true);
        barData.addEntry(entry, 0);

        //通知数据已经改变
        barData.notifyDataChanged();
        barChart.notifyDataSetChanged();
        //设置在曲线图中显示的最大数量
        barChart.setVisibleXRangeMaximum(72);
        //移到某个位置
        barChart.moveViewToX(barData.getEntryCount());
    }

    /**
     * 设置描述信息
     *
     * @param str
     */
    public void setDescription(String str) {
        Description description = new Description();
        description.setText(str);
        description.setTextSize(10f);
        description.setPosition(AppTools.INSTANCE.getW_screen() - 180, 50);
        barChart.setDescription(description);
        barChart.invalidate();
    }
}