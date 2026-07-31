import React, { useState, useEffect } from 'react';
import * as FigureService from '../api/figure';
import InlineEditTextarea from '../utils/inline-edit-textarea';
import FigureUpload from '../components/figure-edit/figure-upload';
import FigureUpdate from '../components/figure-edit/figure-update';
import PropTypes from 'prop-types';
import PubFileCheck from './PubFileCheck';


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

    useEffect(() => {
        setFigureTooltips();
    }, [loading]);

    function setFigureTooltips() {
        const buttons = document.querySelectorAll('.btn-dense');

        Array.from(buttons).forEach((button, index) => {
            const figure = figures[index];
            const tooltip = figureDeleteTooltip(figure);
            if (tooltip) {
                $(button.parentElement).tooltip(tooltip);
            }
        });
    }

    function figureDeleteTooltip(figure) {
        const numExpr = figure.numExpressionStatements;
        const numPheno = figure.numPhenotypeStatements;

        let title = '';
        if (numExpr || numPheno) {
            title = 'This figure cannot be deleted because it is used in ';
            if (numExpr) {
                title += '<b>' + numExpr + ' expression</b> ';
            }
            if (numExpr && numPheno) {
                title += 'and ';
            }
            if (numPheno) {
                title += '<b>' + numPheno + ' phenotype</b> ';
            }
            title += 'statements';
        }
        if (title) {
            return {
                title: title,
                html: true,
                placement: 'left'
            };
        }
        return null;
    }

    function deleteFigure(fig) {
        fig.deleting = true;
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

    async function updateFigureLabel(figure, label) {
        figure.label = label;
        const index = figures.findIndex((f) => f.zdbId === figure.zdbId);

        if (!label) {
            figure.error = 'Label is required';
            const newFigures = [...figures];
            newFigures[index] = figure;
            setFigures(newFigures);
            throw new Error('Label is required');
        }

        try {
            const serverResponse = await FigureService.updateFigure(figure);
            if (serverResponse.code) {
                //error code received
                throw new Error(serverResponse.message);
            }

            if (index > -1) {
                const newFigures = [...figures];
                newFigures[index] = figure;
                setFigures(newFigures);
            }
        } catch (error) {
            console.error('Error updating figure:', error);
            throw error;
        }
    }

    function figureSaved(figures) {
        setFigures(figures);
    }

    return (
        <div>
            <PubFileCheck pubId={pubId} />
            <table className='table'>
                <thead>
                    <tr>
                        <th width='75px'>Label</th>
                        <th>Details</th>
                        <th width='75px'>Remove</th>
                    </tr>
                </thead>
                <tbody>
                    {loading ? (
                        <tr>
                            <td className='text-muted text-center' colSpan='3'>
                                <i className='fas fa-spinner fa-spin'/> Loading...
                            </td>
                        </tr>
                    ) : figures.length === 0 ? (
                        <tr>
                            <td className='text-muted text-center' colSpan='3'>
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
                                        errorClass='error'
                                        wrapperClass='fig-label-edit-container'
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
                                    <div className='figure-delete-button float-right'>
                                        <button
                                            className='btn btn-dense btn-link'
                                            onClick={() => deleteFigure(figure, index)}
                                            disabled={
                                                figure.deleting ||
                                                figure.numExpressionStatements ||
                                                figure.numPhenotypeStatements
                                            }
                                        >
                                            <i className='fas fa-trash'/>
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
