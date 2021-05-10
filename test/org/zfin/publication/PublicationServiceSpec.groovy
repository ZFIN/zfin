package org.zfin.publication

import org.apache.bcel.Repository
import org.zfin.AbstractZfinIntegrationSpec
import org.zfin.AbstractZfinSpec
import org.zfin.profile.Person
import org.zfin.publication.presentation.PublicationService
import org.zfin.repository.RepositoryFactory
import spock.lang.Shared
import spock.lang.Unroll


class PublicationServiceSpec  extends AbstractZfinIntegrationSpec{
    @Shared PublicationService publicationService;

    public static String ZON = "ZDB-PERS-960805-635"
    public static String KHOWE = "ZDB-PERS-001130-2"
    public static String CKIM = "ZDB-PERS-970429-23"

    public def setupSpec() {
        publicationService = new PublicationService();
    }

    public def cleanupSpec() {
        publicationService = null
    }


    @Unroll
    def "#authorString has #authorCount authors"() {
        when:
            def authorList = publicationService.splitAuthorListString(authorString)
        then:
            authorList
            authorCount == authorList.size()
        where:
        authorCount | authorString
        11          | "Pinto, C., Grimaldi, M., Boulahtouf, A., Pakdel, F., Brion, F., Aït-Aïssa, S., Cavaillès, V., Bourguet, W., Gustafsson, J.A., Bondesson, M., Balaguer, P."
        1           | "Xu, Q."
        4           | "Rebagliati, M.R., Toyama, R., Haffter, P., Dawid, I.B."
        9           | "Murray, K.N., Dreska, M., Nasiadka, A., Rinne, M., Matthews, J.L., Carmichael, C., Bauer, J., Varga, Z.M., and Westerfield, M."
     }

    @Unroll
    def "last author name should never include ' and '"() {
        when:
            def authorList = publicationService.splitAuthorListString(authorString)
            def lastAuthor = authorList.last()
        then:
            !lastAuthor.contains(" and ")
        where: // the underscores are a way of having a single column data table for Spock
        authorString | _
        "Pinto, C., Grimaldi, M., Boulahtouf, A., Pakdel, F., Brion, F., Aït-Aïssa, S., Cavaillès, V., Bourguet, W., Gustafsson, J.A., Bondesson, M., Balaguer, P." | _
        "Xu, Q." | _
        "Rebagliati, M.R., Toyama, R., Haffter, P., Dawid, I.B." | _
        "Murray, K.N., Dreska, M., Nasiadka, A., Rinne, M., Matthews, J.L., Carmichael, C., Bauer, J., Varga, Z.M., and Westerfield, M." | _
    }

    @Unroll
    def "#authorString should bring back #author"() {
        when:
            Person author = RepositoryFactory.profileRepository.getPerson(personZdbId)
            def authorList = publicationService.getAuthorSuggestions(authorString)
        then:
            authorList.contains(author)
        where:
        authorString      | personZdbId
        "Zon, L.I."       | ZON
        "Zon, L."         | ZON
        "Howe, K."        | KHOWE
        "Kim, C.H."       | CKIM

    }

}
