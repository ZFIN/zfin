package org.zfin.framework.featureflag;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.BooleanUtils;

@Log4j2
public class FeatureFlags {
    public static final String SESSION_PREFIX = "FEATURE:";

    public static List<FeatureFlag> getFlags() {
        List<FeatureFlag> flags = new ArrayList<>();
        for( FeatureFlagEnum value : FeatureFlagEnum.values() ) {
            FeatureFlag flag = new FeatureFlag();
            flag.setName(value.getName());
            flag.setEnabled(isFlagEnabled(flag));
            flags.add(flag);
        }
        return flags;
    }

    public static boolean isFlagEnabled(FeatureFlagEnum flag) {
        return isFlagEnabled(flag.getName());
    }

    private static boolean isFlagEnabled(FeatureFlag flag) {
        return isFlagEnabled(flag.getName());
    }

    private static boolean isFlagEnabled(String flagName) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Boolean value = (Boolean) request.getSession().getAttribute(SESSION_PREFIX + flagName);
        return BooleanUtils.isTrue(value);
    }

    public static void setSessionFeatureFlag(String name, boolean value) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String featureKey = SESSION_PREFIX + name;
        Object sessionVariable = request.getSession().getAttribute(featureKey);
        log.debug("Current feature session value for " + featureKey);
        log.debug(sessionVariable);
        log.debug("Setting to: " + value);

        request.getSession().setAttribute(featureKey, value);
    }

}
