package com.example.betweenus.backend.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

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

    /**
     * Helper method to copy headers from input stream to an output stream
     * @param headerFields
     * @param resp
     */
    public static void copyHeaders(Map<String, List<String>> headerFields, HttpServletResponse resp) {
        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
            final String headerKey = entry.getKey();
            final List<String> headerValues = entry.getValue();

            if (headerValues != null) {
                StringBuilder builder = new StringBuilder(headerValues.size());
                for (int i = 0; i < headerValues.size(); i++) {
                    builder.append(headerValues.get(i));
                    if (i < headerValues.size() - 1) builder.append(", ");
                }
                resp.addHeader(headerKey, builder.toString());
            }
        }
    }
}
