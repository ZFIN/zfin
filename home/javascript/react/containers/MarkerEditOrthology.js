import React  from 'react';
import PropTypes from 'prop-types';
import EditOrthologyTable from '../components/marker-edit/EditOrthologyTable';
import Section from '../components/layout/Section';
import EditOrthologyNote from '../components/marker-edit/EditOrthologyNote';

const MarkerEditOrthology = ({ defaultPubId, markerId }) => {
    return (
        <>
            <Section>
                <EditOrthologyTable markerId={markerId} defaultPubId={defaultPubId} />
            </Section>

            <Section title='Orthology Note'>
                <EditOrthologyNote markerId={markerId} />
            </Section>
        </>
    );
};

MarkerEditOrthology.propTypes = {
    defaultPubId: PropTypes.string,
    markerId: PropTypes.string,
}

export default MarkerEditOrthology;
