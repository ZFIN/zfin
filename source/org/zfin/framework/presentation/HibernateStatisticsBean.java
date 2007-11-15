package org.zfin.framework.presentation;

import org.hibernate.stat.Statistics;
import org.hibernate.stat.QueryStatistics;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Christian Pich
 * Date: Oct 9, 2006
 * Time: 1:42:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class HibernateStatisticsBean {

    private Statistics statistics;

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public Map<String, QueryStatistics> getCategoryQuery() {
        Map<String, QueryStatistics> map = new HashMap<String, QueryStatistics>();
        String[] queryNames = statistics.getQueries();
        if (queryNames == null)
            return map;

        for (String query : queryNames) {
            QueryStatistics stat = statistics.getQueryStatistics(query);
            map.put(query, stat);
        }
        return map;

    }


}
