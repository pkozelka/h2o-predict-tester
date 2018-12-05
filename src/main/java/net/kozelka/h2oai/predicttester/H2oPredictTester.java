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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class H2oPredictTester {

    private final EasyPredictModelWrapper easyModel;
    private final int responseIdx;
    private final Map<String, Integer> modelIndexes = new LinkedHashMap<>();

    public H2oPredictTester(File mojoFile) throws IOException {
        fileMustExist(mojoFile);

        final EasyPredictModelWrapper.Config config = new EasyPredictModelWrapper.Config();
        System.err.println("Loading mojo " + mojoFile.getAbsolutePath());
        final GenModel model = GbmMojoModel.load(mojoFile.getAbsolutePath());
        config.setModel(model);
        System.err.printf("We are predicting field `%s` (index: %d)%n",
            model.getResponseName(),
            model.getResponseIdx()
        );
        int index = 0;
        for (String name : model.getNames()) {
            modelIndexes.put(name, index);
            index++;
        }
        responseIdx = model.getResponseIdx();
        config.setConvertInvalidNumbersToNa(false);
        easyModel = new EasyPredictModelWrapper(config);
    }

    private void predictAll(CSVParser parser, CSVPrinter csvPrinter) throws IOException, PredictException {
        final int[] modelToCsvIndex = new int[modelIndexes.size()];
        int cnt = 0;
        for (CSVRecord record : parser) {
            if (cnt == 0) {
                // first line contains column names
                int csvIndex = 0;
                for (String name: record) {
                    // translate model index into csv index
                    final int modelIndex = modelIndexes.get(name);
                    modelToCsvIndex[modelIndex] = csvIndex;
                    csvIndex ++;
                }
                final List<String> outputNames = new ArrayList<>(modelIndexes.keySet());
                outputNames.add("predict");
                outputNames.add("error");
                outputNames.add("p0");
                outputNames.add("p1");
                csvPrinter.printRecord(outputNames);
            } else {
                final List<String> outputRecord = new ArrayList<>();
                String expectedValue = null;
                // each record begins with columns from model, in model order
                final RowData row = new RowData();
                for (Map.Entry<String, Integer> entry : modelIndexes.entrySet()) {
                    final int modelIndex = entry.getValue();
                    final int csvIndex = modelToCsvIndex[modelIndex];
                    final String value = record.get(csvIndex);
                    if (modelIndex == responseIdx) {
                        expectedValue = value;
                    }
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
                    if (predictedValue == null) {
                        error = "MISSING PREDICTION";
                    } else if (!predictedValue.equals(expectedValue)) {
                        error = "MISMATCH";
                    }
                } catch (PredictUnknownCategoricalLevelException e) {
                    error = e.getMessage();
                }
                outputRecord.add(predictedValue);
                outputRecord.add(error);
                outputRecord.add(p0);
                outputRecord.add(p1);
                csvPrinter.printRecord(outputRecord);
            }
            //
            cnt ++;
        }
    }

    void predictAll(File csvDataFile, File resultFile) throws IOException, PredictException {
        fileMustExist(csvDataFile);

        final PrintStream ps = resultFile == null ? System.out : new PrintStream(resultFile);

        try (final FileReader fileReader = new FileReader(csvDataFile)) {
            final CSVParser parser = new CSVParser(fileReader, CSVFormat.DEFAULT);
            final CSVFormat tdf = CSVFormat.TDF.withRecordSeparator('\n');
            final CSVPrinter csvPrinter = new CSVPrinter(ps, tdf);
            predictAll(parser, csvPrinter);
        }
    }

    private static void fileMustExist(File file) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
    }

    /**
     * @param argsArray see README
     */
    public static void main(String... argsArray) throws IOException, PredictException {
        final LinkedList<String> args = new LinkedList<>(Arrays.asList(argsArray));
        final File mojoFile = new File(args.removeFirst());
        final H2oPredictTester pt = new H2oPredictTester(mojoFile);

        final File csvDataFile = new File(args.removeFirst());
        pt.predictAll(csvDataFile, null);

    }
}
