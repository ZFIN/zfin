import React, { useEffect, useState } from 'react';
import useWikiSearch from '../hooks/useWikiSearch';
import WikiList from '../components/WikiList';
import { parseDate } from '../utils';

const getDateLabel = (result) => {
    return result.metadata.labels.results
        .map(label => parseDate(label.name))
        .find(date => date !== undefined);
}

const WikiMeetingsFeed = () => {
    // this feed is different than the jobs and news feed. we need to fetch everything,
    // do filtering and sorting by label, and then fake the pagination
    const [limit, setLimit] = useState(5);
    const [filteredResults, setFilteredResults] = useState([]);

    const params = {
        cql: 'space=meetings AND type=page ORDER BY created desc',
        limit: 100,
        expand: 'metadata.labels',
    };

    const {
        pending,
        allResults,
    } = useWikiSearch(params);

    useEffect(() => {
        const today = new Date();
        const filteredResults = allResults
            .map(result => ({ result, date: getDateLabel(result) })) // augment with the meeting date pulled from the label
            .filter(withDate => withDate.date !== undefined)         // remove any without a label
            .filter(withDate => today - withDate.date < 24 * 60 * 60 * 1000) // remove any from before today
            .sort((a, b) => a.date - b.date) // sort by date, ascending
            .map(withDate => withDate.result)                       // un-augment
        setFilteredResults(filteredResults);
    }, [allResults]);

    return (
        <WikiList
            loading={pending}
            hasMore={limit < filteredResults.length}
            onLoadMore={() => setLimit(prev => prev + 5)}
            posts={filteredResults.slice(0, limit)}
            showAllUrl='/display/meetings'
            showDate={false}
        />
    )
}

export default WikiMeetingsFeed;
