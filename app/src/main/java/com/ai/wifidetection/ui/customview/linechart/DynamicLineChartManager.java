package com.ai.wifidetection.ui.customview.linechart;

import android.graphics.Color;
import android.graphics.Point;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DynamicLineChartManager {
    private LineChart lineChart;
    private YAxis leftAxis;
    private YAxis rightAxis;
    private XAxis xAxis;
    private LineData lineData;
    private LineDataSet lineDataSet;
    private List<ILineDataSet> lineDataSets = new ArrayList<>();
    private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");//设置日期格式
    private Point displayRealSize = new Point();
//    private List<String> timeList = new ArrayList<>(); //存储x轴的时间

    //一条曲线
    public DynamicLineChartManager(LineChart mLineChart, String name, int color) {
        this.lineChart = mLineChart;

        leftAxis = lineChart.getAxisLeft();
        rightAxis = lineChart.getAxisRight();
        xAxis = lineChart.getXAxis();
        initLineChart();
        initLineDataSet(name, color);
    }
    //多条曲线
    public DynamicLineChartManager(LineChart mLineChart, List<String> names, List<Integer> colors) {
        this.lineChart = mLineChart;
        leftAxis = lineChart.getAxisLeft();
        rightAxis = lineChart.getAxisRight();
        xAxis = lineChart.getXAxis();
        initLineChart();
        initLineDataSet(names, colors);
    }

    /**
     * 初始化LineChar
     */
    private void initLineChart() {
        //是否展示网格线
        lineChart.setDrawGridBackground(false);
        //显示边界
        lineChart.setDrawBorders(false);
        //是否可以缩放
        lineChart.setScaleEnabled(false);
        //Enabling / disabling interaction
        lineChart.setTouchEnabled(false); // 设置是否可以触摸
        lineChart.setDragEnabled(false);// 是否可以拖拽
        lineChart.setDoubleTapToZoomEnabled(false);//设置是否可以通过双击屏幕放大图表。默认是true

        //折线图例 标签 设置
//        Legend legend = lineChart.getLegend();
//        legend.setForm(Legend.LegendForm.LINE);//正方形，圆形或线
//        legend.setFormLineWidth(10f);//设置Form的宽度
//        legend.setWordWrapEnabled(true);//是否支持自动换行 目前只支持BelowChartLeft, BelowChartRight, BelowChartCenter
//        legend.setTextSize(11f);
//        //显示位置
//        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
//        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
//        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
//        legend.setDrawInside(false);
        lineChart.getLegend().setEnabled(false);        //不显示图例

//        Description description = new Description();
//        description.setEnabled(true);
//        description.setText("Live Line");
        lineChart.setDescription(null);

        //BOTTOM 轴设置显示位置在底部 //设置x轴的显示位置
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

//        xAxis.setLabelCount(500);
        //xAxis.setTextSize(20f);//设置字体

        xAxis.enableGridDashedLine(10f, 10f, 0f);    //背景用虚线表格来绘制  给整成虚线
        xAxis.setLabelRotationAngle(30);//设置x轴标签的旋转角度
        xAxis.setDrawGridLines(false);//设置x轴上每个点对应的线
        xAxis.setDrawAxisLine(false);//是否绘制轴线
        xAxis.setDrawLabels(false);//绘制标签  指x轴上的对应数值
        xAxis.setAxisMinimum(60);
        xAxis.setTextColor(Color.GRAY);

//        xAxis.setGridLineWidth(10f);//设置竖线大小
//        xAxis.setGridColor(Color.RED);//设置竖线颜色
//        xAxis.setAxisLineColor(Color.GREEN);//设置x轴线颜色
//        xAxis.setAxisLineWidth(5f);//设置x轴线宽度
//        xAxis.setValueFormatter();//格式化x轴标签显示字符

//        xAxis.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getAxisLabel(float value, AxisBase axis) {
//                return timeList.get((int) value % timeList.size());
//            }
//        });

//        LimitLine line = new LimitLine(0);
//        line.setLineColor(Color.GRAY);
//        leftAxis.addLimitLine(line);
        //保证Y轴从0开始，不然会上移一点
//        leftAxis.setAxisMinimum(-3f);
//        leftAxis.setAxisMaximum(3f);
        rightAxis.setEnabled(false);//设置图表右边的y轴禁用
        leftAxis.setTextColor(Color.GRAY);
//        leftAxis.setDrawAxisLine(true);
        leftAxis.setDrawGridLinesBehindData(false);
        leftAxis.setDrawZeroLine(false);     //是否绘制0所在的网格线
        leftAxis.setZeroLineColor(Color.DKGRAY);
        leftAxis.setDrawLabels(true);
        leftAxis.setDrawAxisLine(false);
//        leftAxis.setDrawTopYLabelEntry(true);
        leftAxis.setZeroLineWidth(0.5f);
    }

    /**
     * 初始化折线(一条线)
     *
     * @param name
     * @param color
     */
    private void initLineDataSet(String name, int color) {
        lineDataSet = new LineDataSet(null, name);
        lineDataSet.setLineWidth(1.5f);//设置线宽
        lineDataSet.setDrawCircles(false);
        lineDataSet.setCircleRadius(3f);//设置焦点圆心的大小
        lineDataSet.setColor(color);
        lineDataSet.enableDashedHighlightLine(10f, 5f, 0f);//点击后的高亮线的显示样式
        lineDataSet.setCircleColor(color);
        lineDataSet.setHighLightColor(color);//设置点击交点后显示交高亮线的颜色

        //设置曲线填充
        lineDataSet.setDrawFilled(true);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setDrawValues(false);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);//曲线平滑
//        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        //添加一个空的 LineData
        lineData = new LineData();
        lineChart.setData(lineData);
        lineChart.invalidate();

    }

    /**
     * 初始化折线（多条线）
     *
     * @param names
     * @param colors
     */
    private void initLineDataSet(List<String> names, List<Integer> colors) {
        for (int i = 0; i < names.size(); i++) {
            lineDataSet = new LineDataSet(null, names.get(i));
            lineDataSet.setColor(colors.get(i));
            lineDataSet.setLineWidth(1.5f);
            lineDataSet.setColor(colors.get(i));

            lineDataSet.setDrawCircles(true);
            lineDataSet.setCircleRadius(3f);//设置焦点圆心的大小
            lineDataSet.setDrawFilled(false);
            lineDataSet.setCircleColor(colors.get(i));
            lineDataSet.setHighLightColor(colors.get(i));
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineDataSet.setValueTextSize(10f);
            lineDataSets.add(lineDataSet);

        } //添加一个空的 LineData
        lineData = new LineData();
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private int lineColors[] = {Color.YELLOW, Color.RED, Color.LTGRAY};

    public void addEntry(float number) { //最开始的时候才添加 lineDataSet（一个lineDataSet 代表一条线）
        addEntry(number, 0);
    }

    /**
     * 动态添加数据（一条折线图）
     *
     * @param number
     */
    public void addEntry(float number, int status) { //最开始的时候才添加 lineDataSet（一个lineDataSet 代表一条线）
        if (lineDataSet.getEntryCount() == 0) {
            lineData.addDataSet(lineDataSet);
        }
        lineChart.setData(lineData);
        //避免集合数据过多，及时清空（做这样的处理，并不知道有没有用，但还是这样做了）

        lineDataSet.setColor(lineColors[status % lineColors.length]);

        Entry entry = new Entry(lineDataSet.getEntryCount(), number);
        lineData.addEntry(entry, 0);

        //通知数据已经改变
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        //设置在曲线图中显示的最大数量
        lineChart.setVisibleXRangeMaximum(60);
        //移到某个位置
        lineChart.moveViewToX(lineData.getEntryCount());
    }

    /**
     * 动态添加数据（多条折线图）
     *
     * @param numbers
     */
    public void addEntry(List<Float> numbers) {
        if (lineDataSets.get(0).getEntryCount() == 0) {
            lineData = new LineData(lineDataSets);
            lineChart.setData(lineData);
        }

        for (int i = 0; i < numbers.size(); i++) {
            Entry entry = new Entry(lineDataSet.getEntryCount(), numbers.get(i));
            lineData.addEntry(entry, i);
            lineData.notifyDataChanged();
            lineChart.notifyDataSetChanged();
            lineChart.setVisibleXRangeMaximum(6);
            lineChart.moveViewToX(lineData.getEntryCount() - 5);
        }
    }

    /**
     * 设置Y轴值
     *
     * @param max
     * @param min
     * @param labelCount
     */
    public void setYAxis(float max, float min, int labelCount) {
        if (max < min) {
            return;
        }
        leftAxis.setAxisMaximum(max);
        leftAxis.setAxisMinimum(min);
        leftAxis.setLabelCount(labelCount, false);

        rightAxis.setAxisMaximum(max);
        rightAxis.setAxisMinimum(min);
        rightAxis.setLabelCount(labelCount, false);
        lineChart.invalidate();
    }

    /**
     * 设置高限制线
     *
     * @param high
     * @param name
     */
    public void setHightLimitLine(float high, String name, int color) {
        if (name == null) {
            name = "高限制线";
        }
        LimitLine hightLimit = new LimitLine(high, name);
        hightLimit.setLineWidth(4f);
        hightLimit.setTextSize(10f);
        hightLimit.setLineColor(color);
        hightLimit.setTextColor(color);
        leftAxis.addLimitLine(hightLimit);
        lineChart.invalidate();
    }

    /**
     * 设置低限制线
     *
     * @param low
     * @param name
     */
    public void setLowLimitLine(int low, String name) {
        if (name == null) {
            name = "低限制线";
        }
        LimitLine hightLimit = new LimitLine(low, name);
        hightLimit.setLineWidth(4f);
        hightLimit.setTextSize(10f);
        leftAxis.addLimitLine(hightLimit);
        lineChart.invalidate();
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
//        description.setPosition(100, -10);
        lineChart.setDescription(description);
        lineChart.invalidate();
    }
}