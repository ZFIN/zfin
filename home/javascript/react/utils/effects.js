import {useEffect, useState} from 'react';
import produce from 'immer';
import qs from 'qs';
import http from './http';
import {DEFAULT_TABLE_STATE} from '../components/data-table';

const DEFAULT_FETCH_STATE = {
    pending: false,
    rejected: false,
    reason: null,
    fulfilled: false,
    value: null,
};

export const useFetch = (url) => {
    // a container for mutable data
    const status = {};
    const [data, setData] = useState(DEFAULT_FETCH_STATE);
    const [request, setRequest] = useState(null);

    useEffect(() => {
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
        return () => {
            status.mounted = false;
        };
    }, [url]);

    return data;
};

export const useAppendingFetch = (baseUrl, page, setPage) => {
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

export const useTableDataFetch = (baseUrl, tableState) => {
    const params = qs.stringify(tableState, {
        allowDots: true,
        skipNulls: true,
    });
    const separator = baseUrl.indexOf('?') < 0 ? '?' : '&';
    return useFetch(baseUrl + separator + params);
};

export const useTableState = (tableState, setTableState) => {
    if ((tableState && !setTableState) || (!tableState && setTableState)) {
        if (process.env.NODE_ENV === 'development') {
            console.warn('Table state must either be controlled (by setting tableState and onTableStateChange) or uncontrolled (by setting neither)');
        }
    }

    const [controlledTableState, setControlledTableState] = useState(DEFAULT_TABLE_STATE);
    tableState = tableState || controlledTableState;
    setTableState = setTableState || setControlledTableState;

    return [tableState, setTableState];
}

export const useRibbonState = () => {
    const [selected, setSelected] = useState(null);

    const handleItemClick = (subject, group) => {
        if (!subject || !group || (selected && selected.group.id === group.id && selected.group.type === group.type)) {
            setSelected(null);
        } else {
            setSelected({subject, group});
        }
    };

    return [selected, handleItemClick];
};

export const useDebouncedValue = (value, delay) => {
    const [debouncedValue, setDebouncedValue] = useState(value);

    useEffect(() => {
        const timeout = setTimeout(() => setDebouncedValue(value), delay);
        return () => clearTimeout(timeout);
    }, [delay, value]);

    return debouncedValue;
};
