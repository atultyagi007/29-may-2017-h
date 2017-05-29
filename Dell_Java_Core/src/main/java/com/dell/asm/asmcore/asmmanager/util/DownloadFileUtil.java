package com.dell.asm.asmcore.asmmanager.util;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.i18n2.exception.AsmCheckedException;
import com.dell.pg.asm.repositorymgr.common.RemoteFile;
import com.dell.pg.asm.repositorymgr.common.RemoteFileShare;
import com.dell.pg.asm.repositorymgr.common.URLConnectionMgr;
import com.dell.pg.asm.repositorymgr.exceptions.RepositoryException;
import com.dell.pg.asm.repositorymgr.utilities.Utilities;

public class DownloadFileUtil {
    
    private static final Logger logger = Logger.getLogger(DownloadFileUtil.class);
    
    public static final String CIFS = "CIFS";
    public static final String NFS = "NFS";
     

    public static void downloadFileFromURL(String fullURL, String localFileName, String downloadDir, String fileType)
            throws RepositoryException {
        logger.debug("Loading File from URL...");

        if (!new File(downloadDir).exists() && !new File(downloadDir).mkdirs())
            throw new RepositoryException("Failed to create local file.", AsmManagerMessages.cannotCreateDownloadDir(fileType, downloadDir));
        try {
            downloadFileFromURLHelper(fullURL, downloadDir + "/" + localFileName, fileType);
        } catch (RepositoryException e1) {
            logger.error("failed to download files from repository");
            throw new RepositoryException("Failed to download file to local.", AsmManagerMessages.unableToAccessRemoteFile(fileType, fullURL));
        }
    }

    /**
     * Download a file from location string to the specified directory. The downloaded file will
     * have the same filename that it has in the original location string. Any location string
     * supported by {@link #getRemoteLocationInfo(String)} may be used, including NFS and CIFS
     * shares, and FTP URLs. The username and password specified (if any) will only be used for
     * CIFS shares.
     *
     * @param location Full remote location path information
     * @param username Username for CIFS share (null if none)
     * @param password Password for CIFS share (null if none)
     * @param downloadDir The directory to download the file to
     * @param fileType An informational tag that will be used in generated error messages.
     * @return the downloaded file on success
     * @throws RepositoryException on any error downloading the file
     */
    public static File downloadFile(String location, String username, String password, File downloadDir, String fileType) throws RepositoryException {
        RemoteLocationInfo info = getRemoteLocationInfo(location);
        File localFile = new File(downloadDir, info.filename);
        switch (info.type) {
        case "NFS":
        case "CIFS":
            String domain = DownloadFileUtil.getDomainfromShareUserName(username);
            String userWithoutDomain = DownloadFileUtil.getUserNamefromShareUserName(username);
            DownloadFileUtil.downloadFileFromShare(location, info.filename, downloadDir.getAbsolutePath(),
                    domain, userWithoutDomain, password, fileType);
            break;
        case "FTP":
        	 DownloadFileUtil.downloadFileFromURL(location, info.filename, downloadDir.getAbsolutePath(), "catalog");
        	break;
        case "FILE":
            try {
                URL url = new URL(location);
                File source = new File(url.toURI());
                Files.copy(source.toPath(), localFile.toPath());
            } catch (MalformedURLException | URISyntaxException e) {
                // TODO: should be user-facing error; in practice should not be caused by user
                // input because file urls are only used internally...
                throw new AsmManagerRuntimeException("Invalid file url " + location);
            } catch (IOException e) {
                throw new RepositoryException("Failed to download file.", AsmManagerMessages.unableToAccessRemoteFile(fileType, location));
            }
            break;
        default:
            downloadFileFromURLHelper(location, localFile.getAbsolutePath(), fileType);
            break;
        }
        return localFile;
    }

