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
        return searchMarkers(term, null);
    }

    /**
     * Same shape as {@link #searchMarkers(String)} but narrows results
     * to a {@link Marker.TypeGroup} (e.g. {@code GENEDOM} for the gene
     * picker, {@code SSLP} for sequence-tagged-site lookups).
     *
     * <p>Filter is applied via {@code MarkerType.typeGroupStrings} —
     * the typed enum-Set on {@code MarkerType} is {@code @Transient}
     * and unqueryable, but the underlying join table behind
     * {@code typeGroupStrings} is fully mapped.
     *
     * <p>Bad group names quietly return {@code []} so the dropdown
     * stays silent rather than 4xx'ing mid-keystroke. Empty/null
     * {@code typeGroup} skips the filter entirely.
     */
    public List<AutocompleteItemDTO> searchMarkers(String term, String typeGroup) {
        if (term == null || term.isBlank()) {return List.of();}
        String groupString = null;
        if (typeGroup != null && !typeGroup.isBlank()) {
            try {
                groupString = Marker.TypeGroup.valueOf(typeGroup).name();
            } catch (IllegalArgumentException e) {
                return List.of();
            }
        }
        String hql = groupString == null
                ? "from Marker where lower(abbreviation) like :q order by abbreviationOrder"
                : "select m from Marker m join m.markerType.typeGroupStrings tg"
                        + " where lower(m.abbreviation) like :q and tg = :group"
                        + " order by m.abbreviationOrder";
        var query = HibernateUtil.currentSession()
                .createQuery(hql, Marker.class)
                .setParameter("q", "%" + term.toLowerCase() + "%");
        if (groupString != null) {
            query.setParameter("group", groupString);
        }
        return query.setMaxResults(MAX_RESULTS)
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
