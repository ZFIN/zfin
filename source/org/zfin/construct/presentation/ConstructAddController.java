package org.zfin.construct.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.construct.ConstructCuration;
import org.zfin.database.InformixUtil;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.curation.ui.AttributionModule;
import org.zfin.gwt.root.dto.ConstructDTO;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.marker.presentation.TargetGeneLookupEntry;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

import static org.zfin.framework.HibernateUtil.currentSession;

@Controller
@RequestMapping("/construct")
public class ConstructAddController {

    private static Logger LOG = Logger.getLogger(ConstructAddController.class);

    @Autowired
    private static MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private static PublicationRepository pr = RepositoryFactory.getPublicationRepository();
    private static InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();


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
  // ConstructDTO addConstruct(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String validationMessage;
        String constructName = request.getParameter("constructName");

        String pubZdbID = request.getParameter("constructPublicationZdbID");
        String constructAlias = request.getParameter("constructAlias");
        String constructComments = request.getParameter("constructComments");
        String constructCuratorNote = request.getParameter("constructCuratorNote");
        String constructStoredName = request.getParameter("constructStoredName");
        String constructType = request.getParameter("chosenType");


        List<Marker> markerList = RepositoryFactory.getMarkerRepository().getMarkersByAbbreviation(constructName);
        if (markerList.size() > 0) {
           return "\"" + constructName + "\" is not a unique name";
        } else if (constructName.contains(".-")){
        return "\"" + constructName + "\" contains a dot followed by a hyphen, Please check";

        }

        else{

//                ConstructNameGroup cng = new ConstructNameGroup();
            ConstructCuration newConstruct = new ConstructCuration();
            Person currentUser = ProfileService.getCurrentSecurityUser();
            HibernateUtil.createTransaction();
            newConstruct.setName(constructName);
//                newConstruct.setAbbreviation(constructName);
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
            newConstruct.setOwner(currentUser);
          /*  newConstruct.setRegenDate(new Date());*/
            newConstruct.setCreatedDate(new Date());
            newConstruct.setModDate(new Date());
            currentSession().save(newConstruct);
            currentSession().flush();
             String constructZdbID = newConstruct.getZdbID();
//                newConstruct.setZdbID(cng.getZdbID());
//                newConstruct.setConstructGeneratedName(constructName);
//                newConstruct.setName(constructName);
//                newConstruct.setNameOrder(constructName);
//                newConstruct.setAbbreviation(constructName);
//                newConstruct.setAbbreviationOrder(constructName);

//                newConstruct.setOwner(currentUser);
            if (!StringUtils.isEmpty(constructComments)) {
                newConstruct.setPublicComments(constructComments);
            }


            ConstructComponentService.setConstructComponents(constructStoredName, pubZdbID, constructZdbID);

           InformixUtil.runInformixProcedure("regen_construct_marker", constructZdbID + "");
            Marker latestConstruct = mr.getMarkerByID(constructZdbID);
            String constructLink = MarkerPresentation.getLink(latestConstruct);
//             ir.insertRecordAttribution(cng.getZdbID(), pubZdbID);
            Publication constructPub = pr.getPublication(pubZdbID);

            if (!StringUtils.isEmpty(constructAlias)) {

                mr.addMarkerAlias(latestConstruct, constructAlias, constructPub);
            }

            if (!StringUtils.isEmpty(constructCuratorNote)) {
                mr.addMarkerDataNote(latestConstruct, constructCuratorNote);
            }



            HibernateUtil.flushAndCommitCurrentSession();


         // return "\"" + constructName + "\" successfully added";
           return "\"" + constructLink + "\" successfully added " ;



        }
        /*ConstructDTO conDTO = getConstruct(constructName);
        return conDTO;*/
      // return "\"" + constructLink + "\" successfully added";
    }









//method to find markers for autocomplete in construct builder
    @RequestMapping(value = "/find-constructMarkers", method = RequestMethod.GET)
    public
    @ResponseBody
    List<TargetGeneLookupEntry> lookupConstructMarkers(@RequestParam("term") String lookupString, @RequestParam("exclude") String zdbId) {

        return mr.getConstructComponentsForString(lookupString, zdbId);
    }

    public ConstructDTO getConstruct(String featureZdbID) {
        ConstructCuration feature = (ConstructCuration) HibernateUtil.currentSession().get(ConstructCuration.class, featureZdbID);
        return DTOConversionService.convertToConstructDTO(feature);
    }   

}

