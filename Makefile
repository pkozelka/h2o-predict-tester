all: build

build:
	mvn clean verify

run:
	java -jar target/h2o-predict-tester.jar data/cars/models/drf_v1.00_cars.mojo.d data/cars/cars.csv
