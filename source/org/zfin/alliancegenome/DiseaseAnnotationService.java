package org.zfin.alliancegenome;

import org.zfin.mutant.DiseaseAnnotation;
import org.zfin.mutant.DiseaseAnnotationModel;
import si.mazi.rescu.ClientConfig;
import si.mazi.rescu.RestProxyFactory;

public class DiseaseAnnotationService {

    static ClientConfig config = new ClientConfig();

    static {
        config.setJacksonObjectMapperFactory(new JacksonObjectMapperFactoryZFIN());
    }

    //public static final String ALLIANCE_HOST = "https://alpha-curation.alliancegenome.org";
    public static final String ALLIANCE_HOST = "http://localhost:8080";

    public static void submitAnnotation(DiseaseAnnotationModel model) {
        DiseaseAnnotationRESTInterfaceAlliance api = RestProxyFactory.createProxy(DiseaseAnnotationRESTInterfaceAlliance.class, ALLIANCE_HOST, config);
        api.addDiseaseAnnotation(ZfinAllianceConverter.convertDiseaseAnnotation(model));
    }


}
