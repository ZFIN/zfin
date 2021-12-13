package org.zfin.alliancegenome;

import org.zfin.properties.ZfinPropertiesEnum;
import si.mazi.rescu.ClientConfig;
import si.mazi.rescu.RestProxyFactory;

public class AllianceRestManager {

    private static ClientConfig config = new ClientConfig();
    static final String path = ZfinPropertiesEnum.ALLIANCE_CURATION_URL.value();

    static {
        config.setJacksonObjectMapperFactory(new JacksonObjectMapperFactoryZFIN());
    }

    public static DiseaseAnnotationRESTInterfaceAlliance getDiseaseAnnotationEndpoints() {
        return RestProxyFactory.createProxy(DiseaseAnnotationRESTInterfaceAlliance.class, path, config);
    }
}