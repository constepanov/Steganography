package org.constepanov.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Math.*;

public class Utils {

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static String randomString(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static double calculatePSNR(BufferedImage original, BufferedImage processed) {
        int w = original.getWidth();
        int h = original.getHeight();
        int[] a = original.getRGB(0, 0, w, h, null, 0, w);
        int[] b = processed.getRGB(0, 0, w, h, null, 0, w);
        double sum = 0;
        for(int i = 0; i < w * h; i++) {
            sum += pow(new Color(a[i]).getRed() - new Color(b[i]).getRed(), 2);
        }
        return 10 * log10((w * h * pow(255, 2)) / sum);
    }

    public static int[][] getComponent(BufferedImage image, String componentName) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[][] rgb = new int[h][w];
        for(int i = 0; i < h; i++) {
            for(int j = 0; j < w; j++) {
                switch (componentName) {
                    case "red": {
                        rgb[i][j] = new Color(image.getRGB(j, i)).getRed();
                        break;
                    }
                    case "green": {
                        rgb[i][j] = new Color(image.getRGB(j, i)).getGreen();
                        break;
                    }
                    case "blue": {
                        rgb[i][j] = new Color(image.getRGB(j, i)).getBlue();
                        break;
                    }
                    default: {
                        rgb[i][j] = image.getRGB(j, i);
                    }
                }
            }
        }
        return rgb;
    }

