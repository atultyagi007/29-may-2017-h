/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.files;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Calendar;

import org.apache.log4j.Logger;

public class DeleteRepoDownloadDirectoryVisitor extends SimpleFileVisitor<Path> {

    private static final Logger logger = Logger.getLogger(DeleteRepoDownloadDirectoryVisitor.class);

    private FileTime twelveHoursAgo;

    /**
     * Deletes a directory recursively
     */
    public DeleteRepoDownloadDirectoryVisitor() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -12);
        this.twelveHoursAgo = FileTime.fromMillis(cal.getTime().getTime());
    }

    @Override
    public FileVisitResult visitFile(final Path file,
                                     final BasicFileAttributes attrs) throws IOException {
        try {
            if (shouldDelete(file.getParent())) {
                Files.delete(file);
            }
        } catch (Exception e) {
            logger.error("Failed to delete the file: " + file.toString(), e);
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir,
                                              final IOException exc) throws IOException {
        try {
            if (shouldDelete(dir)) {
                Files.delete(dir);
            }
        } catch (Exception e) {
            logger.error("Failed to delete the directory: " + dir.toString(), e);
        }
        return FileVisitResult.CONTINUE;
    }

    private boolean shouldDelete(Path directory) throws IOException {
        boolean delete = false;
        if (directory != null) {
            final boolean isDirectory = Files.isDirectory(directory);
            final Path fileName = directory.getFileName();
            String fileNameString = null;
            if (fileName != null) {
                fileNameString = fileName.toString();
            }
            if (isDirectory && fileNameString != null && fileNameString.startsWith("repo_download_")) {
                final FileTime lastModified = Files.getLastModifiedTime(directory);
                if (lastModified.compareTo(twelveHoursAgo) <= 0) {
                    delete = true;
                }
            }
        }
        return delete;
    }
}
