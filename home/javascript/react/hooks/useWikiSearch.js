import { useState, useEffect } from 'react';
import useFetch from './useFetch';
import qs from 'qs';

export default function useWikiSearch(params = {}) {
    const initialUrl = `https://${process.env.WIKI_HOST}/rest/api/content/search?${qs.stringify(params)}`;
    const [currentUrl, setCurrentUrl] = useState(initialUrl);
    const [allResults, setAllResults] = useState([]);

    const query = useFetch(currentUrl);
    useEffect(() => {
        if (!query.value) {
            return;
        }
        setAllResults(prev => prev.concat(query.value.results));
    }, [query.value]);

    return {
        ...query,
        allResults,
        hasMore: Boolean(query.value && query.value._links.next),
        fetchMore: () => {
            if (!query.value || !query.value._links.next) {
                return;
            }
            setCurrentUrl(`https://${process.env.WIKI_HOST}${query.value._links.next}`);
        }
    }
}