    public static int[][] combineComponents(int[][] red, int[][] green, int[][] blue) {
        int h = red.length;
        int w = red[0].length;
        int[][] result = new int[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                result[i][j] = new Color(red[i][j], green[i][j], blue[i][j]).getRGB();
            }
        }
        return result;
    }

    public static int[][] getRGB(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[][] rgb = new int[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                rgb[i][j] = image.getRGB(j, i);
            }
        }
        return rgb;
    }

    public static BufferedImage compressBMP(BufferedImage image) throws IOException {
        Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = (ImageWriter)iter.next();
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionQuality(1f);
        File file = new File("compressedImage.jpeg");
        FileImageOutputStream output = new FileImageOutputStream(file);
        writer.setOutput(output);
        IIOImage img = new IIOImage(image, null, null);
        writer.write(null, img, iwp);
        writer.dispose();
        BufferedImage bi = ImageIO.read(new File("compressedImage.jpeg"));
        ImageIO.write(bi, "BMP", new File("restoredImage.bmp"));
        return ImageIO.read(new File("restoredImage.bmp"));
    }

    private int[][] combineImage(ArrayList<int[][]> blockList, int width, int height){
        if(width < 8 || height < 8){
            throw new IllegalArgumentException("Width and height parameters are too small.");
        }
        int blockIndex = 0;
        int blockLength = 8;
        int[][] resultArray = new int[width][height];
        for (int i = 0; i < height; i += blockLength) {
            for (int j = 0; j < width; j += blockLength) {
                int[][] block = blockList.get(blockIndex);
                for (int k = 0; k < blockLength; k++) {
                    System.arraycopy(block[k], 0, resultArray[k + i], j, blockLength);
                }
                blockIndex++;
            }
        }
        return resultArray;
    }

    public static int[][] directDiscreteCosineTransform(int[][] block) {
        if (block.length != 8 || block[0].length != 8) {
            throw new IllegalArgumentException("Matrix should be 8 x 8 pixels");
        }
        int n = block.length;
        int[][] result = new int[n][n];
        double coeff1, coeff2;
        for (int k = 0; k < n; k++) {
            for (int l = 0; l < n; l++) {
                coeff1 = (k == 0) ? sqrt((double) 1 / n) : sqrt((double) 2 / n);
                coeff2 = (l == 0) ? sqrt((double) 1 / n) : sqrt((double) 2 / n);

                double sum = 0.0;
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        sum += block[i][j] * cos(((2 * i + 1) * PI) / (2 * n) * k) * cos(((2 * j + 1) * PI) / (2 * n) * l);
                    }
                }
                result[k][l] = (int) (coeff1 * coeff2 * sum);
            }
        }
        return result;
    }

    public static int[][] inverseDiscreteCosineTransform(int[][] block) {
        if (block.length != 8 || block[0].length != 8) {
            throw new IllegalArgumentException("Matrix should be 8x8 pixels");
        }
        int n = block.length;
        int[][] result = new int[n][n];
        double coeff1, coeff2;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double sum = 0.0;
                for (int k = 0; k < n; k++) {
                    for (int l = 0; l < n; l++) {
                        coeff1 = (k == 0) ? sqrt((double) 1 / n) : sqrt((double) 2 / n);
                        coeff2 = (l == 0) ? sqrt((double) 1 / n) : sqrt((double) 2 / n);
                        sum += coeff1 * coeff2 * block[k][l] * cos(((2 * i + 1) * PI) / (2 * n) * k) * cos(((2 * j + 1) * PI) / (2 * n) * l);
                    }
                }
                result[i][j] = clip(sum);
            }
        }
        return result;
    }

    private static int clip(double x) {
        if (x < 0) {
            return 0;
        } else if (x > 255) {
            return 255;
        } else {
            return (int) x;
        }
    }

    public static List<BufferedImage> splitImage(BufferedImage image, int blockSize) {
        if (image.getHeight() < 8 || image.getWidth() < 8) {
            throw new IllegalArgumentException("Image size too small");
        }
        int w = image.getWidth();
        int h = image.getHeight();
        List<BufferedImage> resultSubImages = new ArrayList<>();
        for (int i = 0; i < h; i += 8) {
            for (int j = 0; j < w; j += 8) {
                BufferedImage subImage = image.getSubimage(i, j, blockSize, blockSize);
                resultSubImages.add(subImage);
            }
        }
        return resultSubImages;
    }

    public static List<int[][]> splitImage(int[][] image, int blockLength) {
        if (image.length < 8 || image[0].length < 8) {
            throw new IllegalArgumentException("Image size too small");
        }
        int w = image[0].length;
        int h = image.length;
        List<int[][]> resultSubImages = new ArrayList<>();
        for (int i = 0; i < h; i += 8) {
            for (int j = 0; j < w; j+= 8) {
                int[][] subImage = new int[blockLength][blockLength];
                for (int k = 0; k < blockLength; k++) {
                    System.arraycopy(image[k + i], j, subImage[k],0, blockLength);
                }
                resultSubImages.add(subImage);
            }
        }
        return resultSubImages;
    }

    public static int getErrorsNumber(int[] originalBits, int[] processedBits) {
        return (int) IntStream.range(0, originalBits.length).filter(i -> originalBits[i] != processedBits[i]).count();
    }

    public static void setSubImage(BufferedImage image, int[][] subImage, int x, int y) {
        for (int i = 0; i < subImage.length; i++) {
            for (int j = 0; j < subImage[0].length; j++) {
                image.setRGB(x + j, y + i, subImage[i][j]);
            }
        }
    }

    public static int[] getBitArray(String message) {
        int[] bits = new int[message.length() * 8];
        int index = 0;
        for (byte b : message.getBytes()) {
            String binaryString = Integer.toBinaryString(b & 255 | 256).substring(1);
            for (Character c : binaryString.toCharArray()) {
                bits[index] = Character.digit(c, 2);
                index++;
            }
        }
        return bits;
    }

    public static String getMessageFromBits(int[] bits) {
        StringBuilder message = new StringBuilder();
        StringBuilder symbol = new StringBuilder();
        for (int bit : bits) {
            symbol.append(bit);
            if (symbol.length() == 8) {
                message.appendCodePoint(Integer.parseInt(symbol.toString(), 2));
                symbol.setLength(0); // Clear builder
            }
        }
        return message.toString();
    }

    public static BufferedImage copyImage(BufferedImage src) {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        int[] rgb = src.getRGB(0, 0,src.getWidth(), src.getHeight(), null, 0, src.getWidth());
        copy.setRGB(0, 0, src.getWidth(), src.getHeight(), rgb, 0, src.getWidth());
        return copy;
    }

    public static int[] getIndexOfMax(int[][] array) {
        int x = 1;
        int y = 0;
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array.length; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                if (array[i][j] > array[y][x]) {
                    x = j;
                    y = i;
                }
            }
        }
        return new int[] {y, x};
    }
}
