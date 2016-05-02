package org.zfin

import org.apache.log4j.Logger
import org.hibernate.Session
import org.hibernate.criterion.Restrictions
import org.zfin.anatomy.DevelopmentStage
import org.zfin.expression.*
import org.zfin.figure.service.ImageService
import org.zfin.figure.service.VideoService
import org.zfin.framework.HibernateUtil
import org.zfin.marker.Marker
import org.zfin.mutant.Genotype
import org.zfin.mutant.FishExperiment
import org.zfin.ontology.Term
import org.zfin.ontology.datatransfer.AbstractScriptWrapper
import org.zfin.profile.Person
import org.zfin.publication.Publication
import org.zfin.repository.RepositoryFactory

import static com.xlson.groovycsv.CsvParser.parseCsv

Logger log = Logger.getLogger(getClass());

def env = System.getenv()

AbstractScriptWrapper abstractScriptWrapper = new AbstractScriptWrapper()
abstractScriptWrapper.initProperties("${env['TARGETROOT']}/home/WEB-INF/zfin.properties")
abstractScriptWrapper.initDatabaseWithoutSysmaster()
abstractScriptWrapper.initializeLogger("./log4j.xml")

Session session = HibernateUtil.currentSession()
session.beginTransaction()


Term unspecified = RepositoryFactory.ontologyRepository.getTermByOboID("ZFA:0001093")
//this map is used to map file name to figure, so that if it's a second expression result for a given figure,
// it can be looked up rather than re-created.  Also, the total number of figures created will be checked
// at the end as a confirmation that the script didn't add the same figure multiple times.
def videosAdded = [:]


//HibernateProfileRepository brings in ProfileService, which requires a validation library that
//needs to be excluded from the Classpath for this to load up.  Awkward, but easy enough to get a person this way...
Person owner = (Person) HibernateUtil.currentSession().createCriteria(Person.class)
        .add(Restrictions.eq("zdbID", "ZDB-PERS-000912-1"))  //Yvonne
        .uniqueResult();

def dorskyVideos = parseCsv(new FileReader("/research/zunloads/projects/Dorsky/DorskyVideo/dorsky-videos.csv"))
String mediaDir = "/research/zunloads/projects/Dorsky/DorskyVideo/media/"


def figureLabelIndex = [:]
dorskyVideos.each { csv ->

    println("""
----------------------------------------------------------------
            file:      $csv.file
            gene:      $csv.xpatex_gene_zdb_id
            genotype:  $csv.geno_zdb_id
            genox:     $csv.xpatex_genox_zdb_id
            assay:     $csv.xpatex_assay_name
            pub:       $csv.xpatex_source_zdb_id
            exp:       $csv.exp_zdb_id
            superterm: $csv.superterm_obo_id
            subterm:   $csv.subterm_obo_id
            stage:     $csv.stg_zdb_id

            """
    )

    Marker gene = RepositoryFactory.markerRepository.getMarkerByID(csv.xpatex_gene_zdb_id)
    Publication publication = RepositoryFactory.publicationRepository.getPublication(csv.xpatex_source_zdb_id)
    Genotype genotype = RepositoryFactory.mutantRepository.getGenotypeByID(csv.geno_zdb_id)
    Experiment experiment = RepositoryFactory.expressionRepository.getExperimentByID(csv.exp_zdb_id)
    FishExperiment genotypeExperiment = RepositoryFactory.mutantRepository.getGenotypeExperiment(csv.geno_zdb_id, csv.exp_zdb_id)

    //create a genox if we need one
    if (!genotypeExperiment) {
        genotypeExperiment = createGenotypeExperiment(genotype, experiment)
    }

    Term superTerm = RepositoryFactory.ontologyRepository.getTermByOboID(csv.superterm_obo_id)
    Term subTerm = RepositoryFactory.ontologyRepository.getTermByOboID(csv.subterm_obo_id)
    DevelopmentStage stage = RepositoryFactory.anatomyRepository.getStageByID(csv.stg_zdb_id)

    ExpressionAssay assay = RepositoryFactory.expressionRepository.getAssayByName(csv.xpatex_assay_name)

    //conditional, don't want to create entities again if they were already created...
    if (!videosAdded[csv.file]) {
        figureLabelIndex[publication.zdbID] = figureLabelIndex.get(publication.zdbID, 0) + 1;
        figure = createFigure(figureLabelIndex[publication.zdbID], publication)
        image = ImageService.processImage(figure,  mediaDir + csv.file.replace(".mov", "_still.png"), true, Image.NOT_SPECIFIED)
        videos = []
        videos << VideoService.processVideo(mediaDir + csv.file.replace(".mov", ".mp4"), image)
        videos << VideoService.processVideo(mediaDir + csv.file.replace(".mov", ".webm"), image)
        videosAdded.put(csv.file, figure.zdbID)
        HibernateUtil.currentSession().flush()
    } else {
        figure = RepositoryFactory.figureRepository.getFigure(videosAdded[csv.file])
        assert(figure)
        assert(figure.images)
        image = figure.images?.first()
        assert(image)
        videos = image.videos
    }

    ExpressionExperiment expressionExperiment = getOrCreateExpressionExperiment(genotypeExperiment, gene, publication, assay)
    ExpressionResult expressionResult = createExpressionResult(expressionExperiment, superTerm, subTerm, stage, figure)


    assert(gene)
    assert(publication)
    assert(genotype)
    assert(experiment)
    assert(genotypeExperiment)
    assert(genotypeExperiment.zdbID)
    assert(superTerm)
    //They should either both be not null or both be null
    assert((csv.subterm_obo_id && subTerm) || (!csv.subterm_obo_id && !subTerm))
    assert(stage)
    assert(assay)
    assert(figure)
    assert(figure.zdbID)
    assert(figure.expressionResults)
    assert(figure.expressionResults.contains(expressionResult))
    assert(image)
    assert(image.zdbID)
    assert(videos.size() == 2)
    videos.each { video -> assert(video.id) }
    assert(expressionExperiment)
    assert(expressionExperiment.zdbID)
    assert(expressionResult)
    assert(expressionResult.zdbID)

    if (csv.superterm_obo_id) {
        assert (csv.superterm_obo_id == expressionResult?.superTerm?.oboID)
    } else {
        assert (unspecified.oboID == expressionResult?.superTerm?.oboID)
    }




println """
----------------------------------------------------------------
"""
}

