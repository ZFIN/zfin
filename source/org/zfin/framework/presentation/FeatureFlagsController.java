package org.zfin.framework.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.NameValuePairDTO;
import org.zfin.framework.api.View;
import org.zfin.framework.featureflag.FeatureFlag;
import org.zfin.framework.featureflag.FeatureFlags;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.zfin.profile.Person;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.zfin.framework.featureflag.FeatureFlags.isFlagEnabledForPersonScope;
import static org.zfin.repository.RepositoryFactory.getProfileRepository;

@Log4j2
@RestController
public class FeatureFlagsController {

    @RequestMapping("/devtool/feature-flags/home")
    public ModelAndView homePage(Model model) throws Exception {
        model.addAttribute("flags", FeatureFlags.getFlags());
        return new ModelAndView("dev-tools/feature-flags");
    }

    @RequestMapping("/devtool/feature-flags/per-user")
    public ModelAndView perUser(Model model, HttpServletRequest request) throws Exception {
        String flagname = request.getParameter("flagname");
        model.addAttribute("flagname", flagname);
        return new ModelAndView("dev-tools/feature-flags-per-user", model.asMap());
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/devtool/feature-flags", method = RequestMethod.GET)
    public List<FeatureFlag> getFlags() {
        return FeatureFlags.getFlags();
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/devtool/feature-flags", method = RequestMethod.POST)
    public NameValuePairDTO saveFlag(@Valid @RequestBody NameValuePairDTO nameValuePair,
                                     HttpServletRequest request) {
        String name = nameValuePair.getName();
        String value = nameValuePair.getValue();
        String scope = request.getParameter("scope");

        HibernateUtil.createTransaction();
        if ("global".equals(scope)) {
            FeatureFlags.setFeatureFlagForGlobalScope(name, "true".equals(value));
        } else if ("person".equals(scope)) {
            String person = request.getParameter("person");
            FeatureFlags.setFeatureFlagForPersonByUsername(person, name, "true".equals(value));
        }
        HibernateUtil.flushAndCommitCurrentSession();

        return nameValuePair;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/devtool/feature-flags/people", method = RequestMethod.GET)
    public Map<String, Map<String, FeatureFlags.FlagState>> getPeople() {
        List<Person> users = getProfileRepository().getRootUsers();
        List<FeatureFlag> allFlags = FeatureFlags.getFlags();
        Map<String, Map<String, FeatureFlags.FlagState>> peopleFlags = new HashMap<>();
        for (Person user : users) {
            Map<String, FeatureFlags.FlagState> flagStatus = new HashMap<>();
            for (FeatureFlag featureFlag : allFlags) {
                FeatureFlags.FlagState isSet = isFlagEnabledForPersonScope(featureFlag.getName(), user);
                flagStatus.put(featureFlag.getName(), isSet);
            }
            peopleFlags.put(user.getUsername(), flagStatus);
        }
        return peopleFlags;
    }

}
