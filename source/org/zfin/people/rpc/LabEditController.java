package org.zfin.people.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.people.Lab;
import org.zfin.people.repository.ProfileRepository;

import java.util.List;

/**
 * Page for editing labs features.
 */
@Controller
public class LabEditController {

    private ProfileRepository profileRepository ;

    @Autowired
    public LabEditController(ProfileRepository profileRepository){
        this.profileRepository = profileRepository ;
    }

    @RequestMapping(value={"/dev-tools/test-ajax/{id}"},method = RequestMethod.GET)
    public ModelAndView labTestEditPage(@PathVariable("id") String labZdbId){
        Lab lab = profileRepository.getLabById(labZdbId) ;
        ModelAndView modelAndView = new ModelAndView("profile/lab-edit-test.page");
        modelAndView.addObject("lab",lab);
        List<String> prefixes = profileRepository.getAllPrefixes() ;
        modelAndView.addObject("prefixes",prefixes);
        String prefix = profileRepository.getPrefixForLab(labZdbId) ;
        modelAndView.addObject("prefix",prefix);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE,lab.getName());
        return modelAndView;
    }

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
        List<String> prefixes = profileRepository.getAllPrefixes() ;
        modelAndView.addObject("prefixes",prefixes);
        String prefix = profileRepository.getPrefixForLab(labZdbId) ;
        modelAndView.addObject("prefix",prefix);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE,lab.getName());
        return modelAndView;
    }

    @RequestMapping(value={"/profile/labPrefixes"},method = RequestMethod.GET)
    public @ResponseBody List<String> allLabPrefixes(){
        return profileRepository.getAllPrefixes() ;
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
        String returnPrefix = profileRepository.setLabPrefix(labZdbId, prefix) ;
        if(returnPrefix==null){
            throw new RuntimeException("Failed to save prefix["+ prefix +"] for lab["+labZdbId+"]");
        }
        else{
            return returnPrefix ;
        }
    }

    private String parsePrefix(String prefix) {
        return prefix.substring("prefix=".length(),prefix.indexOf("&")) ;
    }
}
