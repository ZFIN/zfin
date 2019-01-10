#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.zfin.profile.*;
import org.apache.log4j.Logger
import org.hibernate.Session
import org.zfin.anatomy.DevelopmentStage
import org.zfin.expression.*
import org.zfin.figure.service.ImageService
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil
import org.zfin.marker.Marker
import org.zfin.mutant.Genotype
import org.zfin.mutant.Fish
import org.zfin.mutant.FishExperiment
import org.zfin.ontology.Ontology
import org.zfin.ontology.Term
import org.zfin.properties.ZfinProperties
import org.zfin.publication.Publication
import org.zfin.repository.RepositoryFactory
import static com.xlson.groovycsv.CsvParser.parseCsv
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

Logger log = Logger.getLogger(getClass());

def env = System.getenv()

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

new HibernateSessionCreator()
Session session = HibernateUtil.currentSession()
session.beginTransaction()
login("ZDB-PERS-030520-2")


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

def burgessImages = parseCsv(new FileReader("/research/zunloads/projects/HBurgess/legend-hburgess.txt"), separator: '\t')
//def burgessImages = parseCsv(new FileReader("/research/zusers/pm/Projects/releases/HBurgess/234.txt"),separator: '\t')
String mediaDir = "/research/zunloads/projects/HBurgess/images3/"


