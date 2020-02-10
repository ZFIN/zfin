import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/DataTable';
import OrthologyEvidenceLink from '../components/OrthologyEvidenceLink';

const OrthologyTable = ({geneId}) => {
    const columns = [
        {
            label: 'Species',
            content: row => row.orthologousGene.organism,
            width: '100px',
        },
        {
            label: 'Symbol',
            content: row => row.symbol,
            width: '100px',
        },
        {
            label: 'Chromosome',
            content: row => row.chromosome,
            align: 'right',
            width: '100px',
        },
        {
            label: 'Accession #',
            content: row => (<div>{row.orthologousGeneReference.map((reference) => (
                <div><a href={reference.accession.url}  key={reference.accession.url}>{reference.accession.name}
                </a></div>
            ))} </div>),
            width: '200px',
        },
        {
            label: 'Evidence',
            content: row => (<div>{row.evidence.map((evidence) => (
                <div><OrthologyEvidenceLink name={evidence.name} code={evidence.code} publications={evidence.pubIds} orthoID={row.zdbID}/></div>
            ))} </div>),
        },
    ];
    return (
        <DataTable
            columns={columns}
            url={`/action/api/gene/${geneId}/orthologs`}
            pagination={false}
            rowKey={row => row.zdbID}
        />
    );
};

OrthologyTable.propTypes = {
    geneId: PropTypes.string,
};

export default OrthologyTable;
