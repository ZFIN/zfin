package org.zfin.construct.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.construct.ConstructCuration;
import org.zfin.database.InformixUtil;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.profile.Person;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.repository.SequenceRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.zfin.framework.HibernateUtil.currentSession;

@Controller
@RequestMapping("/construct")
public class ConstructEditController {

    private static Logger LOG = Logger.getLogger(ConstructEditController.class);

    @Autowired
    private static MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private static PublicationRepository pr = RepositoryFactory.getPublicationRepository();
    private static InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
    private static SequenceRepository sr = RepositoryFactory.getSequenceRepository();
    private static ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();


    private static Logger logger = Logger.getLogger(ConstructEditController.class);

    private ConstructAddValidator validator = new ConstructAddValidator();

    private Person currentUser = ProfileService.getCurrentSecurityUser();




    @ModelAttribute("formBean")

    private ConstructUpdateBean getDefaultSearchForm(@RequestParam(value = "constructPublicationZdbID", required = false) String pubZdbID) {
        ConstructUpdateBean formBean = new ConstructUpdateBean();
        if (StringUtils.isNotEmpty(pubZdbID))
            formBean.setConstructPublicationZdbID(pubZdbID);
        List<ConstructCuration>constructsInPub=mr.getConstructsForAttribution(formBean.getConstructPublicationZdbID());
        formBean.setConstructsInPub(constructsInPub);
        return formBean;
    }

    @RequestMapping("/construct-update")
    protected String showSearchForm(Model model) throws Exception {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Construct Update");
        return "construct/construct-update.page";
    }

    private
    @Autowired
    HttpServletRequest request;

   /* @RequestMapping(value = "/construct-do-update", method = RequestMethod.GET)

    public String doSearch(Model model, @RequestParam("constructID") String zdbID,
                           @Valid @ModelAttribute("formBean") ConstructUpdateBean constructBean,
                           BindingResult result
    ) throws Exception {

        Marker ct=mr.getMarkerByID(zdbID);

        List<ConstructComponent> cc=mr.getConstructComponent(ct.getZdbID());
        constructBean.setConstructDisplayName(ct.name);

        constructBean.setConstructSynonym(ct.getPublicComments());
        constructBean.setConstructComponent(cc);
            return "construct/construct-update.page";



}*/


    @RequestMapping(value = "/construct-do-update/{constructID}", method = RequestMethod.GET)
    public
    @ResponseBody
    List<ConstructComponentPresentation> listComponents(@PathVariable String constructID,@ModelAttribute("formBean") ConstructUpdateBean constructBean) {
            // constructBean.setConstructAlias(mr.getPreviousNamesLight(mr.getMarkerByID(constructID)));
        constructBean.setConstructDisplayName(mr.getConstructByID(constructID).getName());
            return mr.getConstructComponents(constructID);
       // return mr.getConstructComponentsForDisplay(constructID);

    }



    @RequestMapping(value = "/add-alias/{constructID}/alias/{alias}/publication/{pubID}"
            , method = RequestMethod.POST)
    public
    @ResponseBody
    boolean addConstructAlias(
            @PathVariable String constructID,
            @PathVariable String alias,
            @PathVariable String pubID

    ) {
        //Person currentUser = ProfileService.getCurrentSecurityUser();
        MarkerAlias newConstructAlias;
        HibernateUtil.createTransaction();
        newConstructAlias= mr.addMarkerAlias(mr.getMarkerByID(constructID),alias,pr.getPublication(pubID));
        ir.insertUpdatesTable(mr.getMarkerByID(constructID),"alias","added data alias");
        HibernateUtil.currentSession().getTransaction().commit();
        if (newConstructAlias!=null) {
            return true;
        }
        return true;

    }
    @RequestMapping(value = "/update-comments/{constructID}/constructComments/{constructComments}"
            , method = RequestMethod.POST)
    public
    @ResponseBody
    boolean updateConstructComments(
            @PathVariable String constructID,
            @PathVariable String constructComments


    ) {

        //Person currentUser = Person.getCurrentSecurityUser();
        HibernateUtil.createTransaction();
        Marker m=mr.getMarkerByID(constructID);
        m.setPublicComments(constructComments);
        ir.insertUpdatesTable(m,"comments","updated public notes");
        HibernateUtil.currentSession().getTransaction().commit();

        return true;

    }

