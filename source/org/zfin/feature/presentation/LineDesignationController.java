package org.zfin.feature.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.people.LabFeaturePrefix;
import org.zfin.people.Person;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 */
@Controller
// this is already in the context
//@RequestMapping("/feature")
public class LineDesignationController {

    @RequestMapping(value="/line-designations")
    public String getFeaturePrefixes(Model model) throws Exception {
        LineDesignationBean lineDesignationBean = new LineDesignationBean();
        lineDesignationBean.setFeaturePrefixLightList(RepositoryFactory.getFeatureRepository().getFeaturePrefixWithLabs()); ;
        model.addAttribute(LookupStrings.FORM_BEAN,lineDesignationBean) ;
        model.addAttribute(LookupStrings.DYNAMIC_TITLE,"Line Designations") ;
        return "feature/line-designation.page" ;
    }

    @RequestMapping(value="/features-for-lab/{zdbID}")
    public String getFeatureForLab(@PathVariable String zdbID, Model model) throws Exception {
        model.addAttribute("features",RepositoryFactory.getFeatureRepository().getFeaturesForLab(zdbID)) ;
        return "feature/features-for-lab.insert" ;
    }


    @RequestMapping(value="/alleles/{prefix}")
    public String getAllelesForPrefix(@PathVariable String prefix,Model model) throws Exception {
        AllelesForPrefixBean allelesForPrefixBean = new AllelesForPrefixBean();

        List<LabFeaturePrefix> labFeaturePrefixes = RepositoryFactory.getFeatureRepository().getLabFeaturePrefixForPrefix(prefix);
        Map<String,LabEntry> labEntries = new TreeMap<String,LabEntry>() ;
        // either all entries are current or not
        for(LabFeaturePrefix labFeaturePrefix: labFeaturePrefixes){
            if(Person.isCurrentSecurityUserRoot() || labFeaturePrefix.getCurrentDesignation()){
                labEntries.put(labFeaturePrefix.getLab().getZdbID(),new LabEntry(labFeaturePrefix.getLab(),labFeaturePrefix.getCurrentDesignation())) ;
            }
            if(false==allelesForPrefixBean.isHasNonCurrentLabs() && false==labFeaturePrefix.getCurrentDesignation()){
                allelesForPrefixBean.setHasNonCurrentLabs(true);
            }
        }
        allelesForPrefixBean.setLabs(labEntries.values());


        List<FeatureLabEntry> featureLabEntries = RepositoryFactory.getFeatureRepository().getFeaturesForPrefix(prefix);
        processCurrentLabs(featureLabEntries,labEntries);
        allelesForPrefixBean.setFeatureLabEntries(featureLabEntries);


        model.addAttribute(LookupStrings.FORM_BEAN,allelesForPrefixBean) ;

        return "feature/alleles-for-feature-prefix.page";
    }

    private void processCurrentLabs(List<FeatureLabEntry> featureLabEntries, Map<String,LabEntry> labEntries) {
        for(FeatureLabEntry featureLabEntry: featureLabEntries){
            if(labEntries.containsKey(featureLabEntry.getSourceOrganization().getZdbID())){
                featureLabEntry.setCurrent(labEntries.get(featureLabEntry.getSourceOrganization().getZdbID()).isCurrentLineDesignation());
            }
        }
    }
}
