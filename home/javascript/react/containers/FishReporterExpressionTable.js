import React from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';
import FigureSummary from '../components/FigureSummary';
import ShowExpressionStructureList from '../components/ShowExpressionStructureList';

const FishReporterExpressionTable = ({fishId}) => {

    const columns = [
        {
            label: 'Expressed Gene',
            content: (row) => <EntityLink entity={row.expressedGene}/>,
            filterName: 'geneName',
            width: '120px',
        },
        {
            label: 'Structure',
            content: (row) => <ShowExpressionStructureList expressionTerms={row.expressionTerms}/>,
            filterName: 'termName',
            width: '250px',
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
                <FigureSummary
                    statistics={row}
                    allFiguresUrl={`/action/expression/fish-expression-figure-summary-standard?fishZdbID=${fishId}&geneZdbID=${row.expressedGene.zdbID}&imagesOnly=false`}
                />
            ),
            width: '180px',
        },
    ];

    const params = {};

    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/fish/${fishId}/reporter-expression?${qs.stringify(params)}`}
            rowKey={row => row.zdbID}
        />
    );
};

FishReporterExpressionTable.propTypes = {
    fishId: PropTypes.string,
};

export default FishReporterExpressionTable;
