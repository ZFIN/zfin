import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';
import StageRange from '../components/StageRange';
import PostComposedEntity from '../components/PostComposedEntity';


const FigureExpressionTable = ({figureId}) => {

    const columns = [
        {
            label: 'Gene',
            content: row => <EntityLink entity={row.gene}/>,
            width: '150px',
            filterName: 'geneAbbreviation',
        },
        {
            label: 'Antibody',
            content: row => {
                row.antibody && <EntityLink entity={row.antibody}/>
            },
            width: '150px',
        },
        {
            label: 'Fish',
            content: (row) => <span className='text-break'>
                <a
                    className='text-break'
                    href={`/${row.fish.zdbID}`}
                    dangerouslySetInnerHTML={{__html: row.fish.displayName}}
                />
                <a
                    className='popup-link data-popup-link'
                    href={`/action/fish/fish-detail-popup/${row.fish.zdbID}`}
                />
            </span>,
            width: '150px',
        },
        {
            label: 'Experiment',
            content: (row) => <span className='text-break'>
                <a
                    className='text-break'
                    href={`/${row.experiment.zdbID}`}
                    dangerouslySetInnerHTML={{__html: row.experiment.name}}
                />
                <a
                    className='popup-link data-popup-link'
                    href={`/action/expression/experiment-popup?id=${row.experiment.zdbID}`}
                />
            </span>,
            width: '150px',
        },
        {
            label: 'Stage',
            content: (row) => <StageRange start={row.start} end={row.end}/>,
            width: '150px',
        },
        {
            label: 'Qualifier',
            content: row =>
                row.qualifier
            ,
            width: '150px',
        },
        {
            label: 'Anatomy',
            content: row => <PostComposedEntity postComposedEntity={row.entity}/>,
            width: '150px',
        },
        {
            label: 'Assay',
            content: row => row.assay.abbreviation,
            width: '150px',
        },

    ];
    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/figure/${figureId}/expression-detail`}
            rowKey={row => row.id}
            pagination={true}
        />
    );
};

FigureExpressionTable.propTypes = {
    figureId: PropTypes.string,
};

export default FigureExpressionTable;
