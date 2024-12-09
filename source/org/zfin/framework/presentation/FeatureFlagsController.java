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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Log4j2
@RestController
public class FeatureFlagsController {

    @RequestMapping("/devtool/feature-flags/home")
    public ModelAndView homePage(Model model) throws Exception {
        model.addAttribute("flags", FeatureFlags.getFlags());
        return new ModelAndView("dev-tools/feature-flags");
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
            FeatureFlags.setFeatureFlagForPersonScope(name, "true".equals(value));
        }
        HibernateUtil.flushAndCommitCurrentSession();

        return nameValuePair;
    }

}
