import { useEffect, useState } from 'react';
import produce, {setAutoFreeze} from 'immer';
setAutoFreeze(false);

import http from '../utils/http';
import { DEFAULT_FETCH_STATE } from './constants';

export default function useFetch(url, options = {}) {
    // a container for mutable data
    const status = {};

    const [data, setData] = useState({
        ...DEFAULT_FETCH_STATE,
        value: options.defaultValue || null,
    });
    const [request, setRequest] = useState(null);

    const setValue = (value) => {
        setData(produce(data => {
            data.value = value;
        }));
    };

    const doFetch = () => {
        if (!url) {
            return;
        }
        status.mounted = true;
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
        xhr.then(result => {
            if (!status.mounted) {
                return;
            }
            setData(produce(data => {
                data.fulfilled = true;
                data.rejected = false;
                data.value = result;
                data.reason = null;
            }))
        }).fail(error => {
            if (error.statusText === 'abort' || !status.mounted) {
                return;
            }
            setData(produce(data => {
                data.fulfilled = false;
                data.rejected = true;
                data.value = null;
                data.reason = error;
            }))
        }).always(() => {
            if (!status.mounted) {
                return;
            }
            setRequest(null);
            setData(produce(data => {
                data.pending = false
            }));
        });
    }

    useEffect(() => {
        doFetch();
        return () => {
            status.mounted = false;
        };
    }, [url]);

    return {
        ...data,
        setValue,
        refetch: doFetch,
    };
}