assert(videosAdded.values().size() == 31)


if ("--rollback" in args)
    session.getTransaction().rollback()
else
    session.getTransaction().commit()







ExpressionExperiment getOrCreateExpressionExperiment(FishExperiment genotypeExperiment,
                                                     Marker gene,
                                                     Publication publication,
                                                     ExpressionAssay assay) {
    List<ExpressionExperiment> expressionExperimentList = RepositoryFactory.expressionRepository.getExperimentsByGeneAndFish2(publication.zdbID,gene.zdbID,genotypeExperiment.genotype.zdbID)

    assert(expressionExperimentList.size() == 0 || expressionExperimentList.size() == 1)
    ExpressionExperiment expressionExperiment = null
    if (expressionExperimentList.size() == 1)
        expressionExperiment = expressionExperimentList.first()

    if (expressionExperimentList.size() == 0)
        expressionExperiment = createExpressionExperiment(genotypeExperiment, gene, publication, assay)

    return expressionExperiment
}


Figure createFigure(def figureLabelIndex, Publication publication) {
    Figure figure = new FigureFigure()
    figure.label = "Fig. M" + figureLabelIndex
    figure.publication = publication
    figure.caption = ""
    figure.comments = ""
    HibernateUtil.currentSession().save(figure)
    HibernateUtil.currentSession().flush()

    return figure
}

ExpressionResult createExpressionResult(ExpressionExperiment expressionExperiment,
                                        Term superTerm,
                                        Term subTerm,
                                        DevelopmentStage stage,
                                        Figure figure) {


    ExpressionResult expressionResult = new ExpressionResult()
    expressionResult.with {
        setExpressionExperiment(expressionExperiment)
        setSuperTerm(superTerm)
        setSubTerm(subTerm)
        setStartStage(stage)
        setEndStage(stage)
        setExpressionFound(true)
    }

    List<ExpressionResult> results = RepositoryFactory.expressionRepository.checkForExpressionResultRecord(expressionResult)
    if (results.size() == 1)
        expressionResult = results.get(0)

    HibernateUtil.currentSession().save(expressionResult)
    HibernateUtil.currentSession().flush()

    expressionResult.addFigure(figure)
    figure.expressionResults = [expressionResult]
    HibernateUtil.currentSession().flush()

    return expressionResult
}

FishExperiment createGenotypeExperiment(Genotype genotype, Experiment experiment) {
    FishExperiment genotypeExperiment = new FishExperiment()
    genotypeExperiment.experiment = experiment
    genotypeExperiment.genotype = genotype
    genotypeExperiment.standard = true
    genotypeExperiment.standardOrGenericControl = true
    HibernateUtil.currentSession().save(genotypeExperiment)
    HibernateUtil.currentSession().flush()

    return genotypeExperiment
}

ExpressionExperiment createExpressionExperiment(FishExperiment genotypeExperiment,
                                                Marker gene,
                                                Publication publication,
                                                ExpressionAssay assay) {
    ExpressionExperiment expressionExperiment = new ExpressionExperiment()
    expressionExperiment.setGene(gene)
    expressionExperiment.setFishExperiment(genotypeExperiment)
    expressionExperiment.setPublication(publication)
    expressionExperiment.setAssay(assay)

    HibernateUtil.currentSession().save(expressionExperiment)
    HibernateUtil.currentSession().flush()

    return expressionExperiment
}




