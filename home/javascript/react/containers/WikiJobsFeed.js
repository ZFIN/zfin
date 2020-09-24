import React from 'react';
import useWikiSearch from '../hooks/useWikiSearch';
import WikiList from '../components/WikiList';

const WikiJobsFeed = () => {
    const params = {
        cql: 'space=jobs AND type=blogpost AND created >= now("-120d") ORDER BY created desc',
        limit: 5,
        expand: 'history',
    };

    const {
        pending,
        allResults,
        hasMore,
        fetchMore,
    } = useWikiSearch(params);

    return (
        <WikiList
            loading={pending}
            hasMore={hasMore}
            onLoadMore={fetchMore}
            posts={allResults}
            showAllUrl='/display/jobs/Zebrafish-Related+Job+Announcements'
        />
    )
}

export default WikiJobsFeed;
