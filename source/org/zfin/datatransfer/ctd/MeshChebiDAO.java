package org.zfin.datatransfer.ctd;

import org.hibernate.Session;

import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeshChebiDAO {

    protected Session entityManager;

    public MeshChebiDAO(Session entityManager) {
        this.entityManager = entityManager;
    }

    public MeshChebiMapping find(long id) {
        return entityManager.load(MeshChebiMapping.class, id);

    }

    Map<String, MeshChebiMapping> allMeshChebiMapping;

    public Map<String, MeshChebiMapping> findAll() {
        if (allMeshChebiMapping != null) return allMeshChebiMapping;
        List<MeshChebiMapping> list = forceRetrieveAll();
        allMeshChebiMapping = new HashMap<>();
        list.forEach(meshChebiMapping -> allMeshChebiMapping.put(meshChebiMapping.getMeshID(), meshChebiMapping));
        list.forEach(meshChebiMapping -> allMeshChebiMapping.put(meshChebiMapping.getChebiID(), meshChebiMapping));
        return allMeshChebiMapping;
    }

    public List<MeshChebiMapping> forceRetrieveAll() {
        return entityManager.createQuery("from MeshChebiMapping ", MeshChebiMapping.class).getResultList();
    }

    public boolean dropAll() {
        String tableName = MeshChebiMapping.class.getAnnotation(Table.class).name();
        int numberOfDeletedRecords = entityManager.createSQLQuery("delete from " + tableName).executeUpdate();
        return numberOfDeletedRecords > 0;
    }

    public MeshChebiMapping persist(MeshChebiMapping meshChebiMapping) {
        entityManager.save(meshChebiMapping);
        return meshChebiMapping;
    }

    public void delete(MeshChebiMapping meshChebiMapping) {
        entityManager.delete(meshChebiMapping);
    }

    public String getMeshID(String oboID) {
        MeshChebiMapping meshChebiMapping = findAll().get(oboID);
        return meshChebiMapping == null ? null : meshChebiMapping.getMeshID();
    }
}