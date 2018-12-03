package net.kozelka.h2oai.predicttester;

import hex.genmodel.GenModel;
import hex.genmodel.algos.gbm.GbmMojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.prediction.BinomialModelPrediction;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class H2oPredictTesterTest {
    @Test
    public void gbmV100() throws IOException, PredictException {
        final H2oPredictTester pt = new H2oPredictTester(new File("data/names/models/gbm_v1.00_names.mojo.d"));
        pt.predictAll(new File("data/names/names.csv"), new File("target/gbm_v1.00_names.tsv"));
//        pt.predictAll(new File("data/names/names.csv"), null);
    }

    @Test
    public void name() throws PredictException, IOException {
        final File mojoFile = new File("data/names/models/gbm_v1.30_names.mojo.d");
        final EasyPredictModelWrapper.Config config = new EasyPredictModelWrapper.Config();
        config.setConvertInvalidNumbersToNa(false);
        final GenModel model = GbmMojoModel.load(mojoFile.getAbsolutePath());
        config.setModel(model);
        final EasyPredictModelWrapper easyModel = new EasyPredictModelWrapper(config);
        final RowData row = new RowData();
        row.put("name", "Mary");
        row.put("year", "1880");
        row.put("count", "7065");
        final BinomialModelPrediction pred = easyModel.predictBinomial(row);
        System.out.printf("mojo:%s%nPredicted sex: %s%nClass probabilities: %f, %f%n",
            mojoFile,
            pred.label,
            pred.classProbabilities[0],
            pred.classProbabilities[1]
            );
        Assert.assertEquals("F", pred.label);
        Assert.assertEquals(0.999985313680548, pred.classProbabilities[0], 1E-09);
        Assert.assertEquals(1.46863194523507E-05, pred.classProbabilities[1], 1E-09);
    }
}

