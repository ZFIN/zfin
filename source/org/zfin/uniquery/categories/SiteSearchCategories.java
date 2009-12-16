package org.zfin.uniquery.categories;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.uniquery.SearchCategory;
import org.zfin.uniquery.UrlPattern;
import org.zfin.uniquery.ZfinAnalyzer;
import org.zfin.util.FileUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that contains global properties for the application, such as
 * email addresses, colors, etc.
 */
public final class SiteSearchCategories {

    public static final String JAXB_PACKAGE = "org.zfin.uniquery.categories";
    public static final String CONFIGURATION_DIRECTORY = "conf";

    private static Categories props;
    private static File propertyFile;

    private static final Logger LOG = Logger.getLogger(SiteSearchCategories.class);
    public static final String ZFIN_DEFAULT_CATEGORIES_XML = "site-search-categories.xml";
    public static final String CATALINA_BASE = System.getProperty("catalina.base");
    public static final String OTHER = "Others";

    public static List<SearchCategory> allCategories;
    public static List<SearchCategory> categoriesWithoutAllAndOthers;
    public static List<SearchCategory> wikiCategories = new ArrayList<SearchCategory>();
    public static Map<String, SearchCategory> categoryMap;
    private static final String WIKI_PREFIX = "WIKI";


    /**
     * Initialize properties via property file import.
     *
     * @param properties onfiguration data from the zfin-properties.xml file
     */
    public static void init(Categories properties) {
        props = properties;
    }

    /**
     * Pass in the path to the property file.
     *
     * @param dir      Directory where property file exists.
     * @param propFile The name of  the property file.
     */
    public static void init(String dir, String propFile) {
        propertyFile = FileUtil.createFileFromDirAndName(dir, propFile);
        if (!propertyFile.exists()) {
            LOG.info("Site Search Category file " + propertyFile.getAbsolutePath() + " not found. Use default file.");
            propertyFile = FileUtil.createFileFromDirAndName(dir, ZFIN_DEFAULT_CATEGORIES_XML);
            if (!propertyFile.exists()) {
                String message = "No default Site Search Category file " + propertyFile.getAbsolutePath() + " found!";
                LOG.error(message);
                throw new RuntimeException(message);
            }
        }
        LOG.info("Site Search Category file being used: " + propertyFile.getAbsolutePath());

        try {
            JAXBContext jc = JAXBContext.newInstance(JAXB_PACKAGE);

            // create an Unmarshaller
            Unmarshaller u = jc.createUnmarshaller();
            u.setValidating(true);
            if (props != null) {
                LOG.info("Called more than once");
                //ToDO: find out why on the server (embryonix this is called twice)
                //throw new RuntimeException("ZfinProperties class already initialized! This can only be done once ");
            } else {
                props = (Categories) u.unmarshal(new FileInputStream(propertyFile));
            }
        } catch (Throwable e) {
            LOG.error("Error in initializing the Site Search Category File", e);
            throw new RuntimeException(e);
        }

    }

    /**
     * Retrieve the poperty file.
     *
     * @return File handle
     */
    public static File getZfinPropertyFile() {
        return propertyFile;
    }

    // if initialization happens not via applicationProperties include
    // that logic in here.

    private static void checkValidProperties() {
        if (props == null)
            throw new RuntimeException("Properties are not yet initialized");
    }

    public static List<SearchCategory> getAllSearchCategories() {
        if (allCategories != null)
            return allCategories;

        checkValidProperties();
        List<SearchCategory> categories = new ArrayList<SearchCategory>();
        @SuppressWarnings("unchecked")
        List<CategoryType> list = props.getCategory();
        for (CategoryType category : list) {
            @SuppressWarnings("unchecked")
            List<UrlMappingType.UrlPattern> urls = (List<UrlMappingType.UrlPattern>) category.getUrlMapping().getUrlPattern();
            List<org.zfin.uniquery.UrlPattern> patterns = new ArrayList<org.zfin.uniquery.UrlPattern>();
            for (UrlMappingType.UrlPattern patternInternal : urls) {
                UrlPattern pattern = new UrlPattern();
                UrlPatternType urlPattern = patternInternal.getUrlPattern();
                pattern.setPattern(urlPattern.getValue());
                pattern.setType(urlPattern.getType());
                if (urlPattern.getBoostValue() != null)
                    pattern.setBoostValue(urlPattern.getBoostValue().abs().intValue());
                if (urlPattern.getTitlePrefix() != null)
                    pattern.setTitlePrefix(urlPattern.getTitlePrefix());
                patterns.add(pattern);
            }
            SearchCategory cat = new SearchCategory(category.getID(), category.getDisplayName(), patterns);
            categories.add(cat);
        }
        allCategories = Collections.unmodifiableList(categories);
        checkForStopWordsOverlap(allCategories);
        return categories;
    }

