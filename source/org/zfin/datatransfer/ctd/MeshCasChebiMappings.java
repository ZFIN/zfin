package org.zfin.datatransfer.ctd;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.zfin.gwt.root.util.StringUtils;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;

@Data
public class MeshCasChebiMappings {
    private Map<String, List<String>> meshCasMap = new HashMap<>();
    private Map<String, List<String>> meshChebiMap = new HashMap<>();
    private Map<String, List<String>> casChebiMap = new HashMap<>();
    private Map<String, List<String>> chebiCasMap = new HashMap<>();
    private Map<String, List<String>> casMeshMap = new HashMap<>();
    private Map<String, List<String>> chebiMeshMap = new HashMap<>();

    // CAS, Relation
    private Set<MeshCasChebiRelation> oneToOneRelations = new HashSet<>();
    private Set<MeshCasChebiRelation> oneToOneWithDCRelations = new HashSet<>();
    private List<MeshCasChebiRelation> oneToManyMeshRelations = new ArrayList<>();
    private Map<String, MeshCasChebiRelation> uniqueRelationMap = new HashMap<>();
    // unique mesh-cas-chebi derived from multi-chebi-cas association
    private Map<String, MeshCasChebiRelation> uniqueRelationWithDCMap = new HashMap<>();
    private Map<String, List<MeshCasChebiRelation>> meshRelationMap = new HashMap<>();
    private Map<String, List<MeshCasChebiRelation>> chebiRelationMap = new HashMap<>();
    private Map<String, List<MeshCasChebiRelation>> casRelationMap = new HashMap<>();
    private Map<String, MeshCasChebiRelation> meshCasRelationMap = new HashMap<>();
    private Map<String, MeshCasChebiRelation> casMeshRelationMap = new HashMap<>();
    private Map<String, MeshCasChebiRelation> chebiCasRelationMap = new HashMap<>();
    private Map<String, MeshCasChebiRelation> casChebiRelationMap = new HashMap<>();

    public void addMeshCasRelations(List<MeshCasChebiRelation> relations) {
        relations.forEach(relation -> casMeshRelationMap.put(relation.getCas(), relation));
        relations.forEach(relation -> meshCasRelationMap.put(relation.getMesh(), relation));
        meshRelationMap = relations.stream().collect(groupingBy(MeshCasChebiRelation::getMesh));
        casRelationMap = relations.stream().filter(relation -> relation.getCas() != null).collect(groupingBy(MeshCasChebiRelation::getCas));

        createAtoBMapping(relations.stream()
            .collect(groupingBy(MeshCasChebiRelation::getMesh)), meshCasMap, MeshCasChebiRelation::getCas);
        createAtoBMapping(relations.stream()
            .filter(relation -> StringUtils.isNotEmpty(relation.getCas()))
            .collect(groupingBy(MeshCasChebiRelation::getCas)), casMeshMap, MeshCasChebiRelation::getMesh);
    }

