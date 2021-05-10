#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?
import org.hibernate.Session
import org.hibernate.criterion.Order
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil
import org.zfin.profile.Company
import org.zfin.profile.Lab
import org.zfin.profile.Person
import org.zfin.properties.ZfinProperties
import org.zfin.util.ReportGenerator

import java.util.concurrent.*

static void main(String... args) {
    cli = new CliBuilder(usage: 'LoadAddgene')
    cli.propertyFilePath(args: 1, 'Name of the job to be displayed in report')
    cli.dataDirectory(args: 2, 'Name of the job to be displayed in report')
    cli.jobName(args: 3, 'Name of the job to be displayed in report')
    cli.shortVersion(args: 4, 'Name of the job to be displayed in report')
    options = cli.parse(args)
    if (!options) {
        System.exit(1)
    }
    boolean shortVersion = options.shortVersion
    println options.propertyFilePath
    println options.dataDirectory
    println options.jobName

    ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
    new HibernateSessionCreator()

    Session session = HibernateUtil.currentSession()
    tx = session.beginTransaction()


    List<Lab> labList = new ArrayList<>()
    labList.addAll((List<Lab>) session.createCriteria(Lab.class)
            .addOrder(Order.asc("name"))
            .list())

    println "Total labs: " + labList.size()

    List<List<String>> brokenLabUrls = new ArrayList<>()
    int index = 0
    labList.any({ lab ->
        def url = lab.getUrl()
        if (url != null) {
            if (!isRespondingUrl(url)) {
                println url + " " + getResponseCode(url)
                List<String> values = new ArrayList<>()
                values.add(lab.getZdbID())
                values.add(lab.getName())
                values.add(url)
                brokenLabUrls.add(values)
            }
            index++
            if (shortVersion && index > 10)
                return true
            return
        }
    })

    List<List<String>> brokenPersonUrls = new ArrayList<>()
    List<Person> personList = new ArrayList<>()
    personList.addAll((List<Person>) session.createCriteria(Person.class)
            .addOrder(Order.asc("shortName"))
            .list())

    index = 0
    personList.any({ person ->

        def url = person.getUrl()
        if (url != null) {
            if (!isRespondingUrl(url)) {
                println url + " " + getResponseCode(url)
                List<String> values = new ArrayList<>()
                values.add(person.getZdbID())
                values.add(person.getShortName())
                values.add(url)
                brokenPersonUrls.add(values)
            }
            index++
            if (shortVersion && index > 10)
                return true

            return
        }
    })

    List<List<String>> brokenCompanyUrls = new ArrayList<>()
    List<Company> companyList = new ArrayList<>()
    companyList.addAll((List<Company>) session.createCriteria(Company.class)
            .addOrder(Order.asc("name"))
            .list())

    index = 0
    companyList.any({ company ->

        def url = company.getUrl()
        if (url != null) {
            if (!isRespondingUrl(url)) {
                println url + " " + getResponseCode(url)
                List<String> values = new ArrayList<>()
                values.add(company.getZdbID())
                values.add(company.getName())
                values.add(url)
                brokenCompanyUrls.add(values)
            }
            index++
            if (shortVersion && index > 10)
                return true

            return
        }
    })

    session.close()


    File reportDir = new File(options.dataDirectory + "/" + options.jobName)
    reportDir.deleteDir()
    reportDir.mkdirs()
    println "Generating report ... "
    ReportGenerator rg = new ReportGenerator()
    rg.setReportTitle("Report for $options.jobName")
    rg.includeTimestamp()
    Map<String, Integer> summary = new LinkedHashMap<>()
    summary.put("Lab", brokenLabUrls.size)
    summary.put("Company", brokenCompanyUrls.size)
    summary.put("Person", brokenPersonUrls.size)
    rg.addSummaryTable("Summary: Broken URL links (404)", summary)
    rg.addDataTable("Broken Lab URLs: ${brokenLabUrls.size()}", ["ID", "Name", "URL"], brokenLabUrls.collect { link -> [link.get(0), link.get(1), link.get(2)] })
    rg.addDataTable("Broken Person URLs: ${brokenPersonUrls.size()}", ["ID", "Name", "URL"], brokenPersonUrls.collect { link -> [link.get(0), link.get(1), link.get(2)] })
    rg.addDataTable("Broken Company URLs: ${brokenCompanyUrls.size()}", ["ID", "Name", "URL"], brokenCompanyUrls.collect { link -> [link.get(0), link.get(1), link.get(2)] })
    new File(reportDir, "check-urls-report.html").withWriter { writer ->
        rg.write(writer, ReportGenerator.Format.HTML)
    }


    if (brokenLabUrls.size() > 0 || brokenPersonUrls.size() > 0 || brokenCompanyUrls.size() > 0)
        println "Validation Errors found"

    System.exit(0)
}

static int getResponseCode(String urlString) throws MalformedURLException, IOException {
    URL u = new URL(urlString)
    HttpURLConnection huc = (HttpURLConnection) u.openConnection()
    huc.setRequestMethod("HEAD")
    ExecutorService executor = Executors.newSingleThreadExecutor()
    Future<Integer> future = executor.submit(new Task(huc))
    Integer responseCode = -1
    try {
        responseCode = future.get(3, TimeUnit.SECONDS)
    } catch (TimeoutException ignored) {
        return -1
    } catch (InterruptedException ignored) {
        return -1
    } catch (ExecutionException ignored) {
        return -1
    } finally {
        executor.shutdownNow()
    }
    return responseCode
}

static boolean isRespondingUrl(String url) {
    int responseCode = 404
    try {
        responseCode = getResponseCode(url)
    } catch (IOException ignored) {
        return false
    }

    List<Integer> brokenCodes = new ArrayList<>()
    brokenCodes.add(404)
    return !brokenCodes.contains(responseCode)
}

class Task implements Callable<Integer> {
    private HttpURLConnection huc

    Task(HttpURLConnection huc) {
        this.huc = huc
    }

    @Override
    public Integer call() throws Exception {
        huc.connect()
        return huc.getResponseCode()
    }
}


