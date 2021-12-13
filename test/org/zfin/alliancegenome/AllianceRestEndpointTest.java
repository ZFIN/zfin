package org.zfin.alliancegenome;

import org.alliancegenome.curation_api.model.entities.DiseaseAnnotation;
import org.alliancegenome.curation_api.model.entities.ontology.DOTerm;
import org.alliancegenome.curation_api.response.ObjectResponse;
import org.zfin.properties.ZfinProperties;

public class AllianceRestEndpointTest {

    public static void main(String[] args) {
        ZfinProperties.init("home/WEB-INF/zfin.properties");
        // COnfigue to connect to different URL
        //AllianceRestManager.path = "https://alpha-curation.alliancegenome.org";
        DiseaseAnnotationRESTInterfaceAlliance api = AllianceRestManager.getDiseaseAnnotationEndpoints();
        //ObjectResponse<DiseaseAnnotation> annotation = api.getDiseaseAnnotation("4491701");
        ObjectResponse<DiseaseAnnotation> annotation = api.getDiseaseAnnotation("1516234");

        DOTerm term = new DOTerm();
        term.setCurie("DOID:4");
        DiseaseAnnotation entity = annotation.getEntity();
        entity.setObject(term);
        entity.setCurie("hjkjh");
        entity.setId(null);
        entity.setCurie("Wonnit");
        entity.setNegated(Boolean.TRUE);
        entity.setDiseaseRelation(DiseaseAnnotation.DiseaseRelation.is_marker_for);
        //ObjectResponse<DiseaseAnnotation> ann = api.updateDiseaseAnnotation(annotation.getEntity());
        ObjectResponse<DiseaseAnnotation> ann = api.addDiseaseAnnotation(entity);
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

