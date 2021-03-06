/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBenchmarkTask;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderRenderTask;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.domains.info.SethlansSettingsInfo;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.forms.setup.subclasses.SetupNode;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import com.google.common.base.Throwables;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.lang3.SystemUtils;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;

/**
 * Created Mario Estrella on 3/9/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class SethlansUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SethlansUtils.class);
    private static final String path = System.getProperty("user.home") + File.separator + ".sethlans";
    private static final File configDirectory = new File(path + File.separator + "config");
    private static final File configFile = new File(configDirectory + File.separator + "sethlans.properties");
    private static Properties sethlansProperties = new Properties();


    public static Image createImage(String image, String description) {
        URL imageURL = null;
        try {
            imageURL = new ClassPathResource(image).getURL();
        } catch (IOException e) {
            LOG.error("Failed Creating Image. Resource not found. " + e.getMessage());
            System.exit(1);
        }
        return new ImageIcon(imageURL, description).getImage();
    }

    public static String getShortUUID() {
        return UUID.randomUUID().toString().substring(0, 13);
    }

    public static String getGPUDeviceString(SetupNode setupNode) {
        if (!setupNode.getSelectedGPUs().isEmpty()) {
            StringBuilder result = new StringBuilder();
            for (GPUDevice gpuDevice : setupNode.getSelectedGPUs()) {
                if (result.length() != 0) {
                    result.append(",");
                }
                result.append(gpuDevice.getDeviceID());
            }
            return result.toString();
        }
        return null;
    }

    public static SethlansServer getCurrentServerInfo() {
        SethlansServer currentServer = new SethlansServer();
        currentServer.setNetworkPort(getPort());
        currentServer.setHostname(getHostname());
        currentServer.setIpAddress(getIP());
        return currentServer;
    }

    public static SethlansNode getCurrentNodeInfo() {
        SethlansNode currentNode = new SethlansNode();
        currentNode.setNetworkPort(getPort());
        currentNode.setHostname(getHostname());
        currentNode.setIpAddress(getIP());
        return currentNode;
    }

    public static boolean writeProperty(SethlansConfigKeys configKey, String value) {
        String comment = "";
        String key = configKey.toString();
        comment = updateComment(comment);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(configFile);
            sethlansProperties.setProperty(key, value);
            //Save Properties to File
            sethlansProperties.store(fileOutputStream, comment);
            LOG.debug("SethlansConfigKey:" + key + " written to " + configFile);
            return true;
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return false;
    }

    public static SethlansSettingsInfo getSettings() {
        SethlansSettingsInfo sethlansSettingsInfo = new SethlansSettingsInfo();
        String mode = SethlansUtils.getProperty(SethlansConfigKeys.MODE.toString());
        sethlansSettingsInfo.setHttpsPort(SethlansUtils.getPort());
        sethlansSettingsInfo.setSethlansIP(SethlansUtils.getIP());
        sethlansSettingsInfo.setRootDir(SethlansUtils.getProperty(SethlansConfigKeys.ROOT_DIR.toString()));
        sethlansSettingsInfo.setBinDir(SethlansUtils.getProperty(SethlansConfigKeys.BINARY_DIR.toString()));
        sethlansSettingsInfo.setScriptsDir(SethlansUtils.getProperty(SethlansConfigKeys.SCRIPTS_DIR.toString()));
        sethlansSettingsInfo.setTempDir(SethlansUtils.getProperty(SethlansConfigKeys.TEMP_DIR.toString()));
        sethlansSettingsInfo.setLogFile(SethlansUtils.getProperty(SethlansConfigKeys.LOGGING_FILE.toString()));
        sethlansSettingsInfo.setMode(SethlansMode.valueOf(mode));
        if (SethlansMode.valueOf(mode) == SethlansMode.SERVER || SethlansMode.valueOf(mode) == SethlansMode.DUAL) {
            sethlansSettingsInfo.setProjectDir(SethlansUtils.getProperty(SethlansConfigKeys.PROJECT_DIR.toString()));
            sethlansSettingsInfo.setBlenderDir(SethlansUtils.getProperty(SethlansConfigKeys.BLENDER_DIR.toString()));
            sethlansSettingsInfo.setBenchmarkDir(SethlansUtils.getProperty(SethlansConfigKeys.BENCHMARK_DIR.toString()));
        }
        if (SethlansMode.valueOf(mode) == SethlansMode.NODE || SethlansMode.valueOf(mode) == SethlansMode.DUAL) {
            sethlansSettingsInfo.setCacheDir(SethlansUtils.getProperty(SethlansConfigKeys.CACHE_DIR.toString()));
        }
        return sethlansSettingsInfo;
    }

    private static String updateComment(String comment) {
        if (configFile.exists()) {
            try (FileInputStream fileIn = new FileInputStream(configFile)) {
                sethlansProperties.load(fileIn);
                comment = updateTimeStamp();
            } catch (IOException e) {
                LOG.error(Throwables.getStackTraceAsString(e));
            }
        }
        return comment;
    }

    public static boolean writeProperty(String key, String value) {
        String comment = "";
        comment = updateComment(comment);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(configFile);
            sethlansProperties.setProperty(key, value);
            //Save Properties to File
            sethlansProperties.store(fileOutputStream, comment);
            LOG.debug(key + " written to " + configFile);
            return true;
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return false;
    }

    public static String updateTimeStamp() {
        Date currentDate = GregorianCalendar.getInstance().getTime();
        return String.format("Updated: %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", currentDate);
    }

    public static boolean fileCheckMD5(File file, String md5) throws IOException {
        HashCode hash = Files.hash(file, Hashing.md5());
        LOG.debug("Hash md5: " + hash.toString() + " JSON md5: " + md5);
        if (hash.toString().equals(md5)) {
            return true;
        }
        return false;
    }

    public static List<ComputeType> getAvailableMethods() {
        List<ComputeType> availableMethods = new ArrayList<>();
        if (GPU.listDevices().size() != 0) {
            availableMethods.add(ComputeType.CPU_GPU);
            availableMethods.add(ComputeType.GPU);
            availableMethods.add(ComputeType.CPU);
        } else {
            availableMethods.add(ComputeType.CPU);
        }
        return availableMethods;

    }

    public static File createArchive(List<String> frameFileNames, String projectRootDir, String projectName) {
        File createdArchive = null;
        try {
            ZipFile zipFile = new ZipFile(projectRootDir + File.separator + projectName + ".zip");
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
            for (String frameFileName : frameFileNames) {
                File frame = new File(frameFileName);
                zipFile.addFile(frame, parameters);
            }
            createdArchive = new File(projectRootDir + File.separator + projectName + ".zip");
        } catch (ZipException e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return createdArchive;
    }

    public static boolean archiveExtract(String toExtract, File extractLocation) {
        File archive = new File(extractLocation + File.separator + toExtract);
        try {
            if (archive.toString().contains("txz")) {
                extractLocation.mkdirs();
                LOG.debug("Extracting " + archive + " to " + extractLocation);
                Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.XZ);
                archiver.extract(archive, extractLocation);
                archive.delete();
                return true;
            }
            if (archive.toString().contains("tar.gz")) {
                extractLocation.mkdirs();
                LOG.debug("Extracting " + archive + " to " + extractLocation);
                Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
                archiver.extract(archive, extractLocation);
                archive.delete();
                return true;
            }
            if (archive.toString().contains("tar.bz2")) {
                extractLocation.mkdirs();
                LOG.debug("Extracting " + archive + " to " + extractLocation);
                Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.BZIP2);
                archiver.extract(archive, extractLocation);
                archive.delete();
                return true;
            } else {
                extractLocation.mkdirs();
                ZipFile archiver = new ZipFile(archive);
                LOG.debug("Extracting " + archive + " to " + extractLocation);
                archiver.extractAll(extractLocation.toString());
                archive.delete();
                return true;
            }

        } catch (ZipException | IOException e) {
            LOG.error("Error extracting archive " + e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
            System.exit(1);
        }

        return false;
    }

    public static void openWebpage(URL url) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(url.toURI());
            } catch (Exception e) {
                LOG.error("Unable to Open Web page" + e.getMessage());
                LOG.error(Throwables.getStackTraceAsString(e));
            }
        }
    }

    public static String getHostname() {
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        int iend = hostname.indexOf(".");
        if (iend != -1) {
            LOG.debug(hostname + " contains a domain name. Removing it.");
            hostname = hostname.substring(0, iend);
        }
        return hostname;
    }

    public static String getProperty(String key) {
        final Properties properties = new Properties();
        try {
            FileInputStream fileIn = new FileInputStream(configFile);
            properties.load(fileIn);

            return properties.getProperty(key);
        } catch (IOException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    public static String getIP() {
        String ip = null;
        final Properties properties = new Properties();
        try {
            if (configFile.exists()) {
                FileInputStream fileIn = new FileInputStream(configFile);
                properties.load(fileIn);
            } else {
                properties.load(new InputStreamReader(new Resources("sethlans.properties").getResource(), "UTF-8"));
            }
            ip = properties.getProperty(SethlansConfigKeys.SETHLANS_IP.toString());
            LOG.debug("IP in current config file equals: " + ip);
            if (ip.equals("null")) {
                if (SystemUtils.IS_OS_LINUX) {
                    // Make a connection to 8.8.8.8 DNS in order to get IP address
                    Socket s = new Socket("8.8.8.8", 53);
                    ip = s.getLocalAddress().getHostAddress();
                    s.close();
                } else {
                    ip = InetAddress.getLocalHost().getHostAddress();
                }
            }
        } catch (UnknownHostException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        } catch (IOException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }

        return ip;
    }

    public static String getCores() {
        return getProperty(SethlansConfigKeys.CPU_CORES.toString());
    }

    public static boolean getFirstTime() {
        boolean firsttime = true;
        final Properties properties = new Properties();
        try {
            if (configFile.exists()) {
                FileInputStream fileIn = new FileInputStream(configFile);
                properties.load(fileIn);
            } else {
                properties.load(new InputStreamReader(new Resources("sethlans.properties").getResource(), "UTF-8"));
            }
            firsttime = Boolean.parseBoolean(properties.getProperty(SethlansConfigKeys.FIRST_TIME.toString()));
        } catch (IOException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return firsttime;
    }

    public static String getPort() {
        String port = null;
        final Properties properties = new Properties();
        try {
            if (configFile.exists()) {
                FileInputStream fileIn = new FileInputStream(configFile);
                properties.load(fileIn);
            } else {
                properties.load(new InputStreamReader(new Resources("sethlans.properties").getResource(), "UTF-8"));
            }
            port = properties.getProperty(SethlansConfigKeys.HTTPS_PORT.toString());
        } catch (IOException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return port;
    }

    public static String getOS() {
        if (SystemUtils.IS_OS_WINDOWS) {
            String arch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

            String realArch = arch.endsWith("64")
                    || wow64Arch != null && wow64Arch.endsWith("64")
                    ? "64" : "32";
            if (realArch.equals("64")) {
                return "Windows64";
            } else {
                return "Windows32";
            }
        }
        if (SystemUtils.IS_OS_MAC) {
            return "MacOS";
        }
        if (SystemUtils.IS_OS_LINUX) {
            if (SystemUtils.OS_ARCH.contains("64")) {
                return "Linux64";
            } else {
                return "Linux32";
            }
        }
        return null;
    }

    public static String assignBlenderExecutable(File binDir, String blenderVersion) {
        String executable = null;
        if (getOS().equals("MacOS")) {
            executable = binDir.toString() + File.separator + "blender-" +
                    blenderVersion + File.separator + "blender.app" + File.separator + "Contents" + File.separator + "MacOS" + File.separator + "blender";
        }
        if (getOS().equals("Windows64") || getOS().equals("Windows32")) {
            executable = binDir.toString() + File.separator + "blender-" + blenderVersion + File.separator + "blender.exe";
        }
        if (getOS().equals("Linux64") || getOS().equals("Linux32")) {
            executable = binDir.toString() + File.separator + "blender-" + blenderVersion + File.separator + "blender";
        }
        LOG.debug("Setting executable to: " + executable);
        return executable;
    }

    public static void addCachedBlenderVersion(String blenderVersion) {
        writeProperty(SethlansConfigKeys.CACHED_BLENDER_BINARIES, blenderVersion);
    }

    public static boolean renameBlenderDir(File renderDir, File binDir, BlenderRenderTask renderTask, String cachedBlenderBinaries) {
        if (SethlansUtils.renameBlenderDirectory(binDir, renderTask.getBlenderVersion())) {
            LOG.debug("Blender executable ready");
            renderTask.setRenderDir(renderDir.toString());
            renderTask.setBlenderExecutable(SethlansUtils.assignBlenderExecutable(binDir, renderTask.getBlenderVersion()));
            if (cachedBlenderBinaries == null || cachedBlenderBinaries.isEmpty() || cachedBlenderBinaries.equals("null")) {
                SethlansUtils.addCachedBlenderVersion(renderTask.getBlenderVersion());
            } else {
                SethlansUtils.appendCachedBlenderVersion(renderTask.getBlenderVersion());
            }
            return true;
        } else {
            LOG.debug("Rename failed.");
            return false;
        }

    }

    public static boolean renameBlenderDir(File benchmarkDir, File binDir, BlenderBenchmarkTask benchmarkTask, String cachedBlenderBinaries) {
        if (SethlansUtils.renameBlenderDirectory(binDir, benchmarkTask.getBlenderVersion())) {
            LOG.debug("Blender executable ready");
            benchmarkTask.setBenchmarkDir(benchmarkDir.toString());
            benchmarkTask.setBlenderExecutable(SethlansUtils.assignBlenderExecutable(binDir, benchmarkTask.getBlenderVersion()));
            if (cachedBlenderBinaries == null || cachedBlenderBinaries.isEmpty() || cachedBlenderBinaries.equals("null")) {
                SethlansUtils.addCachedBlenderVersion(benchmarkTask.getBlenderVersion());
            } else {
                SethlansUtils.appendCachedBlenderVersion(benchmarkTask.getBlenderVersion());
            }
            return true;
        } else {
            LOG.debug("Rename failed.");
            return false;
        }

    }

    public static void appendCachedBlenderVersion(String blenderVersion) {
        String currentVersions = getProperty(SethlansConfigKeys.CACHED_BLENDER_BINARIES.toString());
        writeProperty(SethlansConfigKeys.CACHED_BLENDER_BINARIES, currentVersions + ", " + blenderVersion);
    }

    public static boolean renameBlenderDirectory(File binDir, String blenderVersion) {
        LOG.debug("Starting to rename of extracted directory in " + binDir + " to " + binDir + File.separator + "blender-" + blenderVersion);
        File[] files = binDir.listFiles();
        if (files != null) {
            for (File file : files) {
                LOG.debug("Searching " + binDir + " for extracted directory.");
                LOG.debug("Examining " + file);
                LOG.debug("Searching for directories containing " + blenderVersion);
                if (file.isDirectory() && file.toString().contains(blenderVersion)) {
                    LOG.debug(file.toString());
                    LOG.debug("Directory found, renaming");
                    if (file.renameTo(new File(binDir + File.separator + "blender-" + blenderVersion))) {
                        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX) {
                            try {
                                ProcessBuilder pb = new ProcessBuilder("chmod", "-R", "+x", binDir.toString());
                                pb.start();
                                LOG.debug("Setting blender files as executable.");
                            } catch (IOException e) {
                                LOG.error(Throwables.getStackTraceAsString(e));
                            }
                        }
                    } else {
                        LOG.debug("Unable to rename directory.");
                        return false;
                    }
                    return true;
                }
            }
        } else {
            LOG.debug("Unable to get a list of files");
        }

        return false;
    }

    public static String getVersion() {
        String version = null;

        final Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(new Resources("git.properties").getResource(), "UTF-8"));
            String buildNumber = String.format("%04d", Integer.parseInt(properties.getProperty("git.closest.tag.commit.count")));
            version = properties.getProperty("git.build.version") + "." + buildNumber;
        } catch (IOException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }

        if (version == null) {
            // we could not compute the version so use a blank
            version = "";
        }

        return version;
    }


    public static boolean sortedNodeList(ComputeType computeType, List<SethlansNode> listToSort) {
        if (listToSort.size() > 0) {
            switch (computeType) {
                case CPU:
                    LOG.debug("Sorting by CPU");
                    listToSort.sort(Comparator.comparing(SethlansNode::getCpuRating));
                    return true;
                case GPU:
                    LOG.debug("Sorting by GPU");
                    listToSort.sort(Comparator.comparing(SethlansNode::getCombinedGPURating));
                    return true;
                case CPU_GPU:
                    LOG.debug("Sorting by CPU_GPU");
                    listToSort.sort(Comparator.comparing(SethlansNode::getCombinedCPUGPURating));
                    return true;
            }
        }
        return false;
    }

    public static void listofNodes(ComputeType computeType, List<SethlansNode> listToSort, SethlansNode sethlansNode) {
        switch (computeType) {
            case CPU:
                if (sethlansNode.getComputeType().equals(ComputeType.CPU)) {
                    listToSort.add(sethlansNode);
                }
                if (sethlansNode.getComputeType().equals(ComputeType.CPU_GPU)) {
                    listToSort.add(sethlansNode);
                }
                break;
            case GPU:
                if (sethlansNode.getComputeType().equals(ComputeType.GPU)) {
                    listToSort.add(sethlansNode);
                }
                if (sethlansNode.getComputeType().equals(ComputeType.CPU_GPU)) {
                    listToSort.add(sethlansNode);
                }
                break;
            case CPU_GPU:
                if (sethlansNode.getComputeType().equals(ComputeType.CPU)) {
                    listToSort.add(sethlansNode);
                }
                if (sethlansNode.getComputeType().equals(ComputeType.GPU)) {
                    listToSort.add(sethlansNode);
                }
                if (sethlansNode.getComputeType().equals(ComputeType.CPU_GPU)) {
                    listToSort.add(sethlansNode);
                }
        }
    }

    public static boolean isDirectoryEmpty(File directory) {
        if (directory.isDirectory()) {
            String[] files = directory.list();
            return files == null || files.length <= 0;
        }
        return true;
    }

    public static void serveFile(File file, HttpServletResponse response) {
        try {
            String mimeType = "application/octet-stream";
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            response.setContentLength((int) file.length());
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(inputStream, response.getOutputStream());

        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    public static boolean isCuda(String deviceID) {
        return deviceID.contains("CUDA");
    }


}