    private static void checkForStopWordsOverlap(List<SearchCategory> categories) {
        if (categories == null || categories.isEmpty())
            return;

        List<String> list = new ArrayList<String>(Arrays.asList(ZfinAnalyzer.STOP_WORDS));
        for (SearchCategory category : categories) {
            List<UrlPattern> types = category.getUrlPatterns();
            if (types != null) {
                for (UrlPattern urlPattern : types) {
                    String type = urlPattern.getType();
                    if (list.contains(type.toLowerCase()) && !type.equalsIgnoreCase("ALL"))
                        LOG.error("The type <" + type + "> is a stop word and thus is not useful. Check the file " +
                                propertyFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Excluding 'All' and 'Other'.
     *
     * @return list of search categories
     */
    public static List<SearchCategory> getSearchCategories() {
        if (categoriesWithoutAllAndOthers != null)
            return categoriesWithoutAllAndOthers;
        checkValidProperties();
        List<SearchCategory> categories = new ArrayList<SearchCategory>();
        @SuppressWarnings("unchecked")
        List<CategoryType> list = props.getCategory();
        for (CategoryType category : list) {
            @SuppressWarnings("unchecked")
            List<UrlMappingType.UrlPattern> urls = (List<UrlMappingType.UrlPattern>) category.getUrlMapping().getUrlPattern();
            List<org.zfin.uniquery.UrlPattern> patterns = new ArrayList<org.zfin.uniquery.UrlPattern>();
            for (UrlMappingType.UrlPattern patternInternal : urls) {
                UrlPattern pattern = new UrlPattern();
                String urlPatternString = patternInternal.getUrlPattern().getValue();
                if (urlPatternString != null) {
                    Pattern regExpPattern = Pattern.compile("\\$\\{\\w*\\}");
                    Matcher matcher = regExpPattern.matcher(urlPatternString);
                    if (matcher.find()) {
                        String matchedString = matcher.group();
                        matchedString = matchedString.substring(2, matchedString.length() - 1);
                        String environmentVariable = System.getProperty(matchedString);
                        // try an environment variable with the same name.
                        if (StringUtils.isEmpty(environmentVariable))
                            environmentVariable = System.getenv(matchedString);

                        LOG.info("Environment variable for " + matchedString + " found: " + environmentVariable);
                        if (StringUtils.isEmpty(environmentVariable))
                            LOG.error("No environment variable for " + matchedString + " found");
                        else
                            urlPatternString = matcher.replaceAll(environmentVariable);
                    }
                }
                pattern.setPattern(urlPatternString);
                pattern.setType(patternInternal.getUrlPattern().getType());
                if (patternInternal.getUrlPattern() != null && patternInternal.getUrlPattern().getBoostValue() != null)
                    pattern.setBoostValue(patternInternal.getUrlPattern().getBoostValue().abs().intValue());
                if (patternInternal.getUrlPattern() != null && patternInternal.getUrlPattern().getTitlePrefix() != null)
                    pattern.setTitlePrefix(patternInternal.getUrlPattern().getTitlePrefix());
                patterns.add(pattern);
            }
            String id = category.getID();
            if (!(id.equals("ALL") || id.equals(OTHER))) {
                SearchCategory cat = new SearchCategory(id, category.getDisplayName(), patterns);
                categories.add(cat);
            }
        }
        categoriesWithoutAllAndOthers = Collections.unmodifiableList(categories);
        checkForStopWordsOverlap(categoriesWithoutAllAndOthers);
        return categoriesWithoutAllAndOthers;
    }

    public static Map<String, SearchCategory> getCategoryMap() {
        if (categoryMap != null)
            return categoryMap;

        Map<String, SearchCategory> categoryLookup = new HashMap<String, SearchCategory>();
        for (SearchCategory category : getAllSearchCategories()) {
            categoryLookup.put(category.getId(), category);
        }
        categoryMap = Collections.unmodifiableMap(categoryLookup);
        return categoryLookup;
    }

    /*
    *  Based on the URL and data page naming conventions,
    *  we assign the SearchCategory (or DocType) accordingly.
    */

    public static String getDocType(String url) {
        if (categoriesWithoutAllAndOthers == null)
            getSearchCategories();

        if (categoriesWithoutAllAndOthers == null)
            throw new NullPointerException("No categories found.");

        for (SearchCategory category : categoriesWithoutAllAndOthers) {
            List<UrlPattern> urls = category.getUrlPatterns();
            for (UrlPattern urlString : urls) {
                Pattern pattern = Pattern.compile(urlString.getPattern());
                Matcher matcher = pattern.matcher(url);
                if (matcher.find())
                    return urlString.getType();
            }
        }
        return SiteSearchCategories.OTHER;
    }

    public static SearchCategory getCategoryById(String id) {
        return getCategoryMap().get(id);
    }

    public static String getDisplayName(String id) {
        return getCategoryById(id).getDisplayName();
    }

    public static List<SearchCategory> getWikiCategories() {
        if (wikiCategories.size() > 0)
            return wikiCategories;
        if (categoriesWithoutAllAndOthers == null)
            getSearchCategories();

        for (SearchCategory category : categoriesWithoutAllAndOthers) {
            if (category.getId().startsWith(WIKI_PREFIX))
                wikiCategories.add(category);
        }
        return wikiCategories;
    }
}