import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import StageRange from '../components/StageRange';
import Figure from '../components/Figure';
import PhenotypeStatement from '../components/PhenotypeStatement';


const FigurePhenotypeTable = ({url, hideFigureColumn = false, navigationCounter, title}) => {
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
            filterName: 'fish',
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
                    href={`/action/expression/experiment-popup?id=${row.experiment.zdbID}`}
                />
            </span>,
            filterName: 'condition',
        },
        {
            label: 'Stage',
            content: (row) => <StageRange start={row.start} end={row.end}/>,
            filterName: 'stage',
            width: '150px',
        },
        {
            label: 'Phenotype',
            content: (row) => <PhenotypeStatement statement={row.phenotypeStatement}/>,
            filterName: 'phenotype',
        },
        {
            label: 'Figure',
            content: (row) => <Figure figure={row.figure}/>,
            hidden: hideFigureColumn
        },

    ];

    const handleDataLoadedCount = (data) => {
        if (navigationCounter && navigationCounter.setCounts && data.total) {
            navigationCounter.setCounts(title, data.total);
        }
    };

    return (
        <DataTable
            columns={columns}
            dataUrl={url}
            rowKey={() => Math}
            onDataLoaded={handleDataLoadedCount}
            //sortOptions={sortOptions}
        />
    );
};

FigurePhenotypeTable.propTypes = {
    url: PropTypes.string,
    hideFigureColumn: PropTypes.bool,
    title: PropTypes.string,
    navigationCounter: PropTypes.shape({
        setCounts: PropTypes.func
    }),
};

export default FigurePhenotypeTable;
