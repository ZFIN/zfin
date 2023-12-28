package org.zfin.profile;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import static org.zfin.repository.RepositoryFactory.getProfileRepository;


@Component
public class EmailPrivacyPreferenceConverter implements Converter<String, EmailPrivacyPreference> {

    @Override
    public EmailPrivacyPreference convert(String source) {
        return getProfileRepository().getEmailPrivacyPreferenceByName(source);
    }
}