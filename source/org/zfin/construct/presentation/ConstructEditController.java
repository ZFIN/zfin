package org.zfin.construct.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.InvalidConstructNameException;
import org.zfin.construct.name.ConstructName;
import org.zfin.construct.repository.ConstructRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.sequence.repository.SequenceRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

import static org.zfin.construct.presentation.ConstructComponentService.createNewConstructFromSubmittedForm;
import static org.zfin.construct.presentation.ConstructComponentService.getExistingConstructName;


@Controller
@RequestMapping("/construct")
public class ConstructEditController {

    public record ConstructUpdateResult(String message, boolean success) {}

    @Autowired
    private MarkerRepository mr;
    @Autowired
    private PublicationRepository pr;
    @Autowired
    private InfrastructureRepository ir;
    @Autowired
    private ConstructRepository cr;
    @Autowired
    private SequenceRepository sr;

    @Autowired
    private ConstructEditService constructEditService;

    private static final Logger logger = LogManager.getLogger(ConstructEditController.class);


    @ModelAttribute("formBean")
    private ConstructUpdateBean getDefaultSearchForm(@RequestParam(value = "constructPublicationZdbID", required = false) String pubZdbID) {
        ConstructUpdateBean formBean = new ConstructUpdateBean();
        if (StringUtils.isNotEmpty(pubZdbID))
            formBean.setConstructPublicationZdbID(pubZdbID);
        List<ConstructCuration>constructsInPub = mr.getConstructsForAttribution(formBean.getConstructPublicationZdbID());
        formBean.setConstructsInPub(constructsInPub);
        return formBean;
    }

    @RequestMapping("/construct-update")
    protected String showSearchForm(Model model) throws Exception {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Construct Update");
        return "construct/construct-update";
    }

    private
    @Autowired
    HttpServletRequest request;
    @RequestMapping(value = "/construct-do-update/{constructID}", method = RequestMethod.GET)
    public
    @ResponseBody
    List<ConstructComponentPresentation> listComponents(@PathVariable String constructID,@ModelAttribute("formBean") ConstructUpdateBean constructBean) {
        constructBean.setConstructAlias(mr.getPreviousNamesLight(mr.getMarkerByID(constructID)));
        constructBean.setConstructDisplayName(mr.getConstructByID(constructID).getName());
        return mr.getConstructComponents(constructID);
    }

    @RequestMapping(value = "/construct-add-alias", method = RequestMethod.POST)
    public
    @ResponseBody

    void addConstructAlias(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String constructID=request.getParameter("constructEdit");
        String cAlias=request.getParameter("constructEditAlias");
        String pubid=request.getParameter("constructPublicationZdbID");
        HibernateUtil.createTransaction();
        Marker m=mr.getMarkerByID(constructID);

      mr.addMarkerAlias(m,cAlias,pr.getPublication(pubid));
        // ir.insertUpdatesTable(mr.getMarkerByID(constructID),"alias","added data alias");
        HibernateUtil.flushAndCommitCurrentSession();
    }
    @RequestMapping(value = "/construct-add-sequence", method = RequestMethod.POST)
    public
    @ResponseBody

    void addConstructSequence(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String constructID=request.getParameter("constructEdit");
        String constructSequence=request.getParameter("constructEditSequence");
        String pubid=request.getParameter("constructPublicationZdbID");
        HibernateUtil.createTransaction();
        Marker m=mr.getMarkerByID(constructID);
        constructEditService.addGenBankSequenceToConstruct(constructSequence, pubid, m);
        ir.insertUpdatesTable(mr.getMarkerByID(constructID),"sequence","added sequence");
        HibernateUtil.flushAndCommitCurrentSession();
    }


    @RequestMapping(value = "/update-comments/{constructID}/constructEditComments/{constructUpdateComments}"
            , method = RequestMethod.POST)
    public
    @ResponseBody
    void updateConstructComments(
            @PathVariable String constructID,
            @PathVariable String constructUpdateComments
    ) {
        HibernateUtil.createTransaction();
        Marker m=mr.getMarkerByID(constructID);
        if (!constructUpdateComments.equals("null")) {
            m.setPublicComments(constructUpdateComments);

            ConstructCuration c = cr.getConstructByID(constructID);
            c.setPublicComments(constructUpdateComments.replace("slash","/"));
            ir.insertUpdatesTable(m, "comments", "updated public notes");
            HibernateUtil.flushAndCommitCurrentSession();
        }
        else{
            m.setPublicComments("");

            ConstructCuration c = cr.getConstructByID(constructID);
            c.setPublicComments("");
            ir.insertUpdatesTable(m, "comments", "updated public notes");
            HibernateUtil.flushAndCommitCurrentSession();
        }
    }

