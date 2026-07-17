import React from 'react';
import PropTypes from 'prop-types';
import ProcessingTaskItem from './ProcessingTaskItem';

const hasCompletedTask = (pub, task) => pub.processingTasks.find(entry => entry.task === task) !== undefined;

const disabledFiguresMessage = 'Publication does not have permission to show images. Check for open access and then use ' +
    'the edit form to set the Has Image Permissions checkbox, if applicable, before adding figures.';

const ProcessingTaskList = ({ pub }) => {
    const pdfComplete = hasCompletedTask(pub, 'ADD_PDF');
    const figuresComplete = hasCompletedTask(pub, 'ADD_FIGURES');
    const linkComplete = hasCompletedTask(pub, 'LINK_AUTHORS');
    return (
        <ul className='processing-tasks'>
            <ProcessingTaskItem
                href={`/action/publication/${pub.zdbId}/edit#files`}
                isComplete={pdfComplete}
            >
                {!pdfComplete && pub.pdfPath ? 'Review uploaded PDF' : 'Add PDF'}
            </ProcessingTaskItem>

            <ProcessingTaskItem
                href={`/action/publication/${pub.zdbId}/edit#figures`}
                isComplete={figuresComplete}
                disabledMessage={pub.canShowImages ? undefined : disabledFiguresMessage}
            >
                {!figuresComplete && pub.images && pub.images.length ? 'Review uploaded figures' : 'Add figures'}
            </ProcessingTaskItem>

            <ProcessingTaskItem
                href={`/action/publication/${pub.zdbId}/link`}
                isComplete={linkComplete}
            >
                Link Authors
            </ProcessingTaskItem>
        </ul>
    );
};

ProcessingTaskList.propTypes = {
    pub: PropTypes.object,
};

export default ProcessingTaskList;