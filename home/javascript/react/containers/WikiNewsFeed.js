import React from 'react';
import useWikiSearch from '../hooks/useWikiSearch';
import WikiList from '../components/WikiList';

const WikiNewsFeed = () => {
    const params = {
        cql: 'space=news AND type=blogpost ORDER BY created desc',
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
            showAllUrl='/display/news'
        />
    )
}

export default WikiNewsFeed;