    @RequestMapping(value = "/add-notes/{constructID}/notes/{notes}/publication/{pubID}"
            , method = RequestMethod.POST)
    public
    @ResponseBody
    void addCuratorNotes(
            @PathVariable String constructID,
            @PathVariable String notes,
            @PathVariable String pubID

    ) {
        HibernateUtil.createTransaction();
        mr.addMarkerDataNote(mr.getMarkerByID(constructID),notes.replace("slash","/"));
        ir.insertUpdatesTable(mr.getMarkerByID(constructID),"curator notes","added new curator note");
        HibernateUtil.currentSession().getTransaction().commit();
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


    @RequestMapping(value = "/delete-note/{constructID}/noteID/{noteID}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    void deleteNote(@PathVariable String constructID,@PathVariable String noteID) throws Exception{
        HibernateUtil.createTransaction();
        DataNote curatorNote=ir.getDataNoteByID(noteID);
        Marker m=mr.getMarkerByID(constructID);
        ir.insertUpdatesTable(m,"curator note","deleted curator note");
        mr.removeCuratorNote(m,curatorNote);
        HibernateUtil.flushAndCommitCurrentSession();
    }

    @RequestMapping(value = "/rename", method = RequestMethod.GET)
    public String renameConstructForm() {
        return "construct/construct-rename";
    }

    //must be logged in to use this
    //example request:
    // curl -X POST -k https://<SITE>.zfin.org/action/construct/rename/ZDB-TGCONSTRCT-161115-2 -H "Content-Type: application/json" -d '{"constructStoredName": "tdg.1#-#Hsa.TEST1#:EGFP#Cassette#,#tdg.2#-#Hsa.TEST2#:EGFP#", "pubZdbID": "ZDB-PUB-190507-21", "constructType": "Tg", "constructPrefix": ""}'
    @RequestMapping(value = "/rename/{constructID}", method = RequestMethod.POST)
    public
    @ResponseBody
    String renameConstruct(@PathVariable String constructID,
                           @RequestBody AddConstructFormFields request) throws Exception{

        String storedName = request.getConstructStoredName();
        String constructType = request.getConstructType();
        String constructPrefix = request.getConstructPrefix();
        String pubZdbID = request.getPubZdbID();

        //create a construct name object and set the cassettes from the stored name
        ConstructName constructName = new ConstructName(constructType, constructPrefix);
        constructName.setCassettesFromStoredName(storedName);

        HibernateUtil.createTransaction();
        Marker newMarker = ConstructComponentService.updateConstructName(constructID, constructName, pubZdbID);
        HibernateUtil.flushAndCommitCurrentSession();

        return """
                {
                    "message": "%s",
                    "success": true
                }
                """.formatted(newMarker.getZdbID() + " renamed to " + newMarker.getName());
    }

    //must be logged in to use this
    //example request:
    // curl -X POST -k https://<SITE>.zfin.org/action/construct/rename/ZDB-TGCONSTRCT-161115-2 -H "Content-Type: application/json" -d '{"constructStoredName": "tdg.1#-#Hsa.TEST1#:EGFP#Cassette#,#tdg.2#-#Hsa.TEST2#:EGFP#", "pubZdbID": "ZDB-PUB-190507-21", "constructType": "Tg", "constructPrefix": ""}'
    @RequestMapping(value = "/update/{constructID}", method = RequestMethod.POST)
    public
    @ResponseBody
    ConstructUpdateResult updateConstruct(@PathVariable String constructID,
                           @RequestBody EditConstructFormFields request) throws Exception{


        HibernateUtil.createTransaction();

        constructEditService.updateConstruct(constructID, request);
        Marker newMarker = mr.getMarkerByID(constructID);

        HibernateUtil.flushAndCommitCurrentSession();

        return new ConstructUpdateResult(newMarker.getZdbID() + " saved as " + newMarker.getName(), true);
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

            String message = String.format("%s saved as <a target=\"_blank\" href=\"%s\">%s</a>",
                    newMarker.getZdbID(), newMarker.getZdbID(), newMarker.getName());
            return new ConstructUpdateResult(message, true);

            //catch both types of exceptions
        } catch (Exception e) {
            try {
                HibernateUtil.rollbackTransaction();
            } catch (HibernateException he) {
                logger.error("Error during roll back of transaction", he);
            }
            logger.error("Error in Transaction", e);
            if (e instanceof InvalidConstructNameException) {
                return new ConstructUpdateResult(e.getMessage(), false);
            }
            return new ConstructUpdateResult("Construct could not be created", false);
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
