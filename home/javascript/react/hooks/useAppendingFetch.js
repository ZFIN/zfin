import { useEffect, useState } from 'react';
import qs from 'qs';
import produce, {setAutoFreeze} from 'immer';
setAutoFreeze(false);

import http from '../utils/http';
import { DEFAULT_FETCH_STATE } from './constants';

export default function useAppendingFetch(baseUrl, page, setPage) {
    const isMounted = {};
    const [data, setData] = useState(DEFAULT_FETCH_STATE);

    const fetchImages = (page, successHandler) => {
        if (!baseUrl) {
            return;
        }
        isMounted.current = true;
        const separator = baseUrl.indexOf('?') < 0 ? '?' : '&';
        const url = baseUrl + separator + qs.stringify({ page });
        setData(produce(data => {
            data.pending = true;
            data.rejected = false;
            data.fulfilled = false;
            if (data.value && page === 1) {
                data.value.results = [];
            }
        }));
        http.get(url)
            .then(successHandler)
            .fail(error => {
                if (!isMounted.current) {
                    return;
                }
                setData(produce(data => {
                    data.fulfilled = false;
                    data.rejected = true;
                    data.reason = error;
                }))
            })
            .always(() => {
                if (!isMounted.current) {
                    return;
                }
                setData(produce(data => {
                    data.pending = false;
                }));
            });
        return () => { isMounted.current = false; };
    };

    useEffect(() => {
        setPage(1);
        return fetchImages(1, response => {
            if (!isMounted.current) {
                return;
            }
            setData(produce(data => {
                data.fulfilled = true;
                data.rejected = false;
                data.value = response;
                data.reason = null;
            }));
        })
    }, [baseUrl]);

    useEffect(() => {
        if (page === 1) {
            return;
        }
        return fetchImages(page, response => {
            if (!isMounted.current) {
                return
            }
            const previousResults = data.value.results;
            setData(produce(data => {
                data.fulfilled = true;
                data.rejected = false;
                data.value = response;
                data.reason = null;
                if (previousResults) {
                    data.value.results = previousResults.concat(response.results);
                }
            }))
        })
    }, [page]);

    return data;
}
