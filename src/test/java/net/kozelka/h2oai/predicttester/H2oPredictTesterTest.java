package net.kozelka.h2oai.predicttester;

import hex.genmodel.easy.exception.PredictException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class H2oPredictTesterTest {
    @Test
    public void testSomething() {
        Assert.assertEquals(2,1+1);
    }

    @Test
    public void gbmV100() throws IOException, PredictException {
        final H2oPredictTester pt = new H2oPredictTester(new File("data/names/models/gbm_v1.00_names.mojo.d"));
        pt.predictAll(new File("data/names/names.csv"), new File("target/data/names/models/gbm_v1.00_names.tsv"));
//        pt.predictAll(new File("data/names/names.csv"), null);
    }
}

