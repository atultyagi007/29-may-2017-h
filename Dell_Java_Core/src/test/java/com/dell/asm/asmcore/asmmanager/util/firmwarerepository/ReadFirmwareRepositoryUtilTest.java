package com.dell.asm.asmcore.asmmanager.util.firmwarerepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareBundleEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareComponentEntity;
import com.dell.pg.asm.catalogmgr.exceptions.CatalogException;
import com.dell.pg.asm.repositorymgr.exceptions.RepositoryException;
import com.google.common.io.Resources;

public class ReadFirmwareRepositoryUtilTest {

    private void assertValidSoftwareComponent(SoftwareComponentEntity comp) {
        assertNotNull("Invalid package id", comp.getPackageId());
        assertNotSame("Invalid package id", "missing", comp.getPackageId());
        assertNotNull("Invalid path", comp.getPath());
        assertNotSame("Invalid path", "missing", comp.getPath());
        assertNotNull("Invalid md5 hash", comp.getHashMd5());
        assertNotSame("Invalid md5 hash", "missing", comp.getHashMd5());
    }

    @Test
    public void testParseCatalog() throws IOException, URISyntaxException {
        URL url = Resources.getResource("firmware/CustomCatalog.xml");
        File file = new File(url.toURI());
        FirmwareRepositoryEntity repo = ReadFirmwareRepositoryUtil.loadFirmwareRepositoryFromFile(file);
        assertNotNull(repo);
        assertNull(repo.getBaseLocation());
        assertEquals("ASM 7.7 Test", repo.getName());
        assertNotNull(repo.getSoftwareComponents());
        for (SoftwareComponentEntity component : repo.getSoftwareComponents()) {
            assertValidSoftwareComponent(component);
        }
    }

    @Test
    public void testParseDellCatalog() throws IOException, URISyntaxException, CatalogException, RepositoryException {
        URL url = Resources.getResource("catalog.xml.gz");
        File gzFile = new File(url.toURI());
        File file = gunzipFile(gzFile);

        FirmwareRepositoryEntity repo = ReadFirmwareRepositoryUtil.loadFirmwareRepositoryFromFile(file);
        assertNotNull(repo);
        assertEquals(ReadFirmwareRepositoryUtil.DEFAULT_CATALOG_NAME, repo.getName());
        assertEquals("ftp.dell.com", repo.getBaseLocation());
        repo.setDiskLocation(file.getParent());
        repo.setFilename(file.getName());

        Set<SoftwareComponentEntity> components = repo.getSoftwareComponents();
        assertNotNull(components);
        for (SoftwareComponentEntity component : components) {
            assertValidSoftwareComponent(component);
        }
    }

    @Test
    public void testUpdateSoftwareBundlesByCatalog() throws URISyntaxException {
            URL url = Resources.getResource("firmware/CustomCatalog.xml");
            File file = new File(url.toURI());
            FirmwareRepositoryEntity repo = ReadFirmwareRepositoryUtil.loadFirmwareRepositoryFromFile(file);
            assertNotNull(repo);
            assertNull(repo.getBaseLocation());
            assertEquals("ASM 7.7 Test", repo.getName());
            assertNotNull(repo.getSoftwareComponents());
            assertNotNull(repo.getSoftwareBundles());
            Map<String,List<SoftwareComponentEntity>> components = new HashMap<String,List<SoftwareComponentEntity>>();
            for (SoftwareComponentEntity component : repo.getSoftwareComponents()) {
                    assertValidSoftwareComponent(component);
                    ReadFirmwareRepositoryUtil.addToMap(components,component);
                }
            Map<String, SoftwareBundleEntity> bundles = new HashMap<String,SoftwareBundleEntity>();
            for (SoftwareBundleEntity bundle : repo.getSoftwareBundles()) {
                    bundles.put(bundle.getName(),bundle);
                    assertTrue("SoftwareBundle " + bundle.getName() + " should have components during build of map!",bundle.getSoftwareComponents().size() > 0);
                    bundle.getSoftwareComponents().clear();
                    assertTrue("SoftwareBundle " + bundle.getName() + " should not have components after clear!",bundle.getSoftwareComponents().size() == 0);
                }
            ReadFirmwareRepositoryUtil.updateSoftwareBundlesByCatalog(bundles,components,file);
            for (SoftwareBundleEntity bundle : repo.getSoftwareBundles()) {
                    assertTrue("SoftwareBundle " + bundle.getName() + " should have components after update!",bundle.getSoftwareComponents().size() > 0);
                }
        }


    private File gunzipFile(File gzFile) throws IOException {
        File file = null;
        InputStream in = null;
        OutputStream out = null;

        try {
            file = File.createTempFile(this.getClass().getSimpleName(), "xml");
            file.deleteOnExit();

            in = new GZIPInputStream(new FileInputStream(gzFile));
            out = new FileOutputStream(file);

            int len;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
        return file;
    }
}
