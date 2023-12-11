export GRAALVM_HOME ~= "C:\\Users\\caiow\\apps\\graalvm-jdk-17.0.9+11.1"

products_run_perf_test:
	locust -f ./perf-test/products.py --users 100000 --spawn-rate 100 --run-time 10m -H http://localhost:8080

customers_run_perf_test:
	locust -f ./perf-test/customers.py --users 100000 --spawn-rate 100 --run-time 10m -H http://localhost:8080