    public void addChebiCasRelations(List<MeshCasChebiRelation> relations) {
        relations.forEach(relation -> chebiCasRelationMap.put(relation.getChebi(), relation));
        relations.forEach(relation -> casChebiRelationMap.put(relation.getCas(), relation));
        Map<String, List<MeshCasChebiRelation>> chebiRelationMapTemp = relations.stream().collect(groupingBy(MeshCasChebiRelation::getChebi));
        relations.forEach(relation -> {
            List<MeshCasChebiRelation> meshCasRelations = casRelationMap.get(relation.getCas());
            if (CollectionUtils.isEmpty(meshCasRelations)) {
                return;
            }
            List<MeshCasChebiRelation> additionalMappings = new ArrayList<>();
            // either populate chebi onto cas-mesh mappings or create / copy a new one with the new chebi info
            meshCasRelations.forEach(relation1 -> {
                if (relation1.getChebi() == null) {
                    relation1.setChebi(relation.getChebi());
                    relation1.setChebiName(relation.getChebiName());
                } else {
                    MeshCasChebiRelation newMapping = new MeshCasChebiRelation();
                    newMapping.setMesh(relation1.getMesh());
                    newMapping.setMeshName(relation1.getMeshName());
                    newMapping.setCas(relation1.getCas());
                    newMapping.setChebi(relation.getChebi());
                    newMapping.setChebiName(relation.getChebiName());
                    additionalMappings.add(newMapping);
                }
            });
            additionalMappings.forEach(relation1 -> {
                if (!meshCasRelations.contains(relation1)) {
                    meshCasRelations.add(relation1);
                }
            });
        });
        // populate the chebiRelationMap with
        // 1. all matching casRelationMap (from the mesh-cas association)
        // 2. add non-matching (to mesh-cas)) chebi-cas relations
        chebiRelationMapTemp.forEach((chebiID, relationList) -> {
            // add all meshCas mappings to chebiMap that have a common cas term
            relationList.forEach(relation -> {
                List<MeshCasChebiRelation> chebiList = chebiRelationMap.computeIfAbsent(chebiID, k -> new ArrayList<>());
                List<MeshCasChebiRelation> casMeshRelations = casRelationMap.get(relation.getCas());
                if (casMeshRelations != null) {
                    casMeshRelations.forEach(relation1 -> {
                        if (relation1.getChebi().equals(chebiID)) {
                            chebiList.add(relation1);
                        }
                    });
                } else {
                    chebiList.add(relation);
                }
            });
        });

        createAtoBMapping(relations.stream()
            .collect(groupingBy(MeshCasChebiRelation::getChebi)), chebiCasMap, MeshCasChebiRelation::getCas);
        createAtoBMapping(relations.stream()
            .filter(relation -> StringUtils.isNotEmpty(relation.getCas()))
            .collect(groupingBy(MeshCasChebiRelation::getCas)), casChebiMap, MeshCasChebiRelation::getChebi);
    }

    public void generateUniqueMapping() {
        chebiRelationMap.forEach((chebiID, relations) -> {
            MeshCasChebiRelation value = relations.get(0);
            if (relations.size() == 1 && value.getMesh() != null) {
                uniqueRelationMap.put(chebiID, value);
                uniqueRelationMap.put(value.getCas(), value);
                uniqueRelationMap.put(value.getMesh(), value);
                oneToOneRelations.add(value);
            }
        });
    }

    public void generateUniqueMappingWithDanglingCas() {
        chebiRelationMap.forEach((chebiID, relations) -> {
            if (relations.size() > 1) {
                List<MeshCasChebiRelation> list = relations.stream().filter(relation -> relation.getMesh() != null).toList();
                if (CollectionUtils.isNotEmpty(list) && list.size() == 1) {
                    MeshCasChebiRelation value = list.get(0);
                    uniqueRelationWithDCMap.put(chebiID, value);
                    uniqueRelationWithDCMap.put(value.getCas(), value);
                    uniqueRelationWithDCMap.put(value.getMesh(), value);
                    oneToOneWithDCRelations.add(value);
                }

                if (CollectionUtils.isNotEmpty(list) && list.size() > 1) {
                    oneToManyMeshRelations.addAll(list);
                }
            }
        });
    }

    private MeshCasChebiRelation getMeshCasChebiRelation(String casID, String meshID, String chebiID) {
        MeshCasChebiRelation relation = new MeshCasChebiRelation();
        relation.setCas(casID);
        relation.setMesh(meshID);
        relation.setChebi(chebiID);
        relation.setMeshName(casMeshRelationMap.get(casID).getMeshName());
        relation.setChebiName(casChebiRelationMap.get(casID).getChebiName());
        return relation;
    }

    private void createAtoBMapping(Map<String, List<MeshCasChebiRelation>> map, Map<String, List<String>> groupedMap, Function<MeshCasChebiRelation, String> getElement) {
        map.forEach((aID, relations) -> groupedMap.put(aID, relations.stream().map(getElement).filter(StringUtils::isNotEmpty).toList()));
        List<String> keysToBeRemoved = new ArrayList<>();
        groupedMap.forEach((s, strings) -> {
            if (strings.size() == 0)
                keysToBeRemoved.add(s);
        });
        keysToBeRemoved.forEach(groupedMap::remove);
    }

    public Map<String, String> getMeshCasUnique() {
        Map<String, String> map = new HashMap<>();
        meshCasMap.forEach((s, values) -> {
            if (values.size() == 1)
                map.put(s, values.get(0));
        });
        return map;
    }

