import React from 'react';
import PropTypes from 'prop-types';
import {CollapseTable} from '../../components/data-table';
import SingularPlural from '../SingularPlural';


const PubAlleleTable = ({pubId}) => {
    const columns = [
        {
            label: 'Name',
            content: row => <a href={`/${row.id}`}>{row.name}</a>,
            width: '150px',
        },
        {
            label: 'Is allele of',
            content: row => <a href={`/${row.affectedMarkerId}`}>{row.affectedMarkerName}</a>,
            width: '100px',
        },
        {
            label: 'New with this paper',
            content: row => row.newWithThisPaper ? <i className='text-muted'>Yes </i> : <i className='text-muted'>No</i>,
            width: '120px',
        },
        {
            label: 'Phenotype Data',
            content: row => (row.phenotypeFigures > 0 &&
                <> <SingularPlural singular='figure' plural='figures' value={row.phenotypeFigures}/> from &nbsp;
                    <SingularPlural singular='pub' plural='pubs' value={row.phenotypePublication}/></>
            ),
            width: '100px',
        },

    ];
    return (
        <CollapseTable
            columns={columns}
            dataUrl={`/action/api/publication/${pubId}/prioritization/features`}
            rowKey={row => row.id}
            //sortOptions={sortOptions}
        />
    );
};

PubAlleleTable.propTypes = {
    pubId: PropTypes.string,
};

export default PubAlleleTable;
