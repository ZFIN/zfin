import React from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import LoadingSpinner from '../components/LoadingSpinner';
import Table from '../components/data-table/Table';
import EditOrthologyEvidenceCell from '../components/marker-edit/EditOrthologyEvidenceCell';

const MarkerEditOrthology = ({markerId}) => {
    const {
        value,
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
    ]

    if (pending) {
        return <LoadingSpinner />;
    }

    if (!value) {
        return null;
    }

    return (
        <div className='data-table-container'>
            <Table data={value.results} columns={columns} rowKey='zdbID' />
            <div className='data-pagination-container' />
        </div>
    );
};

MarkerEditOrthology.propTypes = {
    markerId: PropTypes.string,
}

export default MarkerEditOrthology;
