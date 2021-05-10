package org.zfin.util.downloads.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Download registry.
 */
@XmlRootElement(namespace = "org.zfin.util.download.jaxb")
@XmlType(propOrder = {"downloadDefinition", "categoryDefinition"})
public class DownloadFileRegistry {

    // XmlElement sets the name of the entities

    @XmlElement(name = "DownloadCategories")
    private CategoryDefinition categoryDefinition;

    @XmlElement(name = "DownloadDefinitions")
    private DownloadDefinition downloadDefinition;

    public DownloadDefinition getDownloadDefinitions() {
        return downloadDefinition;
    }

    public void setDownloadDefinition(DownloadDefinition downloadDefinition) {
        this.downloadDefinition = downloadDefinition;
    }

    public List<DownloadFileEntry> getDownloadFileEntryList() {
        return downloadDefinition.getDownloadFileEntryList();
    }

    public List<DownloadCategory> getDownloadCategoryList() {
        return categoryDefinition.getDownloadCategoryList();
    }

    public void setCategoryDefinition(CategoryDefinition categoryDefinition) {
        this.categoryDefinition = categoryDefinition;
    }

    public DownloadFileEntry getDownloadFileEntryByName(String fileName) {
        for (DownloadFileEntry entry : downloadDefinition.getDownloadFileEntryList()) {
            if (entry.getFullFileName().equals(fileName))
                return entry;
        }
        return null;
    }

    public static class DownloadDefinition {
        @XmlElement(name = "Download")
        private List<DownloadFileEntry> downloadFileEntries;

        public List<DownloadFileEntry> getDownloadFileEntryList() {
            return downloadFileEntries;
        }

        public void setDownloadFileEntries(List<DownloadFileEntry> downloadFileEntries) {
            this.downloadFileEntries = downloadFileEntries;
        }
    }

    public static class CategoryDefinition {
        @XmlElement(name = "Category")
        private List<DownloadCategory> downloadCategories;

        public List<DownloadCategory> getDownloadCategoryList() {
            return downloadCategories;
        }

        public void setDownloadCategories(List<DownloadCategory> downloadCategories) {
            this.downloadCategories = downloadCategories;
        }

        public DownloadCategory getCategoryDefinitionByName(String categoryName) {
            for(DownloadCategory category: downloadCategories)
                if(category.getName().equalsIgnoreCase(categoryName))
                    return category;
            return null;
        }
    }
}

