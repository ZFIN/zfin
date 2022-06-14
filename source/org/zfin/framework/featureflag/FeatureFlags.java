package org.zfin.framework.featureflag;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import org.zfin.infrastructure.ZdbFlag;
import org.zfin.repository.RepositoryFactory;

@Log4j2
public class FeatureFlags {
    public static final String SESSION_PREFIX = "FEATURE:";

    enum SessionState {ENABLED, DISABLED, UNSET};

    public static List<FeatureFlag> getFlags() {
        List<FeatureFlag> flags = new ArrayList<>();
        for( FeatureFlagEnum value : FeatureFlagEnum.values() ) {
            FeatureFlag flag = new FeatureFlag();
            flag.setName(value.getName());
            flag.setEnabled(isFlagEnabled(flag));
            flag.setEnabledForGlobalScope(isFlagEnabledForGlobalScope(flag));
            flags.add(flag);
        }
        return flags;
    }

    public static boolean isFlagEnabled(FeatureFlagEnum flag) {
        return isFlagEnabled(flag.getName());
    }

    public static boolean isFlagEnabled(FeatureFlag flag) {
        return isFlagEnabled(flag.getName());
    }

    private static boolean isFlagEnabled(String flagName) {
        SessionState flagEnabledForSession = isFlagEnabledForSessionScope(flagName);
        if (flagEnabledForSession == SessionState.UNSET) {
            return isFlagEnabledForGlobalScope(flagName);
        } else if (flagEnabledForSession == SessionState.ENABLED) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isFlagEnabledForGlobalScope(FeatureFlagEnum flag) {
        return isFlagEnabledForGlobalScope(flag.getName());
    }

    private static boolean isFlagEnabledForGlobalScope(FeatureFlag flag) {
        return isFlagEnabledForGlobalScope(flag.getName());
    }

    private static boolean isFlagEnabledForGlobalScope(String flagName) {
        ZdbFlag updatesFlag = RepositoryFactory.getInfrastructureRepository().getUpdatesFlag();
        log.debug(updatesFlag);
        FeatureFlag flag = RepositoryFactory.getInfrastructureRepository().getFeatureFlag(flagName);
        return flag.isEnabledForGlobalScope();
    }

    public static SessionState isFlagEnabledForSessionScope(FeatureFlag flag) {
        return isFlagEnabledForSessionScope(flag.getName());
    }

    private static SessionState isFlagEnabledForSessionScope(String flagName) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return SessionState.UNSET;
        }
        HttpServletRequest request = requestAttributes.getRequest();

        Boolean value = (Boolean) request.getSession().getAttribute(SESSION_PREFIX + flagName);
        log.debug("isFlagEnabledForSessionScope: Current feature session value for flag named " + flagName);
        log.debug(value);

        if (value == null) {
            return SessionState.UNSET;
        } else if (value == true) {
            return SessionState.ENABLED;
        } else {
            return SessionState.DISABLED;
        }
    }

    public static void setFeatureFlagForSessionScope(String name, boolean value) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String featureKey = SESSION_PREFIX + name;
        Object sessionVariable = request.getSession().getAttribute(featureKey);
        log.debug("Current feature session value for " + featureKey);
        log.debug(sessionVariable);
        log.debug("Setting to: " + value);

        request.getSession().setAttribute(featureKey, value);
    }

    public static void setFeatureFlagForGlobalScope(String name, boolean enabled) {
        RepositoryFactory.getInfrastructureRepository().setFeatureFlag(name, enabled);
    }

}
