package com.example.betweenus.backend.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ninjakiki on 2/18/16.
 */
public class ServletUtils {
    private static int CHUNK_SIZE = 4096;

    /**
     * Helper method to copy bytes from an input stream into an output stream
     * @param in The stream to read from
     * @param out The stream to write to
     * @return the nunber of bytes copied
     * @throws IOException If any error occurs processing data from the input stream
     */
    public static int copyBytes(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[CHUNK_SIZE];

        int len = 0;
        int count = 0;

        // read in one chunk at a time until the stream is empty
        while ((len = in.read(buffer)) != -1) {
            count += len;
            out.write(buffer, 0, len);
        }

        return count;
    }
}
