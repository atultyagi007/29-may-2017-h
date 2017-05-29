package com.dell.asm.asmcore.asmmanager.db;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.firmware.RepositoryState;
import com.dell.asm.asmcore.asmmanager.client.firmware.RepositoryStatus;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;

public class GenericDAOIT {

    FirmwareRepositoryEntity buildFirmwareRepositoryEntity(String name) {
        FirmwareRepositoryEntity entity = new FirmwareRepositoryEntity();
        entity.setName(name);
        entity.setSourceLocation("Embedded");
        entity.setDownloadStatus(RepositoryStatus.PENDING);
        entity.setState(RepositoryState.COPYING);
        entity.setDiskLocation("/foo/bar");
        entity.setFilename("Catalog.xml");
        return entity;
    }

    @Test
    public void testCrud() {
        // Create
        GenericDAO dao = GenericDAO.getInstance();
        FirmwareRepositoryEntity entity = buildFirmwareRepositoryEntity("Test Name");
        FirmwareRepositoryEntity created = dao.create(entity);
        assertNotNull(created);
        String repoId = created.getId();
        assertNotNull(repoId);

        // Read
        FirmwareRepositoryEntity got = dao.get(repoId, FirmwareRepositoryEntity.class);
        assertNotNull(got);
        assertEquals(entity.getName(), got.getName());

        // Update
        String newName = "Changed name";
        created.setName(newName);
        FirmwareRepositoryEntity updated = dao.update(created);
        assertEquals(newName, updated.getName());

        // Delete
        dao.delete(repoId, FirmwareRepositoryEntity.class);

        // Make sure it's gone
        FirmwareRepositoryEntity got2 = dao.get(repoId, FirmwareRepositoryEntity.class);
        assertNull(got2);
    }

    @Test
    public void testGetByNameStartsWith() {
        GenericDAO dao = GenericDAO.getInstance();
        FirmwareRepositoryEntity entity = buildFirmwareRepositoryEntity("Test name");
        FirmwareRepositoryEntity created = dao.create(entity);
        assertNotNull(created);

        String repoId = created.getId();
        List<FirmwareRepositoryEntity> matches = dao.getByNameStartsWith("Test name", FirmwareRepositoryEntity.class);
        assertNotNull(matches);
        boolean found = false;
        for (FirmwareRepositoryEntity match : matches) {
            if (repoId.equals(match.getId())) {
                found = true;
                break;
            }
        }
        assertTrue(found);

        // Clean up
        dao.delete(repoId, FirmwareRepositoryEntity.class);
    }
}
