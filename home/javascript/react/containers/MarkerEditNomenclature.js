import React from 'react';
import PropTypes from 'prop-types';
import MarkerNameForm from '../components/marker-edit/MarkerNameForm';
import Section from '../components/layout/Section';
import MarkerAliases from '../components/marker-edit/MarkerAliases';
import useFetch from '../hooks/useFetch';
import { stringToBool } from '../utils';

const MarkerEditNomenclature = ({ markerId, showAbbreviationField, showReasonFields }) => {
    const {
        value: nomenclature,
        setValue: setNomenclature,
    } = useFetch(
        `/action/marker/${markerId}/nomenclature`,
        {
            defaultValue: {
                name: '',
                abbreviation: '',
            }
        }
    );

    const {
        value: aliases,
        setValue: setAliases,
        refetch: refetchAliases,
    } = useFetch(`/action/marker/${markerId}/aliases`, { defaultValue: [] });

    return (
        <>
            <Section>
                <MarkerNameForm
                    markerId={markerId}
                    nomenclature={nomenclature}
                    setNomenclature={setNomenclature}
                    onSave={refetchAliases}
                    showAbbreviationField={stringToBool(showAbbreviationField)}
                    showReasonFields={stringToBool(showReasonFields)}
                />
            </Section>

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
    showAbbreviationField: PropTypes.string,
    showReasonFields: PropTypes.string,
};

export default MarkerEditNomenclature;
