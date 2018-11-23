package net.kozelka.h2oai.predicttester;

import hex.genmodel.GenModel;
import hex.genmodel.algos.gbm.GbmMojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.exception.PredictUnknownCategoricalLevelException;
import hex.genmodel.easy.prediction.BinomialModelPrediction;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class H2oPredictTester {
    /**
     * @param argsArray see README
     */
    public static void main(String... argsArray) throws IOException, PredictException {
        final LinkedList<String> args = new LinkedList<>(Arrays.asList(argsArray));
        final File mojoFile = new File(args.removeFirst());
        if (!mojoFile.exists()) {
            throw new FileNotFoundException(mojoFile.getAbsolutePath());
        }

        final EasyPredictModelWrapper.Config config = new EasyPredictModelWrapper.Config();
        System.out.println("Loading mojo " + mojoFile.getAbsolutePath());
        final GenModel model = GbmMojoModel.load(mojoFile.getAbsolutePath());
        config.setModel(model);
        config.setConvertInvalidNumbersToNa(false);
        final EasyPredictModelWrapper easyModel = new EasyPredictModelWrapper(config);

        final File csvDataFile = new File(args.removeFirst());
        if (!csvDataFile.exists()) {
            throw new FileNotFoundException(csvDataFile.getAbsolutePath());
        }

        final CSVPrinter csvPrinter = new CSVPrinter(System.out, CSVFormat.TDF);

        final FileReader fileReader = new FileReader(csvDataFile);
        final CSVParser parser = new CSVParser(fileReader, CSVFormat.DEFAULT);
        final Map<String, Integer> nameIndexes = new HashMap<>();
        for (CSVRecord record : parser) {
            if (nameIndexes.isEmpty()) {
                // first line contains column names
                for (String name : record) {
                    nameIndexes.put(name, nameIndexes.size());
                }
                final List<String> outputNames = new ArrayList<>(nameIndexes.keySet());
                outputNames.add("predictedValue");
                outputNames.add("error");
                outputNames.add("p0");
                outputNames.add("p1");
                csvPrinter.printRecord(outputNames);
            } else {
                final List<String> outputRecord = new ArrayList<>();
                // set
                final RowData row = new RowData();
                for (Map.Entry<String, Integer> entry : nameIndexes.entrySet()) {
                    final String value = record.get(entry.getValue());
                    outputRecord.add(value);
                    row.put(entry.getKey(), value);
                }
                // predict
                String predictedValue = null;
                String error = null;
                String p0 = null;
                String p1 = null;
                try {
                    final BinomialModelPrediction pred = easyModel.predictBinomial(row);
                    predictedValue = pred.label;
                    p0 = String.format("%f", pred.classProbabilities[0]);
                    p1 = String.format("%f", pred.classProbabilities[1]);
/*
                    if (predictedValue.equals(testValue)) {
                        error = "MISMATCH";
                    }
*/
                } catch (PredictUnknownCategoricalLevelException e) {
                    error = String.format("%s: %s", e.getClass().getName(), e.getMessage());
                }
                outputRecord.add(predictedValue);
                outputRecord.add(error);
                outputRecord.add(p0);
                outputRecord.add(p1);
                csvPrinter.printRecord(outputRecord);
            }
        }
        csvPrinter.close();
    }
}
