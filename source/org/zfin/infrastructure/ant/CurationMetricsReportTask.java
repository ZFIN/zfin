package org.zfin.infrastructure.ant;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CurationMetricsReportTask extends DataReportTask {

    public CurationMetricsReportTask(String jobName, String propertyFilePath, String dataDirectoryString) {
        super(jobName, propertyFilePath, dataDirectoryString);
    }

    @Override
    protected String handleBlankParameterValue() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DATE, -30);  // 30 days ago
        Calendar end = Calendar.getInstance();
        return dateFormat.format(start.getTime()) + "__" + dateFormat.format(end.getTime());
    }

}
