package com.ai.wifidetection.ui.customview.combinedchart;

import android.graphics.Color;
import android.util.Log;

import com.ai.wifidetection.beans.ChartDataInfo;
import com.ai.wifidetection.utils.DateTimeUtils;
import com.ai.wifidetection.utils.LogUtil;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class CombinedChartManager {
    private CombinedChart mCombinedChart;
    private YAxis leftAxis;
    private YAxis rightAxis;
    private XAxis xAxis;

    private List<Float> barChartYs = new ArrayList<>();
    private List<List<Float>> lineChartYs = new ArrayList<>();

    //Bar Data init
    private BarData barData;
    private BarDataSet barDataSet;
    private List<IBarDataSet> barDataSets = new ArrayList<>();
    List<BarEntry> barEntries = new ArrayList<>();

    //Lines Data init
    private LineData lineData;
    private LineDataSet lineDataSet;
    private List<ILineDataSet> lineDataSets = new ArrayList<>();

    //combined
    CombinedData combinedData = new CombinedData();

    public CombinedChartManager(CombinedChart combinedChart,
                                String barName, List<String> lineNames, Integer barColor, List<Integer> lineColors) {
        this.mCombinedChart = combinedChart;

        leftAxis = mCombinedChart.getAxisLeft();
        rightAxis = mCombinedChart.getAxisRight();
        xAxis = mCombinedChart.getXAxis();

        initCombinedChart();
        initBarDataSet(barName, barColor);
        initLinesDataSet(lineNames, lineColors);
    }

    private void initBarDataSet(String barName, Integer barColor) {
        List<BarEntry> yVals = new ArrayList<>();
        barDataSet = new BarDataSet(yVals, barName);
        barDataSet.setColor(barColor);
        barDataSet.setValueTextSize(10f);
        barDataSet.setValueTextColor(barColor);
        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        //添加一个空的 BarData
        barData = new BarData();
        barData.setValueTextSize(10f);
        barData.setDrawValues(false);
        // 设置bar的宽度，但是点很多少的时候好像没作用，会拉得很宽
        barData.setBarWidth(2f);
        barData.addDataSet(barDataSet);
    }

    private void initLinesDataSet(List<String> lineNames, List<Integer> lineColors) {
        for (int i = 0; i < lineNames.size(); i++) {
            List<Entry> yVals = new ArrayList<>();
            lineDataSet = new LineDataSet(yVals, lineNames.get(i));
            lineDataSet.setColor(lineColors.get(i));
            lineDataSet.setCircleColor(lineColors.get(i));
            lineDataSet.setValueTextColor(lineColors.get(i));

            lineDataSet.setCircleSize(1);
            lineDataSet.setDrawValues(true);
            lineDataSet.setValueTextSize(10f);
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineData = new LineData();
            lineData.addDataSet(lineDataSet);
        }
    }

    /**
     * 初始化Chart
     */
    public void initCombinedChart() {
        //不显示描述内容
        mCombinedChart.getDescription().setEnabled(false);

        mCombinedChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR,
                CombinedChart.DrawOrder.LINE
        });

        mCombinedChart.setBackgroundColor(Color.WHITE);
        mCombinedChart.setDrawGridBackground(false);
        mCombinedChart.setDrawBarShadow(false);
        mCombinedChart.setHighlightFullBarEnabled(false);
        //显示边界
        mCombinedChart.setDrawBorders(false);
        //图例说明
        Legend legend = mCombinedChart.getLegend();
        legend.setWordWrapEnabled(true);

        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(true);
        //Y轴设置
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(10f);
        rightAxis.setDrawLabels(false);
        rightAxis.setEnabled(false);

        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setDrawLabels(true);
        leftAxis.setDrawZeroLine(true);

        mCombinedChart.animateY(500); // 立即执行的动画,x轴

        //设置X轴在底部
        XAxis xAxis = mCombinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGranularity(0.5f);   //设置最小间隔，防止当放大时，出现重复标签
        xAxis.setLabelCount(10, true);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(true);
        xAxis.setDrawGridLines(false);
    }

    /**
     * 设置X轴坐标值
     *
     * @param xAxisValues x轴坐标集合
     */
    public void setXAxis(final List<String> xAxisValues) {
        //设置X轴在底部
        XAxis xAxis = mCombinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGranularity(1f);

        xAxis.setLabelCount(10, false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
//                return xAxisValues.get((int) value % xAxisValues.size());
                return DateTimeUtils.Companion.getTime((long) (value * 5 * 60 * 1000L - 8 * 3600 * 1000), DateTimeUtils.Companion.getDATE_FORMAT_TIME());
            }
        });
        mCombinedChart.invalidate();
    }

    public void addBarEntry(float barValue){
        if (barDataSet.getEntryCount() == 0) {
            barData.addDataSet(barDataSet);
        }
        //避免集合数据过多，及时清空（做这样的处理，并不知道有没有用，但还是这样做了）
        BarEntry entry = new BarEntry(barDataSet.getEntryCount(), barValue);
        barData.setHighlightEnabled(true);
        barData.addEntry(entry, 0);

        //通知数据已经改变
        barData.notifyDataChanged();
        combinedData.setData(barData);
//        mCombinedChart.setVisibleXRangeMaximum(20);
//        mCombinedChart.moveViewToX(barChartYs.size() - 5);
    }

    public void addLineEntries(List<Float> lineValues) {

        for (int i = 0; i < lineChartYs.size(); i++) {
            ArrayList<Entry> yValues = new ArrayList<>();
            for (int j = 0; j < lineChartYs.get(i).size(); j++) {
                yValues.add(new Entry(j, lineChartYs.get(i).get(j)));
            }

            lineDataSet.setCircleSize(1);
            lineDataSet.setDrawValues(true);
            lineDataSet.setValueTextSize(10f);
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineData.addDataSet(lineDataSet);
            lineData.notifyDataChanged();
            combinedData.setData(lineData);
            mCombinedChart.setVisibleXRangeMaximum(20);
            mCombinedChart.moveViewToX(barChartYs.size() - 5);
        }
    }

    /**
     * 得到折线图(多条)
     *
     * @param lineChartYs 折线Y轴值
     * @param lineNames   折线图名字
     * @param lineColors  折线颜色
     * @return
     */
    private LineData getLineData(List<List<Float>> lineChartYs, List<String> lineNames, List<Integer> lineColors) {
        LineData lineData = new LineData();

        for (int i = 0; i < lineChartYs.size(); i++) {
            ArrayList<Entry> yValues = new ArrayList<>();
            for (int j = 0; j < lineChartYs.get(i).size(); j++) {
                yValues.add(new Entry(j, lineChartYs.get(i).get(j)));
            }
            LineDataSet dataSet = new LineDataSet(yValues, lineNames.get(i));
            dataSet.setColor(lineColors.get(i));
            dataSet.setCircleColor(lineColors.get(i));
            dataSet.setValueTextColor(lineColors.get(i));

            dataSet.setCircleSize(1);
            dataSet.setDrawValues(true);
            dataSet.setValueTextSize(10f);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineData.addDataSet(dataSet);
        }
        return lineData;
    }

    /**
     * 得到柱状图 一条柱状图
     *
     * @param barChartY Y轴值
     * @param barName   柱状图名字
     * @param barColor  柱状图颜色
     * @return
     */

    private BarData getBarData(List<Float> barChartY, String barName, int barColor) {
        BarData barData = new BarData();
        ArrayList<BarEntry> yValues = new ArrayList<>();
        for (int i = 0; i < barChartY.size(); i++) {
            yValues.add(new BarEntry(i, barChartY.get(i)));
        }

        BarDataSet barDataSet = new BarDataSet(yValues, barName);
        barDataSet.setColor(barColor);
        barDataSet.setValueTextSize(10f);
        barDataSet.setDrawValues(false);
        barDataSet.setBarShadowColor(Color.rgb(240,240,240));
        barDataSet.setValueTextColor(barColor);
        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        barData.addDataSet(barDataSet);

        //以下是为了解决 柱状图 左右两边只显示了一半的问题 根据实际情况 而定
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum((float) (barChartY.size() - 0.5));
        return barData;
    }

    public void showCombinedChart(ChartDataInfo mChartDataInfo){
        showCombinedChart(
                mChartDataInfo.getXAxisValues(),
                mChartDataInfo.getBarChartYs(),
                mChartDataInfo.getLineChartYs(),
                mChartDataInfo.getBarName(),
                mChartDataInfo.getLineNames(),
                mChartDataInfo.getBarColor(),
                mChartDataInfo.getLineColors());
    }

    /**
     * 显示混合图(柱状图+折线图)
     *
     * @param xAxisValues X轴坐标
     * @param barChartYs  柱状图Y轴值
     * @param lineChartYs 折线图Y轴值
     * @param barName     柱状图名字
     * @param lineNames   折线图名字
     * @param barColor    柱状图颜色
     * @param lineColors  折线图颜色
     */

    public void showCombinedChart(
            List<String> xAxisValues, List<Float> barChartYs, List<List<Float>> lineChartYs,
            String barName, List<String> lineNames, Integer barColor, List<Integer> lineColors) {

        this.barChartYs.addAll(barChartYs);
//        this.lineChartYs = lineChartYs;
//        if (xAxisValues.size() > 0){
//            setXAxis(xAxisValues);
//        }
//        Log.d(TAG, "showCombinedChart: " + lineChartYs.get(0).size());

//        combinedData.setData(getLineData(this.lineChartYs, lineNames, lineColors));
        combinedData.setData(getBarData(this.barChartYs, barName, barColor));

        mCombinedChart.setData(combinedData);
        mCombinedChart.setVisibleXRangeMaximum(72);
        mCombinedChart.moveViewToX(barChartYs.size() - 5);
        combinedData.notifyDataChanged();
        mCombinedChart.invalidate();
    }

    public void notifyUi(){
        combinedData.notifyDataChanged();
        mCombinedChart.invalidate();
    }
}
