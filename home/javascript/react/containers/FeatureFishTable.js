import React, {useState} from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityList} from '../components/entity';
import FigureSummary from '../components/FigureSummary';
import Checkbox from '../components/Checkbox';
import qs from 'qs';

const FeatureFishTable = ({featureId}) => {
    const [excludeFishWithSTR, setExcludeFishWithSTR] = useState(false);


    const columns = [
        {
            label: 'Fish',
            content: ({fish}) =>
                <span className='text-break'>
                    <a
                        className='text-break'
                        href={`/${fish.zdbID}`}
                        dangerouslySetInnerHTML={{__html: fish.displayName}}
                    />
                    <a
                        className='popup-link data-popup-link'
                        href={`/action/fish/fish-detail-popup/${fish.zdbID}`}
                    />
                </span>
            ,
            width: '200px',
        },
        {
            label: 'Genomic Feature Zygosity',
            content: ({zygosity}) => zygosity,
            width: '100px',
        },
        {
            label: 'Parental Zygosity',
            content: ({parentalZygosity}) => <span dangerouslySetInnerHTML={{__html: parentalZygosity}}/>,
            width: '100px',
        },
        {
            label: 'Affected Genomic Regions',
            content: ({affectedMarkers}) => <EntityList entities={affectedMarkers}/>,
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

    const baseUrl = `/action/api/feature/${featureId}/fish`;
    const params = {};
    if (excludeFishWithSTR) {
        params.excludeFishWithSTR = true;
    }
    const dataUrl = baseUrl + qs.stringify(params, {addQueryPrefix: true});

    return (
        <>
            <div className='mb-2'>
                <Checkbox
                    checked={excludeFishWithSTR}
                    id='excludeSTRCheckbox'
                    onChange={e => setExcludeFishWithSTR(e.target.checked)}
                >
                    Show only fish without sequence targeting reagents
                </Checkbox>
            </div>

            <DataTable
                columns={columns}
                dataUrl={dataUrl}
                rowKey={row => row.fish.zdbID}
            />
        </>
    )
};

FeatureFishTable.propTypes = {
    featureId: PropTypes.string,
};

export default FeatureFishTable;
