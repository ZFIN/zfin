import React from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';
import CommaSeparatedList from '../components/CommaSeparatedList';
import FigureSummary from '../components/FigureSummary';

const GenotypeFishTable = ({genotypeId}) => {

    const columns = [
        {
            label: 'Fish',
            content: (row) => <>
                <EntityLink entity={row.fish}/>
                <a
                    className='popup-link data-popup-link'
                    href={`/action/fish/fish-detail-popup/${row.fish.zdbID}`}
                />
            </>,
            filterName: 'fishName',
            width: '180px',
        },
        {
            label: 'Affected Genomic Region',
            content: (row) => <CommaSeparatedList>
                {row.affectedMarkers.map(entity => {
                    return <EntityLink entity={entity} key={entity.zdbID}/>
                })}
            </CommaSeparatedList>,
            filterName: 'geneName',
            width: '130px',
        },
        {
            label: 'Phenotype',
            content: row => (
                <FigureSummary
                    statistics={row.fishGenotypePhenotypeStatistics}
                    allFiguresUrl={`/action/fish/phenotype-summary?fishID=${row.fish.zdbID}&imagesOnly=false`}
                />
            ),
            width: '180px',
        },
        {
            label: 'Gene Expression',
            content: row => (
                <FigureSummary
                    statistics={row.fishGenotypeExpressionStatistics}
                    allFiguresUrl={`/action/expression/fish-expression-figure-summary?fishID=${row.fish.zdbID}&imagesOnly=false`}
                />
            ),
            width: '180px',
        },
    ];

    const params = {};

    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/genotype/${genotypeId}/fish?${qs.stringify(params)}`}
            rowKey={row => row.zdbID}
        />
    );
};

GenotypeFishTable.propTypes = {
    genotypeId: PropTypes.string,
};

export default GenotypeFishTable;
