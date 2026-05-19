package org.zfin.zirc.service;

import org.springframework.stereotype.Service;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.profile.Person;
import org.zfin.zirc.dto.AutocompleteItemDTO;

import java.util.List;

/**
 * Type-ahead lookups for cross-entity links on the ZIRC form. Three
 * surfaces: gene/marker autocomplete (M6), feature autocomplete (M7),
 * person autocomplete (existing add-submitter modal).
 *
 * <p>Each method does a case-insensitive prefix-or-substring match,
 * caps results at 20, and returns the standard {@link
 * AutocompleteItemDTO} shape (label + value). The label embeds the ZDB
 * ID in parens so curators can disambiguate similarly-named entries.
 *
 * <p>Queries use HQL rather than a repository abstraction because the
 * endpoints are read-only and structurally identical — adding a
 * repository layer here would be more indirection than help.
 */
@Service
public class ZircAutocompleteService {

    /** Hard cap so a wildcard query doesn't return thousands of rows. */
    private static final int MAX_RESULTS = 20;

    public List<AutocompleteItemDTO> searchMarkers(String term) {
        if (term == null || term.isBlank()) {return List.of();}
        return HibernateUtil.currentSession()
                .createQuery(
                    "from Marker where lower(abbreviation) like :q order by abbreviationOrder",
                    Marker.class)
                .setParameter("q", "%" + term.toLowerCase() + "%")
                .setMaxResults(MAX_RESULTS)
                .list()
                .stream()
                .map(m -> new AutocompleteItemDTO(
                        m.getAbbreviation() + " (" + m.getZdbID() + ")",
                        m.getZdbID()))
                .toList();
    }

    public List<AutocompleteItemDTO> searchFeatures(String term) {
        if (term == null || term.isBlank()) {return List.of();}
        return HibernateUtil.currentSession()
                .createQuery(
                    "from Feature where lower(abbreviation) like :q order by abbreviationOrder",
                    Feature.class)
                .setParameter("q", "%" + term.toLowerCase() + "%")
                .setMaxResults(MAX_RESULTS)
                .list()
                .stream()
                .map(f -> new AutocompleteItemDTO(
                        f.getAbbreviation() + " (" + f.getZdbID() + ")",
                        f.getZdbID()))
                .toList();
    }

    public List<AutocompleteItemDTO> searchPersons(String term) {
        if (term == null || term.isBlank()) {return List.of();}
        return HibernateUtil.currentSession()
                .createQuery(
                    "from Person where lower(fullName) like :q order by fullName",
                    Person.class)
                .setParameter("q", "%" + term.toLowerCase() + "%")
                .setMaxResults(MAX_RESULTS)
                .list()
                .stream()
                .map(p -> new AutocompleteItemDTO(
                        p.getFullName() + " (" + p.getZdbID() + ")",
                        p.getZdbID()))
                .toList();
    }
}
