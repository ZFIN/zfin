import React from 'react';
import PropTypes from 'prop-types';

const templates = [
    {
        label: 'PDF Request',
        subject: 'Requesting a copy of your recent article',
        body: (citation, sender) => `Dear [[NAME]],

I am writing to request a full text PDF and published supplemental data from the following article:
${citation}

Background information and intended use:

ZFIN (Zebrafish Information Network) is the zebrafish model organism database, a centralized community resource for zebrafish genetic, genomic, and developmental data. ZFIN is an NIH-funded Model Organism Database, affiliated with the University of Oregon.

It is important to note that your publication will not be made publicly available. Our curation team records a variety of data from every published article that involves zebrafish. We then integrate that data into a searchable on-line database.

Since 1994, ZFIN has provided current and free information to zebrafish researchers, educators and students around the world.

Thank you,
${sender}`,
    }
];

const PubCorrespondenceTemplateSelector = ({onSelect}) => {
    const handleSelect = (event, template) => {
        event.preventDefault();
        onSelect(template.subject, template.body);
    };

    return (
        <div className="dropdown">
            <button className="btn btn-default dropdown-toggle" type="button" data-toggle="dropdown">
                Insert Template <span className="caret" />
            </button>
            <ul className="dropdown-menu">
                {templates.map(template => (
                    <li key={template.label}>
                        <a href="#" onClick={e => handleSelect(e, template)}>{template.label}</a>
                    </li>
                ))}
            </ul>
        </div>
    );
};

PubCorrespondenceTemplateSelector.propTypes = {
    onSelect: PropTypes.func,
}

export default PubCorrespondenceTemplateSelector;
