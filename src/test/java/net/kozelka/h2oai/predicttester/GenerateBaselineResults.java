package net.kozelka.h2oai.predicttester;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;

@RunWith(Parameterized.class)
public class GenerateBaselineResults {
    private static final File DATASETS = new File("data");
    private static final File RESULT = new File("target/result");

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<? extends String> deepFiles() throws IOException {
        return FileUtils.getDirectoryNames(DATASETS, "*/models/*v?.??_*.mojo.d", null, false);
    }

    @Parameterized.Parameter
    public String modelPath;

    @Test
    public void generateBaseline() throws Exception {
        final String[] mpa = modelPath.split("/");
        final String datasetName = mpa[0];

        final File csvFile = new File(DATASETS, String.format("%1$s/%1$s.csv", datasetName));
        final File resultFile = new File(RESULT, String.format("%s/models/%s.tsv",
            datasetName,
            mpa[2].replace(".mojo.d", "")
        ));
        resultFile.getParentFile().mkdirs();

        final H2oPredictTester pt = new H2oPredictTester(new File(DATASETS, modelPath));
        final long start = System.currentTimeMillis();
        pt.predictAll(csvFile, resultFile);
        long duration = System.currentTimeMillis() - start;
        System.out.printf("%s: %d millis%n", modelPath, duration);
    }

}
