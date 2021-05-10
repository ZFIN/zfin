import React from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../data-table';
import StageRange from '../StageRange';
import {tableStateType} from '../../utils/types';
import PublicationCitationLink from '../PublicationCitationLink';

const PhenotypeAnnotationDetailTable = ({geneId, selectedPhenotypeIds, setTableState, tableState}) => {
    const columns = [
        {
            label: 'Fish',
            content: ({phenotypeSourceGenerated}) => <a href={`/${phenotypeSourceGenerated.fishExperiment.fish.zdbID}`} dangerouslySetInnerHTML={{__html: phenotypeSourceGenerated.fishExperiment.fish.displayName}}/>,
            width: '200px',
        },
        {
            label: 'Experiment',
            content: ({phenotypeSourceGenerated}) => phenotypeSourceGenerated.fishExperiment.experiment.conditions,
            width: '150px',
        },
        {
            label: 'Stage',
            content: ({phenotypeSourceGenerated}) => (
                <StageRange
                    start={phenotypeSourceGenerated.start}
                    end={phenotypeSourceGenerated.end}
                />
            ),
            width: '200px',
        },
        {
            label: 'Figure',
            content: ({phenotypeSourceGenerated}) => <a href={`/${phenotypeSourceGenerated.figure.zdbID}`} dangerouslySetInnerHTML={{__html: phenotypeSourceGenerated.figure.label}}/>,
            width: '100px',
        },
        {
            label: 'Publication',
            content: ({phenotypeSourceGenerated}) => <PublicationCitationLink publication={phenotypeSourceGenerated.figure.publication} />,
            width: '200px',
        },
    ];

    const detailTableQuery = {
        termId: selectedPhenotypeIds,
    };

    return (
        <DataTable
            dataUrl={`/action/api/marker/${geneId}/phenotype/detail?${qs.stringify(detailTableQuery)}`}
            columns={columns}
            rowKey={row => row.id}
            tableState={tableState}
            setTableState={setTableState}
        />
    );
}

PhenotypeAnnotationDetailTable.propTypes = {
    geneId: PropTypes.string,
    selectedPhenotypeIds: PropTypes.string,
    setTableState: PropTypes.func,
    tableState: tableStateType,
};

export default PhenotypeAnnotationDetailTable;
