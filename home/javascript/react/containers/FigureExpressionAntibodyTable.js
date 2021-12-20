import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';
import StageRange from '../components/StageRange';
import PostComposedEntity from '../components/PostComposedEntity';


const FigureExpressionAntibodyTable = ({figureId}) => {
    const columns = [
        {
            label: 'Antibody',
            content: row => <EntityLink entity={row.antibody}/>,
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
            label: 'Conditions',
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
        },
        {
            label: 'Stage',
            content: (row) => <StageRange start={row.start} end={row.end}/>,
        },
        {
            label: 'Qualifier',
            content: row => row.qualifier,
        },
        {
            label: 'Anatomy',
            content: row => <PostComposedEntity postComposedEntity={row.entity}/>,
        },
        {
            //label: 'Assay <a className="popup-link info-popup-link" href="/action/expression/assay-abbrev-popup"></a>',
            label: 'Assay',
            content: row => row.assay.abbreviation,
        },

    ];
    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/figure/${figureId}/antibody-labeling`}
            rowKey={row => row.antibody.zdbID}
            //sortOptions={sortOptions}
        />
    );
};

FigureExpressionAntibodyTable.propTypes = {
    figureId: PropTypes.string,
};

export default FigureExpressionAntibodyTable;
