package org.zfin.webservice;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;
import org.zfin.webservice.schema.Anatomy;
import org.zfin.webservice.schema.Gene;
import org.zfin.webservice.schema.GeneSearchResponse;

import java.util.List;

/**
 * Comes in via /webservice as defined by web.xml.
 */
@Controller
@RequestMapping("/gene")
public class MarkerWebServiceController extends AbstractMarkerWebService{

    private ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();

    @RequestMapping(value="id/{id}.xml", method=RequestMethod.GET )
    public @ResponseBody String getGeneByZdbIdAsXml(
            @PathVariable("id") String zdbID
            , @RequestParam(defaultValue = "false", required = false) boolean showExpressionAnatomyWildtype
    ){
        Marker marker = getGeneForValue(zdbID);
        return WebserviceXmlMarshaller.marshal(SchemaMapper.convertMarkerToGeneWebObject(new Gene(), marker, showExpressionAnatomyWildtype));
    }

    @RequestMapping(value={"id/{id}","id/{id}.json"}, method=RequestMethod.GET )
    public @ResponseBody Gene getGeneByZdbID(
            @PathVariable("id") String zdbID
            , @RequestParam(defaultValue = "false", required = false) boolean showExpressionAnatomyWildtype
    ){
        Marker marker = getGeneForValue(zdbID);
        return SchemaMapper.convertMarkerToGeneWebObject(new Gene(),marker,showExpressionAnatomyWildtype);
    }

    @RequestMapping(value="expression/anatomy/wildtype/{id}.xml", method=RequestMethod.GET )
    public @ResponseBody String getAnatomyExpressionForGeneAsXml(@PathVariable("id") String zdbID){
        Marker marker = getGeneForValue(zdbID);
        List<AnatomyItem> anatomyItemList = expressionRepository.getWildTypeAnatomyExpressionForMarker(marker.getZdbID());
        return WebserviceXmlMarshaller.marshal(SchemaMapper.convertAnatomyListFromAnatomyItemList(anatomyItemList));
    }

    @RequestMapping(value={"expression/anatomy/wildtype/{id}","geneAnatomyExpression/{id}.json"}, method=RequestMethod.GET )
    public @ResponseBody List<Anatomy> getAnatomyExpressionForGene(@PathVariable("id") String zdbID){
        Marker marker = getGeneForValue(zdbID);
        List<AnatomyItem> anatomyItemList = expressionRepository.getWildTypeAnatomyExpressionForMarker(marker.getZdbID());
        return SchemaMapper.convertAnatomyListFromAnatomyItemList(anatomyItemList);
    }

    @RequestMapping(value={"search/name/{name}.xml"}, method=RequestMethod.GET )
    public @ResponseBody String getGenesByNameAsXml(
            @PathVariable("name") String name
            , @RequestParam(defaultValue = "false", required = false) boolean showExpressionAnatomyWildtype
    ){
        List<Marker> markers = markerRepository.getGenesByAbbreviation(name) ;
        return WebserviceXmlMarshaller.marshal(SchemaMapper.convertMarkersToGeneWebObjects(new GeneSearchResponse(), markers, showExpressionAnatomyWildtype));
    }

    @RequestMapping(value={"search/name/{name}","search/{name}.json"}, method=RequestMethod.GET )
    public @ResponseBody
    List<Gene> getGenesByName(
            @PathVariable("name") String name
            ,@RequestParam(defaultValue = "false", required = false) boolean showExpressionAnatomyWildtype
    ){
        List<Marker> markers = markerRepository.getGenesByAbbreviation(name) ;
        return SchemaMapper.convertMarkersToGeneWebObjects(new GeneSearchResponse(),markers,showExpressionAnatomyWildtype).getGenes();
    }



}