    public Map<String, String> getChebiCasUnique() {
        Map<String, String> map = new HashMap<>();
        chebiCasMap.forEach((s, values) -> {
            if (values.size() == 1)
                map.put(s, values.get(0));
        });
        return map;
    }

    public Map<String, String> getCasChebiUnique() {
        Map<String, String> map = new HashMap<>();
        casChebiMap.forEach((s, values) -> {
            if (values.size() == 1)
                map.put(s, values.get(0));
        });
        return map;
    }

    public Map<String, List<String>> getChebiCasMultiple() {
        Map<String, List<String>> map = new HashMap<>();
        chebiCasMap.forEach((s, values) -> {
            if (values.size() > 1)
                map.put(s, values);
        });
        return map;
    }

    public Map<String, List<MeshCasChebiRelation>> getChebiCasMulti() {
        Map<String, List<MeshCasChebiRelation>> map = new HashMap<>();
        chebiCasMap.forEach((chebiID, values) -> {
            if (values.size() > 1) {
                List<MeshCasChebiRelation> list = values.stream()
                    .map(casID -> {
                        MeshCasChebiRelation meshCasChebiRelation = casMeshRelationMap.get(casID);
                        if (meshCasChebiRelation == null) {
                            meshCasChebiRelation = new MeshCasChebiRelation();
                            meshCasChebiRelation.setCas(casID);
                        }
                        meshCasChebiRelation.setChebi(chebiID);
                        meshCasChebiRelation.setChebiName(chebiCasRelationMap.get(chebiID).getChebiName());
                        return meshCasChebiRelation;
                    }).toList();
                map.put(chebiID, list);
            }
        });
        return map;
    }

    public Map<String, List<MeshCasChebiRelation>> getCasChebiMulti() {
        Map<String, List<MeshCasChebiRelation>> map = new HashMap<>();
        casChebiMap.forEach((casID, values) -> {
            if (values.size() > 1) {
                List<MeshCasChebiRelation> list = values.stream()
                    .map(chebiID -> {
                        MeshCasChebiRelation meshCasChebiRelation = chebiCasRelationMap.get(chebiID);
                        if (meshCasChebiRelation == null) {
                            meshCasChebiRelation = new MeshCasChebiRelation();
                            meshCasChebiRelation.setCas(chebiID);
                        }
                        meshCasChebiRelation.setChebiName(chebiCasRelationMap.get(chebiID).getChebiName());
                        return meshCasChebiRelation;
                    }).toList();
                map.put(casID, list);
            }
        });
        return map;
    }

    public Map<String, List<String>> getChebiCasMultipleNames() {
        Map<String, List<String>> map = new HashMap<>();
        chebiCasMap.forEach((chebi, values) -> {
            if (values.size() > 1) {
                ///MeshCasChebiRelation term = chebiCasRelations.stream().filter(meshCasChebiRelation -> meshCasChebiRelation.getChebi().equals(chebi)).findFirst().get();
                List<String> meshRelations = new ArrayList<>(values.size());
                values.forEach(cas -> {
                    if (meshCasRelationMap.get(cas) != null)
                        meshRelations.add(meshCasRelationMap.get(cas).getMeshName());
                });
                /// map.put(term.getChebiName(), meshRelations);
            }
        });
        return map;
    }

    public Map<String, String> getCasMeshUnique() {
        Map<String, String> map = new HashMap<>();
        casMeshMap.forEach((s, values) -> {
            if (values.size() == 1)
                map.put(s, values.get(0));
        });
        return map;
    }

    public Map<String, List<String>> getCasChebiMultiple() {
        Map<String, List<String>> map = new HashMap<>();
        casChebiMap.forEach((s, values) -> {
            if (values.size() > 1)
                map.put(s, values);
        });
        return map;
    }

    public String getCasByChebi(String chebiID) {
        return chebiCasRelationMap.get(chebiID).getCas();
    }

    public String getChebiByCas(String casID) {
        return casMeshRelationMap.get(casID).getMesh();
    }

    public String getCasByMesh(String meshID) {
        return meshCasRelationMap.get(meshID).getCas();
    }

    public String getMeshByCas(String casID) {
        return casMeshRelationMap.get(casID).getMesh();
    }

    public String getMeshByChebi(String chebiID) {
        return casMeshRelationMap.get(chebiID).getMesh();
    }

}
