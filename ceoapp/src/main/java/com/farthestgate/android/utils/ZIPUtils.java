package com.farthestgate.android.utils;

import android.app.Activity;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.helper.FileHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created by Hanson on 30/07/2014.
 */
public class ZIPUtils {

    public static final Integer BUFFER = 2048;
    private static  Activity context;
    private static int attempt = 0;
    public static void zip(File directory, File zipFile, Activity context, int attempt) throws Exception {
        ZIPUtils.context = context;
        File crcDir = directory;
        try {
            URI base = directory.toURI();
            Deque<File> queue = new LinkedList<File>();
            queue.push(directory);
            OutputStream out = new FileOutputStream(zipFile);
            Closeable res = out;
            Boolean zipped = true;
            try {
                ZipOutputStream zout = new ZipOutputStream(out);
                res = zout;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                String dateStamp = simpleDateFormat.format(new Date());
                zout.putNextEntry(new ZipEntry(dateStamp + "/"));
                while (!queue.isEmpty()) {
                    directory = queue.pop();
                    for (File file : directory.listFiles()) {
                        if (!file.getName().contains(".zip")) {
                            String name = base.relativize(file.toURI()).getPath();
                            if (file.isDirectory()) {
                                queue.push(file);
                                name = name.endsWith("/") ? dateStamp + "/" + name : name + "/";
                                zout.putNextEntry(new ZipEntry(name));
                            } else {
                                name = dateStamp + "/" + name;
                                zout.putNextEntry(new ZipEntry(name));
                                copy(file, zout);
                                zout.closeEntry();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                try {
                    zipped = false;
                    CroutonUtils.error(context, "Failed to zip the directory:" + crcDir.getName());
                    throw ex;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } finally {
                res.close();
            }
            if (zipped) {
                ZIPUtils.attempt = attempt + 1;
                doCRC(crcDir, zipFile);
            }
        } catch (Exception ex) {
            CeoApplication.LogError("Failed to zip the directory:" + crcDir.getName() + " Caused by:" + ex.getMessage());
            throw ex;
        }
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }
    private static void doCRC(File directory, File file) throws Exception {
        ZipFile zipFile = null;
        long checksum = 0;
        boolean crcCheckFailed = false;
        try {
            zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                String entryName = entry.getName();
                // get the CRC-32 checksum of the uncompressed entry data, or -1 if not known
                long crc = entry.getCrc();
                checksum = checksum + crc;
                if (crc == -1) {
                    crcCheckFailed = true;
                    break;
                }
            }
            long sourceSize = folderSize(directory);
            if ((sourceSize > checksum) || crcCheckFailed) {
                file.delete();
                if (attempt < 3) {
                    CeoApplication.LogError("Attempt:" + attempt + " CRC check failed for zip file:" + file.getName() + " Zip file deleted and trying again");
                    CroutonUtils.error(context, "Attempt:" + attempt + " CRC check failed for zip file:" + file.getName());
                    zip(directory, file, context, attempt);
                } else {
                    CeoApplication.LogError("Last attempt for CRC check failed for zip file:" + file.getName() + " Zip file deleted");
                    CroutonUtils.error(context, "Last attempt for CRC check failed for zip file:" + file.getName());
                    throw new Exception("Last attempt for CRC check failed for zip file:" + file.getName());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            if (file.exists())
                file.delete();
            if (attempt < 3) {
                CeoApplication.LogError("Attempt:" + attempt + " CRC check failed for zip file:" + file.getName() + " Caused by:" + ex.getMessage() + " Zip file deleted and trying again");
                CroutonUtils.error(context, "Attempt:" + attempt + " CRC check failed for zip file:" + file.getName());
                zip(directory, file, context, attempt);
            } else {
                CeoApplication.LogError("Last attempt for CRC check failed for zip file:" + file.getName() + " Caused by:" + ex.getMessage() + " Zip file deleted");
                CroutonUtils.error(context, "Last attempt for CRC check failed for zip file:" + file.getName());
                throw new Exception("Last attempt CRC check failed for zip file:" + file.getName());
            }

        } finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    //how to call this method--> long cs = getChecksumValue(new CRC32(), args[0]);
    public static long getChecksumValue(Checksum checksum, String fName) {
        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(fName));
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = is.read(bytes)) >= 0) {
                checksum.update(bytes, 0, len);
            }
            is.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return checksum.getValue();
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
    }

    private static void copy(File file, OutputStream out) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            copy(in, out);
        } finally {
            in.close();
        }
    }

}
