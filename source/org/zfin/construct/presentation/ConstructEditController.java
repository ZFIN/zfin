package org.zfin.construct.presentation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zfin.construct.InvalidConstructNameException;
import org.zfin.construct.name.ConstructName;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.repository.MarkerRepository;

import java.util.List;

import static org.zfin.construct.presentation.ConstructComponentService.createNewConstructFromSubmittedForm;
import static org.zfin.construct.presentation.ConstructComponentService.getExistingConstructName;


@Controller
@RequestMapping("/construct")
public class ConstructEditController {

    public record ConstructUpdateResult(String message, String zdbID, boolean success) {}

    @Autowired
    private MarkerRepository mr;
    @Autowired
    private InfrastructureRepository ir;

    @Autowired
    private ConstructEditService constructEditService;

    private static final Logger logger = LogManager.getLogger(ConstructEditController.class);

    @RequestMapping(value = "/construct-do-update/{constructID}", method = RequestMethod.GET)
    public
    @ResponseBody
    List<ConstructComponentPresentation> listComponents(@PathVariable String constructID) {
        return mr.getConstructComponents(constructID);
    }

    @RequestMapping(value = "/delete-alias/{constructID}/aliasID/{aliasID}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    void deleteAlias(@PathVariable String constructID,@PathVariable String aliasID) throws Exception{
        HibernateUtil.createTransaction();
        Marker m=mr.getMarkerByID(constructID);
        MarkerAlias constructAlias=mr.getMarkerAlias(aliasID);
        mr.deleteMarkerAlias(m,constructAlias);
       ir.insertUpdatesTable(m,"alias","deleted data alias");
        HibernateUtil.flushAndCommitCurrentSession();

    }

    @RequestMapping(value = "/delete-sequence/{constructID}/sequenceID/{sequenceID}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    void deleteSequence(@PathVariable String constructID,@PathVariable String sequenceID) throws Exception{
        HibernateUtil.createTransaction();
        constructEditService.removeSequenceFromConstruct(constructID, sequenceID);
        HibernateUtil.flushAndCommitCurrentSession();
    }


    @RequestMapping(value = {"/construct-add-sequence", "/construct-add-sequence/"}, method = RequestMethod.POST)
    public
    @ResponseBody
    void addConstructSequence(@RequestParam("constructEdit") String constructID,
                              @RequestParam("constructEditSequence") String constructSequence,
                              @RequestParam("constructPublicationZdbID") String pubid) throws Exception {
        HibernateUtil.createTransaction();
        Marker m = mr.getMarkerByID(constructID);
        constructEditService.addGenBankSequenceToConstruct(constructSequence, pubid, m);
        ir.insertUpdatesTable(mr.getMarkerByID(constructID), "sequence", "added sequence");
        HibernateUtil.flushAndCommitCurrentSession();
    }

    @RequestMapping(value = "/update/{constructID}", method = RequestMethod.POST)
    public
    @ResponseBody
    ConstructUpdateResult updateConstruct(@PathVariable String constructID,
                           @RequestBody EditConstructFormFields request) throws Exception{


        HibernateUtil.createTransaction();

        constructEditService.updateConstruct(constructID, request);
        Marker newMarker = mr.getMarkerByID(constructID);

        HibernateUtil.flushAndCommitCurrentSession();

        return new ConstructUpdateResult(newMarker.getZdbID() + " saved as " + newMarker.getName(), newMarker.getZdbID(), true);
    }

//    /action/construct/create-and-update
    @RequestMapping(value = "/create-and-update", method = RequestMethod.POST)
    public
    @ResponseBody
    ConstructUpdateResult createAndUpdate(@RequestBody EditConstructFormFields request) throws Exception{

        //set the construct name from the object
        ConstructName constructName = request.getConstructName();
        constructName.reinitialize();

        AddConstructFormFields createConstructValues = new AddConstructFormFields();
        createConstructValues.setConstructNameObject(constructName);
        createConstructValues.setConstructName(constructName.toString());
        createConstructValues.setPubZdbID(request.getPublicationZdbID());
        createConstructValues.setConstructType(constructName.getTypeAbbreviation());
        createConstructValues.setConstructPrefix(constructName.getPrefix());

        try {
            HibernateUtil.createTransaction();

            Marker newConstruct = createNewConstructFromSubmittedForm(createConstructValues);
            constructEditService.updateConstruct(newConstruct.getZdbID(), request);
            Marker newMarker = mr.getMarkerByID(newConstruct.getZdbID());

            HibernateUtil.flushAndCommitCurrentSession();

            String message = String.format("%s saved as <a target=\"_blank\" href=\"/%s\">%s</a>",
                    newMarker.getZdbID(), newMarker.getZdbID(), newMarker.getName());
            return new ConstructUpdateResult(message, newMarker.getZdbID(), true);

            //catch both types of exceptions
        } catch (Exception e) {
            try {
                HibernateUtil.rollbackTransaction();
            } catch (HibernateException he) {
                logger.error("Error during roll back of transaction", he);
            }
            logger.error("Error in Transaction", e);
            if (e instanceof InvalidConstructNameException) {
                return new ConstructUpdateResult(e.getMessage(),null, false);
            }
            return new ConstructUpdateResult("Construct could not be created", null, false);
        }
    }


    //Send the json representation of a construct name to the client
    @RequestMapping(value = "/json/{constructID}", method = RequestMethod.GET)
    public
    @ResponseBody
    ConstructName getConstructJson(@PathVariable String constructID) {
        ConstructName oldConstructName = getExistingConstructName(constructID);
        return oldConstructName;
    }
}
