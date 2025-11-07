package org.zfin.alliancegenome;

import org.zfin.alliancegenome.presentation.FishRESTInterfaceAlliance;
import org.zfin.alliancegenome.presentation.PriorityRESTInterface;
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

    public static PriorityRESTInterface getPriorityEndpoint() {
        return RestProxyFactory.createProxy(PriorityRESTInterface.class, "https://literature-rest.alliancegenome.org", config);
    }

    public static AgmRESTInterfaceAlliance getAgmEndpoints() {
        return RestProxyFactory.createProxy(AgmRESTInterfaceAlliance.class, path, config);
    }

    public static AlleleRESTInterfaceAlliance getAlleleEndpoints() {
        return RestProxyFactory.createProxy(AlleleRESTInterfaceAlliance.class, path, config);
    }

    public static GeneRESTInterfaceAlliance getGeneEndpoints() {
        return RestProxyFactory.createProxy(GeneRESTInterfaceAlliance.class, path, config);
    }

    public static FishRESTInterfaceAlliance getAGMEndpoints() {
        return RestProxyFactory.createProxy(FishRESTInterfaceAlliance.class, path, config);
    }

    public static FishRESTInterfaceAlliance getAGMPRodEndpoints() {
        // use the production site
        return RestProxyFactory.createProxy(FishRESTInterfaceAlliance.class, path.replace("alpha-", ""), config);
        //return RestProxyFactory.createProxy(FishRESTInterfaceAlliance.class, "=http://localhost:8080", config);
    }
}