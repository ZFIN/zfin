import {useEffect, useState} from 'react';
import produce from 'immer';
import http from './http';

export const useFetch = (url) => {
    const [data, setData] = useState({
        pending: false,
        rejected: false,
        reason: null,
        fulfilled: false,
        value: null,
    });
    const [request, setRequest] = useState(null);

    useEffect(() => {
        setData(produce(data => {
            data.pending = true;
            data.rejected = false;
            data.fulfilled = false;
        }));
        if (request) {
            request.abort();
        }
        const xhr = http.get(url);
        setRequest(xhr);
        xhr.then(result => setData(produce(data => {
            data.fulfilled = true;
            data.rejected = false;
            data.value = result;
            data.reason = null;
        }))).fail(error => {
            if (error.statusText === 'abort') {
                return;
            }
            setData(produce(data => {
                data.fulfilled = false;
                data.rejected = true;
                data.value = null;
                data.reason = error;
            }))
        }).always(() => {
            setRequest(null);
            setData(produce(data => {
                data.pending = false
            }));
        });
    }, [url]);

    return data;
};

export const useTableDataFetch = (baseUrl, tableState) => {
    const url = baseUrl +
        (baseUrl.indexOf('?') < 0 ? '?' : '&') +
        `limit=${tableState.limit}&page=${tableState.page}`;
    return useFetch(url);
};
