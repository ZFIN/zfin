import React from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import EntityList from '../components/entity/EntityList';

const FeatureLabTable = ({orgId}) => {

    const queryParams = qs.stringify({
    }, {addQueryPrefix: true});

    const columns = [
        {
            label: 'Allele',
            content: row => <>
                <a href={`/${row.zdbID}`}>
                    <span>{row.name}</span>
                </a>
            </>,
            width: '250px',
        },
        {
            label: 'Type',
            content: row => row.type.display,
            width: '200px',
        },
        {
            label: 'Affected Genomic Region',
            content: row => <>
                {row.affectedGenes && <EntityList entities={row.affectedGenes}/>}
            </>    ,
            width: '200px',
        },
        {
            label: 'Affected Genomic Region',
            content: row => <>
                {row.tgConstructs && <EntityList entities={row.tgConstructs}/>}
            </>,
            width: '200px',
        },
    ];

    return (
        <>
            <DataTable
                columns={columns}
                dataUrl={`/action/api/lab/${orgId}/features${queryParams}`}
                rowKey={row => row.zdbID}
                pagination={true}
            />
        </>
    );
};

FeatureLabTable.propTypes = {
    orgId: PropTypes.string,
};

export default FeatureLabTable;
