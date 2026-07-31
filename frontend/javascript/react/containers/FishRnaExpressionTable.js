import React from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';
import CommaSeparatedList from '../components/CommaSeparatedList';
import FigureSummary from '../components/FigureSummary';

const FishZebrafishModelTable = ({fishId}) => {

    const columns = [
        {
            label: 'Expressed Gene',
            content: (row) => <EntityLink entity={row.expressedGene}/>,
            filterName: 'geneName',
            width: '120px',
        },
        {
            label: 'Structure',
            content: (row) => <CommaSeparatedList>
                {row.expressionTerms.map(entity => {
                    return <>
                        <EntityLink entity={entity}/>
                        <a
                            className='popup-link data-popup-link'
                            href={`/action/ontology/term-detail-popup?termID=${entity.oboID}`}
                        />
                    </>
                })}
            </CommaSeparatedList>,
            filterName: 'termName',
            width: '190px',
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
            width: '380px',
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
            dataUrl={`/action/api/fish/${fishId}/rna-expression?${qs.stringify(params)}`}
            rowKey={row => row.zdbID}
        />
    );
};

FishZebrafishModelTable.propTypes = {
    fishId: PropTypes.string,
};

export default FishZebrafishModelTable;
