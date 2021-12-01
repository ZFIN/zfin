package org.zfin.alliancegenome;

import org.alliancegenome.curation_api.model.entities.DiseaseAnnotation;
import org.alliancegenome.curation_api.response.ObjectResponse;
import si.mazi.rescu.ClientConfig;
import si.mazi.rescu.RestProxyFactory;

public class CallRestEndpoint {

    public static void main(String[] args) {
        final String path = "https://alpha-curation.alliancegenome.org/api";
        final String path1 = "https://alpha-curation.alliancegenome.org";
        ClientConfig config = new ClientConfig();
        config.setJacksonObjectMapperFactory(new JacksonObjectMapperFactoryZFIN());
        //DiseaseAnnotationRESTInterface api1 = RestProxyFactory.createProxy(DiseaseAnnotationRESTInterface.class, path, config);
        DiseaseAnnotationRESTInterfaceAlliance api = RestProxyFactory.createProxy(DiseaseAnnotationRESTInterfaceAlliance.class, path1, config);
        //ObjectResponse<DiseaseAnnotation> annotation = api.getDiseaseAnnotation("4491701");
        ObjectResponse<DiseaseAnnotation> annotation = api.getDiseaseAnnotation("1516234");
        //ObjectResponse<DiseaseAnnotation> annotation1 = api1.get(4491701L);
        //ObjectResponse<DiseaseAnnotation> annotation2 = api1.get("4491701");


/*
        ResteasyClient client = new ResteasyClientBuilder().register(resteasyJacksonProvider).build();
        //ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(UriBuilder.fromPath(path));
        DiseaseAnnotationRESTInterfaceAlliance proxy = target.proxy(DiseaseAnnotationRESTInterfaceAlliance.class);

        AllianceDiseaseAnnotation annotation = proxy.getDiseaseAnnotation(4491701);
*/
        System.out.println("HTTP code: ");
    }

}

