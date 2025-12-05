#!/bin/bash

# Check if benchmark is running
if jps -l | grep -q "benchmarks.jar"; then
    echo "✓ Benchmark is running (PID: $(jps -l | grep benchmarks.jar | cut -d' ' -f1))"
    echo ""

    # Show last 15 lines of log
    echo "=== Last 15 lines of log ==="
    tail -15 results/benchmark-run.log
    echo ""

    # Count completed benchmarks
    completed=$(grep -c "Result.*:" results/benchmark-run.log 2>/dev/null || echo "0")
    echo "=== Progress ==="
    echo "Completed benchmark runs: $completed / 32"

    # Show current progress line
    progress=$(grep "Run progress:" results/benchmark-run.log | tail -1)
    if [ -n "$progress" ]; then
        echo "$progress"
    fi
else
    echo "✗ Benchmark is not running"
    echo ""
    echo "Check results/benchmark-run.log for details"

    # Check if results file exists
    if [ -f "results/benchmark-results.json" ]; then
        echo ""
        echo "✓ Results file exists - benchmark may have completed!"
        echo "Run: java -cp target/benchmarks.jar com.zoho.perf.report.HtmlReportGenerator"
    fi
fi
