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
    const isEdit = modalData && modalData.zdbID;
    const hdr = type;
    const assemblies = ['GRCz11', 'GRCz10', 'Zv9'];
    const evidenceCodes = ['IC','TAS'];

    const {
        value: liveData,
        setValue: setLiveData,
        pending,
    } = useFetch(`/action/marker/${markerId}/chromosomal-location`);

    const handleOnSuccess = () => {
        setModalData(null);
    };

    const parseIntIgnoreCommas = (value) => {
        if (typeof value != "string") {
            return "";
        }
        return parseInt(value.replace(/,/g,""));
    };

    const handleValidateChromosome = () => {
        if (!values) {
            return false;
        }
        const startLocation = parseIntIgnoreCommas(values.startLocation);
        const endLocation = parseIntIgnoreCommas(values.endLocation);

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
        const startLocation = parseIntIgnoreCommas(values.startLocation);
        const endLocation = parseIntIgnoreCommas(values.endLocation);
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
        editUrl: isEdit ? `/action/marker/${markerId}/chromosomal-location/${modalData.zdbID}` : '',
        deleteUrl: isEdit ? `/action/marker/${markerId}/chromosomal-location/${modalData.zdbID}` : '',
        onSuccess: handleOnSuccess,
        items: liveData,
        setItems: setLiveData,
        itemKeyProp: 'zdbID',
        defaultValues: modalData,
        isEdit
    });

    const formatLink = (item, editLink) => {
        const leftColumnClass="col-sm-4 col-md-3 col-lg-2";
        const rightColumnClass="col-sm-8 col-md-9 col-lg-10";

        return <><dl className="row">
            <dt className={leftColumnClass}>Location</dt>
            <dd className={rightColumnClass}>Chr {item.chromosome}: {item.startLocation.toLocaleString()} - {item.endLocation.toLocaleString()} ({item.assembly}) <em>{item.locationEvidence}</em> {editLink}</dd>
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
                    entityID: markerId,
                    assembly: '',
                    chromosome: '',
                    startLocation: '',
                    endLocation: '',
                    locationEvidence: '',
                }}
                setModalItem={setModalData}
                maxLength={1}
            />

            {/* Hack: for some reason, without this span, the delete action throws a JS exception. Maybe the formatLink function? */}
            <span className="invisible"></span>

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
                    <FormGroup
                        labelClassName="col-md-3"
                        inputClassName="col-md-9"
                        label="Evidence Code"
                        id="evidence-code"
                        field="locationEvidence"
                        tag="select"
                        validate={value => value ? false : 'An evidence code is required'}
                    >
                        <option value=""/>
                        {evidenceCodes.map(code => (
                            <option
                                value={code}
                                key={code}
                            >{code}</option>
                        ))}
                    </FormGroup>                    
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