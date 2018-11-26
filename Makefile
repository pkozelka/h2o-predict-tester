all: build

build: $(shell find pom.xml src -type f)
	mvn clean verify

run: build
	# GBM
	java -jar target/dist/h2o-predict-tester.jar data/names/models/gbm_v1.00_names.mojo.d data/names/names.csv >target/names-gbm-v1.00.tsv
	java -jar target/dist/h2o-predict-tester.jar data/names/models/gbm_v1.10_names.mojo.d data/names/names.csv >target/names-gbm-v1.10.tsv
	java -jar target/dist/h2o-predict-tester.jar data/names/models/gbm_v1.20_names.mojo.d data/names/names.csv >target/names-gbm-v1.20.tsv
	java -jar target/dist/h2o-predict-tester.jar data/names/models/gbm_v1.30_names.mojo.d data/names/names.csv >target/names-gbm-v1.30.tsv
	# DRF
	java -jar target/dist/h2o-predict-tester.jar data/names/models/drf_v1.00_names.mojo.d data/names/names.csv >target/names-drf-v1.00.tsv
	java -jar target/dist/h2o-predict-tester.jar data/names/models/drf_v1.10_names.mojo.d data/names/names.csv >target/names-drf-v1.10.tsv
	java -jar target/dist/h2o-predict-tester.jar data/names/models/drf_v1.20_names.mojo.d data/names/names.csv >target/names-drf-v1.20.tsv
	java -jar target/dist/h2o-predict-tester.jar data/names/models/drf_v1.30_names.mojo.d data/names/names.csv >target/names-drf-v1.30.tsv

