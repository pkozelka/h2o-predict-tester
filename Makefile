all: build

build:
	mvn clean verify

run:
	java -jar target/dist/h2o-predict-tester.jar data/names/models/gbm_v1.00_names.mojo.d data/names/names.csv
