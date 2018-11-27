all: build

SRC=$(shell find pom.xml src -type f)

target/dist/h2o-predict-tester.jar: $(SRC)
	mvn clean verify

build: target/dist/h2o-predict-tester.jar

run: build
	java -jar target/dist/h2o-predict-tester.jar data/names/models/gbm_v1.00_names.mojo.d data/names/names.csv >target/sample.tsv

# This target should be executed in order to re-generate all the baseline results that serve for future comparison
gen-baseline: build
	mvn test -Dtest=GenerateBaselineResults
	cp -a target/result/* data/
