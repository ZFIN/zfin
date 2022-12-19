import React, {useState} from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import FormGroup from '../components/form/FormGroup';
import MarkerInput from '../components/form/MarkerInput';
import NoData from '../components/NoData';
import AddEditDeleteModal from '../components/AddEditDeleteModal';
import useAddEditDeleteForm from '../hooks/useAddEditDeleteForm';
import AddEditList from '../components/AddEditList';
import InputField from '../components/form/InputField';
import PublicationInput from '../components/form/PublicationInput';
import {EntityLink} from '../components/entity';

const AntibodyEditAntigenGenes = ({
    antibodyId,
}) => {

    const [modalRelationship, setModalRelationship] = useState(null);
    const isEdit = modalRelationship && !!modalRelationship.zdbID;

    const relatedMarkerTypeGroup = 'GENEDOM_PROD_PROTEIN';

    const {
        value: antigenGenes,
        setValue: setAntigens,
    } = useFetch(`/action/api/antibody/${antibodyId}/antigen-genes`, {defaultValue: []});

    const {
        pushFieldValue,
        removeFieldValue,
        values,
        modalProps
    } = useAddEditDeleteForm({
        addUrl: `/action/api/antibody/${antibodyId}/antigen-genes`,
        editUrl: isEdit ? `/action/api/antibody/${antibodyId}/antigen-genes/${modalRelationship.markerRelationshipZdbId}` : '',
        deleteUrl: isEdit ? `/action/api/antibody/${antibodyId}/antigen-genes/${modalRelationship.markerRelationshipZdbId}` : '',
        onSuccess: () => setModalRelationship(null),
        items: antigenGenes,
        setItems: setAntigens,
        defaultValues: modalRelationship,
    });

    const formatRelationship = (antigenGene, editLink) => {
        if (!antigenGene) {
            return null;
        }
        return (
            <>
                <EntityLink entity={antigenGene}/>
                {antigenGene.numberOfPublications > 0 && <> ({antigenGene.numberOfPublications})</>}
                {editLink}
            </>
        );
    };


    if (!antigenGenes) {
        return null;
    }

    const labelClass = 'col-md-3 col-form-label';
    const inputClass = 'col-md-9';

    return (
        <>
            {antigenGenes.length === 0 && <NoData placeholder=' ' />}

            <AddEditList
                items={antigenGenes}
                newItem={{
                    abbreviation: '',
                    attributionZdbIDs: [''],
                }}
                setModalItem={setModalRelationship}
                itemKeyProp='zdbID'
                formatItem={formatRelationship}
            />

            <AddEditDeleteModal {...modalProps} header='Antigen Gene'>
                {values &&
                <>
                    <FormGroup
                        inputClassName={inputClass}
                        labelClassName={labelClass}
                        label='Antigen Gene'
                        id='antigen-gene'
                        field='abbreviation'
                        typeGroup={relatedMarkerTypeGroup}
                        tag={MarkerInput}
                    />
                    <div className='form-group row'>
                        <label className={labelClass}>Citations</label>
                        <div className={inputClass}>
                            {
                                values.attributionZdbIDs.map((reference, idx) => (
                                    <div key={idx} className={`d-flex align-items-baseline ${idx > 0 ? 'mt-2' : ''}`}>
                                        <div className='flex-grow-1'>
                                            <InputField
                                                tag={PublicationInput}
                                                field={`attributionZdbIDs.${idx}`}
                                                validate={value => {
                                                    if (!value) {
                                                        return 'A publication ZDB ID is required';
                                                    }
                                                    return false
                                                }}
                                            />
                                        </div>
                                        <button
                                            type='button'
                                            onClick={() => removeFieldValue('attributionZdbIDs', idx)}
                                            className='btn btn-link'
                                        >
                                            <i className='fas fa-times' />
                                        </button>
                                    </div>
                                ))
                            }
                            <button
                                type='button'
                                className='btn btn-link px-0'
                                onClick={() => pushFieldValue('attributionZdbIDs')}
                            >
                                Add Citation
                            </button>
                        </div>
                    </div>
                </>
                }

            </AddEditDeleteModal>
        </>
    );
};

AntibodyEditAntigenGenes.propTypes = {
    antibodyId: PropTypes.string,
};

export default AntibodyEditAntigenGenes;
