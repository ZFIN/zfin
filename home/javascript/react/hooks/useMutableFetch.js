import { useMemo, useState } from 'react';
import useFetch from './useFetch';

export default function useMutableFetch(url, defaultValue) {
    const [mutatedValue, setMutatedValue] = useState(null);
    const request = useFetch(url);

    const value = useMemo(() => {
        if (mutatedValue) {
            return mutatedValue;
        }
        if (!request.value) {
            return defaultValue;
        }
        return request.value;
    }, [request.value, mutatedValue]);

    return {
        ...request,
        value,
        setValue: setMutatedValue,
    }
}
