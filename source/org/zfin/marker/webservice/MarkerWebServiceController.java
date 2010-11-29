package org.zfin.marker.webservice;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.framework.webservice.WebserviceXmlMarshaller;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * Comes in via /webservice as defined by web.xml.
 */
@Controller
@RequestMapping("/gene")
public class MarkerWebServiceController {

    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

    @RequestMapping(value="/id/{id}.xml", method=RequestMethod.GET )
    public @ResponseBody String getMarkerByZdbIdAsXml(@PathVariable("id") String zdbID){
        return WebserviceXmlMarshaller.marshal(new Gene(markerRepository.getGeneByID(zdbID)));
    }

    @RequestMapping(value={"/id/{id}","id/{id}.json"}, method=RequestMethod.GET )
    public @ResponseBody Gene getMarkerByZdbID(@PathVariable("id") String zdbID){
        return new Gene(markerRepository.getGeneByID(zdbID)) ;
    }

    @RequestMapping(value={"/search/name/{name}.xml"}, method=RequestMethod.GET )
    public @ResponseBody String getGenesByNameAsXml(@PathVariable("name") String name){
        List<Marker> markers = markerRepository.getGenesByAbbreviation(name) ;
        return WebserviceXmlMarshaller.marshal(new GeneList(markers));
    }

    @RequestMapping(value={"/search/name/{name}","search/{name}.json"}, method=RequestMethod.GET )
    public @ResponseBody GeneList getMarkersByName(@PathVariable("name") String name){
        List<Marker> markers = markerRepository.getGenesByAbbreviation(name) ;
        return new GeneList(markers);
    }



}
