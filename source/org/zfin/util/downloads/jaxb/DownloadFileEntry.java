package org.zfin.util.downloads.jaxb;

import org.biojava.utils.ObjectUtil;
import org.zfin.util.downloads.DownloadFileService;
import org.zfin.util.downloads.presentation.DownloadFileInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 */
@XmlRootElement(name = "Download")
@XmlType(propOrder = {"name", "category", "description", "wikiLink", "fileName", "fileExtension", "fileFormat", "query", "columnHeaderList"})
public class DownloadFileEntry {

    private String name;
    private String category;
    private String description;
    private String wikiLink;
    private String fileName;
    private String fileExtension;
    private String fileFormat;
    private String query;
    private boolean privateDownload;
    private int orderIndex;

    @XmlElement(name = "Column")
    private List<ColumnHeader> columnHeaderList;

    @XmlElement(name = "Name")
    @XmlJavaTypeAdapter(CleanupAdapter.class)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "Category")
    @XmlJavaTypeAdapter(CleanupAdapter.class)
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @XmlElement(name = "Description")
    @XmlJavaTypeAdapter(CleanupAdapter.class)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement(name = "WikiLink")
    @XmlJavaTypeAdapter(CleanupAdapter.class)
    public String getWikiLink() {
        return wikiLink;
    }

    public void setWikiLink(String wikiLink) {
        this.wikiLink = wikiLink;
    }

    @XmlElement(name = "FileName")
    @XmlJavaTypeAdapter(CleanupAdapter.class)
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @XmlElement(name = "FileExtension")
    @XmlJavaTypeAdapter(CleanupAdapter.class)
    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    @XmlElement(name = "FileFormat")
    @XmlJavaTypeAdapter(CleanupAdapter.class)
    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    @XmlElement(name = "Query")
    @XmlJavaTypeAdapter(CleanupAdapter.class)
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<ColumnHeader> getColumnHeaders() {
        return columnHeaderList;
    }

    public void setColumnHeaderList(List<ColumnHeader> columnHeaderList) {
        this.columnHeaderList = columnHeaderList;
    }

    @XmlAttribute(name = "private")
    public boolean isPrivateDownload() {
        return privateDownload;
    }

    public void setPrivateDownload(boolean privateDownload) {
        this.privateDownload = privateDownload;
    }

    @XmlAttribute(name = "orderIndex")
    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    @Override
    public String toString() {
        return "DownloadFileEntry{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", wikiLink='" + wikiLink + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileExtension='" + fileExtension + '\'' +
                ", query='" + query + '\'' +
                ", columnHeaderList=" + columnHeaderList +
                '}';
    }

    public String getFullFileName() {
        return fileName + "." + fileExtension;
    }

    public static int compare(DownloadFileEntry downloadFile, DownloadFileEntry downloadFile1, List<DownloadCategory> categories, List<DownloadFileInfo> downloadFileInfo) {
        DownloadCategory categoryOne = null;
        DownloadCategory categoryTwo = null;
        for (DownloadCategory category : categories) {
            if (category.getName().equalsIgnoreCase(downloadFile.getCategory()))
                categoryOne = category;
            if (category.getName().equalsIgnoreCase(downloadFile1.getCategory()))
                categoryTwo = category;
        }
        if (categoryOne == null)
            throw new RuntimeException("No category found in registry for " + downloadFile.getCategory());
        if (categoryTwo == null)
            throw new RuntimeException("No category found in registry for " + downloadFile1.getCategory());

        if (!categoryOne.getName().equals(categoryTwo.getName()))
            return categoryOne.getOrderIndex() - categoryTwo.getOrderIndex();

        DownloadFileInfo fileInfoOne = null;
        DownloadFileInfo fileInfoTwo = null;
        for (DownloadFileInfo fileInfo : downloadFileInfo) {
            if (fileInfo.getDownloadFile().getName().equalsIgnoreCase(downloadFile.getName()))
                fileInfoOne = fileInfo;
            if (fileInfo.getDownloadFile().getName().equalsIgnoreCase(downloadFile1.getName()))
                fileInfoTwo = fileInfo;
        }
        if (fileInfoOne == null)
            throw new RuntimeException("No download file found in registry for " + downloadFile.getName());
        if (fileInfoTwo == null)
            throw new RuntimeException("No category found in registry for " + downloadFile1.getName());

        return fileInfoOne.getDownloadFile().getOrderIndex() - fileInfoTwo.getDownloadFile().getOrderIndex();
    }

    public String getHeaderLine(String delimiter) {
        if (delimiter == null)
            return null;
        StringBuilder builder = new StringBuilder();
        for (ColumnHeader column : columnHeaderList) {
            builder.append(column.getColumn());
            builder.append(delimiter);
        }
        return builder.toString();
    }
}
