import React from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import PhenotypeStatementLink from '../components/entity/PhenotypeStatementLink';
import FigureProteinSummary from '../components/FigureProteinSummary';

const FishPhenotypeTable = ({fishId}) => {

    const columns = [
        {
            label: 'Phenotype',
            content: (row) => <PhenotypeStatementLink key={row.phenoStatement.id} entity={row.phenoStatement}/>,
            filterName: 'phenotype',
            width: '220px',
        },
        {
            label: 'Conditions',
            content: (row) => <span className='text-break'>
                <a
                    className='text-break'
                    href={`/${row.experiment.zdbID}`}
                    dangerouslySetInnerHTML={{__html: row.experiment.conditions}}
                />
                <a
                    className='popup-link data-popup-link'
                    href={`/action/experiment/popup/${row.experiment.zdbID}`}
                />
            </span>,
            filterName: 'conditionName',
            width: '320px',
        },
        {
            label: 'Figures',
            content: row => (
                <FigureProteinSummary
                    statistics={row}
                    fishID={fishId}
                />
            ),
            width: '180px',
        },
    ];

    const params = {};

    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/fish/${fishId}/phenotype?${qs.stringify(params)}`}
            rowKey={row => row.zdbID}
        />
    );
};

FishPhenotypeTable.propTypes = {
    fishId: PropTypes.string,
};

export default FishPhenotypeTable;
