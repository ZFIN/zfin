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
import org.zfin.feature.Feature
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
Term subTerm1=RepositoryFactory.ontologyRepository.getTermByName("neural rod",Ontology.ANATOMY)
//this map is used to map file name to figure, so that if it's a second expression result for a given figure,
// it can be looked up rather than re-created.  Also, the total number of figures created will be checked
// at the end as a confirmation that the script didn't add the same figure multiple times.
def videosAdded = [:]
def sameFigure = [:]


//HibernateProfileRepository brings in ProfileService, which requires a validation library that
//needs to be excluded from the Classpath for this to load up.  Awkward, but easy enough to get a person this way...
/*Person owner = (Person) HibernateUtil.currentSession().createCriteria(Person.class)
        .add(Restrictions.eq("zdbID", "ZDB-PERS-030612-1"))  //Yvonne
        .uniqueResult();*/
//Person owner=RepositoryFactory.profileRepository.getPerson("ZDB-PERS-030520-2")

def burgessImages = parseCsv(new FileReader("/research/zusers/pm/Projects/releases/HBurgess/hburgessexp23Feb.txt"),separator: '\t')
//def burgessImages = parseCsv(new FileReader("/research/zusers/pm/Projects/releases/HBurgess/234.txt"),separator: '\t')
String mediaDir = "/research/zusers/pm/Projects/releases/HBurgess/images2/"


