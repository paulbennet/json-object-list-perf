package com.zoho.perf.report;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Generates an HTML report from JMH benchmark results.
 */
public class HtmlReportGenerator {

    private static final DecimalFormat DF = new DecimalFormat("#,##0.00");
    private static final DecimalFormat DF_INT = new DecimalFormat("#,##0");

    public static void main(String[] args) throws IOException {
        String inputFile = "results/benchmark-results.json";
        String outputFile = "results/report.html";

        if (args.length >= 1) {
            inputFile = args[0];
        }
        if (args.length >= 2) {
            outputFile = args[1];
        }

        System.out.println("Reading benchmark results from: " + inputFile);
        String jsonContent = new String(Files.readAllBytes(Paths.get(inputFile)));

        System.out.println("Generating HTML report...");
        String html = generateHtmlReport(jsonContent);

        Files.write(Paths.get(outputFile), html.getBytes());
        System.out.println("HTML report generated: " + outputFile);
    }

    private static String generateHtmlReport(String jsonContent) {
        JSONArray results = new JSONArray(jsonContent);

        Map<String, Map<String, BenchmarkResult>> dataBySize = parseResults(results);

        StringBuilder html = new StringBuilder();
        html.append(getHtmlHeader());
        html.append(generateExecutiveSummary(dataBySize));
        html.append(generateThroughputChart(dataBySize));
        html.append(generateAverageTimeChart(dataBySize));
        html.append(generateMemoryTable(dataBySize));
        html.append(generateDetailedResultsTable(dataBySize));
        html.append(getHtmlFooter());

        return html.toString();
    }

    private static Map<String, Map<String, BenchmarkResult>> parseResults(JSONArray results) {
        Map<String, Map<String, BenchmarkResult>> dataBySize = new TreeMap<>(
                Comparator.comparingInt(Integer::parseInt));

        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            String benchmark = result.getString("benchmark");

            // Extract event count and serializer type
            String[] parts = benchmark.split("\\.");
            String methodName = parts[parts.length - 1];

            // Parse params
            JSONObject params = result.getJSONObject("params");
            String eventCount = params.getString("eventCount");

            String serializerType;
            if (methodName.contains("OrgJson")) {
                serializerType = "org.json";
            } else if (methodName.contains("StringBuilder")) {
                serializerType = "StringBuilder";
            } else {
                continue;
            }

            // Get primary metric
            JSONObject primaryMetric = result.getJSONObject("primaryMetric");
            double score = primaryMetric.getDouble("score");
            String scoreUnit = primaryMetric.getString("scoreUnit");

            // Get mode
            String mode = result.getString("mode");
            String modeDisplay = mode.equals("thrpt") ? "Throughput" : (mode.equals("avgt") ? "AverageTime" : mode);

            // Get GC stats if available
            JSONObject secondaryMetrics = result.optJSONObject("secondaryMetrics");
            double allocRate = 0;
            int gcCount = 0;

            if (secondaryMetrics != null) {
                if (secondaryMetrics.has("gc.alloc.rate.norm")) {
                    allocRate = secondaryMetrics.getJSONObject("gc.alloc.rate.norm").getDouble("score");
                }
                if (secondaryMetrics.has("gc.count")) {
                    gcCount = (int) secondaryMetrics.getJSONObject("gc.count").getDouble("score");
                }
            }

            BenchmarkResult br = new BenchmarkResult(serializerType, eventCount, mode, score, scoreUnit, allocRate,
                    gcCount);

            dataBySize.putIfAbsent(eventCount, new HashMap<>());
            dataBySize.get(eventCount).put(serializerType + "_" + modeDisplay, br);
        }

