import React, { useState } from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import LoadingSpinner from '../components/LoadingSpinner';
import Table from '../components/data-table/Table';
import EditOrthologyEvidenceCell from '../components/marker-edit/EditOrthologyEvidenceCell';
import http from '../utils/http';
import produce from 'immer';

const MarkerEditOrthology = ({markerId}) => {
    const [ncbiGeneId, setNcbiGeneId] = useState('');
    const [ncbiGeneError, setNcbiGeneError] = useState('');
    const {
        value,
        setValue,
        pending,
    } = useFetch(`/action/api/marker/${markerId}/orthologs`);

    const columns = [
        {
            label: '',
            content: () => <a href='#'>Delete</a>,
            width: '65px',
        },
        {
            label: 'Species',
            content: ({orthologousGene}) => orthologousGene.organism,
            width: '75px',
        },
        {
            label: 'Symbol',
            content: ({orthologousGene}) => orthologousGene.abbreviation,
            width: '75px',
        },
        {
            label: 'Chromosome',
            content: ({chromosome}) => chromosome,
            width: '100px',
        },
        {
            label: 'Accession #',
            content: ({orthologousGeneReference}) => (
                <ul className='list-unstyled'>
                    {orthologousGeneReference.map(({ accession }) => (
                        <li key={accession.url}><a href={accession.url}>{accession.name}</a></li>
                    ))}
                </ul>
            ),
            width: '250px',
        },
        {
            label: 'Evidence',
            content: ({evidenceSet}) => <EditOrthologyEvidenceCell evidenceSet={evidenceSet} />,
            width: '200px',
        }
    ];

    const handleNcbiGeneIdChange = (event) => {
        setNcbiGeneError('');
        setNcbiGeneId(event.target.value);
    };

    const handleAddOrtholog = async () => {
        const alreadyAdded = value.results.some(o => o.orthologousGene.ID === ncbiGeneId);
        if (alreadyAdded) {
            setNcbiGeneError('Ortholog already added');
            return;
        }
        try {
            const newOrtholog = await http.post(`/action/api/marker/${markerId}/orthologs`, { orthologousGene: { ID: ncbiGeneId }});
            setValue(produce(value, prevValue => {
                prevValue.results.push(newOrtholog);
            }));
            setNcbiGeneId('');
        } catch (error) {
            setNcbiGeneError(error.responseJSON.message);
        }
    };

    if (pending) {
        return <LoadingSpinner />;
    }

    if (!value) {
        return null;
    }

    return (
        <>
            <form className='form-inline mb-3' noValidate>
                <label className='mr-sm-2' htmlFor='markerEditOrthologyNCBIImport'>Import from NCBI</label>
                <input
                    type='text'
                    className={`form-control mr-sm-2 ${ncbiGeneError ? 'is-invalid' : ''}`}
                    id='markerEditOrthologyNCBIImport'
                    placeholder='Enter NCBI Gene ID'
                    onChange={handleNcbiGeneIdChange}
                    value={ncbiGeneId}
                />
                <button
                    type='button'
                    className='btn btn-primary'
                    disabled={ncbiGeneId === ''}
                    onClick={handleAddOrtholog}
                >
                    Import
                </button>
                {ncbiGeneError && <div className='invalid-feedback'>{ncbiGeneError}</div>}
            </form>

            <div className='data-table-container'>
                <Table data={value.results} columns={columns} rowKey='zdbID' />
                <div className='data-pagination-container' />
            </div>
        </>
    );
};

MarkerEditOrthology.propTypes = {
    markerId: PropTypes.string,
}

export default MarkerEditOrthology;
