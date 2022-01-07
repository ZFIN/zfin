import React, { useState } from 'react';
import PropTypes from 'prop-types';
import useFetch from '../../hooks/useFetch';
import LoadingSpinner from '../../components/LoadingSpinner';
import Table from '../../components/data-table/Table';
import EditOrthologyEvidenceCell from '../../components/marker-edit/EditOrthologyEvidenceCell';
import http from '../../utils/http';
import produce, {setAutoFreeze} from 'immer';
setAutoFreeze(false);

import Modal from '../../components/Modal';
import LoadingButton from '../../components/LoadingButton';
import useCurationTabLoadEvent from '../../hooks/useCurationTabLoadEvent';

const EditOrthologyTable = ({ defaultPubId, markerId }) => {
    const [ncbiGeneId, setNcbiGeneId] = useState('');
    const [ncbiGeneError, setNcbiGeneError] = useState('');
    const [deleteOrtholog, setDeleteOrtholog] = useState(null);
    const [deleting, setDeleting] = useState(false);
    const [genericError, setGenericError] = useState('');

    const {
        value,
        setValue,
        pending,
    } = useFetch(`/action/api/marker/${markerId}/orthologs`);

    useCurationTabLoadEvent('ORTHOLOGY', pending);

    const columns = [
        {
            label: '',
            content: (ortholog) => <a href='#' onClick={handleDeleteLinkClick(ortholog)}>Delete</a>,
            width: '65px',
        },
        {
            label: 'Species',
            content: ({ orthologousGene }) => orthologousGene.organism,
            width: '75px',
        },
        {
            label: 'Symbol',
            content: ({ orthologousGene }) => orthologousGene.abbreviation,
            width: '75px',
        },
        {
            label: 'Chromosome',
            content: ({ chromosome }) => chromosome,
            width: '100px',
        },
        {
            label: 'Accession #',
            content: ({ orthologousGeneReference }) => (
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
            content: ({ evidenceSet, orthologousGene, zdbID }) => (
                <EditOrthologyEvidenceCell
                    defaultPubId={defaultPubId}
                    evidenceCodes={value.supplementalData.evidenceCodes}
                    evidenceSet={evidenceSet}
                    orthoZdbId={zdbID}
                    ortholog={orthologousGene}
                    onSave={handleEvidenceUpdate}
                />
            ),
            width: '200px',
        }
    ];

    const handleEvidenceUpdate = (ortholog) => {
        setValue(produce(value, initialValue => {
            const idx = initialValue.results.findIndex(o => o.zdbID === ortholog.zdbID);
            initialValue.results[idx] = ortholog;
        }));
    }

    const handleDeleteLinkClick = (ortholog) => {
        return (event) => {
            event.preventDefault();
            setGenericError('');
            setDeleteOrtholog(ortholog);
        }
    };

    const handleDeleteModalClose = () => {
        setDeleteOrtholog(null);
    }

    const handleDeleteOrtholog = async () => {
        setDeleting(true);
        try {
            await http.delete(`/action/api/marker/orthologs/${deleteOrtholog.zdbID}`)
            setValue(produce(value, prevValue => {
                prevValue.results = prevValue.results.filter(o => o.zdbID !== deleteOrtholog.zdbID)
            }));
        } catch (error) {
            console.error(error);
            setGenericError(error.responseJSON.message);
        }
        setDeleting(false);
        setDeleteOrtholog(null);
    }

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
            const newOrtholog = await http.post(`/action/api/marker/${markerId}/orthologs`, { orthologousGene: { ID: ncbiGeneId } });
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
                <Table
                    data={value.results}
                    columns={columns}
                    rowKey='zdbID'
                    total={value.total}
                    noDataMessage='No Orthologs Yet'
                />
                <div className='data-pagination-container' />
            </div>

            {genericError && <div className='error'>{genericError}</div> }

            <Modal open={deleteOrtholog !== null}>
                {deleteOrtholog && <>
                    <h4 className='mb-3'>
                        Delete {deleteOrtholog.orthologousGene.organism} {deleteOrtholog.orthologousGene.abbreviation} ortholog?
                    </h4>
                    <div className='horizontal-buttons'>
                        <button className='btn btn-outline-secondary' onClick={handleDeleteModalClose} type='button'>Cancel</button>
                        <LoadingButton
                            className='btn btn-danger'
                            type='button'
                            loading={deleting}
                            onClick={handleDeleteOrtholog}
                        >
                            Delete
                        </LoadingButton>
                    </div>
                </>}
            </Modal>
        </>
    );
};

EditOrthologyTable.propTypes = {
    defaultPubId: PropTypes.string,
    markerId: PropTypes.string.isRequired,
}

export default EditOrthologyTable;
