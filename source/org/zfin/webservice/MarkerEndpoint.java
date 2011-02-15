package org.zfin.webservice;

import org.jdom.JDOMException;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;
import org.zfin.webservice.schema.*;

import javax.xml.bind.JAXBElement;
import java.util.List;

/**
 */
@Endpoint
public class MarkerEndpoint extends AbstractMarkerWebService{


    public static final String NAMESPACE_URI = "http://localhost/webservice/definitions";
    public static String WEBSERVICE_WSDL_URL = "/zfin.wsdl";
    public static final String GENE_REQUEST_LOCAL_NAME = "GeneRetrieveRequest";
    public static final String GENE_SEARCH_REQUEST_LOCAL_NAME = "GeneSearchRequest";
    public static final String GENE_ANATOMY_EXPRESSION_REQUEST_LOCAL_NAME = "GeneExpressionAnatomyWildTypeRequest";

    private ObjectFactory objectFactory = new ObjectFactory();

    private ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();


    public MarkerEndpoint() throws JDOMException{
    }


    @PayloadRoot( localPart = GENE_REQUEST_LOCAL_NAME, namespace = NAMESPACE_URI )
    @ResponsePayload
    public GeneRetrieveResponse getGene(@RequestPayload GeneRetrieveRequest geneRequestElement) throws Exception {

        String geneZdbId = geneRequestElement.getGeneName();
        Marker returnGene = getGeneForValue(geneZdbId);

        Gene gene = objectFactory.createGene() ;
        gene = SchemaMapper.convertMarkerToGeneWebObject(gene,returnGene,
                ( geneRequestElement.isExpressionAnatomyWildType() == null ? false : geneRequestElement.isExpressionAnatomyWildType())
        );

        GeneRetrieveResponse geneResponse = new GeneRetrieveResponse();
        geneResponse.setGene(gene);

        return geneResponse ;
    }

    @PayloadRoot( localPart = GENE_ANATOMY_EXPRESSION_REQUEST_LOCAL_NAME, namespace = NAMESPACE_URI )
    @ResponsePayload
    public GeneExpressionAnatomyWildTypeResponse getGeneAnatomyExpression(@RequestPayload JAXBElement<String> geneRequestElement) throws Exception {

        String geneZdbId = geneRequestElement.getValue();
        Marker returnGene = getGeneForValue(geneZdbId);

        List<Anatomy> anatomyReturnList ;
        GeneExpressionAnatomyWildTypeResponse anatomyExpressionRetrieveResponse = objectFactory.createGeneExpressionAnatomyWildTypeResponse() ;
        if(returnGene!=null){
            List<AnatomyItem> anatomyItemList = expressionRepository.getWildTypeAnatomyExpressionForMarker(returnGene.getZdbID());
            anatomyReturnList = SchemaMapper.convertAnatomyListFromAnatomyItemList(anatomyItemList);
            anatomyExpressionRetrieveResponse.getAnatomy().addAll(anatomyReturnList) ;
        }

        return anatomyExpressionRetrieveResponse ;
    }

    @PayloadRoot( localPart = GENE_SEARCH_REQUEST_LOCAL_NAME, namespace = NAMESPACE_URI )
    @ResponsePayload
    public GeneSearchResponse getGenesForName(@RequestPayload GeneSearchRequest geneRequestElement) throws Exception {

        String abbreviation = geneRequestElement.getGeneName();
        List<Marker> markers = RepositoryFactory.getMarkerRepository().getGenesByAbbreviation(abbreviation) ;

        GeneSearchResponse geneSearchResponse = objectFactory.createGeneSearchResponse();
        geneSearchResponse = SchemaMapper.convertMarkersToGeneWebObjects(geneSearchResponse,markers,
                        ( geneRequestElement.isExpressionAnatomyWildType() == null ? false : geneRequestElement.isExpressionAnatomyWildType())
        );

        return geneSearchResponse;
    }
}
