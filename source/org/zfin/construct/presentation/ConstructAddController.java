package org.zfin.construct.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.Species;
import org.zfin.construct.ConstructComponent;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.repository.ConstructRepository;
import org.zfin.database.InformixUtil;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupEntry;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.dto.ConstructDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.repository.SequenceRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/construct")
public class ConstructAddController {

    private static Logger LOG = Logger.getLogger(ConstructAddController.class);

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


    @ModelAttribute("formBean")
    private ConstructAddBean getDefaultSearchForm(@RequestParam(value = "constructPublicationZdbID", required = false) String pubZdbID) {
        ConstructAddBean constructBean = new ConstructAddBean();


        if (StringUtils.isNotEmpty(pubZdbID))
            constructBean.setConstructPublicationZdbID(pubZdbID);

        return constructBean;

    }

    @RequestMapping("/construct-add")
    protected String showForm(Model model) throws Exception {

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Add Construct");
        return "construct/construct-add.page";
    }


    @RequestMapping(value = "/construct-add-component", method = RequestMethod.POST)
    public
    @ResponseBody
        String addConstruct(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String constructName = request.getParameter("constructName");
        String pubZdbID = request.getParameter("constructPublicationZdbID");
        String constructAlias = request.getParameter("constructAlias");
        String constructSequence = request.getParameter("constructSequence");
        String constructComments = request.getParameter("constructComments");
        String constructCuratorNote = request.getParameter("constructCuratorNote");
        String constructStoredName = request.getParameter("constructStoredName");
        String constructType = request.getParameter("chosenType");
        String constructPrefix = request.getParameter("prefix");




        List<Marker> markerList = RepositoryFactory.getMarkerRepository().getMarkersByAbbreviation(constructName);
        ConstructCuration newConstruct = new ConstructCuration();
        Publication constructPub = pr.getPublication(pubZdbID);

        if (markerList.size() > 0) {
            return "\"" + constructName + "\" is not a unique name";
        } else if (constructName.contains(".-")) {
            return "\"" + constructName + "\" contains a dot followed by a hyphen, Please check";

        } else if (constructName.contains("..")) {
            return "\"" + constructName + "\" contains a dot followed by a dot, Please check";
        } else if (constructName.contains(".,")) {
            return "\"" + constructName + "\" contains a dot followed by a comma, Please check";

        } else if (constructName.contains("()")) {
            return " Construct Name cannot be blank";

        } else {
            newConstruct.setName(constructName);
            //TODO use chosenType or any other way to set Construct Type
            if (constructName.startsWith("Tg")) {


                newConstruct.setConstructType(mr.getMarkerTypeByName(Marker.Type.TGCONSTRCT.toString()));
            }
            if (constructName.startsWith("Et")) {

                newConstruct.setConstructType(mr.getMarkerTypeByName(Marker.Type.ETCONSTRCT.toString()));

            }
            if (constructName.startsWith("Gt")) {

                newConstruct.setConstructType(mr.getMarkerTypeByName(Marker.Type.GTCONSTRCT.toString()));
            }
            if (constructName.startsWith("Pt")) {

                newConstruct.setConstructType(mr.getMarkerTypeByName(Marker.Type.PTCONSTRCT.toString()));
            }
            newConstruct.setName(constructName);

          /*  newConstruct.setRegenDate(new Date());*/
            newConstruct.setCreatedDate(new Date());
            newConstruct.setModDate(new Date());

            try {
                HibernateUtil.createTransaction();
                cr.createConstruct(newConstruct,constructPub);


                String constructZdbID = newConstruct.getZdbID();


                if (!StringUtils.isEmpty(constructComments)) {
                    newConstruct.setPublicComments(constructComments);
                }

                //storing Construct Components

                String constructCassettes[] = ConstructComponentService.getCassettes(constructStoredName);
                int numCassettes = constructCassettes.length;

                ConstructComponentService.setConstructWrapperComponents(constructType, constructPrefix, constructZdbID, 1);

                for (int i = 0; i < numCassettes; i++) {
                    ConstructComponentService.getPromotersAndCoding(constructCassettes[i], i + 1, constructZdbID, pubZdbID);
                }


                int lastComp = ConstructComponentService.getComponentCount(constructZdbID);
                mr.addConstructComponent(numCassettes, lastComp, constructZdbID, ")", ConstructComponent.Type.CONTROLLED_VOCAB_COMPONENT, "construct wrapper component", ir.getCVZdbIDByTerm(")").getZdbID());

                //moving construct record to marker table
                InformixUtil.runInformixProcedure("regen_construct_marker", constructZdbID + "");
		InformixUtil.runInformixProcedure("regen_names_marker", constructZdbID + "");
                Marker latestConstruct = mr.getMarkerByID(newConstruct.getZdbID());




                if (!StringUtils.isEmpty(constructAlias)) {

                    mr.addMarkerAlias(latestConstruct, constructAlias, constructPub);
                }

                if (!StringUtils.isEmpty(constructCuratorNote)) {
                    mr.addMarkerDataNote(latestConstruct, constructCuratorNote);
                }
                if (!StringUtils.isEmpty(constructSequence)) {
                    ReferenceDatabase genBankRefDB = sr.getReferenceDatabase(ForeignDB.AvailableName.GENBANK,
                            ForeignDBDataType.DataType.GENOMIC, ForeignDBDataType.SuperType.SEQUENCE, Species.Type.ZEBRAFISH);
                    mr.addDBLink(latestConstruct, constructSequence, genBankRefDB,pubZdbID);
                }


                HibernateUtil.flushAndCommitCurrentSession();
            }
                catch (Exception e) {
                    try {
                        HibernateUtil.rollbackTransaction();
                    } catch (HibernateException he) {
                        LOG.error("Error during roll back of transaction", he);
                    }
                    LOG.error("Error in Transaction", e);
                    return " Construct  could not be created";
                    //throw new RuntimeException("Error during transaction. Rolled back.", e);

                }




            }
        Marker latestConstruct = mr.getMarkerByID(newConstruct.getZdbID());
        String constructLink = MarkerPresentation.getLink(latestConstruct);
        return "\"" + constructLink + "\" successfully added ";


    }


//method to find markers for autocomplete in construct builder
    @RequestMapping(value = "/find-constructMarkers", method = RequestMethod.GET)
    public
    @ResponseBody
    List<LookupEntry> lookupConstructMarkers(@RequestParam("term") String lookupString, @RequestParam("pub") String zdbId) {

        return mr.getConstructComponentsForString(lookupString, zdbId);
    }

    public ConstructDTO getConstruct(String featureZdbID) {
        ConstructCuration feature = (ConstructCuration) HibernateUtil.currentSession().get(ConstructCuration.class, featureZdbID);
        return DTOConversionService.convertToConstructDTO(feature);
    }   

}