    public static void downloadFileFromShare(String fullFilePath, String localFileName, String downloadDir, String domain,
                                             String shareUser, String sharePassword, String fileType) throws RepositoryException {
        logger.debug("Loading" + fileType + " File...");

        RemoteFile rfile = null;
        String filename;
        String savepath;
        InputStream remoteInputStream = null;
        FileOutputStream outStream = null;

        try {
            try {
                int lastSeparator = fullFilePath.lastIndexOf('\\'); // CIFS share
                if (lastSeparator <= 0) {
                    lastSeparator = fullFilePath.lastIndexOf('/'); // NFS share
                }
                filename = fullFilePath.substring(lastSeparator + 1);
                savepath = fullFilePath.substring(0, lastSeparator);
                logger.info("The filename is " + filename);
                logger.info("The path is " + savepath);

                rfile = RemoteFileShare.connectToRemoteShare(savepath, domain, shareUser, sharePassword, filename,
                        false);
            } catch (RepositoryException e) {
                logger.error("Unable to open the remote share for reading", e);
                throw e;
            }

            try {
                remoteInputStream = rfile.getInputStream();
            } catch (IOException e) {
                logger.error("unable to read the remote file", e);
                throw new RepositoryException("Failed to read the remote file", AsmManagerMessages.cannotOpenRemoteFile(fileType, filename));
            }


            if (!new File(downloadDir).exists() && !new File(downloadDir).mkdirs())
                throw new RepositoryException("Failed to create local image file.", AsmManagerMessages.cannotCreateDownloadDir(fileType, downloadDir));
            try {
                outStream = new FileOutputStream(downloadDir + File.separator + localFileName);
            } catch (FileNotFoundException e) {
                logger.error(e);
                throw new RepositoryException("Failed to open local file for writing.", AsmManagerMessages.cannotCreateLocalFile(fileType, filename));
            }
            if (null != remoteInputStream) {
                int num;
                byte[] data = new byte[1024];
                try {
                    while ((num = remoteInputStream.read(data)) != -1) {
                        outStream.write(data, 0, num);
                    }
                } catch (IOException e) {
                    logger.error(e);
                    throw new RepositoryException("Failed to download file.", AsmManagerMessages.unableToAccessRemoteFile(fileType, fullFilePath));
                }
                try {
                    outStream.close();
                    remoteInputStream.close();
                } catch (IOException e) {
                    logger.error("Failed saving the file from the remote share", e);
                    throw new RepositoryException("Failed save the remote file to local.", AsmManagerMessages.cannotSaveLocalFile(fileType, localFileName));
                }
            } else {
                logger.info("Could not read data from the share");
            }


            String basePath = rfile.getPath().replace("/" + filename, ""); // catch nfs format
            basePath = basePath.replace("\\" + filename, ""); // catch unc format

            logger.debug("the basepath is " + basePath);
            logger.debug("the fullfilepath is " + fullFilePath);
        } finally {
            try {
                Utilities.closeStreamQuietly(rfile);
                Utilities.closeStreamQuietly(outStream);
                Utilities.closeStreamQuietly(remoteInputStream);
            } catch (Exception e) {
                logger.error("Could not close the file streams correctly.");
            }
        }
    }

    private static void downloadFileFromURLHelper(String fullURL, String localFile, String fileType) throws RepositoryException {
        logger.debug("Downloading file " + fullURL);
        InputStream inStream = null;
        FileOutputStream outStream = null;
        URLConnectionMgr connectionMgr = new URLConnectionMgr();
        try {
            inStream = connectionMgr.connectToURL(fullURL);
            try {
                outStream = new FileOutputStream(localFile);
            } catch (FileNotFoundException e) {
                logger.error("file not found at the location", e);
                throw new RepositoryException("File not found at location.", AsmManagerMessages.remoteFileDoesNotExist(fileType, fullURL));
            }

            if (null != inStream) {
                int num;
                byte[] data = new byte[1024];
                try {
                    while ((num = inStream.read(data)) != -1) {
                        outStream.write(data, 0, num);
                    }
                } catch (IOException e) {
                    logger.error("Failed downloading file", e);
                    throw new RepositoryException("Failed to Download the remote file.", AsmManagerMessages.cannotExpandRemoteFile(fileType, fullURL));
                }
                try {
                    inStream.close();
                    outStream.close();
                } catch (IOException e) {
                    logger.error("Failed saving the file from the remote share", e);
                    throw new RepositoryException("Failed save the remote file to local.", AsmManagerMessages.cannotSaveLocalFile(fileType, localFile));
                }
            } else {
                logger.info("input stream is null !!! ");
            }
        } catch (RepositoryException e1) {
            logger.error("failed to connect to the share", e1);
            throw new RepositoryException("Failed to download the file", AsmManagerMessages.couldNotConnectToPath(fullURL));
        } finally {
            Utilities.closeStreamQuietly(inStream);
            Utilities.closeStreamQuietly(outStream);
        }
    }

    public static void testConnectionToURL(String fullURL)
            throws RepositoryException {
        logger.debug("Testing Connection to URL...");
        InputStream inStream = null;
        URLConnectionMgr connectionMgr = new URLConnectionMgr();
        try {
            inStream = connectionMgr.connectToURL(fullURL);
        } catch (RepositoryException e1) {
            logger.error("failed to connect to the share", e1);
            throw new RepositoryException("Failed to download the file", AsmManagerMessages.couldNotConnectToPath(fullURL));
        } finally {
            Utilities.closeStreamQuietly(inStream);
        }
    }