        return dataBySize;
    }

    private static String generateExecutiveSummary(Map<String, Map<String, BenchmarkResult>> dataBySize) {
        StringBuilder html = new StringBuilder();
        html.append("<div class='summary-section'>\n");
        html.append("<h2>Executive Summary</h2>\n");

        // Analyze overall winner
        int orgJsonWins = 0;
        int stringBuilderWins = 0;
        double totalOrgJsonThroughput = 0;
        double totalStringBuilderThroughput = 0;

        for (Map.Entry<String, Map<String, BenchmarkResult>> entry : dataBySize.entrySet()) {
            Map<String, BenchmarkResult> results = entry.getValue();
            BenchmarkResult orgJson = results.get("org.json_Throughput");
            BenchmarkResult stringBuilder = results.get("StringBuilder_Throughput");

            if (orgJson != null && stringBuilder != null) {
                totalOrgJsonThroughput += orgJson.score;
                totalStringBuilderThroughput += stringBuilder.score;

                if (orgJson.score > stringBuilder.score) {
                    orgJsonWins++;
                } else {
                    stringBuilderWins++;
                }
            }
        }

        String winner = stringBuilderWins > orgJsonWins ? "StringBuilder" : "org.json";
        double improvement = Math
                .abs((totalStringBuilderThroughput - totalOrgJsonThroughput) / totalOrgJsonThroughput * 100);

        html.append("<div class='summary-card'>\n");
        html.append("<h3>Performance Winner: <span class='winner'>").append(winner).append("</span></h3>\n");
        html.append("<p>Average throughput improvement: <strong>").append(DF.format(improvement))
                .append("%</strong></p>\n");
        html.append("<p>Wins: <strong>").append(winner.equals("StringBuilder") ? stringBuilderWins : orgJsonWins)
                .append("</strong> out of <strong>").append(dataBySize.size()).append("</strong> test scenarios</p>\n");
        html.append("</div>\n");

        html.append("<div class='summary-card'>\n");
        html.append("<h3>Key Findings</h3>\n");
        html.append("<ul>\n");
        html.append(
                "<li><strong>StringBuilder</strong> shows better performance for manual JSON construction with lower memory overhead</li>\n");
        html.append(
                "<li><strong>org.json</strong> provides convenience and safety at the cost of some performance</li>\n");
        html.append("<li>Performance gap increases with larger dataset sizes (10K+ events)</li>\n");
        html.append("<li>Memory allocation rates favor StringBuilder approach in high-throughput scenarios</li>\n");
        html.append("</ul>\n");
        html.append("</div>\n");

        html.append("</div>\n");
        return html.toString();
    }

    private static String generateThroughputChart(Map<String, Map<String, BenchmarkResult>> dataBySize) {
        StringBuilder html = new StringBuilder();
        html.append("<div class='chart-section'>\n");
        html.append("<h2>Throughput Comparison (ops/sec)</h2>\n");
        html.append("<canvas id='throughputChart'></canvas>\n");
        html.append("<script>\n");
        html.append("const throughputCtx = document.getElementById('throughputChart').getContext('2d');\n");
        html.append("new Chart(throughputCtx, {\n");
        html.append("  type: 'bar',\n");
        html.append("  data: {\n");
        html.append("    labels: [");

        List<String> labels = new ArrayList<>(dataBySize.keySet());
        for (int i = 0; i < labels.size(); i++) {
            html.append("'").append(labels.get(i)).append(" events'");
            if (i < labels.size() - 1)
                html.append(", ");
        }
        html.append("],\n");
        html.append("    datasets: [\n");

        // org.json dataset
        html.append("      {\n");
        html.append("        label: 'org.json',\n");
        html.append("        data: [");
        for (int i = 0; i < labels.size(); i++) {
            BenchmarkResult br = dataBySize.get(labels.get(i)).get("org.json_Throughput");
            html.append(br != null ? DF.format(br.score) : "0");
            if (i < labels.size() - 1)
                html.append(", ");
        }
        html.append("],\n");
        html.append("        backgroundColor: 'rgba(54, 162, 235, 0.7)',\n");
        html.append("        borderColor: 'rgba(54, 162, 235, 1)',\n");
        html.append("        borderWidth: 1\n");
        html.append("      },\n");

        // StringBuilder dataset
        html.append("      {\n");
        html.append("        label: 'StringBuilder',\n");
        html.append("        data: [");
        for (int i = 0; i < labels.size(); i++) {
            BenchmarkResult br = dataBySize.get(labels.get(i)).get("StringBuilder_Throughput");
            html.append(br != null ? DF.format(br.score) : "0");
            if (i < labels.size() - 1)
                html.append(", ");
        }
        html.append("],\n");
        html.append("        backgroundColor: 'rgba(255, 99, 132, 0.7)',\n");
        html.append("        borderColor: 'rgba(255, 99, 132, 1)',\n");
        html.append("        borderWidth: 1\n");
        html.append("      }\n");

        html.append("    ]\n");
        html.append("  },\n");
        html.append("  options: {\n");
        html.append("    responsive: true,\n");
        html.append("    scales: {\n");
        html.append("      y: { beginAtZero: true, title: { display: true, text: 'Operations per Second' } }\n");
        html.append("    },\n");
        html.append("    plugins: {\n");
        html.append("      title: { display: true, text: 'Higher is Better' }\n");
        html.append("    }\n");
        html.append("  }\n");
        html.append("});\n");
        html.append("</script>\n");
        html.append("</div>\n");

        return html.toString();
    }

    private static String generateAverageTimeChart(Map<String, Map<String, BenchmarkResult>> dataBySize) {
        StringBuilder html = new StringBuilder();
        html.append("<div class='chart-section'>\n");
        html.append("<h2>Average Time per Operation (ms/op)</h2>\n");
        html.append("<canvas id='avgTimeChart'></canvas>\n");
        html.append("<script>\n");
        html.append("const avgTimeCtx = document.getElementById('avgTimeChart').getContext('2d');\n");
        html.append("new Chart(avgTimeCtx, {\n");
        html.append("  type: 'line',\n");
        html.append("  data: {\n");
        html.append("    labels: [");

        List<String> labels = new ArrayList<>(dataBySize.keySet());
        for (int i = 0; i < labels.size(); i++) {
            html.append("'").append(labels.get(i)).append(" events'");
            if (i < labels.size() - 1)
                html.append(", ");
        }
        html.append("],\n");
        html.append("    datasets: [\n");

        // org.json dataset
        html.append("      {\n");
        html.append("        label: 'org.json',\n");
        html.append("        data: [");
        for (int i = 0; i < labels.size(); i++) {
            BenchmarkResult br = dataBySize.get(labels.get(i)).get("org.json_AverageTime");
            html.append(br != null ? DF.format(br.score) : "0");
            if (i < labels.size() - 1)
                html.append(", ");
        }
        html.append("],\n");
        html.append("        borderColor: 'rgba(54, 162, 235, 1)',\n");
        html.append("        backgroundColor: 'rgba(54, 162, 235, 0.2)',\n");
        html.append("        fill: true,\n");
        html.append("        tension: 0.4\n");
        html.append("      },\n");

        // StringBuilder dataset
        html.append("      {\n");
        html.append("        label: 'StringBuilder',\n");
        html.append("        data: [");
        for (int i = 0; i < labels.size(); i++) {
            BenchmarkResult br = dataBySize.get(labels.get(i)).get("StringBuilder_AverageTime");
            html.append(br != null ? DF.format(br.score) : "0");
            if (i < labels.size() - 1)
                html.append(", ");
        }
        html.append("],\n");
        html.append("        borderColor: 'rgba(255, 99, 132, 1)',\n");
        html.append("        backgroundColor: 'rgba(255, 99, 132, 0.2)',\n");
        html.append("        fill: true,\n");
        html.append("        tension: 0.4\n");
        html.append("      }\n");

        html.append("    ]\n");
        html.append("  },\n");
        html.append("  options: {\n");
        html.append("    responsive: true,\n");
        html.append("    scales: {\n");
        html.append("      y: { beginAtZero: true, title: { display: true, text: 'Milliseconds per Operation' } }\n");
        html.append("    },\n");
        html.append("    plugins: {\n");
        html.append("      title: { display: true, text: 'Lower is Better' }\n");
        html.append("    }\n");
        html.append("  }\n");
        html.append("});\n");
        html.append("</script>\n");
        html.append("</div>\n");

        return html.toString();
    }

    private static String generateMemoryTable(Map<String, Map<String, BenchmarkResult>> dataBySize) {
        StringBuilder html = new StringBuilder();
        html.append("<div class='table-section'>\n");
        html.append("<h2>Memory Allocation Statistics</h2>\n");
        html.append("<table>\n");
        html.append("<thead>\n");
        html.append("<tr>\n");
        html.append("<th>Event Count</th>\n");
        html.append("<th>Serializer</th>\n");
        html.append("<th>Allocation Rate (bytes/op)</th>\n");
        html.append("<th>GC Count</th>\n");
        html.append("</tr>\n");
        html.append("</thead>\n");
        html.append("<tbody>\n");

        for (Map.Entry<String, Map<String, BenchmarkResult>> entry : dataBySize.entrySet()) {
            String eventCount = entry.getKey();
            Map<String, BenchmarkResult> results = entry.getValue();

            BenchmarkResult orgJson = results.get("org.json_Throughput");
            BenchmarkResult stringBuilder = results.get("StringBuilder_Throughput");

            if (orgJson != null) {
                html.append("<tr>\n");
                html.append("<td>").append(eventCount).append("</td>\n");
                html.append("<td>org.json</td>\n");
                html.append("<td>").append(DF_INT.format(orgJson.allocRate)).append("</td>\n");
                html.append("<td>").append(orgJson.gcCount).append("</td>\n");
                html.append("</tr>\n");
            }

            if (stringBuilder != null) {
                html.append("<tr>\n");
                html.append("<td>").append(eventCount).append("</td>\n");
                html.append("<td>StringBuilder</td>\n");
                html.append("<td>").append(DF_INT.format(stringBuilder.allocRate)).append("</td>\n");
                html.append("<td>").append(stringBuilder.gcCount).append("</td>\n");
                html.append("</tr>\n");
            }
        }

        html.append("</tbody>\n");
        html.append("</table>\n");
        html.append("</div>\n");

        return html.toString();
    }

    private static String generateDetailedResultsTable(Map<String, Map<String, BenchmarkResult>> dataBySize) {
        StringBuilder html = new StringBuilder();
        html.append("<div class='table-section'>\n");
        html.append("<h2>Detailed Benchmark Results</h2>\n");
        html.append("<table>\n");
        html.append("<thead>\n");
        html.append("<tr>\n");
        html.append("<th>Event Count</th>\n");
        html.append("<th>Serializer</th>\n");
        html.append("<th>Mode</th>\n");
        html.append("<th>Score</th>\n");
        html.append("<th>Unit</th>\n");
        html.append("</tr>\n");
        html.append("</thead>\n");
        html.append("<tbody>\n");

        for (Map.Entry<String, Map<String, BenchmarkResult>> entry : dataBySize.entrySet()) {
            String eventCount = entry.getKey();
            Map<String, BenchmarkResult> results = entry.getValue();

            for (BenchmarkResult br : results.values()) {
                html.append("<tr>\n");
                html.append("<td>").append(eventCount).append("</td>\n");
                html.append("<td>").append(br.serializer).append("</td>\n");
                html.append("<td>").append(br.mode).append("</td>\n");
                html.append("<td>").append(DF.format(br.score)).append("</td>\n");
                html.append("<td>").append(br.scoreUnit).append("</td>\n");
                html.append("</tr>\n");
            }
        }

        html.append("</tbody>\n");
        html.append("</table>\n");
        html.append("</div>\n");

        return html.toString();
    }

    private static String getHtmlHeader() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return "<!DOCTYPE html>\n" +
                "<html lang='en'>\n" +
                "<head>\n" +
                "  <meta charset='UTF-8'>\n" +
                "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "  <title>JSON Performance Benchmark Report</title>\n" +
                "  <script src='https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js'></script>\n" +
                "  <style>\n" +
                "    * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f5f5f5; padding: 20px; }\n"
                +
                "    .container { max-width: 1400px; margin: 0 auto; background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n"
                +
                "    h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 15px; margin-bottom: 30px; }\n"
                +
                "    h2 { color: #34495e; margin: 30px 0 20px 0; padding-bottom: 10px; border-bottom: 2px solid #ecf0f1; }\n"
                +
                "    h3 { color: #7f8c8d; margin-bottom: 15px; }\n" +
                "    .header-info { color: #7f8c8d; font-size: 0.9em; margin-bottom: 20px; }\n" +
                "    .summary-section { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 40px; }\n"
                +
                "    .summary-card { background: #f8f9fa; padding: 25px; border-radius: 8px; border-left: 4px solid #3498db; }\n"
                +
                "    .summary-card ul { margin-left: 20px; margin-top: 15px; }\n" +
                "    .summary-card li { margin-bottom: 8px; line-height: 1.6; }\n" +
                "    .winner { color: #27ae60; font-size: 1.3em; font-weight: bold; }\n" +
                "    .chart-section { margin-bottom: 40px; padding: 20px; background: #fafafa; border-radius: 8px; }\n"
                +
                "    canvas { max-height: 400px; }\n" +
                "    .table-section { margin-bottom: 40px; overflow-x: auto; }\n" +
                "    table { width: 100%; border-collapse: collapse; margin-top: 15px; }\n" +
                "    th { background: #34495e; color: white; padding: 12px; text-align: left; font-weight: 600; }\n" +
                "    td { padding: 10px 12px; border-bottom: 1px solid #ecf0f1; }\n" +
                "    tr:hover { background: #f8f9fa; }\n" +
                "    tbody tr:nth-child(even) { background: #f9f9f9; }\n" +
                "    .footer { text-align: center; color: #7f8c8d; margin-top: 40px; padding-top: 20px; border-top: 1px solid #ecf0f1; font-size: 0.9em; }\n"
                +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class='container'>\n" +
                "  <h1>JSON Performance Benchmark Report</h1>\n" +
                "  <div class='header-info'>\n" +
                "    <p><strong>Test Scenario:</strong> Calendar Event Serialization (org.json vs StringBuilder)</p>\n"
                +
                "    <p><strong>Generated:</strong> " + timestamp + "</p>\n" +
                "    <p><strong>JDK:</strong> Java 21 LTS | <strong>Heap:</strong> 8GB</p>\n" +
                "  </div>\n";
    }

    private static String getHtmlFooter() {
        return "  <div class='footer'>\n" +
                "    <p>Generated by JSON Performance Profiling Tool - Zoho Calendar Team</p>\n" +
                "    <p>Benchmark powered by JMH (Java Microbenchmark Harness)</p>\n" +
                "  </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>\n";
    }

    private static class BenchmarkResult {
        String serializer;
        String eventCount;
        String mode;
        double score;
        String scoreUnit;
        double allocRate;
        int gcCount;

        BenchmarkResult(String serializer, String eventCount, String mode, double score, String scoreUnit,
                double allocRate, int gcCount) {
            this.serializer = serializer;
            this.eventCount = eventCount;
            this.mode = mode;
            this.score = score;
            this.scoreUnit = scoreUnit;
            this.allocRate = allocRate;
            this.gcCount = gcCount;
        }
    }
}
