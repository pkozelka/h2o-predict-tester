package net.kozelka.h2oai.predicttester;

import hex.genmodel.GenModel;
import hex.genmodel.algos.gbm.GbmMojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.prediction.BinomialModelPrediction;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
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
        final GenModel model = GbmMojoModel.load(mojoFile.getAbsolutePath());
        config.setModel(model);
        config.setConvertInvalidNumbersToNa(false);
        final EasyPredictModelWrapper easyModel = new EasyPredictModelWrapper(config);

        final File csvDataFile = new File(args.removeFirst());
        if (!csvDataFile.exists()) {
            throw new FileNotFoundException(csvDataFile.getAbsolutePath());
        }

        final FileReader fileReader = new FileReader(csvDataFile);
        final CSVParser parser = new CSVParser(fileReader, CSVFormat.DEFAULT);
        final Map<String, Integer> nameIndexes = new HashMap<>();
        for (CSVRecord record : parser) {
            if (nameIndexes.isEmpty()) {
                // first line contains column names
                for (String name : record) {
                    nameIndexes.put(name, nameIndexes.size());
                }
            } else {
                // set
                final RowData row = new RowData();
                int i = 0;
                for (String name : nameIndexes.keySet()) {
                    row.put(name, record.get(i++));
                }
                // predict
                final BinomialModelPrediction pred = easyModel.predictBinomial(row);
                System.out.printf("record = %s, predict = %s :: %s %n", record, pred.label, Arrays.asList(pred.classProbabilities));
            }
            // TODO call prediction from a mojo
        }

        System.out.printf("Hello from %s%n", H2oPredictTester.class.getName());
    }
}
