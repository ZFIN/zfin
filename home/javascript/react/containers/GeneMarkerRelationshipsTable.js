import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import CommaSeparatedList from '../components/CommaSeparatedList';
import {EntityLink} from '../components/entity';

const AccessionNumberList = ({dblinks}) => (
    <CommaSeparatedList>
        {dblinks.map(dblink => {
            return (
                <a href={`${dblink.referenceDatabase.foreignDB.dbUrlPrefix}${dblink.accessionNumber}`} key={dblink.accessionNumber}>
                    {dblink.accessionNumber}
                </a>
            );
        })}
    </CommaSeparatedList>
);

const GeneMarkerRelationshipsTable = ({geneId}) => {
    const columns = [
        {
            label: 'Relationship',
            content: row => row.relationshipType,
            width: '100px',
            align: 'left'
        },
        {
            label: 'Marker Type',
            content: row => row.markerType,
            width: '100px',
            align: 'left'
        },
        {
            label: 'Marker',
            content: ({relatedMarker}) => <EntityLink entity={relatedMarker} />,
            width: '150px',
        },
        {
            label: 'Accession Numbers',
            content: ({otherMarkerGenBankDBLink}) => <AccessionNumberList dblinks={otherMarkerGenBankDBLink} />,
            width: '120px',
            hidden: geneId.indexOf('GENE')===-1
        },
        {
            label: 'Citations',
            content: row => (row.numberOfPublications > 0 &&
            <a href={`/action/infrastructure/data-citation-list/${row.markerRelationshipZdbId}`}>{row.numberOfPublications}</a>),
            width: '100px',
            align: 'right',
        }
    ];
    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/marker/${geneId}/relationships`}
            rowKey={row => row.zdbID}
        />
    );
};

GeneMarkerRelationshipsTable.propTypes = {
    geneId: PropTypes.string,
};

export default GeneMarkerRelationshipsTable;
