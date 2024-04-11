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
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupEntry;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.dto.ConstructDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.marker.repository.MarkerRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

import static org.zfin.construct.presentation.ConstructComponentService.createNewConstructFromSubmittedForm;

@Controller
@RequestMapping("/construct")
public class ConstructAddController {

    private static Logger LOG = LogManager.getLogger(ConstructAddController.class);

    @Autowired
    private MarkerRepository mr;

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
        return "construct/construct-add";
    }

    //TODO: deprecate and replace with the below method
    @RequestMapping(value = "/construct-add-component", method = RequestMethod.POST)
    public
    @ResponseBody
    String addConstruct(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Marker newConstruct;
        AddConstructFormFields form = AddConstructFormFields.fromRequest(request);

        try {
            HibernateUtil.createTransaction();
            newConstruct = createNewConstructFromSubmittedForm(form);
            HibernateUtil.flushAndCommitCurrentSession();
        //catch both types of exceptions
        } catch (Exception e) {
            try {
                HibernateUtil.rollbackTransaction();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            if (e instanceof InvalidConstructNameException) {
                return e.getMessage();
            }
            return " Construct  could not be created";
        }

        String constructLink = MarkerPresentation.getLink(newConstruct);
        return "\"" + constructLink + "\" successfully added ";
    }

    //TODO: combine with the above method
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public
    @ResponseBody
    String createConstruct(@RequestBody AddConstructFormFields constructValues) {
        Marker newConstruct;
        //set the construct name from the object
        constructValues.getConstructNameObject().reinitialize();
        constructValues.setConstructName(constructValues.getConstructNameObject().toString());
        constructValues.setConstructSequence(constructValues.getConstructSequence().toUpperCase());
        try {
            HibernateUtil.createTransaction();
            newConstruct = createNewConstructFromSubmittedForm(constructValues);
            HibernateUtil.flushAndCommitCurrentSession();
            //catch both types of exceptions
        } catch (Exception e) {
            try {
                HibernateUtil.rollbackTransaction();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            if (e instanceof InvalidConstructNameException) {
                return e.getMessage();
            }
            return " Construct  could not be created";
        }

        String constructLink = MarkerPresentation.getLink(newConstruct);
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

