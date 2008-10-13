package org.zfin.uniquery;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 *  SearchCategory
 *
 *  The list of categories used by the Quick Search tool.
 *
 *  If modified, remember to also update Indexer.java, and SearchBean.java
 *  since they contain category-specific code.
 *
 *  ORDER IS IMPORTANT!  (See comment below regarding categoryList order.)
 */
public class SearchCategory
    {
    // IMPORTANT! any changes to these categories means that Indexer.java and SearchBean.java must also change!
	// Tomcat needs to be bounced for the chagnes to show.
    public static final String MUTANTS_GENOVIEW        = "MUTANTS_GENOVIEW";
	//public static final String MUTANTS_MAPPINGDETAIL    = "MUTANTS_MAPPINGDETAIL";
    public static final String GENES_MARKERVIEW         = "GENES_MARKERVIEW";
    public static final String MARKERS_MARKERVIEW       = "MARKERS_MARKERVIEW";
    public static final String GENES_SEQUENCE           = "GENES_SEQUENCE";
    public static final String GENES_GENEPRDDESCRIPTION = "GENES_GENEPRDDESCRIPTION";
    public static final String GENES_MARKERGOVIEW       = "GENES_MARKERGOVIEW";
	//public static final String GENES_MAPPINGDETAIL      = "GENES_MAPPINGDETAIL";
    public static final String EXPRESSION_FXFIGVIEW     = "EXPRESSION_FXFIGVIEW";
    public static final String ANATOMY_ITEM             = "ANATOMY_ITEM";
    public static final String ANATOMY_ZFINFO           = "ANATOMY_ZFINFO";
    public static final String IMAGES                   = "IMAGES";
	//public static final String PUBLICATIONS             = "PUBLICATIONS";
    public static final String PEOPLE_PERSVIEW          = "PEOPLE_PERSVIEW";
    public static final String PEOPLE_LABVIEW           = "PEOPLE_LABVIEW";
    public static final String NOMENCLATURE_LAB         = "NOMENCLATURE_LAB";
    public static final String NOMENCLATURE_NOMEN       = "NOMENCLATURE_NOMEN";
    public static final String ZEBRAFISH_BOOK           = "ZEBRAFISH_BOOK";
    public static final String MEETINGS                 = "MEETINGS";
    public static final String JOBS                     = "JOBS";
    public static final String OTHERS                   = "OTHERS";
    public static final String ALL                      = "ALL";
    public static final String ANTIBODY                 = "ANTIBODY";


    public static final Map CATEGORY_LOOKUP;
    public static final List<SearchCategory> CATEGORIES;
    static
        {
        HashMap categoryLookup = new HashMap();
        List<SearchCategory> categoryList = new ArrayList<SearchCategory>();


        /*
         *  ORDER IS IMPORTANT!  Doug Howe spent a lot of time determining the order
         *  of these categories.  They have been hard coded according to the order
         *  he specified.
         */
        String[] allTypes = {ALL};
        categoryList.add(new SearchCategory("ALL", "All", allTypes));

        String[] geneTypes = {GENES_MARKERVIEW, MARKERS_MARKERVIEW};
        categoryList.add(new SearchCategory("GENES", "Genes/Markers/Clones", geneTypes));

	String[] mutantTypes = {MUTANTS_GENOVIEW};
	categoryList.add(new SearchCategory("MUTANTS", "Mutants/Transgenics", mutantTypes));

        String[] expressionTypes = {EXPRESSION_FXFIGVIEW};
        categoryList.add(new SearchCategory("EXPRESSION", "Expression/Phenotype", expressionTypes));

        /*
         * Remove the image and mapping details categories since they
         * have redundant data that doesn't make sense having a separate category
         * for.  Images generally are under Expression, and mapping details data
         * can be found on other, more useful pages.
         */
        //String[] imageTypes = {IMAGES};
        //categoryList.add(new SearchCategory("IMAGES", "Images", imageTypes));

        //String[] mappingTypes = {GENES_MAPPINGDETAIL, MUTANTS_MAPPINGDETAIL};
        //categoryList.add(new SearchCategory("MAPPING", "Mapping Data", mappingTypes));

        String[] sequenceTypes = {GENES_SEQUENCE};
        categoryList.add(new SearchCategory("SEQUENCE", "Sequence Information", sequenceTypes));

        String[] anatomyTypes = {ANATOMY_ITEM, ANATOMY_ZFINFO};
        categoryList.add(new SearchCategory("ANATOMY", "Anatomy", anatomyTypes));

        String[] antibodies = {ANTIBODY};
        categoryList.add(new SearchCategory("ANTIBODY", "Antibody", antibodies));

        String[] productTypes = {GENES_GENEPRDDESCRIPTION};
        categoryList.add(new SearchCategory("PRODUCT", "Gene Product", productTypes));

        String[] ontologyTypes = {GENES_MARKERGOVIEW};
        categoryList.add(new SearchCategory("ONTOLOGY", "Gene Ontology", ontologyTypes));

        String[] zfbookTypes = {ZEBRAFISH_BOOK};
        categoryList.add(new SearchCategory("ZF_BOOK", "The Zebrafish Book", zfbookTypes));

        String[] nomenclatureTypes = {NOMENCLATURE_LAB, NOMENCLATURE_NOMEN};
        categoryList.add(new SearchCategory("NOMENCLATURE", "Nomenclature", nomenclatureTypes));

        String[] meetingTypes = {MEETINGS, JOBS};
        categoryList.add(new SearchCategory("MEETINGS", "Jobs/Meetings", meetingTypes));

        //String[] publicationTypes = {PUBLICATIONS};
        //categoryList.add(new SearchCategory("PUBLICATIONS", "Publications", publicationTypes));

        String[] peopleTypes = {PEOPLE_PERSVIEW, PEOPLE_LABVIEW};
        categoryList.add(new SearchCategory("PEOPLE", "People", peopleTypes));

        String[] otherTypes = {OTHERS};
        categoryList.add(new SearchCategory("OTHER", "Other", otherTypes));

        for (int i=0; i<categoryList.size(); i++)
            {
            SearchCategory category = (SearchCategory) categoryList.get(i);
            categoryLookup.put(category.getId(), category);
            }

        CATEGORY_LOOKUP = Collections.unmodifiableMap(categoryLookup);
        CATEGORIES = Collections.unmodifiableList(categoryList);
        }




    private String id = null;
    private String description = null;
    private String[] types;

    public SearchCategory(String id, String description, String[] types)
        {
        this.id = id;
        this.description = description;
        this.types = types;
        }

    public static SearchCategory getCategoryById(String id) {
        return (SearchCategory) CATEGORY_LOOKUP.get(id);
    }

    public static SearchCategory getCategoryByIndex(int index) {
        return (SearchCategory) CATEGORIES.get(index);
    }


    public static String getDescriptionById(String id) {
        return getCategoryById(id).getDescription();
    }

    public static String getDescriptionByIndex(int index) {
        return getCategoryByIndex(index).getDescription();
    }

    public static String getIdByIndex(int index) {
        return getCategoryByIndex(index).getId();
    }

    public String getId()
        {
        return id;
        }


    public String getDescription()
        {
        return description;
        }


    public String[] getTypes()
        {
        return types;
        }

    }
