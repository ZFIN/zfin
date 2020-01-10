package org.zfin.construct.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.Species;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.repository.ConstructRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.repository.SequenceRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/construct")
public class ConstructEditController {

    private static Logger LOG = LogManager.getLogger(ConstructEditController.class);

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


    private static Logger logger = LogManager.getLogger(ConstructEditController.class);

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
        ReferenceDatabase genBankRefDB = sr.getReferenceDatabase(ForeignDB.AvailableName.GENBANK,
                ForeignDBDataType.DataType.GENOMIC, ForeignDBDataType.SuperType.SEQUENCE, Species.Type.ZEBRAFISH);
        mr.addDBLink(m, constructSequence, genBankRefDB,pubid);
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
        Session session = HibernateUtil.currentSession();
        Marker m=mr.getMarkerByID(constructID);
        ReferenceDatabase genBankRefDB = sr.getReferenceDatabase(ForeignDB.AvailableName.GENBANK,
                ForeignDBDataType.DataType.GENOMIC, ForeignDBDataType.SuperType.SEQUENCE, Species.Type.ZEBRAFISH);
        ir.deleteActiveDataByZdbID(sequenceID);
        String hql = "" +
                "delete from MarkerDBLink dbl where dbl.id = :sequenceID";
        Query query = session.createQuery(hql);
        query.setParameter("sequenceID", sequenceID);
        query.executeUpdate();
        ir.insertUpdatesTable(m,"sequence","deleted sequence");
        HibernateUtil.flushAndCommitCurrentSession();

    }

    @RequestMapping(value = "/delete-note/{constructID}/noteID/{noteID}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    void deleteNote(@PathVariable String constructID,@PathVariable String noteID) throws Exception{
        DataNote curatorNote=ir.getDataNoteByID(noteID);
        Marker m=mr.getMarkerByID(constructID);
        ir.insertUpdatesTable(m,"curator note","deleted curator note");
        mr.removeCuratorNote(m,curatorNote);
    }


    }









