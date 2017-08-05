#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import groovy.json.JsonSlurper
import org.hibernate.Session
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil
import org.zfin.Species
import org.zfin.infrastructure.RecordAttribution
import org.zfin.properties.ZfinProperties
import org.zfin.repository.RepositoryFactory
import org.zfin.sequence.ForeignDB
import org.zfin.sequence.ForeignDBDataType
import org.zfin.sequence.MarkerDBLink
import org.zfin.sequence.ReferenceDatabase
import org.zfin.util.ReportGenerator
import org.zfin.profile.*


import java.util.concurrent.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;


import org.apache.commons.validator.UrlValidator

import java.util.zip.GZIPInputStream
import java.util.zip.ZipException

 static void main(String... args) {   
cli = new CliBuilder(usage: 'LoadAddgene')
cli.propertyFilePath(args: 1, 'Name of the job to be displayed in report')
cli.dataDirectory(args: 2, 'Name of the job to be displayed in report')
cli.jobName(args: 3, 'Name of the job to be displayed in report')
options = cli.parse(args)
if (!options) {
    System.exit(1)
}
println options.propertyFilePath
println options.dataDirectory
println options.jobName

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
new HibernateSessionCreator()

Session session = HibernateUtil.currentSession()
tx = session.beginTransaction()


        List<Lab> labList = new ArrayList<>();
        labList.addAll((List<Lab>) session.createCriteria(Lab.class)
                .list());

print labList.size() + " labs";

int index = 0;
List<String> brokenUrls = new ArrayList<>();
labList.any({lab ->
    
    def url = lab.getUrl()
    if (url != null){
    if(!isRespondingUrl(url)){
        println url + " "+ getResponseCode(url);
	List<String> values = new ArrayList<>();
	values.add(lab.getZdbID())
	values.add(lab.getName())
	values.add(url)
	brokenUrls.add(values);
    }
	index++;
//	if(index++ > 10)
//	return true;

	return;
}
})

println index + " Labs have broken links"
index = 0;

        List<Person> personList = new ArrayList<>();
        personList.addAll((List<Person>) session.createCriteria(Person.class)
                .list());

personList.any({person ->

    def url = person.getUrl()
    if (url != null){
    if(!isRespondingUrl(url)){
        println url + " "+ getResponseCode(url);
        List<String> values = new ArrayList<>();
        values.add(person.getZdbID())
        values.add(person.getShortName())
        values.add(url)
        brokenUrls.add(values);
    }
//   	if(index++ > 10)
//      return true;

        return;
}
})

println index + " Persons have broken links"
index = 0;

       List<Company> companyList = new ArrayList<>();
        companyList.addAll((List<Company>) session.createCriteria(Company.class)
               	.list());

companyList.any({company ->

    def url = company.getUrl()
    if (url != null){
    if(!isRespondingUrl(url)){
        println url + " "+ getResponseCode(url);
        List<String> values = new ArrayList<>();
        values.add(company.getZdbID())
        values.add(company.getName())
        values.add(url)
       	brokenUrls.add(values);
    }
//      if(index++ > 10)
//      return true;

        return;
}
})

println index + " Companies have broken links"

println brokenUrls.size()
session.close()


	File reportDir = new File(options.dataDirectory+"/"+options.jobName)
	reportDir.deleteDir()
        reportDir.mkdirs()
    print "Generating report ... "
    ReportGenerator rg = new ReportGenerator();
    rg.setReportTitle("Report for $options.jobName")
    rg.includeTimestamp();
    rg.addIntroParagraph("The following URLs are broken:")
    rg.addDataTable("${brokenUrls.size()} Lab / Person / Copany records with broken URLs", ["ID", "Name", "URL"], brokenUrls.collect { link -> [link.get(0), link.get(1), link.get(2)] })
    new File(reportDir,"check-urls-report.html").withWriter { writer ->
        rg.write(writer, ReportGenerator.Format.HTML)
    }


println "done"
if(brokenUrls.size() > 0)
	println "Validation Errors found"

System.exit(0)
}

    public static int getResponseCode(String urlString) throws MalformedURLException, IOException {
        URL u = new URL(urlString);
        HttpURLConnection huc =  (HttpURLConnection)  u.openConnection();
        huc.setRequestMethod("GET");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Integer> future = executor.submit(new Task(huc));
        Integer responseCode;
        try {
            responseCode = future.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return -1;
        } catch (InterruptedException e) {
            return -1;
        } catch (ExecutionException e) {
            return -1;
        } finally {
            executor.shutdownNow();
        }
        return responseCode;
    }

    public static boolean isRespondingUrl(String url){
	int responseCode = 404;
        try {
            responseCode = getResponseCode(url);
        } catch (IOException e) {
            return false;
        }

	List<Integer> okCodes = new ArrayList<>();
	okCodes.add(200);
	okCodes.add(301);
	okCodes.add(303);
//	if(!okCodes.contains(responseCode))
//		println url + ", "+ responseCode;
        return okCodes.contains(responseCode);
    }

class Task implements Callable<Integer> {
    private HttpURLConnection huc;

    Task(HttpURLConnection huc) {
        this.huc = huc;
    }

    @Override
    public Integer call() throws Exception {
        huc.connect();
        return huc.getResponseCode();
    }
}


