package org.zfin.framework.presentation;

import java.util.List;

/**
 * Form bean being used for classpath inspection.
 */
public class ClasspathInfoBean {

    private List<ClassLibraryWrapper> bootLibraries;
    private List<ClassLibraryWrapper> extensionLibraries;
    private List<ClassLibraryWrapper> applicationLibraries;
    private List<ClassLibraryWrapper> classesLibraries;
    private String className;
    private String fullClassName;
    private String classFileName;
    private String type;
    private String errorMessage;
    private String classLoaderName;
    private List<ClassLibraryWrapper> classLoaderParents;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassFileName() {
        return classFileName;
    }

    public void setClassFileName(String classFileName) {
        this.classFileName = classFileName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<ClassLibraryWrapper> getBootLibraries() {
        return bootLibraries;
    }

    public void setBootLibraries(List<ClassLibraryWrapper> bootLibraries) {
        this.bootLibraries = bootLibraries;
    }

    public List<ClassLibraryWrapper> getExtensionLibraries() {
        return extensionLibraries;
    }

    public void setExtensionLibraries(List<ClassLibraryWrapper> extensionLibraries) {
        this.extensionLibraries = extensionLibraries;
    }

    public List<ClassLibraryWrapper> getApplicationLibraries() {
        return applicationLibraries;
    }

    public void setApplicationLibraries(List<ClassLibraryWrapper> applicationLibraries) {
        this.applicationLibraries = applicationLibraries;
    }

    public List<ClassLibraryWrapper> getClassesLibraries() {
        return classesLibraries;
    }

    public void setClassesLibraries(List<ClassLibraryWrapper> classesLibraries) {
        this.classesLibraries = classesLibraries;
    }

    public boolean isInquireClassName(){
        if(type != null && type.equalsIgnoreCase("submit"))
            return true;
        else
            return false;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getFullClassName() {
        return fullClassName;
    }

    public void setFullClassName(String fullClassName) {
        this.fullClassName = fullClassName;
    }

    public String getClassLoaderName() {
        return classLoaderName;
    }

    public void setClassLoaderName(String classLoaderName) {
        this.classLoaderName = classLoaderName;
    }

    public List<ClassLibraryWrapper> getClassLoaderParents() {
        return classLoaderParents;
    }

    public void setClassLoaderParents(List<ClassLibraryWrapper> classLoaderParents) {
        this.classLoaderParents = classLoaderParents;
    }
}
