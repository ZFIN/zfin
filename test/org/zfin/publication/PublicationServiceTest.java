package org.zfin.publication;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.profile.Person;
import org.zfin.publication.presentation.PublicationService;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.zfin.repository.RepositoryFactory.getProfileRepository;

public class PublicationServiceTest extends AbstractDatabaseTest {

    @Test
    public void testAuthorSuggestionWithAccents() {
        List<String> queries = List.of("Ozhan, Gunes", "Ozhan, G", "Ozha, G", "Ozhan", "Ozha");
        List<String> accentedQueries = List.of("Özhan, Günes", "Özhan, G", "Özha, G", "Özhan", "Özha");

        Person author = getProfileRepository().getPerson("ZDB-PERS-100120-1");
        PublicationService publicationService = new PublicationService();

        List<Person> authorList;
        for(String query : queries) {
            authorList = publicationService.getAuthorSuggestions(query);
            assertNotNull(authorList);
            assertTrue(authorList.size() > 0);
            assertTrue(authorList.contains(author));
        }

        for(String query : accentedQueries) {
            authorList = publicationService.getAuthorSuggestions(query);
            assertNotNull(authorList);
            assertTrue(authorList.size() > 0);
            assertTrue(authorList.contains(author));
        }

    }
}

