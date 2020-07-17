package com.farthestgate.android.helper;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Hanson on 30/07/2014.
 */
public class FileHelper {


    public interface OnCopy {
        void OnFileTransferUpdate(Integer copiedMb);

        void OnFileCopied();
    }

    OnCopy onCopyListener;

    public FileHelper(Context target) {
        onCopyListener = (OnCopy) target;
    }

    public void CopyDirectory(File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            File targetDir = new File(targetLocation.getAbsolutePath() + "/" + sourceLocation.getName());
            if (!targetDir.exists())
                targetDir.mkdir();

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {

                CopyDirectory(new File(sourceLocation, children[i]),
                        new File(targetDir, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);

            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            onCopyListener.OnFileCopied();
        }

    }

    public static void deleteZeroByteFile(File directory) {
        if (directory.exists()) {
            if (directory.isDirectory()) {
                for (File file : directory.listFiles()) {
                    if (file.length() == 0) {
                        file.delete();
                    }
                }
            } else {
                if (directory.length() == 0) {
                    directory.delete();
                }
            }
        }
    }

    public static void deleteDirectory(File directory) {
        if (directory.exists()) {
            if (directory.isDirectory()) {
                if (directory.listFiles().length == 0) {
                    directory.delete();
                } else {
                    File[] subFiles = directory.listFiles();
                    for (File file : subFiles) {
                        deleteDirectory(file);
                    }
                    if (directory.listFiles().length == 0) {
                        directory.delete();
                    }
                }
            } else {
                directory.delete();
            }
        }
    }
}
