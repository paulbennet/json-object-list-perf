#!/bin/bash

# JSON Performance Benchmark Execution Script
# This script compiles the project, runs benchmarks, and generates HTML reports

set -e

echo "=========================================="
echo "JSON Performance Benchmark Runner"
echo "=========================================="
echo ""

# Step 1: Clean and compile
echo "[1/4] Compiling project..."
mvn clean package -DskipTests
echo "✓ Compilation complete"
echo ""

# Step 2: Run validation tests
echo "[2/4] Running validation tests..."
mvn test
echo "✓ Tests passed"
echo ""

# Step 3: Run benchmarks
echo "[3/4] Running JMH benchmarks..."
echo "This may take 15-30 minutes depending on dataset sizes..."
echo ""

# Create results directory if it doesn't exist
mkdir -p results

# Run benchmarks with GC profiling
java -jar target/benchmarks.jar \
    -rf json \
    -rff results/benchmark-results.json \
    -prof gc \
    -foe true

echo ""
echo "✓ Benchmarks complete"
echo ""

# Step 4: Generate HTML report
echo "[4/4] Generating HTML report..."
java -cp target/benchmarks.jar com.benchmark.perf.report.HtmlReportGenerator \
    results/benchmark-results.json \
    results/report.html

echo "✓ Report generated"
echo ""

echo "=========================================="
echo "Benchmark Complete!"
echo "=========================================="
echo ""
echo "Results saved to:"
echo "  - JSON: results/benchmark-results.json"
echo "  - HTML: results/report.html"
echo ""
echo "Opening report in browser..."

# Open report in default browser (works on macOS, Linux, Windows)
if command -v open &> /dev/null; then
    # macOS
    open results/report.html
elif command -v xdg-open &> /dev/null; then
    # Linux
    xdg-open results/report.html
elif command -v start &> /dev/null; then
    # Windows
    start results/report.html
else
    echo "Please open results/report.html manually in your browser"
fi

echo ""
echo "Done!"
