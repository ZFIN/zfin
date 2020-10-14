import React from 'react';
import PropTypes from 'prop-types';
import MarkerNameForm from '../components/MarkerNameForm';
import Section from '../components/layout/Section';
import MarkerAliases from '../components/MarkerAliases';
import useMutableFetch from '../hooks/useMutableFetch';

const MarkerEditNomenclature = ({ markerId }) => {
    const {
        value: nomenclature,
        setValue: setNomenclature,
    } = useMutableFetch(
        `/action/marker/${markerId}/nomenclature`,
        {
            name: '',
            abbreviation: '',
        }
    );

    const {
        value: aliases,
        setValue: setAliases,
        refetch: refetchAliases,
    } = useMutableFetch(`/action/marker/${markerId}/aliases`, []);

    return (
        <>
            <MarkerNameForm
                markerId={markerId}
                nomenclature={nomenclature}
                setNomenclature={setNomenclature}
                onSave={refetchAliases}
            />
            <Section title='Previous Names'>
                <MarkerAliases
                    markerId={markerId}
                    aliases={aliases}
                    setAliases={setAliases}
                />
            </Section>
        </>
    )
};

MarkerEditNomenclature.propTypes = {
    markerId: PropTypes.string.isRequired,
};

export default MarkerEditNomenclature;
