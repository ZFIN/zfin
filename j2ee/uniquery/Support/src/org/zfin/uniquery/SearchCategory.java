
package org.zfin.uniquery;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


public class SearchCategory
    {
    // IMPORTANT! any changes to these categories means that both the IndexApp 
    // and the Spider.java must also change!
    public static final String MUTANTS_FISHVIEW         = "MUTANTS_FISHVIEW";
    public static final String MUTANTS_LOCUSVIEW        = "MUTANTS_LOCUSVIEW";
    public static final String MUTANTS_MAPPINGDETAIL    = "MUTANTS_MAPPINGDETAIL";
    public static final String GENES_MARKERVIEW         = "GENES_MARKERVIEW";
    public static final String GENES_SEQUENCE           = "GENES_SEQUENCE";
	public static final String GENES_GENEPRDDESCRIPTION = "GENES_GENEPRDDESCRIPTION";
    public static final String GENES_MARKERGOVIEW       = "GENES_MARKERGOVIEW"; 
    public static final String GENES_MAPPINGDETAIL      = "GENES_MAPPINGDETAIL";
    public static final String EXPRESSION_XPATVIEW      = "EXPRESSION_XPATVIEW";
    public static final String EXPRESSION_XPATINDEXVIEW = "EXPRESSION_XPATINDEXVIEW";
    public static final String ANATOMY_ITEM             = "ANATOMY_ITEM";
    public static final String ANATOMY_ZFINFO           = "ANATOMY_ZFINFO";
    public static final String PUBLICATIONS             = "PUBLICATIONS";
    public static final String PEOPLE_PERSVIEW          = "PEOPLE_PERSVIEW";
    public static final String PEOPLE_LABVIEW           = "PEOPLE_LABVIEW";
    public static final String NOMENCLATURE_LAB         = "NOMENCLATURE_LAB";
    public static final String NOMENCLATURE_NOMEN       = "NOMENCLATURE_NOMEN";
    public static final String ZEBRAFISH_BOOK           = "ZEBRAFISH_BOOK";
    public static final String MEETINGS                 = "MEETINGS";
    public static final String JOBS                     = "JOBS";
    public static final String OTHERS                   = "OTHERS";


    public static final Map CATEGORY_LOOKUP;
    public static final List CATEGORIES;
    static
        {
        HashMap categoryLookup = new HashMap();
        ArrayList categoryList = new ArrayList();
        
        String[] mutantTypes = {MUTANTS_FISHVIEW, MUTANTS_LOCUSVIEW, MUTANTS_MAPPINGDETAIL};
        categoryList.add(new SearchCategory("MUTANTS", "Mutants/Transgenics", mutantTypes));
        
        String[] geneTypes = {GENES_MARKERVIEW, GENES_SEQUENCE, GENES_GENEPRDDESCRIPTION, GENES_MARKERGOVIEW, GENES_MAPPINGDETAIL};
        categoryList.add(new SearchCategory("GENES", "Genes/Markers/Clones", geneTypes));
        
        String[] expressionTypes = {EXPRESSION_XPATVIEW, EXPRESSION_XPATINDEXVIEW};
        categoryList.add(new SearchCategory("EXPRESSION", "Expression", expressionTypes));

		String[] anatomyTypes = {ANATOMY_ITEM, ANATOMY_ZFINFO};
        categoryList.add(new SearchCategory("ANATOMY", "Anatomy", anatomyTypes));

		String[] publicationTypes = {PUBLICATIONS};
        categoryList.add(new SearchCategory("PUBLICATIONS", "Publications", publicationTypes));
		
        String[] peopleTypes = {PEOPLE_PERSVIEW, PEOPLE_LABVIEW};
        categoryList.add(new SearchCategory("PEOPLE", "People", peopleTypes));

        String[] zfbookTypes = {ZEBRAFISH_BOOK};
        categoryList.add(new SearchCategory("ZF_BOOK", "The Zebrafish Book", zfbookTypes));

        String[] nomenclatureTypes = {NOMENCLATURE_LAB, NOMENCLATURE_NOMEN};
        categoryList.add(new SearchCategory("NOMENCLATURE", "Nomenclature", nomenclatureTypes));

        String[] meetingTypes = {MEETINGS, JOBS};
        categoryList.add(new SearchCategory("MEETINGS", "Jobs/Meetings", meetingTypes));

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
