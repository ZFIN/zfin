package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Clone;
import org.zfin.marker.service.MarkerService;
import org.zfin.people.Person;
import org.zfin.repository.RepositoryFactory;

/**
 */
@Controller
public class CloneAddController {

    private static Logger logger = Logger.getLogger(CloneAddController.class);

    private CloneAddValidator validator = new CloneAddValidator();

    @RequestMapping( value = "/clone-add",method = RequestMethod.GET)
    public String getView(
            Model model
            ,@ModelAttribute("formBean") CloneAddBean formBean
            ,BindingResult result
    ) throws Exception {
        model.addAttribute("cloneMarkerTypes", MarkerService.getCloneMarkerTypes());
        model.addAttribute("cloneLibraries", RepositoryFactory.getMarkerRepository().getProbeLibraries());
        return "marker/clone-add.page";
    }


    @RequestMapping( value = "/clone-add",method = RequestMethod.POST)
    public String addClone(Model model
            ,@ModelAttribute("formBean") CloneAddBean formBean
            ,BindingResult result ) throws Exception {

        validator.validate(formBean, result);
        String name = formBean .getName();

        if(result.hasErrors()){
            return getView(model,formBean,result);
        }

        // because it can come through on both the request and the command, will put both in with the same name
        // delimited by a comma.  This addresses that.
        String[] names = name.split(",");
        if (names.length == 2 && names[0].equals(names[1])) {
            formBean .setName(names[0]);
        }

        Clone clone = null;
        try {
            formBean .setOwnerZdbID(Person.getCurrentSecurityUser().getZdbID());
            HibernateUtil.createTransaction();
            clone = MarkerService.createClone(formBean);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            logger.error(e);
            HibernateUtil.rollbackTransaction();
            result.reject("no lookup","Failed to add clone: "+e.getMessage());
            return getView(model,formBean,result);
        }

        String zdbID = clone.getZdbID();
        return "redirect:/action/marker/marker-edit?zdbID=" + zdbID;
    }


}
