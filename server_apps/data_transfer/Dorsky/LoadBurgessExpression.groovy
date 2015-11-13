import org.apache.log4j.Logger
import org.hibernate.Query
import org.hibernate.Session
import org.hibernate.criterion.Restrictions
import org.zfin.anatomy.DevelopmentStage
import org.zfin.expression.*
import org.zfin.figure.service.ImageService
import org.zfin.figure.service.VideoService
import org.zfin.framework.HibernateUtil
import org.zfin.marker.Marker
import org.zfin.mutant.Genotype
import org.zfin.mutant.Fish
import org.zfin.mutant.FishExperiment
import org.zfin.ontology.Ontology
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
        .add(Restrictions.eq("zdbID", "ZDB-PERS-030612-1"))  //Yvonne
        .uniqueResult();
//Person owner=RepositoryFactory.profileRepository.getPerson("ZDB-PERS-030520-2")

def dorskyVideos = parseCsv(new FileReader("/research/zusers/pm/Projects/releases/HBurgess/burgessExpression.csv"))
String mediaDir = "/research/zusers/pm/Projects/releases/HBurgess/images/"


figureLabelIndex = 1
dorskyVideos.each { csv ->

    println("""
----------------------------------------------------------------
            file:      $csv.file
            gene:      $csv.xpatexgene
            ftr1:   $csv.ftr1
            ftr2:   $csv.ftr2
            superterm: $csv.superterm
            stage:     $csv.stagestart
            orientation: $csv.orientation
            figLegend: $csv.figlegend
geno: $csv.ftr1 ; $csv.ftr2(TL)

            """
    )

    Marker gene = RepositoryFactory.markerRepository.getMarkerByName(csv.xpatexgene)
    Publication publication = RepositoryFactory.publicationRepository.getPublication("ZDB-PUB-141007-8")
    Genotype genotype = RepositoryFactory.mutantRepository.getGenotypeByID("ZDB-GENO-151112-6")

    Fish burgessFish=getFishByGeno(genotype)
    Experiment experiment = RepositoryFactory.expressionRepository.getExperimentByID("ZDB-EXP-041102-1")
    FishExperiment fishExperiment = RepositoryFactory.mutantRepository.getFishExperimentByFishAndExperimentID(burgessFish.getZdbID(),experiment.getZdbID() )


    //create a genox if we need one
    if (!fishExperiment) {
        fishExperiment = createFishExperiment(burgessFish, experiment)
    }

    Term superTerm = RepositoryFactory.ontologyRepository.getTermByName(csv.superterm,Ontology.ANATOMY)
    if (superTerm==null){
        superTerm=RepositoryFactory.ontologyRepository.getTermByName(csv.superterm,Ontology.ANATOMY)
    }
    DevelopmentStage stage = RepositoryFactory.anatomyRepository.getStageByID("ZDB-STAGE-010723-35")


    ExpressionAssay assay = RepositoryFactory.expressionRepository.getAssayByName("Intrinsic fluorescence")


    //conditional, don't want to create entities again if they were already created...
    if (!videosAdded[csv.file]) {
        figCaption="";
//        figureLabelIndex[publication.zdbID] = figureLabelIndex.get(publication.zdbID, 0) + 1;
        figureLabelIndex ++;
        if (!(csv.file.contains("mov")||(csv.file.contains("mp4")))) {
            figPrefix="Fig."
        }
        else{
            figPrefix="Fig. M"
        }
        figure = createFigure(figureLabelIndex, publication,superTerm,figPrefix)

            image = ImageService.processImage(figure, owner,mediaDir + csv.file.replace(".jpg", ".jpg"), false,orientation)
            videosAdded.put(csv.file, figure.zdbID)


        HibernateUtil.currentSession().flush()
}    

    ExpressionExperiment expressionExperiment = getOrCreateExpressionExperiment(fishExperiment, gene, publication, assay)
    ExpressionResult expressionResult = createExpressionResult(expressionExperiment, superTerm, stage, figure)


    assert(gene)
    assert(publication)
    assert(genotype)
    assert(experiment)
    assert(genotypeExperiment)
    assert(genotypeExperiment.zdbID)
    assert(superTerm)
    //They should either both be not null or both be null
    //assert((csv.subterm_obo_id && subTerm) || (!csv.subterm_obo_id && !subTerm))

    assert(stage)
    assert(assay)
    assert(figure)
    assert(figure.zdbID)
    assert(figure.expressionResults)
    assert(figure.expressionResults.contains(expressionResult))
    assert(image)
    assert(image.zdbID)
    /*assert(videos.size() == 2)
    videos.each { video -> assert(video.id) }*/
    assert(expressionExperiment)
    assert(expressionExperiment.zdbID)
    assert(expressionResult)
    assert(expressionResult.zdbID)





println """
----------------------------------------------------------------
"""
}

//assert(videosAdded.values().size() == 31)


if ("--rollback" in args)
    session.getTransaction().rollback()
else
    session.getTransaction().commit()







ExpressionExperiment getOrCreateExpressionExperiment(FishExperiment genotypeExperiment,
                                                     Marker gene,
                                                     Publication publication,
                                                     ExpressionAssay assay) {
    List<ExpressionExperiment> expressionExperimentList = RepositoryFactory.expressionRepository.getExperimentsByGeneAndFish2(publication.zdbID,gene.zdbID,genotypeExperiment.fish.zdbID)

    assert(expressionExperimentList.size() == 0 || expressionExperimentList.size() == 1)
    ExpressionExperiment expressionExperiment = null
    if (expressionExperimentList.size() == 1)
        expressionExperiment = expressionExperimentList.first()

    if (expressionExperimentList.size() == 0)
        expressionExperiment = createExpressionExperiment(genotypeExperiment, gene, publication, assay)

    return expressionExperiment
}


Figure createFigure(figureLabelIndex, Publication publication, Term superterm, String prefix) {
    Figure figure = new FigureFigure()
    figCaption=figCaption+"" + superterm.termName
    figure.label = prefix + figureLabelIndex
    figure.publication = publication
    figure.caption = "test"
    figure.comments = ""
    HibernateUtil.currentSession().save(figure)
    HibernateUtil.currentSession().flush()

    return figure
}

ExpressionResult createExpressionResult(ExpressionExperiment expressionExperiment,
                                        Term superTerm,

                                        DevelopmentStage stage,
                                        Figure figure) {


    ExpressionResult expressionResult = new ExpressionResult()
    expressionResult.with {
        setExpressionExperiment(expressionExperiment)
        setSuperTerm(superTerm)
       // setSubTerm(subTerm)
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

FishExperiment createFishExperiment(Fish fish, Experiment experiment) {
    FishExperiment genotypeExperiment = new FishExperiment()
    genotypeExperiment.experiment = experiment
    genotypeExperiment.fish = fish
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

Fish getFishByGeno(Genotype genotype){

        String hql = "select fish from Fish as fish " +
                "where fish.genotype = :genotype";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("genotype", genotype);
    return (Fish) query.uniqueResult();
    }


DevelopmentStage getStageByStart(float stage) {
    if (stage == null)
        return null;

    return (DevelopmentStage) session.get(DevelopmentStage.class, stage);
}




