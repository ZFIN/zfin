package org.zfin.profile.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.zfin.feature.FeaturePrefix;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.profile.Lab;
import org.zfin.profile.repository.ProfileRepository;

import java.util.List;

/**
 * Page for editing labs features.
 */
@Controller
public class LabEditController {

    @Autowired
    private ProfileRepository profileRepository ;

    @Autowired
    private FeatureRepository featureRepository ;

    /**
     * Unused, just here as an example.  Make sure to remove jsp when this goes away.
     * @param labZdbId
     * @return
     */
    @RequestMapping(value={"/profile/editLabPrefix/{id}"},method = RequestMethod.GET)
    public ModelAndView editLabPrefix(@PathVariable("id") String labZdbId){
        Lab lab = profileRepository.getLabById(labZdbId) ;
        ModelAndView modelAndView = new ModelAndView("profile/lab-edit-popup.page");
        modelAndView.addObject("lab",lab);
        List<String> prefixes = featureRepository.getAllFeaturePrefixes() ;
        modelAndView.addObject("prefixes",prefixes);
        String prefix = featureRepository.getCurrentPrefixForLab(labZdbId) ;
        modelAndView.addObject("prefix",prefix);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE,lab.getName());
        return modelAndView;
    }

    @RequestMapping(value={"/profile/labPrefixes"},method = RequestMethod.GET)
    public @ResponseBody List<String> allLabPrefixes(){
        return featureRepository.getAllFeaturePrefixes() ;
    }

    /**
     * prefix=bo&id=prefixId+
     * @param labZdbId
     * @param prefix
     * @return
     */
    @RequestMapping(value={"/profile/saveLabPrefix/{id}"},method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String saveLabPrefix(@PathVariable("id") String labZdbId,
            @RequestBody String prefix){
        prefix = parsePrefix(prefix) ;
        HibernateUtil.createTransaction();
        String returnPrefix = featureRepository.setCurrentPrefix(labZdbId, prefix) ;
        if(returnPrefix==null){
            FeaturePrefix featurePrefix = featureRepository.getFeaturePrefixByPrefix(prefix);
            Lab lab = profileRepository.getLabById(labZdbId);
            featureRepository.insertOrganizationPrefix(lab, featurePrefix);
            HibernateUtil.flushAndCommitCurrentSession();
            return prefix ;
//            throw new RuntimeException("Failed to save prefix["+ prefix +"] for lab["+labZdbId+"]");
        }
        else{
            HibernateUtil.flushAndCommitCurrentSession();
            return returnPrefix ;
        }
    }

    private String parsePrefix(String prefix) {
        return prefix.substring("prefix=".length(),prefix.indexOf("&")) ;
    }
}
