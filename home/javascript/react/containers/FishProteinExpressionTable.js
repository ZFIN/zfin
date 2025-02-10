import React from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';
import CommaSeparatedList from '../components/CommaSeparatedList';
import FigureProteinSummary from '../components/FigureProteinSummary';

const FishZebrafishModelTable = ({fishId}) => {

    const columns = [
        {
            label: 'Antibody',
            content: (row) => <EntityLink entity={row.antibody}/>,
            filterName: 'antibodyName',
            width: '90px',
        },
        {
            label: 'Antigen Gene',
            content: (row) => <>
                {row.antiGene && <EntityLink entity={row.antiGene}/>}
            </>,
            filterName: 'geneName',
            width: '90px',
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
            width: '260px',
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
                    markerID={row.antibody.zdbID}
                />
            ),
            width: '180px',
        },
    ];

    const params = {};

    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/fish/${fishId}/protein-expression?${qs.stringify(params)}`}
            rowKey={row => row.zdbID}
        />
    );
};

FishZebrafishModelTable.propTypes = {
    fishId: PropTypes.string,
};

export default FishZebrafishModelTable;
