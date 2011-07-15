package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;

/**
 */
@Controller
@RequestMapping("/geo")
public class GeoWebServiceController {

    @Autowired
    private ExpressionService expressionService ;

    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

    private Logger logger = Logger.getLogger(GeoWebServiceController.class);

//    /action/marker/geo/check/ZDB-GENE-050320-101
    @RequestMapping(value="/check/{zdbID}", method= RequestMethod.GET )
    public @ResponseBody
    int checkGeoExists( @PathVariable("zdbID") String zdbID){
        Marker m = markerRepository.getMarkerByID(zdbID);
        if(m==null){
           logger.error("Failed to find marker for id: "+zdbID);
            return 0 ;
        }
        HibernateUtil.createTransaction();
        try {
            int numChanged = expressionService.updateGeoLinkForMarker(m);
            HibernateUtil.flushAndCommitCurrentSession();
            return numChanged ;
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            logger.error("Failed to update goe link",e);
            return 0 ;
        }
    }
}
