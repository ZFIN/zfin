package org.zfin.feature.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.profile.OrganizationFeaturePrefix;
import org.zfin.profile.service.ProfileService;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;


@Controller
@RequestMapping("/feature")
public class LineDesignationController {

    @Autowired
    private ProfileService profileService;


    @RequestMapping(value = "/line-designations")
    public String getFeaturePrefixes(Model model) throws Exception {
        LineDesignationBean lineDesignationBean = new LineDesignationBean();
        lineDesignationBean.setFeaturePrefixLightList(RepositoryFactory.getFeatureRepository().getFeaturePrefixWithLabs());
        model.addAttribute(LookupStrings.FORM_BEAN, lineDesignationBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Line Designations");

        return "feature/line-designation.page";
    }

    @RequestMapping(value = "/features-for-lab/{zdbID}")
    public String getFeatureForLab(@PathVariable String zdbID, Model model) throws Exception {
        model.addAttribute("features", RepositoryFactory.getFeatureRepository().getFeaturesForLab(zdbID, 50));
        model.addAttribute("labID", zdbID);
        return "feature/features-for-lab.insert";
    }

    @RequestMapping(value = "/alleles/{prefix}")
    public String getAllelesForPrefix(@PathVariable String prefix, Model model) throws Exception {
        AllelesForPrefixBean allelesForPrefixBean = new AllelesForPrefixBean();

        List<OrganizationFeaturePrefix> organizationFeaturePrefixes = RepositoryFactory.getFeatureRepository().getOrganizationFeaturePrefixForPrefix(prefix);
        Map<String, LabEntry> labEntries = new TreeMap<>();
        // either all entries are current or not
        for (OrganizationFeaturePrefix organizationFeaturePrefix : organizationFeaturePrefixes) {
            if (profileService.isCurrentSecurityUserRoot() || organizationFeaturePrefix.getCurrentDesignation()) {
                labEntries.put(organizationFeaturePrefix.getOrganization().getZdbID(), new LabEntry(organizationFeaturePrefix.getOrganization(), organizationFeaturePrefix.getCurrentDesignation()));
            }
            if (false == allelesForPrefixBean.isHasNonCurrentLabs() && false == organizationFeaturePrefix.getCurrentDesignation()) {
                allelesForPrefixBean.setHasNonCurrentLabs(true);
            }
        }
        allelesForPrefixBean.setLabs(labEntries.values());


        List<FeatureLabEntry> featureLabEntries = RepositoryFactory.getFeatureRepository().getFeaturesForPrefix(prefix);
        processCurrentLabs(featureLabEntries, labEntries);
        allelesForPrefixBean.setFeatureLabEntries(featureLabEntries);


        model.addAttribute(LookupStrings.FORM_BEAN, allelesForPrefixBean);

        return "feature/alleles-for-feature-prefix.insert";
    }

    private void processCurrentLabs(List<FeatureLabEntry> featureLabEntries, Map<String, LabEntry> labEntries) {
        for (FeatureLabEntry featureLabEntry : featureLabEntries) {
            if (featureLabEntry.getSourceOrganization() != null && labEntries.containsKey(featureLabEntry.getSourceOrganization().getZdbID())) {
                featureLabEntry.setCurrent(labEntries.get(featureLabEntry.getSourceOrganization().getZdbID()).isCurrentLineDesignation());
            }
        }
    }
}
