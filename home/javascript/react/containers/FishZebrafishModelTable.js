import React from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import PublicationSummary from '../components/PublicationSummary';
import {EntityLink} from '../components/entity';

const FishZebrafishModelTable = ({fishId}) => {

    const columns = [
        {
            label: 'Disease',
            content: ({disease}) => <EntityLink entity={disease}/>,
            filterName: 'diseaseName',
            width: '180px',
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
        },
        {
            label: 'Citation',
            content: row => (
                <PublicationSummary
                    numberOfPublications={row.numberOfPublications}
                    firstPublication={row.singlePublication}
                    fishID={row.fish.zdbID}
                    experimentID={row.experiment.zdbID}
                    termID={row.disease.zdbID}
                    allPublicationUrl={`/action/ontology/phenotype-summary/${row.fish.zdbID}`}
                />
            ),
            width: '230px',
        },

    ];

    const params = {};

    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/ontology/fish/${fishId}/zebrafish-models?${qs.stringify(params)}`}
            rowKey={row => row.zdbID}
        />
    );
};

FishZebrafishModelTable.propTypes = {
    fishId: PropTypes.string,
};

export default FishZebrafishModelTable;
