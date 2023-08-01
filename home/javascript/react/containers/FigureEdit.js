import React, { useState, useEffect, useRef } from 'react';
import * as FigureService from '../api/figure';
import InlineEditTextarea from "../utils/inline-edit-textarea";
import FileInput from "../utils/file-input";
import FigureUpload from "../components/figure-edit/figure-upload";
import FigureUpdate from "../components/figure-edit/figure-update";
import PropTypes from "prop-types";


function FigureEdit({ pubId }) {
    const [loading, setLoading] = useState(true);
    const [figures, setFigures] = useState([]);
    const [pubCanShowImages, setPubCanShowImages] = useState(false);

    useEffect(() => {
        FigureService.getFigures(pubId)
            .then((response) => {
                setFigures(response.figures);
                setPubCanShowImages(response.pubCanShowImages);
            })
            .finally(() => {
                setLoading(false);
            });
    }, [pubId]);

    function deleteFigure(fig, idx) {
        fig.deleting = true;
        console.log('delete figure', fig);
        FigureService.deleteFigure(fig)
            .then(() => {
                const newFigures = figures.filter(
                    (f) => {
                        return f.zdbId !== fig.zdbId;
                    });
                setFigures(newFigures);
            })
            .finally(() => {
                fig.deleting = false;
            });
    }

    function updateFigureLabel(figure, label) {
        figure.label = label;
        const index = figures.findIndex((f) => f.zdbId === figure.zdbId);

        if (!label) {
            figure.error = 'Label is required';
            const newFigures = [...figures];
            newFigures[index] = figure;
            setFigures(newFigures);
            return Promise.reject({ message: 'Label is required'});
        }

        return FigureService.updateFigure(figure)
            .then((response) => {
                if (index > -1) {
                    const newFigures = [...figures];
                    newFigures[index] = figure;
                    setFigures(newFigures);
                }
            });
    }

    function figureSaved(figures) {
        setFigures(figures);
    }

    return (
        <div>
            <table className="table">
                <thead>
                <tr>
                    <th width="75px">Label</th>
                    <th>Details</th>
                    <th width="75px">Remove</th>
                </tr>
                </thead>
                <tbody>
                {loading ? (
                    <tr>
                        <td className="text-muted text-center" colSpan="3">
                            <i className="fas fa-spinner fa-spin"></i> Loading...
                        </td>
                    </tr>
                ) : figures.length === 0 ? (
                    <tr>
                        <td className="text-muted text-center" colSpan="3">
                            No figures yet.
                        </td>
                    </tr>
                ) : (
                    figures.map((figure, index) => (
                        <tr key={figure.zdbId}>
                            <td>
                                <InlineEditTextarea
                                    text={figure.label}
                                    useIcons={true}
                                    useInput={true}
                                    error={figure.error}
                                    errorClass="error"
                                    wrapperClass="fig-label-edit-container"
                                    onSave={(label) => {return updateFigureLabel(figure, label);}}
                                />
                            </td>
                            <td>
                                <FigureUpdate
                                    figure={figure}
                                    hasPermissions={pubCanShowImages}
                                />
                            </td>
                            <td>
                                <div className="figure-delete-button float-right">
                                    <button
                                        className="btn btn-dense btn-link"
                                        onClick={() => deleteFigure(figure, index)}
                                        disabled={
                                            figure.deleting ||
                                            figure.numExpressionStatements ||
                                            figure.numPhenotypeStatements
                                        }
                                    >
                                        <i className="fas fa-trash"></i>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    ))
                )}
                </tbody>
            </table>
            <h4>Create New Figure</h4>
            <FigureUpload pubId={pubId} figures={figures} hasPermissions={pubCanShowImages} onSave={figureSaved} />
        </div>
    );
}

FigureEdit.propTypes = {
    pubId: PropTypes.string,
};

export default FigureEdit;
