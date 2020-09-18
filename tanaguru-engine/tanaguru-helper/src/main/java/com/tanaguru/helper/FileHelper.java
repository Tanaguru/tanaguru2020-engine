package com.tanaguru.helper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

/**
 * @author rcharre
 */
public class FileHelper {
    private ClassLoader classLoader;

    public FileHelper(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Return the file content from a file corresponding to the given filename
     *
     * @param filename the name of the file
     * @return A String containing the content of the file
     * @throws IOException If error occurs when reading the file
     */
    public String getFileContentFromResources(String filename) throws IOException {
        return getFileContent(getFileFromResources(filename));
    }

    /**
     * Get a file from the module Resource folder
     *
     * @param fileName The name of the file
     * @return The corresponding File
     */
    public File getFileFromResources(String fileName) {
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile());
        }
    }

    /**
     * Get the content of a File
     *
     * @param file The file to get content of
     * @return A String containing the content of the file
     * @throws IOException If error occurs when reading the file
     */
    public static String getFileContent(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }
}
