package org.zfin.alliancegenome;

import org.zfin.properties.ZfinPropertiesEnum;
import si.mazi.rescu.ClientConfig;
import si.mazi.rescu.RestProxyFactory;

public class AllianceRestManager {

    static String path = ZfinPropertiesEnum.ALLIANCE_CURATION_URL.value();

    private static ClientConfig config = new ClientConfig();
    static {
        config.setJacksonObjectMapperFactory(new JacksonObjectMapperFactoryZFIN());
    }

    public static DiseaseAnnotationRESTInterfaceAlliance getDiseaseAnnotationEndpoints() {
        return RestProxyFactory.createProxy(DiseaseAnnotationRESTInterfaceAlliance.class, path, config);
    }

    public static AgmRESTInterfaceAlliance getAgmEndpoints() {
        return RestProxyFactory.createProxy(AgmRESTInterfaceAlliance.class, path, config);
    }
}