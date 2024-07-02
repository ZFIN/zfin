import React, {useState} from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import FormGroup from '../components/form/FormGroup';
import NoData from '../components/NoData';
import AddEditDeleteModal from '../components/AddEditDeleteModal';
import useAddEditDeleteForm from '../hooks/useAddEditDeleteForm';
import AddEditList from '../components/AddEditList';
import FPBaseProteinInput from '../components/form/FPBaseProteinInput';


const FPBase = ({efgId,}) => {

    const [modalRelationship, setModalRelationship] = useState(null);

    const {
        value: fpBaseProteins,
        setValue: setFpBaseProteins,
    } = useFetch(`/action/api/efg/${efgId}/fpbase`, {defaultValue: []});

    const {
        values,
        modalProps
    } = useAddEditDeleteForm({
        addUrl: `/action/api/efg/${efgId}/fpbase`,
        //editUrl: isEdit ? `/action/api/antibody/${antibodyId}/antigen-genes/${modalRelationship.markerRelationshipZdbId}` : '',
        //deleteUrl: isEdit ? `/action/api/antibody/${antibodyId}/antigen-genes/${modalRelationship.markerRelationshipZdbId}` : '',
        onSuccess: () => setModalRelationship(null),
        items: fpBaseProteins,
        setItems: setFpBaseProteins,
        defaultValues: modalRelationship,
    });

    const formatFpBase = (fpBaseProtein, editLink) => {
        if (!fpBaseProtein) {
            return null;
        }
        return (
            <>
                {fpBaseProtein.abbreviation} {editLink}
            </>
        );
    };

    const validateFPBase = (value) => {
        if (!value) {
            return 'An FPBase Protein is required';
        }
        return false;
    };


    if (!fpBaseProteins) {
        return null;
    }

    const labelClass = 'col-md-3 col-form-label';
    const inputClass = 'col-md-9';

    return (
        <>
            {fpBaseProteins.length === 0 && <NoData placeholder=' '/>}

            <AddEditList
                items={fpBaseProteins}
                newItem={{
                    abbreviation: '',
                }}
                setModalItem={setModalRelationship}
                itemKeyProp='id'
                formatItem={formatFpBase}
            />

            <AddEditDeleteModal {...modalProps} header='FPBase Protein'>
                {values &&
                    <>
                        <FormGroup
                            inputClassName={inputClass}
                            labelClassName={labelClass}
                            label='FPBase Protein'
                            id='fpId'
                            field='fpId'
                            tag={FPBaseProteinInput}
                            validate={validateFPBase}
                        />
                    </>
                }

            </AddEditDeleteModal>
        </>
    );
};

FPBase.propTypes = {
    efgId: PropTypes.string,
};

export default FPBase;
