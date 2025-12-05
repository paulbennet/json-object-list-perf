#!/bin/bash

# Check if benchmark is running
if jps -l | grep -q "benchmarks.jar"; then
    PID=$(jps -l | grep benchmarks.jar | cut -d' ' -f1)
    echo "✓ Benchmark is running (PID: $PID)"
    echo ""

    # Show recent benchmark output from the process
    echo "=== Benchmark Status ==="
    echo "Use 'jps -l' to see running JVM processes"
    echo "Use 'kill $PID' to stop the benchmark if needed"
    echo ""

    # Check if results file is being updated
    if [ -f "results/benchmark-results.json" ]; then
        SIZE=$(wc -c < results/benchmark-results.json)
        echo "Results file size: $SIZE bytes"
        RESULTS_COUNT=$(grep -c '"benchmark"' results/benchmark-results.json 2>/dev/null || echo "0")
        echo "Completed benchmark entries: $RESULTS_COUNT"
    else
        echo "No results file yet - benchmark still initializing or in early stages"
    fi
    echo ""
    echo "💡 Tip: The benchmark runs in the background. Check the terminal where you started it for detailed progress."
else
    echo "✗ Benchmark is not running"
    echo ""

    # Check if results file exists
    if [ -f "results/benchmark-results.json" ]; then
        SIZE=$(wc -c < results/benchmark-results.json)
        RESULTS_COUNT=$(grep -c '"benchmark"' results/benchmark-results.json 2>/dev/null || echo "0")

        echo "✓ Results file exists!"
        echo "  - Size: $SIZE bytes"
        echo "  - Completed benchmarks: $RESULTS_COUNT"
        echo ""
        echo "To generate/regenerate the HTML report, run:"
        echo "  mvn exec:java -Dexec.mainClass=\"com.benchmark.perf.report.HtmlReportGenerator\" -Dexec.args=\"results/benchmark-results.json results/report.html\""
    else
        echo "No benchmark results found. Run './run-benchmark.sh' to start a benchmark."
    fi
fi
