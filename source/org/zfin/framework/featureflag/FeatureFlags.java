package org.zfin.framework.featureflag;

import lombok.extern.log4j.Log4j2;

import jakarta.persistence.NoResultException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.zfin.profile.Person;

import static org.zfin.profile.service.ProfileService.getCurrentSecurityUser;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

@Log4j2
public class FeatureFlags {

    enum FlagState {ENABLED, DISABLED, UNSET}

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

    public static boolean isFlagEnabled(String flagName) {
        FlagState flagEnabledForPerson = isFlagEnabledForPersonScope(flagName);
        if (flagEnabledForPerson == FlagState.UNSET) {
            return isFlagEnabledForGlobalScope(flagName);
        } else if (flagEnabledForPerson == FlagState.ENABLED) {
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
        try {
            FeatureFlag flag = getInfrastructureRepository().getFeatureFlag(flagName);
            return flag.isEnabledForGlobalScope();
        } catch (NoResultException e) {
            try {
                return FeatureFlagEnum.getFlagByName(flagName).isEnabledByDefault();
            } catch (NoSuchElementException nsee) {
                return false;
            }
        }
    }

    public static FlagState isFlagEnabledForPersonScope(FeatureFlag flag) {
        return isFlagEnabledForPersonScope(flag.getName());
    }

    private static FlagState isFlagEnabledForPersonScope(String flagName) {
        try {
            PersonalFeatureFlag personalFeatureFlag = getInfrastructureRepository().getPersonalFeatureFlag(getCurrentSecurityUser(), flagName);
            if (personalFeatureFlag.isEnabled()) {
                return FlagState.ENABLED;
            } else {
                return FlagState.DISABLED;
            }
        } catch (NoResultException e) {
            return FlagState.UNSET;
        }
    }

    public static void setFeatureFlagForPersonScope(String name, boolean value) {
        Person currentUser = getCurrentSecurityUser();
        getInfrastructureRepository().setPersonalFeatureFlag(currentUser, name, value);
    }


    public static void setFeatureFlagForGlobalScope(String name, boolean enabled) {
        getInfrastructureRepository().setFeatureFlag(name, enabled);
    }

}
