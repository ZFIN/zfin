import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import { EntityList } from '../components/entity';
import FigureSummary from '../components/FigureSummary';

const FeatureFishTable = ({featureId}) => {
    const columns = [
        {
            label: 'Fish',
            content: ({fish}) => <a className='text-break' href={`/${fish.zdbID}`} dangerouslySetInnerHTML={{__html: fish.displayName}} />,
            width: '200px',
        },
        {
            label: 'Genomic Feature Zygosity',
            content: ({zygosity}) => zygosity,
            width: '100px',
        },
        {
            label: 'Paternal Zygosity',
            content: ({parentalZygosity}) => <span dangerouslySetInnerHTML={{__html: parentalZygosity }} />,
            width: '100px',
        },
        {
            label: 'Affected Genomic Regions',
            content: ({affectedMarkers}) => <EntityList entities={affectedMarkers} />,
            width: '150px',
        },
        {
            label: 'Phenotype',
            content: ({fish, fishGenotypePhenotypeStatistics}) => (
                <FigureSummary
                    statistics={fishGenotypePhenotypeStatistics}
                    allFiguresUrl={`/action/fish/phenotype-summary?fishID=${fish.zdbID}&imagesOnly=false`}
                />
            ),
            width: '200px',
        },
        {
            label: 'Gene Expression',
            content: ({fish, fishGenotypeExpressionStatistics}) => (
                <FigureSummary
                    statistics={fishGenotypeExpressionStatistics}
                    allFiguresUrl={`/action/expression/fish-expression-figure-summary?fishID=${fish.zdbID}&imagesOnly=false`}
                />
            ),
            width: '200px',
        }
    ];
    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/feature/${featureId}/fish`}
            rowKey={row => row.fish.zdbID}
        />
    )
};

FeatureFishTable.propTypes = {
    featureId: PropTypes.string,
};

export default FeatureFishTable;