figureLabelIndex = 0
burgessImages.each { csv ->

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
geno: $csv.ftr2; $csv.ftr1(TL)

            """
    )

    Marker gene = RepositoryFactory.markerRepository.getMarkerByName(csv.xpatexgene)
    Publication publication = RepositoryFactory.publicationRepository.getPublication("ZDB-PUB-151008-10")


  //  Fish burgessFish=RepositoryFactory.mutantRepository.getFish(csv.fishid)
    genoStr=csv.ftr2+"; "+ csv.ftr1
    print genoStr

    Genotype burgessGeno=RepositoryFactory.mutantRepository.getGenotypeByName(genoStr)
    if (burgessGeno==null){
        burgessGeno=RepositoryFactory.mutantRepository.getGenotypesByFeature(RepositoryFactory.featureRepository.getFeatureByAbbreviation(csv.ftr1)).first()
    }
    Fish burgessFish=RepositoryFactory.mutantRepository.getFishByGenotype(burgessGeno).first()
    print burgessFish.zdbID

    Experiment experiment = RepositoryFactory.expressionRepository.getExperimentByID("ZDB-EXP-041102-1")
    FishExperiment fishExperiment = RepositoryFactory.mutantRepository.getFishExperimentByFishAndExperimentID(burgessFish.getZdbID(),experiment.getZdbID() )


    //create a genox if we need one
    if (!fishExperiment) {
        fishExperiment = createFishExperiment(burgessFish, experiment)
    }

    Term superTerm = RepositoryFactory.ontologyRepository.getTermByName(csv.superterm,Ontology.ANATOMY)

    if (csv.subterm.length()!=0) {
        print "yes"
        List<Ontology> ontologies = new ArrayList<Ontology>(2);
        ontologies.add(Ontology.QUALITY);
                ontologies.add(Ontology.ANATOMY);
        Term subTerm = RepositoryFactory.ontologyRepository.getTermByName(csv.subterm, ontologies)
    }




    DevelopmentStage stage = RepositoryFactory.anatomyRepository.getStageByID("ZDB-STAGE-010723-35")


    ExpressionAssay assay = RepositoryFactory.expressionRepository.getAssayByName("Intrinsic fluorescence")


    //conditional, don't want to create entities again if they were already created...
    if (!videosAdded[csv.file]) {
        figCaption="";
//        figureLabelIndex[publication.zdbID] = figureLabelIndex.get(publication.zdbID, 0) + 1;

        if (!(csv.file.contains("mov")||(csv.file.contains("mp4")))) {
            figPrefix="Fig."
        }
        else{
            figPrefix="Fig. M"
        }
        if (!sameFigure[csv.figlegend]) {
            figureLabelIndex ++;
            figure = createFigure(figureLabelIndex, publication, superTerm, figPrefix,csv.figlegend)
            sameFigure.put(csv.figlegend, figure.zdbID)
        }
            //image = ImageService.processImage(figure, owner,mediaDir + csv.file.replace(".jpg", ".jpg"), false)
        image = ImageService.processImage(figure, mediaDir + csv.file.replace(".jpg", ".jpg"), false,csv.orientation)

            videosAdded.put(csv.file, figure.zdbID)


        HibernateUtil.currentSession().flush()
}
   /* ExpressionExperiment2 expressionExperiment = getOrCreateExpressionExperiment(fishExperiment, gene, publication, assay)
    ExpressionResult2 expressionResult = createExpressionResult(expressionExperiment, superTerm,stage, figure)*/
  /*  ExpressionExperiment2 expressionExperiment = getOrCreateExpressionExperiment(fishExperiment, gene, publication, assay)
    ExpressionResult2 expressionResult = createExpressionResult(expressionExperiment, superTerm,stage, figure)*/

    ExpressionExperiment2 expressionExperiment = getOrCreateExpressionExperiment(fishExperiment, gene, publication, assay)
    ExpressionResult2 expressionResult = createExpressionResult(expressionExperiment, superTerm,csv.subterm,stage, figure)

    assert(gene)
    assert(publication)
    assert(experiment)
    assert(fishExperiment)
    assert(fishExperiment.zdbID)
    assert(superTerm)
 //   assert(subTerm)
    //They should either both be not null or both be null
    //assert((csv.subterm_obo_id && subTerm) || (!csv.subterm_obo_id && !subTerm))

    assert(stage)
    assert(assay)
    assert(figure)
    assert(figure.zdbID)
    //assert(expressionResult.ID)
   /* assert(figure.expressionResults)
    assert(figure.expressionResults.contains(expressionResult))*/
    assert(image)
    assert(image.zdbID)
    /*assert(videos.size() == 2)
    videos.each { video -> assert(video.id) }*/
   /* assert(expressionExperiment)
    assert(expressionExperiment.zdbID)
    assert(expressionResult)
    assert(expressionResult.ID)*/





println """
----------------------------------------------------------------
"""
}

//assert(videosAdded.values().size() == 31)


if ("--rollback" in args)
    session.getTransaction().rollback()
else
    session.getTransaction().commit()







ExpressionExperiment2 getOrCreateExpressionExperiment(FishExperiment genotypeExperiment,
                                                     Marker gene,
                                                     Publication publication,
                                                     ExpressionAssay assay) {
    List<ExpressionExperiment2> expressionExperimentList = RepositoryFactory.expressionRepository.getExperimentsByGeneAndFish(publication.zdbID,gene.zdbID,genotypeExperiment.fish.zdbID)

    assert(expressionExperimentList.size() == 0 || expressionExperimentList.size() == 1)
    ExpressionExperiment2 expressionExperiment = null
    if (expressionExperimentList.size() == 1)
        expressionExperiment = expressionExperimentList.first()

    if (expressionExperimentList.size() == 0)
        expressionExperiment = createExpressionExperiment(genotypeExperiment, gene, publication, assay)

    return expressionExperiment
}


Figure createFigure(figureLabelIndex, Publication publication, Term superterm, String prefix,String caption) {
    Figure figure = new FigureFigure()

    figure.label = prefix + figureLabelIndex
    figure.publication = publication
    figure.caption = caption
    figure.comments = ""
    HibernateUtil.currentSession().save(figure)
  //  HibernateUtil.currentSession().flush()

    return figure
}

ExpressionResult2 createExpressionResult(ExpressionExperiment2 expressionExperiment,
                                        Term superTerm,
        String subterm,
                                        DevelopmentStage stage,
                                        Figure figure) {

    // HibernateUtil.currentSession().flush()
    ExpressionFigureStage expFigStage = RepositoryFactory.expressionRepository.getExperimentFigureStage(expressionExperiment.zdbID, figure.zdbID, stage.zdbID, stage.zdbID)

    if (expFigStage == null) {

        ExpressionFigureStage expFigStage1 = new ExpressionFigureStage()
        expFigStage1.with {
            setExpressionExperiment(expressionExperiment)
            setStartStage(stage)
            setEndStage(stage)
        }


        expFigStage1.setFigure(figure)
        HibernateUtil.currentSession().save(expFigStage1)


        ExpressionResult2 expressionResult = new ExpressionResult2()
        expressionResult.with {

            setSuperTerm(superTerm)

            setExpressionFigureStage(expFigStage1)
            setExpressionFound(true)
        }

        List<ExpressionResult2> results = RepositoryFactory.expressionRepository.checkForExpressionResultRecord2(expressionResult)
        if (results.size() == 1)
            expressionResult = results.get(0)

        HibernateUtil.currentSession().save(expressionResult)
        // HibernateUtil.currentSession().flush()


        return expressionResult
    }
        else{

            ExpressionResult2 expressionResult = new ExpressionResult2()
            expressionResult.with {

                setSuperTerm(superTerm)
                if (superTerm.termName=="sensory system"){
                    print "subterm"
                    setSubTerm(RepositoryFactory.ontologyRepository.getTermByName("cranial ganglion",Ontology.ANATOMY))
                }
                if (subterm=="anterior region"){
                    print "ant"
                    setSubTerm(RepositoryFactory.ontologyRepository.getTermByOboID("BSPO:0000071"))
                }
                setExpressionFigureStage(expFigStage)
                setExpressionFound(true)
            }

            List<ExpressionResult2> results = RepositoryFactory.expressionRepository.checkForExpressionResultRecord2(expressionResult)
            if (results.size() == 1)
                expressionResult = results.get(0)

            HibernateUtil.currentSession().save(expressionResult)
            // HibernateUtil.currentSession().flush()



            return expressionResult

        }



    }







FishExperiment createFishExperiment(Fish fish, Experiment experiment) {
    FishExperiment genotypeExperiment = new FishExperiment()
    genotypeExperiment.experiment = experiment
    genotypeExperiment.fish = fish
    genotypeExperiment.standard = true
    genotypeExperiment.standardOrGenericControl = true
    HibernateUtil.currentSession().save(genotypeExperiment)
//   HibernateUtil.currentSession().flush()

    return genotypeExperiment
}

ExpressionExperiment2 createExpressionExperiment(FishExperiment genotypeExperiment,
                                                Marker gene,
                                                Publication publication,
                                                ExpressionAssay assay) {
    ExpressionExperiment2 expressionExperiment = new ExpressionExperiment2()
    expressionExperiment.setGene(gene)
    expressionExperiment.setFishExperiment(genotypeExperiment)
    expressionExperiment.setPublication(publication)
    expressionExperiment.setAssay(assay)
    HibernateUtil.currentSession().save(expressionExperiment)
  //  HibernateUtil.currentSession().flush()

    return expressionExperiment
}



DevelopmentStage getStageByStart(float stage) {
    if (stage == null)
        return null;

    return (DevelopmentStage) session.get(DevelopmentStage.class, stage);
}




