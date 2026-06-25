package com.kyoko.utils;

import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Div;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChartUtils {

    // 📊 Plot a Bar Chart (Fully Custom Scaling)
    public static void plotBarChart(Div container, double maxY, double minY, List<Double> values) {
        plotChart(container, "bar", maxY, minY, values, "Bar Values",null);
    }

    // 📈 Plot a Line Chart
    public static void plotLineChart(Div container, double maxY, double minY, List<Double> values) {
        plotChart(container, "line", maxY, minY, values, "Line Values",null);
    }

    // 🥧 Plot a Pie Chart (No min/max Y needed)
    public static void plotPieChart(Div container, List<Double> values) {
        plotChart(container, "pie", 0, 0, values, "Pie Distribution",null);
    }

    // 🔥 Core function for drawing charts (Custom Tick Scaling)
    public static void plotChart(Div container, String chartType, double maxY, double minY, List<Double> values, String label, ArrayList<String> items) {
        String containerId = container.getUuid(); // Get unique ZK component ID

        // ✅ Custom step size: Divide the range into 5 parts
        double stepSize = (maxY - minY) / 5;

        // Convert values to JavaScript Array Format
        String dataArray = values.toString();

        // Generate colors dynamically
        String backgroundColors = values.stream()
            .map(v -> "'rgba(" + ((v.intValue() * 50) % 255) + ", " + ((v.intValue() * 100) % 255) + ", 200, 0.6)'")
            .collect(Collectors.joining(", "));

        String borderColors = values.stream()
            .map(v -> "'rgba(" + ((v.intValue() * 50) % 255) + ", " + ((v.intValue() * 100) % 255) + ", 200, 1)'")
            .collect(Collectors.joining(", "));

        // JavaScript Code for Chart.js (Forces Custom Scaling)
        String script =
            "setTimeout(function() {" +
            "    var container = document.getElementById('" + containerId + "');" +
            "    container.innerHTML = '<canvas id=\"" + containerId + "_canvas\" width=\"600\" height=\"400\"></canvas>'; " +
            "    var ctx = document.getElementById('" + containerId + "_canvas').getContext('2d');" +
            "    if (window.myChart) { window.myChart.destroy(); window.myChart = null; }" +
            "    window.myChart = new Chart(ctx, {" +
            "        type: '" + chartType + "'," +
            "        data: {" +
            "            labels: " + generateLabels(values.size(),items) + "," +
            "            datasets: [{" +
            "                label: '" + label + "'," +
            "                data: " + dataArray + "," +
            "                backgroundColor: [" + backgroundColors + "]," +
            "                borderColor: [" + borderColors + "]," +
            "                borderWidth: 1" +
            "            }]" +
            "        }," +
            "        options: {" +
            "            responsive: true," +
            (chartType.equals("pie") ? "" :
            "            scales: { y: { " +
            "                suggestedMin: " + minY + "," +  // ✅ Use `suggestedMin` instead of `min`
            "                suggestedMax: " + maxY + "," +  // ✅ Use `suggestedMax` instead of `max`
            "                beginAtZero: false," + 
            "                ticks: { stepSize: " + stepSize + ", precision: 1 }" +  // ✅ Now stepSize will work!
            "            } }") + 
            "        }" +
            "    });" +
            "}, 50);"; 

        Clients.evalJavaScript(script);
    }

    // 🧹 Clear the Chart
    public static void clearChart(Div container) {
        String containerId = container.getUuid();
        String script =
            "setTimeout(function() {" +
            "    if (window.myChart) { window.myChart.destroy(); window.myChart = null; }" +
            "    var container = document.getElementById('" + containerId + "');" +
            "    container.innerHTML = '<canvas id=\"" + containerId + "_canvas\" width=\"600\" height=\"400\"></canvas>'; " +
            "}, 50);";
        Clients.evalJavaScript(script);
    }

    // 🏷️ Generate labels ["Item 1", "Item 2", ...]
    private static String generateLabels(int count,ArrayList<String> items) {
        StringBuilder labels = new StringBuilder("[");
        for (int i = 1; i <= count; i++) {
        	if(items != null && items.size() > (i-1)) {
        		labels.append("'").append(items.get(i-1)).append("'");
        	} else {
        		labels.append("'Item ").append(i).append("'");
        	}
            if (i < count) {
                labels.append(", ");
            }
        }
        labels.append("]");
        return labels.toString();
    }
}