    public static void testConnectionToShare(String fullFilePath, String domain, String shareUser, String sharePassword) throws AsmCheckedException {
        logger.debug("Testing connection to share at " + fullFilePath);

        RemoteFile rfile = null;
        String filename;
        String remotePath;
        try {
            try {
                int lastSeparator = fullFilePath.lastIndexOf('\\'); // CIFS share
                if (lastSeparator <= 0) {
                    lastSeparator = fullFilePath.lastIndexOf('/'); // NFS share
                }
                filename = fullFilePath.substring(lastSeparator + 1);
                remotePath = fullFilePath.substring(0, lastSeparator);
                logger.info("The filename is " + filename);
                logger.info("The path is " + remotePath);

                rfile = RemoteFileShare.connectToRemoteShare(remotePath, domain, shareUser, sharePassword, filename,
                        false);
            } catch (RepositoryException e) {
                logger.error("Unable to connect to the remote server", e);
                throw new AsmCheckedException("Could not connect to path specified", AsmManagerMessages.couldNotConnectToPath(fullFilePath));
            }

        } finally {
            try {
                Utilities.closeStreamQuietly(rfile);
            } catch (Exception e) {
                logger.error("Could not close the file stream correctly.");
            }
        }
    }
    
    public static String getType(String path) throws RepositoryException {
        if (path.matches("ftp:/(/[^\\\\]+)+/[^\\\\ || ^/]+"))
            return "FTP";
        else if (path.matches("http:/(/[^\\\\]+)+/[^\\\\ || ^/]+"))
            return "HTTP";
        else if (path.matches("^(\\\\)(\\\\[^/]+)+\\\\[^/ || ^\\\\]+"))
            return "CIFS";
        else if (path.matches("file:/(/[^\\\\]+)+/[^\\\\ || ^/]+")) 
            return "FILE";
        else if(path.matches("(?!file:|http:|ftp:).+[:].+")) 
            return "NFS";     
        else 
            throw new RepositoryException("Unsupported path type:  Only NFS, CIFS, FTP, and HTTP paths are allowed", AsmManagerMessages.unsupportedPathFormat(path));
    }

    public static String getDomainfromShareUserName(String shareUserName) throws RepositoryException {
        if(shareUserName == null || shareUserName.equals(""))
            return null;
        else if (shareUserName.contains("\\")) {
            return shareUserName.substring(0, shareUserName.indexOf('\\'));
        } else if (shareUserName.contains("@")) {
            return shareUserName.substring(0, shareUserName.indexOf('@'));
        } else {
            return null;
        }
    }

    public static String getUserNamefromShareUserName(String shareUserName) throws RepositoryException {
        if(shareUserName == null || shareUserName.equals(""))
            //need to return null for creating NtlmPasswordAuthentication object later down the line, empty string will cause errors
            return null;
        if (shareUserName.contains("\\"))
            return shareUserName.substring(shareUserName.indexOf('\\') + 1);
        else if (shareUserName.contains("@"))
            return shareUserName.substring(shareUserName.indexOf('@') + 1);
        else
            return shareUserName;
    }

    public static class RemoteLocationInfo {
        public final String type;
        public final String path;
        public final String filename;

        public RemoteLocationInfo(String type, String path, String filename) {
            this.type = type;
            this.path = path;
            this.filename = filename;
        }
    }

    /**
     * Given a source string that matches the formatting expectations of {code getType},
     * returns the type, path and filename.
     *
     * @param source Full source path string
     * @return the remote location information
     * @throws RepositoryException If the {code source} string is formatted incorrectly
     */
    public static RemoteLocationInfo getRemoteLocationInfo(String source) throws RepositoryException {
        String type = getType(source);
        char separator;
        switch (type) {
        case "CIFS":
            separator = '\\';
            break;
        case "FILE":
            separator = File.separatorChar;
            break;
        default:
            separator = '/';
        }
        int pos = source.lastIndexOf(separator);
        String basePath = source.substring(0, pos);
        String filename = source.substring(pos + 1);
        return new RemoteLocationInfo(type, basePath, filename);
    }

    public static String getFileNameFromPath(String fullFilePath) throws RepositoryException {
        return getRemoteLocationInfo(fullFilePath).filename;
    }
}
