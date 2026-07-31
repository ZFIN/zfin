import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import LoadingSpinner from '../components/LoadingSpinner';
import MarkerEditOrthology from './MarkerEditOrthology';
import Section from '../components/layout/Section';
import useCurationTabLoadEvent from '../hooks/useCurationTabLoadEvent';

const CurateOrthology = ({pubId}) => {
    const [selectedGene, setSelectedGene] = useState('');
    const {
        value: genes,
        pending,
        refetch,
    } = useFetch(`/action/api/publication/${pubId}/genes`);

    useEffect(() => {
        document.addEventListener('UpdateGeneList', refetch);
        return () => document.removeEventListener('UpdateGeneList', refetch);
    });

    useCurationTabLoadEvent('ORTHOLOGY', pending);

    if (!genes) {
        return null;
    }

    return (
        <Section className='no-border'>
            { pending && <LoadingSpinner /> }

            <form className='form-inline mb-3' noValidate>
                <label className='mr-sm-2'>Zebrafish gene</label>
                <select className='form-control' value={selectedGene} onChange={e => setSelectedGene(e.target.value)}>
                    <option value='' />
                    {genes.map(gene => (
                        <option key={gene.zdbID} value={gene.zdbID}>{gene.name}</option>
                    ))}
                </select>
            </form>

            {!selectedGene && <i className='text-muted'>Select a Zebrafish Gene to Curate Orthology</i>}

            {selectedGene && <MarkerEditOrthology key={selectedGene} markerId={selectedGene} defaultPubId={pubId} />}
        </Section>
    );
};

CurateOrthology.propTypes = {
    pubId: PropTypes.string,
};

export default CurateOrthology;
