import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import OrthologyEvidenceLink from '../components/OrthologyEvidenceLink';

const OrthologyTable = ({geneId}) => {
    const columns = [
        {
            label: 'Species',
            content: row => row.orthologousGene.organism,
            width: '70px',
        },
        {
            label: 'Symbol',
            content: row => row.symbol,
            width: '70px',
        },
        {
            label: 'Chromosome',
            content: row => row.chromosome,
            width: '100px',
        },
        {
            label: 'Accession #',
            content: row => (
                row.orthologousGeneReference.map((reference) => (
                    <div key={reference.accession.url}>
                        <a href={reference.accession.url}>{reference.accession.name}</a>
                    </div>
                ))
            ),
            width: '215px',
        },
        {
            label: 'Evidence',
            content: row => (
                row.evidence.map((evidence) => (
                    <div key={evidence.code}>
                        <OrthologyEvidenceLink
                            name={evidence.name}
                            code={evidence.code}
                            publications={evidence.pubIds}
                            orthoID={row.zdbID}
                        />
                    </div>
                ))
            ),
            width: '200px',
        },
    ];

    const downloadOptions = [
        {
            format: 'CSV',
            url: `/action/marker/${geneId}/download/orthology`,
        },
    ];

    return (
        <DataTable
            columns={columns}
            downloadOptions={downloadOptions}
            dataUrl={`/action/api/marker/${geneId}/orthologs`}
            pagination={false}
            rowKey={row => row.zdbID}
        />
    );
};

OrthologyTable.propTypes = {
    geneId: PropTypes.string,
};

export default OrthologyTable;
