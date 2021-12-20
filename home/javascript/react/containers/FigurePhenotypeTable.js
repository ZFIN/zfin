import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import StageRange from '../components/StageRange';
import PhenotypeStatement from '../components/PhenotypeStatement';


const FigurePhenotypeTable = ({figureId}) => {
    const columns = [
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
            width: '150px',
        },
        {
            label: 'Phenotype',
            content: (row) => <PhenotypeStatement statement={row.phenotypeStatement}/>
        },

    ];
    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/figure/${figureId}/phenotype-detail`}
            rowKey={row => row.fish.zdbID}
            //sortOptions={sortOptions}
        />
    );
};

FigurePhenotypeTable.propTypes = {
    figureId: PropTypes.string,
};

export default FigurePhenotypeTable;
