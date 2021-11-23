package org.zfin.feature.repair;

import org.zfin.feature.Feature;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getFeatureRepository;

public class FeatureAttributionRepair extends AbstractScriptWrapper {

    private BufferedWriter outputFileWriter;
    private static final String DEFAULT_OUTPUT_PATH = "FeatureAttributionRepairReport.csv";

    public static void main(String[] args) throws IOException {
        FeatureAttributionRepair repair = new FeatureAttributionRepair();
        repair.generateReport();
        System.exit(0);
    }

    public void generateReport() {
        initAll();
        List<Feature> allFeatures = getFeatureRepository().getAllFeatures();
        Feature feature = allFeatures.get(0);
        outputReport(feature);
    }

    public void outputReport(Feature feature) {
        System.out.println(feature.getAbbreviation());
    }

}



