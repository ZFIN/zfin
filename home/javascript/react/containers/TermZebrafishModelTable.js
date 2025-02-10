import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import DataTableSummaryToggle from '../components/DataTableSummaryToggle';
import PublicationSummary from '../components/PublicationSummary';
import {EntityLink} from '../components/entity';

const TermZebrafishModelTable = ({termId, directAnnotationOnly, isChebi = false}) => {

    const [directAnnotation, setDirectAnnotation] = useState(directAnnotationOnly === 'true');
    const [count, setCount] = useState({'countDirect': 0, 'countIncludingChildren': 0});

    const endpointUrl = isChebi ? 'chebi-zebrafish-models' : 'zebrafish-models'

    const columns = [
        {
            label: 'Fish',
            content: (row) => <a
                href={'/' + row.fish.zdbID}
                dangerouslySetInnerHTML={{__html: row.fish.name}}
            />,
            filterName: 'fishName',
            width: '300px',
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
            width: '200px',
            filterName: 'conditionName',
        },
        {
            label: 'Disease',
            content: ({disease}) => <EntityLink entity={disease}/>,
            filterName: 'diseaseName',
            width: '180px',
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
    if (directAnnotation) {
        params.directAnnotation = true;
    }

    return (
        <>
            {directAnnotationOnly && count.countIncludingChildren > 0 && (
                <DataTableSummaryToggle
                    showPopup={directAnnotation}
                    directCount={count.countDirect}
                    childrenCount={count.countIncludingChildren}
                    onChange={setDirectAnnotation}
                />
            )}
            <DataTable
                columns={columns}
                dataUrl={`/action/api/ontology/${termId}/${endpointUrl}?${qs.stringify(params)}`}
                onDataLoadedCount={(count) => setCount(count)}
                rowKey={row => row.zdbID}
            />
        </>
    );
};

TermZebrafishModelTable.propTypes = {
    termId: PropTypes.string,
    isChebi: PropTypes.bool,
    directAnnotationOnly: PropTypes.string,
};

export default TermZebrafishModelTable;
