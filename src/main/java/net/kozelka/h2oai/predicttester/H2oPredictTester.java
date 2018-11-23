package net.kozelka.h2oai.predicttester;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

public class H2oPredictTester {
    /**
     * @param argsArray see README
     */
    public static void main(String... argsArray) throws IOException {
        final LinkedList<String> args = new LinkedList<>(Arrays.asList(argsArray));
        final File mojoFile = new File(args.removeFirst());
        if (!mojoFile.exists()) {
            throw new FileNotFoundException(mojoFile.getAbsolutePath());
        }
        // TODO load mojo
        final File csvDataFile = new File(args.removeFirst());
        if (!csvDataFile.exists()) {
            throw new FileNotFoundException(csvDataFile.getAbsolutePath());
        }

        final FileReader fileReader = new FileReader(csvDataFile);
        final CSVParser parser = new CSVParser(fileReader, CSVFormat.DEFAULT);
        for (CSVRecord record : parser) {
            // TODO call prediction from a mojo
            System.out.println("record = " + record);
        }

        System.out.printf("Hello from %s%n", H2oPredictTester.class.getName());
    }
}