    @RequestMapping(value = "/add-notes/{constructID}/notes/{notes}/publication/{pubID}"
            , method = RequestMethod.POST)
    public
    @ResponseBody
    boolean addCuratorNotes(
            @PathVariable String constructID,
            @PathVariable String notes,
            @PathVariable String pubID

    ) {
        DataNote newConstructNote ;
     //  Person currentUser = Person.getCurrentSecurityUser();
        HibernateUtil.createTransaction();
        newConstructNote= mr.addMarkerDataNote(mr.getMarkerByID(constructID),notes);
        ir.insertUpdatesTable(mr.getMarkerByID(constructID),"curator notes","added new curator note");
        HibernateUtil.currentSession().getTransaction().commit();
        if (newConstructNote!=null) {
            return true;
        }
        return true;

    }

    @RequestMapping(value = "/delete-alias/{constructID}/aliasID/{aliasID}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    void deleteAlias(@PathVariable String constructID,@PathVariable String aliasID) throws Exception{
        HibernateUtil.createTransaction();

        //if the person being removed is the contact person, set the contact person to null

       // Person currentUser = Person.getCurrentSecurityUser();
        Marker m=mr.getMarkerByID(constructID);
        MarkerAlias constructAlias=mr.getMarkerAlias(aliasID);
        mr.deleteMarkerAlias(m,constructAlias);
        ir.insertUpdatesTable(m,"alias","deleted data alias");
        HibernateUtil.flushAndCommitCurrentSession();

    }
    @RequestMapping(value = "/delete-note/{constructID}/noteID/{noteID}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    void deleteNote(@PathVariable String constructID,@PathVariable String noteID) throws Exception{


        //if the person being removed is the contact person, set the contact person to null
      //  Person currentUser = Person.getCurrentSecurityUser();
        Marker m=mr.getMarkerByID(constructID);
        DataNote curatorNote=ir.getDataNoteByID(noteID);
        ir.insertUpdatesTable(m,"curator note","deleted curator note");
        mr.removeCuratorNote(m,curatorNote);




    }

        @RequestMapping(value = "/construct-run-update/", method = RequestMethod.POST)
    public @ResponseBody String updateConstruct(HttpServletRequest request,HttpServletResponse response)  {
        String validationMessage;
        String constructName=request.getParameter("constructName");
        String constructPrefix=request.getParameter("prefix");
        String pubZdbID=request.getParameter("constructPublicationZdbID");
        String constructID=request.getParameter("constructEdit");
        String constructAlias = request.getParameter("constructSynonym");
            String constructComments = request.getParameter("constructComments");
        String weirdString="),";
        String replaceString=")";


        String constructStoredName = request.getParameter("constructStoredName");
        //  String[] cpt= getComponentsFromName(constructStoredName);


        List<Marker> markerList = RepositoryFactory.getMarkerRepository().getMarkersByAbbreviation(constructName);
        if (markerList.size() > 0) {

            return "\"" + constructName + "\" is not a unique name";
        } else{
            // code to delete the construct component group and the relationships by construct id.
            // Relationships to be deleted should be restricted to promoter and coding.
            HibernateUtil.createTransaction();
            mr.deleteConstructComponentByID(constructID);
            ConstructCuration construct=mr.getConstructByID(constructID);
//            construct.setConstructGeneratedName(constructName);
            construct.setName(constructName);
            /*construct.setNameOrder(constructName);*/

            construct.setPublicComments(constructComments);
           // Marker marker=mr.getMarkerByID(constructID);
            construct.setName(constructName);
            //marker.setAbbreviation(constructName);

            currentSession().save(construct);
            ConstructComponentService.setConstructComponents(constructStoredName, pubZdbID, constructID);
            HibernateUtil.flushAndCommitCurrentSession();
            InformixUtil.runInformixProcedure("regen_construct_marker", constructID + "");


            return "\"" + constructName + "\" successfully updated";
        }



    }





}



