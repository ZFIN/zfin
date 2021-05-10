package org.zfin.figure.repository
import org.zfin.AbstractZfinIntegrationSpec
import org.zfin.expression.Figure
import org.zfin.figure.FigureData
import org.zfin.marker.Clone
import org.zfin.profile.Person
import org.zfin.publication.Publication
import org.zfin.repository.RepositoryFactory
import spock.lang.Ignore
import spock.lang.Shared

class FigureRepositorySpec extends AbstractZfinIntegrationSpec {



    //Figures to test, named for why they're interesting
    @Shared
    def figures = FigureData.figures


    def "should be able to get figure by ID"() {
        when: "the figure is fetched from the database"
        Figure figure = RepositoryFactory.figureRepository.getFigure("ZDB-FIG-090127-36")

        then: "it's not null"
        figure
    }


    def "#figZdbID should have phenotype data"() {
        when: "we get a figure with phenotype data"
        Figure figure = RepositoryFactory.figureRepository.getFigure("ZDB-FIG-080325-45")

        then: "it should have phenotype experiments, and those experiments should have fish, stages and at least one phenotype statement"
        figure.getPhenotypeExperiments().size() > 0
        figure.getPhenotypeExperiments().first().fishExperiment
        figure.getPhenotypeExperiments().first().startStage
        figure.getPhenotypeExperiments().first().endStage
        figure.getPhenotypeExperiments().first().phenotypeStatements.size() > 0
        figure.getPhenotypeExperiments().first().phenotypeStatements.first().entity
        figure.getPhenotypeExperiments().first().phenotypeStatements.first().entity.superterm
    }


    def "#figZdbID should have the correct submitter list"() {
        when: "get the figure submitter list"
        Figure figure = RepositoryFactory.figureRepository.getFigure(figZdbID)
        Clone probe = null
        if (probeZdbID)
            probe = RepositoryFactory.markerRepository.getCloneById(probeZdbID)
        List<Person> submitters = RepositoryFactory.figureRepository.getSubmitters(figure.publication, probe)

        then: "it should have the correct values, in the right order"
        submitterFullNames == submitters*.fullName

        where: "Regular pub has no submitters, Thisse pub has 3, in that particular order"
        submitterFullNames                                         | figZdbID                         | probeZdbID
        []                                                         | figures.hasNoData                | null
        ["Degrave, Agnes", "Thisse, Bernard", "Thisse, Christine"] | figures.directSubmissionThisse   | "ZDB-EST-031204-27"

    }

    def "DirectSubmissionPublication should have the correct number of figures"() {
        when: "get the figure list using a given publication and probe"
        Publication publication = RepositoryFactory.publicationRepository.getPublication("ZDB-PUB-010810-1")
        Clone probe = RepositoryFactory.markerRepository.getCloneById("ZDB-EST-010914-90")
        List<Figure> figures = RepositoryFactory.figureRepository.getFiguresForDirectSubmissionPublication(publication, probe)

        then: "it should have the correct number of figures in ascending order"
        figures.size() == 7;
        ["Fig. 1", "Fig. 2", "Fig. 3", "Fig. 4", "Fig. 5", "Fig. 6", "Fig. 7"]== figures*.label
    }

}


