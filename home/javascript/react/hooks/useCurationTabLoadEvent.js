import { useEffect } from 'react';

export default function useCurationTabLoadEvent(tabName, pending) {
    useEffect(() => {
        const event = new CustomEvent('CurationTabLoad', {
            detail: {
                tab: tabName,
                pending,
            }
        });
        document.dispatchEvent(event);
    }, [tabName, pending]);
}