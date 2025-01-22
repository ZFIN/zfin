package org.zfin.webservice;

import lombok.extern.java.Log;
import org.jdom.JDOMException;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;
import org.zfin.webservice.schema.*;

import jakarta.xml.bind.JAXBElement;
import java.util.List;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;


/**
 */
@Log
@Endpoint
public class MarkerEndpoint extends AbstractMarkerWebService {


    public static final String NAMESPACE_URI = "http://localhost/webservice/definitions";
    public static String WEBSERVICE_WSDL_URL = "/zfin.wsdl";
    public static final String GENE_REQUEST_LOCAL_NAME = "GeneRetrieveRequest";
    public static final String GENE_SEARCH_REQUEST_LOCAL_NAME = "GeneSearchRequest";
    public static final String GENE_ANATOMY_EXPRESSION_REQUEST_LOCAL_NAME = "GeneExpressionAnatomyWildTypeRequest";

    private ObjectFactory objectFactory = new ObjectFactory();

    private ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();


    public MarkerEndpoint() throws JDOMException {
    }


    @PayloadRoot(localPart = GENE_REQUEST_LOCAL_NAME, namespace = NAMESPACE_URI)
    @ResponsePayload
    public GeneRetrieveResponse getGene(@RequestPayload GeneRetrieveRequest geneRequestElement) throws Exception {
        logRequest("getGene", geneRequestElement.getGeneName());

        String geneZdbId = geneRequestElement.getGeneName();
        Marker returnGene = getGeneForValue(geneZdbId);

        Gene gene = objectFactory.createGene();
        gene = SchemaMapper.convertMarkerToGeneWebObject(gene, returnGene,
                (geneRequestElement.isExpressionAnatomyWildType() == null ? false : geneRequestElement.isExpressionAnatomyWildType())
        );

        GeneRetrieveResponse geneResponse = new GeneRetrieveResponse();
        geneResponse.setGene(gene);

        return geneResponse;
    }

    @PayloadRoot(localPart = GENE_ANATOMY_EXPRESSION_REQUEST_LOCAL_NAME, namespace = NAMESPACE_URI)
    @ResponsePayload
    public GeneExpressionAnatomyWildTypeResponse getGeneAnatomyExpression(@RequestPayload JAXBElement<String> geneRequestElement) throws Exception {
        logRequest("getGeneAnatomyExpression", geneRequestElement.getValue());

        String geneZdbId = geneRequestElement.getValue();
        Marker returnGene = getGeneForValue(geneZdbId);

        List<Anatomy> anatomyReturnList;
        GeneExpressionAnatomyWildTypeResponse anatomyExpressionRetrieveResponse = objectFactory.createGeneExpressionAnatomyWildTypeResponse();
        if (returnGene != null) {
            List<GenericTerm> anatomyItemList = expressionRepository.getWildTypeAnatomyExpressionForMarker(returnGene.getZdbID());
            anatomyReturnList = SchemaMapper.convertAnatomyListFromAnatomyItemList(anatomyItemList);
            anatomyExpressionRetrieveResponse.getAnatomy().addAll(anatomyReturnList);
        }

        return anatomyExpressionRetrieveResponse;
    }

    @PayloadRoot(localPart = GENE_SEARCH_REQUEST_LOCAL_NAME, namespace = NAMESPACE_URI)
    @ResponsePayload
    public GeneSearchResponse getGenesForName(@RequestPayload GeneSearchRequest geneRequestElement) throws Exception {
        logRequest("getGenesForName", geneRequestElement.getGeneName());

        String abbreviation = geneRequestElement.getGeneName();
        List<Marker> markers = RepositoryFactory.getMarkerRepository().getGenesByAbbreviation(abbreviation);

        GeneSearchResponse geneSearchResponse = objectFactory.createGeneSearchResponse();
        geneSearchResponse = SchemaMapper.convertMarkersToGeneWebObjects(geneSearchResponse, markers,
                (geneRequestElement.isExpressionAnatomyWildType() == null ? false : geneRequestElement.isExpressionAnatomyWildType())
        );

        return geneSearchResponse;
    }

    /**
     * Some logging to help figure out if these methods are safe to deprecate.
     * If these methods are never called, then it should be safe to remove them.
     * Once removed, we can take out our dependencies: spring-ws-*.jar, spring-xml*.jar, and jdom-1.1.jar
     */
    private void logRequest(String methodName, String message) {
        log.info("DEPRECATE THIS METHOD? " + methodName + ": " + message);

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            log.info("Request Attributes is null");
            return;
        }

        HttpServletRequest request = requestAttributes.getRequest();
        if (request != null) {
            log.info("Request URL: " + request.getRequestURL());
            log.info("Request URI: " + request.getRequestURI());
            log.info("More Request Info: " + request.getRemoteAddr() + " " + request.getRemoteHost() + " " + request.getRemotePort());
        } else {
            log.info("Request is null");
        }
    }
}