figureLabelIndex = 0
burgessImages.each { csv ->

    println("""
----------------------------------------------------------------
            file:      $csv.file
            
            gene:      $csv.xpatexgene
            ftr1:   $csv.ftr1
            ftr2:   $csv.ftr2
            superterm: $csv.superterm
            subterm: $csv.subterm
            stage:     $csv.stagestart
            orientation: $csv.orientation
            figLegend: $csv.figlegend


            """
    )

    Marker gene = RepositoryFactory.markerRepository.getMarkerByName(csv.xpatexgene)
    Publication publication = RepositoryFactory.publicationRepository.getPublication("ZDB-PUB-151008-10")

    //  Fish burgessFish=RepositoryFactory.mutantRepository.getFish(csv.fishid)
    genoStr = csv.ftr2 + "; " + csv.ftr1

    if (csv.ftr2=="-") {
        genoStr = csv.ftr1
    }
    print genoStr
    Genotype burgessGeno = RepositoryFactory.mutantRepository.getGenotypeByName(genoStr)
    if (burgessGeno == null) {
        List<Genotype> bGenos = RepositoryFactory.mutantRepository.getGenotypesByFeature(RepositoryFactory.featureRepository.getFeatureByAbbreviation(csv.ftr1))
        for (Genotype item : bGenos) {
            if (item.zdbID.contains("ZDB-GENO-151216")||item.zdbID.contains("ZDB-GENO-1901")) {

                print(item.name)
                burgessGeno = item
            }
        }
    }

    /*if (burgessGeno==null){
        genoStr=csv.ftr2+"; "+ csv.ftr1
        print genoStr

        Feature feature = RepositoryFactory.getFeatureRepository().getFeatureByAbbreviation(csv.ftr1);
        Publication pub = getPublicationRepository().getPublication("ZDB-PUB-151205-4");
        Genotype background = getMutantRepository().getGenotypeByID("ZDB-GENO-990623-2");
        burgessGeno=RepositoryFactory.mutantRepository.getGenotypesByFeatureAndBackground(feature,background,pub).first()
    }*/
    Fish burgessFish=new Fish()
    List<Fish> bFish = RepositoryFactory.mutantRepository.getFishByGenotype(burgessGeno)
    for (Fish fishItem : bFish ){
        if (!(fishItem.name.contains("MO"))){
            burgessFish=fishItem
        }

    }
    print burgessFish.zdbID

    Experiment experiment = RepositoryFactory.expressionRepository.getExperimentByID("ZDB-EXP-041102-1")
    FishExperiment fishExperiment = RepositoryFactory.mutantRepository.getFishExperimentByFishAndExperimentID(burgessFish.getZdbID(), experiment.getZdbID())

    //create a genox if we need one
    if (!fishExperiment) {
        fishExperiment = createFishExperiment(burgessFish, experiment)
    }

    Term superTerm = RepositoryFactory.ontologyRepository.getTermByName(csv.superterm, Ontology.ANATOMY)

    DevelopmentStage stage = RepositoryFactory.anatomyRepository.getStageByID("ZDB-STAGE-010723-35")


    ExpressionAssay assay = RepositoryFactory.expressionRepository.getAssayByName("Intrinsic fluorescence")

    //conditional, don't want to create entities again if they were already created...
    figCaption = "";
    figPrefix = "Fig."

    if (!sameFigure[csv.figlegend]) {
        figureLabelIndex++;
        figure = createFigure(figureLabelIndex, publication, superTerm, figPrefix, csv.figlegend)
        sameFigure.put(csv.figlegend, figure.zdbID)
    }
    if (!videosAdded[csv.file]) {
            Figure figure = RepositoryFactory.figureRepository.getFigure(sameFigure[csv.figlegend])
            image = ImageService.processImage(figure, mediaDir + csv.file.replace(".jpg", ".jpg"), false, csv.orientation)
            HibernateUtil.currentSession().flush()
            videosAdded.put(csv.file, image.zdbID)
            print figure.zdbID
            print image.zdbID
     }

        ExpressionExperiment2 expressionExperiment = getOrCreateExpressionExperiment(fishExperiment, gene, publication, assay)
//        ExpressionResult2 expressionResult = createExpressionResult(expressionExperiment, superTerm, csv.subterm, stage, figure)

        ExpressionResult2 expressionResult = createExpressionResult(expressionExperiment, superTerm, csv.subterm,stage, figure)

        assert (gene)
        assert (publication)
        assert (experiment)
        assert (fishExperiment)
        assert (fishExperiment.zdbID)
        assert (superTerm)
        assert (stage)
        assert (assay)
        assert (figure)
        assert (figure.zdbID)
        assert (expressionResult.ID)
        assert (image)
        assert (image.zdbID)

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
        List<ExpressionExperiment2> expressionExperimentList = RepositoryFactory.expressionRepository.getExperimentsByGeneAndFish(publication.zdbID, gene.zdbID, genotypeExperiment.fish.zdbID)

        assert (expressionExperimentList.size() == 0 || expressionExperimentList.size() == 1)
        ExpressionExperiment2 expressionExperiment = null
        if (expressionExperimentList.size() == 1)
            expressionExperiment = expressionExperimentList.first()

        if (expressionExperimentList.size() == 0)
            expressionExperiment = createExpressionExperiment(genotypeExperiment, gene, publication, assay)

        return expressionExperiment
    }


    Figure createFigure(figureLabelIndex, Publication publication, Term superterm, String prefix, String caption) {
        Figure figure = new FigureFigure()

        figure.label = prefix + figureLabelIndex
        figure.publication = publication
        figure.caption = caption
        figure.comments = ""
        HibernateUtil.currentSession().save(figure)
        HibernateUtil.currentSession().flush()
        return figure
    }

   /* ExpressionResult2 createExpressionResult(ExpressionExperiment2 expressionExperiment,
                                             Term superTerm, String subterm,
                                             DevelopmentStage stage,
                                             Figure figure) {*/
        ExpressionResult2 createExpressionResult(ExpressionExperiment2 expressionExperiment,
                                                 Term superTerm, String subTermStr,
                                                 DevelopmentStage stage,
                                                 Figure figure) {


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
                if (subTermStr!=''){
                    Term subTerm = RepositoryFactory.ontologyRepository.getTermByName(subTermStr, Ontology.ANATOMY)
                    print(subTerm.getZdbID())
                    setSubTerm(subTerm)

                }







                setExpressionFigureStage(expFigStage1)
                setExpressionFound(true)
            }

            List<ExpressionResult2> results = RepositoryFactory.expressionRepository.checkForExpressionResultRecord2(expressionResult)
            if (results.size() == 1)
                expressionResult = results.get(0)

            HibernateUtil.currentSession().save(expressionResult)
            // HibernateUtil.currentSession().flush()


            return expressionResult
        } else {

            ExpressionResult2 expressionResult = new ExpressionResult2()
            expressionResult.with {

                setSuperTerm(superTerm)
                if (subTermStr!=''){
                    Term subTerm = RepositoryFactory.ontologyRepository.getTermByName(subTermStr, Ontology.ANATOMY)
                    print(subTerm.getZdbID())
                    setSubTerm(subTerm)


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


public void login(String userID)
{

        Authentication auth = new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                Person person = RepositoryFactory.getProfileRepository().getPerson(userID);
                return person;
            }

            @Override
            public boolean isAuthenticated() {
                return false;
            }

            @Override
            public void setAuthenticated(boolean b) throws IllegalArgumentException {

            }

            @Override
            public String getName() {
                return null;
            }
        };

    SecurityContext securityContext = SecurityContextHolder.getContext();
    securityContext.setAuthentication(auth);
}


