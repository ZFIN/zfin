import React from 'react';
import { useWizard } from '../state/WizardContext';
import WizardField from '../components/WizardField';

const Step06_GeneInfo = () => {
    const { state, updateField } = useWizard();
    const li = state.currentLineIndex;
    const mi = state.currentMutationIndex;
    const gi = state.currentGeneIndex;
    const gene = state.lines[li].mutations[mi].genes[gi];

    const path = (field) => ['lines', li, 'mutations', mi, 'genes', gi, field];

    return (
        <div>
            <WizardField
                label='Mutated gene'
                id='mutatedGene'
                value={gene.mutatedGene}
                onChange={(val) => updateField(path('mutatedGene'), val)}
            />
            <WizardField
                label='Linkage group'
                id='linkageGroup'
                value={gene.linkageGroup}
                onChange={(val) => updateField(path('linkageGroup'), val)}
            />
            <WizardField
                label='GenBank accession # for genomic DNA'
                id='genbankGenomic'
                value={gene.genbankGenomic}
                onChange={(val) => updateField(path('genbankGenomic'), val)}
            />
            <WizardField
                label='GenBank accession # for cDNA'
                id='genbankCdna'
                value={gene.genbankCdna}
                onChange={(val) => updateField(path('genbankCdna'), val)}
            />
            <WizardField
                label='ZDB gene # (ZFIN ID)'
                id='zdbGeneId'
                value={gene.zdbGeneId}
                onChange={(val) => updateField(path('zdbGeneId'), val)}
            />
        </div>
    );
};

export default Step06_GeneInfo;
