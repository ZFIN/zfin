package org.zfin.construct.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.construct.ConstructComponent;
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
    private MarkerRepository mr;
    private PublicationRepository pr;
    private InfrastructureRepository ir;


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
        String constructComments = request.getParameter("constructComments");
        String constructCuratorNote = request.getParameter("constructCuratorNote");
        String constructStoredName = request.getParameter("constructStoredName");
        String constructType = request.getParameter("chosenType");
        String constructPrefix = request.getParameter("prefix");




        List<Marker> markerList = RepositoryFactory.getMarkerRepository().getMarkersByAbbreviation(constructName);
        if (markerList.size() > 0) {
            return "\"" + constructName + "\" is not a unique name";
        } else if (constructName.contains(".-")) {
            return "\"" + constructName + "\" contains a dot followed by a hyphen, Please check";

        } else if (constructName.contains("()")) {
            return "\"" + "Construct Name" + "\" cannot be blank";

        } else {

            ConstructCuration newConstruct = new ConstructCuration();
            Person currentUser = ProfileService.getCurrentSecurityUser();
            HibernateUtil.createTransaction();
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
            newConstruct.setOwner(currentUser);
          /*  newConstruct.setRegenDate(new Date());*/
            newConstruct.setCreatedDate(new Date());
            newConstruct.setModDate(new Date());
            currentSession().save(newConstruct);
            currentSession().flush();

            String constructZdbID = newConstruct.getZdbID();
            Publication constructPub = pr.getPublication(pubZdbID);

            if (!StringUtils.isEmpty(constructComments)) {
                newConstruct.setPublicComments(constructComments);
            }

            //storing Construct Components

            String constructCassettes[]=ConstructComponentService.getCassettes(constructStoredName);
                int numCassettes = constructCassettes.length;

                ConstructComponentService.setConstructWrapperComponents(constructType, constructPrefix, constructZdbID, 1);

                for (int i = 0; i < numCassettes; i++) {
                    ConstructComponentService.getPromotersAndCoding(constructCassettes[i], i+1, constructZdbID, pubZdbID);
                }

               /* String sqlCount = " select MAX(cc_order) from construct_component where cc_construct_zdb_id=:zdbID ";
                Query query = currentSession().createSQLQuery(sqlCount);
                query.setString("zdbID", constructZdbID);
                Session session = HibernateUtil.currentSession();
                int lastComp = (Integer) query.uniqueResult() + 1;*/
            int lastComp=ConstructComponentService.getComponentCount(constructZdbID);
                mr.addConstructComponent(numCassettes, lastComp, constructZdbID, ")", ConstructComponent.Type.CONTROLLED_VOCAB_COMPONENT, "construct wrapper component", ir.getCVZdbIDByTerm(")").getZdbID());

                //moving construct record to marker table
                InformixUtil.runInformixProcedure("regen_construct_marker", constructZdbID + "");

                Marker latestConstruct = mr.getMarkerByID(constructZdbID);
                String constructLink = MarkerPresentation.getLink(latestConstruct);


                if (!StringUtils.isEmpty(constructAlias)) {

                    mr.addMarkerAlias(latestConstruct, constructAlias, constructPub);
                }

                if (!StringUtils.isEmpty(constructCuratorNote)) {
                    mr.addMarkerDataNote(latestConstruct, constructCuratorNote);
                }


                HibernateUtil.flushAndCommitCurrentSession();


                
                return "\"" + constructLink + "\" successfully added ";


            }

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

