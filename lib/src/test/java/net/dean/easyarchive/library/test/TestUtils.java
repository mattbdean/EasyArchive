package net.dean.easyarchive.library.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class TestUtils {
    // md5Byte() and md5() adapted from http://www.rgagnon.com/javadetails/java-0416.html
    private static byte[] md5Byte(File f) throws IOException, NoSuchAlgorithmException {
        InputStream fis =  new FileInputStream(f);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) complete.update(buffer, 0, numRead);
        } while (numRead != -1);
        fis.close();
        return complete.digest();
    }

    public static String md5(File f) throws Exception {
        byte[] checksum = md5Byte(f);
        StringBuilder result = new StringBuilder();
        for (byte b : checksum)
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }
}