package com.tanaguru.helper;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class ImageHelper {
    public static BufferedImage getFromByteArray(byte[] picture) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(picture);
        BufferedImage image = ImageIO.read(bis);
        bis.close();
        return image;
    }

    public static BufferedImage appendImages(BufferedImage image1, BufferedImage image2) {
        BufferedImage result = image2;
        if(image1 != null) {
            int maxWidth = Math.max(image1.getWidth(), image2.getWidth());
            int totalHeight = image1.getHeight() + image2.getHeight();
            BufferedImage concatImage = new BufferedImage(maxWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = concatImage.createGraphics();
            g2d.drawImage(image1, 0, 0, null);
            g2d.drawImage(image2, 0, image1.getHeight(), null);
            g2d.dispose();
            result = concatImage;
        }
        return result;
    }

    public static BufferedImage scaleImage(BufferedImage image, float scale){
        int width = Math.round(image.getWidth() * scale);
        int height = Math.round(image.getHeight() * scale);
        BufferedImage outputImage = new BufferedImage(width, height, image.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(image, 0, 0, width, height, null);
        g2d.dispose();
        return outputImage;
    }

    public static byte[] compressImage(BufferedImage picture, float quality, String format) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        if (!writers.hasNext())
            throw new IllegalStateException("No writers found");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter writer = writers.next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();

        // compress to a given quality
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        writer.write(null, new IIOImage(picture, null, null), param);
        baos.close();
        ios.close();
        return baos.toByteArray();
    }
}
