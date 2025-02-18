package org.zfin.framework.featureflag;

import lombok.extern.log4j.Log4j2;

import jakarta.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.zfin.profile.Person;

import static org.zfin.profile.service.ProfileService.getCurrentSecurityUserNoGuest;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getProfileRepository;

@Log4j2
public class FeatureFlags {

    public enum FlagState {ENABLED, DISABLED, UNSET}

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
            Person person = getCurrentSecurityUserNoGuest();
            if (person == null) {
                return FlagState.UNSET;
            }

            PersonalFeatureFlag personalFeatureFlag = getInfrastructureRepository().getPersonalFeatureFlag(person, flagName);
            if (personalFeatureFlag.isEnabled()) {
                return FlagState.ENABLED;
            } else {
                return FlagState.DISABLED;
            }
        } catch (NoResultException e) {
            return FlagState.UNSET;
        }
    }

    public static FlagState isFlagEnabledForPersonScope(String flagName, Person person) {
        try {
            PersonalFeatureFlag personalFeatureFlag = getInfrastructureRepository().getPersonalFeatureFlag(person, flagName);
            if (personalFeatureFlag.isEnabled()) {
                return FlagState.ENABLED;
            } else {
                return FlagState.DISABLED;
            }
        } catch (NoResultException e) {
            return FlagState.UNSET;
        }
    }

    public static void setFeatureFlagForCurrentPerson(String flagName, boolean value) {
        Person currentUser = getCurrentSecurityUserNoGuest();
        if (currentUser != null) {
            setFeatureFlagForPerson(currentUser, flagName, value);
        }
    }

    public static void setFeatureFlagForPersonByUsername(String username, String flagName, boolean value) {
        if (username == null) {
            setFeatureFlagForCurrentPerson(flagName, value);
            return;
        }
        Person person = getProfileRepository().getPersonByName(username);
        if (person == null) {
            setFeatureFlagForCurrentPerson(flagName, value);
        } else {
            setFeatureFlagForPerson(person, flagName, value);
        }
    }

    public static void setFeatureFlagForPerson(Person person, String flagName, boolean value) {
        getInfrastructureRepository().setPersonalFeatureFlag(person, flagName, value);
    }

    public static void setFeatureFlagForGlobalScope(String name, boolean enabled) {
        getInfrastructureRepository().setFeatureFlag(name, enabled);
    }

}
