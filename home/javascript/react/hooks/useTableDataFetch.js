import qs from 'qs';
import useFetch from './useFetch';

export default function useTableDataFetch(baseUrl, tableState) {
    const params = qs.stringify(tableState, {
        allowDots: true,
        skipNulls: true,
    });
    const separator = baseUrl.indexOf('?') < 0 ? '?' : '&';
    return useFetch(baseUrl + separator + params);
}
