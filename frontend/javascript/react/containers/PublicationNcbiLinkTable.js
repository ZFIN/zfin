import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';

const PublicationNcbiLinkTable = ({url}) => {
    const columns = [
        {
            label: 'ZFIN ID',
            content: row => <a href={`/${row.zdbID}`}>{row.zdbID}</a>,
            filterName: 'zdbID',
            width: '200px',
        },
        {
            label: 'Symbol',
            content: row => row.symbol && <EntityLink entity={{zdbID: row.zdbID, abbreviation: row.symbol}}/>,
            filterName: 'symbol',
            width: '150px',
        },
        {
            label: 'Accession',
            content: row => <a href={row.url}>{row.accession}</a>,
            filterName: 'accession',
            width: '150px',
        },
        {
            label: 'Type',
            content: row => row.type,
            filterName: 'type',
            filterOptionFromSupplementalData: 'linkTypes',
            filterOptionCountsFromSupplementalData: 'linkTypeCounts',
            width: '240px',
        },
        {
            label: 'Assemblies',
            content: row => row.assemblies,
            filterName: 'assembly',
            filterOptionFromSupplementalData: 'assemblies',
            filterOptionCountsFromSupplementalData: 'assemblyCounts',
            width: '190px',
        },
        {
            label: 'DB Link Created',
            content: row => row.dblinkDate,
            filterName: 'dblinkDate',
            filterOptionFromSupplementalData: 'dblinkDates',
            filterOptionCountsFromSupplementalData: 'dblinkDateCounts',
            width: '200px',
        },
        {
            label: 'Attribution Date',
            content: row => row.attributionDate,
        },
    ];

    return (
        <DataTable
            columns={columns}
            dataUrl={url}
            rowKey={row => row.dblinkID}
            pagination={true}
        />
    );
};

PublicationNcbiLinkTable.propTypes = {
    url: PropTypes.string,
};

export default PublicationNcbiLinkTable;
