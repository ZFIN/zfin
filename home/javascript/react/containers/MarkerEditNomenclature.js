import React from 'react';
import PropTypes from 'prop-types';
import MarkerNameForm from '../components/MarkerNameForm';
import Section from '../components/layout/Section';
import MarkerAliases from '../components/MarkerAliases';

const MarkerEditNomenclature = ({ markerId }) => {
    return (
        <>
            <MarkerNameForm markerId={markerId} />
            <Section title='Previous Names'>
                <MarkerAliases markerId={markerId} />
            </Section>
        </>
    )
};

MarkerEditNomenclature.propTypes = {
    markerId: PropTypes.string.isRequired,
};

export default MarkerEditNomenclature;
