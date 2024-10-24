import React from 'react';
import PropTypes from 'prop-types';

const PhenotypeStatement = ({statement}) => {
    return (
        <div
            key={statement.id}
        >
            <a
                key={statement.id}
                href={'/action/phenotype/statement/' + statement.id}
                dangerouslySetInnerHTML={{__html: statement.shortName}}
                rel='nofollow'
            />
            <a
                className='popup-link data-popup-link'
                href={`/action/phenotype/statement-popup/${statement.id}`}
                rel='nofollow'
            />
        </div>
    )
};

PhenotypeStatement.propTypes = {
    statement: PropTypes.shape({
        id: PropTypes.long,
        shortName: PropTypes.string,
    })
};

export default PhenotypeStatement;
