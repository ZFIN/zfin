import React, {useState} from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import useAddEditDeleteForm from '../hooks/useAddEditDeleteForm';
import FormGroup from '../components/form/FormGroup';
import AddEditDeleteModal from '../components/AddEditDeleteModal';
import AddEditList from '../components/AddEditList';
import LoadingSpinner from '../components/LoadingSpinner';

const MarkerEditChromosomalLocation = ({
    markerId,
    type,
}) => {
    const [error, setError] = useState('');
    const [modalData, setModalData] = useState(null);
    const isEdit = modalData && modalData.id;
    const hdr = type;
    const assemblies = ['GRCz11', 'GRCz10', 'Zv9'];

    const {
        value: liveData,
        setValue: setLiveData,
        pending,
    } = useFetch(`/action/marker/${markerId}/chromosomal-location`);

    const handleOnSuccess = () => {
        setModalData(null);
    };

    const handleValidateChromosome = (val) => {
        if (!values) {
            return false;
        }
        const startLocation = parseInt(values.startLocation);
        const endLocation = parseInt(values.endLocation);

        if ( (startLocation || endLocation)
            && (values.chromosome === ""))
        {
            return "Must provide chromosome.";
        }

        return false;
    };

    const handleValidateLocations = () => {
        if (!values) {
            return false;
        }
        const startLocation = parseInt(values.startLocation);
        const endLocation = parseInt(values.endLocation);
        if (startLocation && endLocation
            && (startLocation > endLocation))
        {
            return "Start location must be before end location.";
        }

        return false;
    };

    const {
        values,
        modalProps
    } = useAddEditDeleteForm({
        addUrl: `/action/marker/${markerId}/chromosomal-location`,
        editUrl: isEdit ? `/action/marker/${markerId}/chromosomal-location/${modalData.id}` : '',
        deleteUrl: isEdit ? `/action/marker/${markerId}/chromosomal-location/${modalData.id}` : '',
        onSuccess: handleOnSuccess,
        items: liveData,
        setItems: setLiveData,
        itemKeyProp: 'id',
        defaultValues: modalData,
        isEdit
    });

    const formatLink = (item, editLink) => {
        const leftColumnClass="col-sm-4 col-md-3 col-lg-2";
        const rightColumnClass="col-sm-8 col-md-9 col-lg-10";
        return <><dl className="row">
            <dt className={leftColumnClass}>Assembly</dt>
            <dd className={rightColumnClass}>{item.assembly}</dd>
            <dt className={leftColumnClass}>Chromosome</dt>
            <dd className={rightColumnClass}>{item.chromosome}</dd>
            <dt className={leftColumnClass}>Start</dt>
            <dd className={rightColumnClass}>{item.startLocation}</dd>
            <dt className={leftColumnClass}>End</dt>
            <dd className={rightColumnClass}>{item.endLocation}</dd>
            <dt className={leftColumnClass}><span className="invisible placeholder">Edit</span></dt>
            <dd className={rightColumnClass}>{editLink} <span className="invisible placeholder">Edit</span></dd>
        </dl></>;
    };

    if (pending) {
        return <LoadingSpinner/>;
    }

    if (!liveData) {
        return null;
    }

    return (
        <>
            {error && <div className="text-danger">{error}</div>}

            <AddEditList
                formatItem={formatLink}
                itemKeyProp="zdbID"
                items={liveData}
                newItem={{
                    assembly: '',
                    chromosome: '',
                    startLocation: '',
                    endLocation: '',
                }}
                setModalItem={setModalData}
                maxLength={1}
            />

            <AddEditDeleteModal {...modalProps} header={hdr}>
                {values && <>
                <FormGroup
                    labelClassName="col-md-3"
                    inputClassName="col-md-9"
                    label="Assembly"
                    id="assembly"
                    field="assembly"
                    tag="select"
                    validate={value => value ? false : 'An assembly is required'}
                >
                    <option value=""/>
                    {assemblies.map(assembly => (
                        <option
                            value={assembly}
                            key={assembly}
                        >{assembly}</option>
                    ))}
                </FormGroup>
                <FormGroup
                    labelClassName="col-md-3"
                    inputClassName="col-md-9"
                    label="Chromosome"
                    id="chromosome"
                    field="chromosome"
                    validate={handleValidateChromosome}
                />
                <FormGroup
                    labelClassName="col-md-3"
                    inputClassName="col-md-9"
                    label="Start Location"
                    id="start-location"
                    field="startLocation"
                    validate={handleValidateLocations}
                />
                <FormGroup
                    labelClassName="col-md-3"
                    inputClassName="col-md-9"
                    label="End Location"
                    id="end-location"
                    field="endLocation"
                    validate={handleValidateLocations}
                />
            </>}
            </AddEditDeleteModal>
        </>
    );
};

MarkerEditChromosomalLocation.propTypes = {
    markerId: PropTypes.string,
    type: PropTypes.string,
};

export default MarkerEditChromosomalLocation;