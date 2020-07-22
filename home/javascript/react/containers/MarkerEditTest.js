import React, { useState, useEffect } from 'react';

import PublicationInput from '../components/PublicationInput';
import { useFetch } from '../utils/effects';
import LoadingSpinner from '../components/LoadingSpinner';

const ANTIBODY_PUBS = [
    {
        id: 'ZDB-PUB-080117-1',
        name: 'Antibody Data Submissions',
    },
    {
        id: 'ZDB-PUB-020723-5',
        name: 'Manually Curated Data',
    },
    {
        id: 'ZDB-PUB-081107-1',
        name: 'Antibody information from supplier',
    }
];

const ORTHO_PUB = {
    id: 'ZDB-PUB-030905-1',
    name: 'Curation of orthology data',
};

const MarkerEditTest = ({orthoPubs}) => {
    const [ valueA, setValueA ] = useState('');
    const [ valueB, setValueB ] = useState('');
    const [ valueC, setValueC ] = useState('');
    const aliases = useFetch('/action/marker/ZDB-MRPHLNO-080213-5/aliases');
    useEffect(() => {
        if (aliases.pending || !aliases.value) {
            return;
        }
        setValueC(aliases.value[0].references[0].zdbID);
    }, [aliases]);

    const allOrthoPubs = [
        ORTHO_PUB,
    ];
    if (orthoPubs) {
        const parsed = JSON.parse(orthoPubs);
        allOrthoPubs.push(...parsed);
    }

    if (aliases.loading) {
        return <LoadingSpinner />;
    }

    return (
        <div className='section'>
            <div className='heading'>Antibody Example</div>
            <p>This example has three pre-defined publications presented by default if nothing is typed into the box.</p>
            <div className='form-group scrollable-dropdown-menu'>
                <PublicationInput
                    className='form-control'
                    defaultPubs={ANTIBODY_PUBS}
                    onChange={e => setValueA(e.target.value)}
                    value={valueA}
                />
            </div>

            <div className='heading'>Gene Orthology Example</div>
            <p>This example has one pre-defined publication and any publications already used in orthology annotations
                presented by default. These will change from gene to gene. </p>
            <div className='form-group scrollable-dropdown-menu'>
                <PublicationInput
                    className='form-control'
                    defaultPubs={allOrthoPubs}
                    onChange={e => setValueB(e.target.value)}
                    value={valueB}
                />
            </div>

            <div className='heading'>Populated</div>
            <p>This example has a pub already filled in and no default suggestions.</p>
            <div className='form-group scrollable-dropdown-menu'>
                <PublicationInput
                    className='form-control'
                    defaultPubs={[]}
                    onChange={e => setValueC(e.target.value)}
                    value={valueC}
                />
            </div>
        </div>
    );
};

export default MarkerEditTest;
