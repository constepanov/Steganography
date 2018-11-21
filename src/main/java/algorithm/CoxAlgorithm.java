package algorithm;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static algorithm.Utils.*;

public class CoxAlgorithm {

    private BufferedImage image;

    public CoxAlgorithm(BufferedImage image) {
        this.image = image;
    }

    private int getContainerCapacity() {
        return (image.getWidth() / 8) * (image.getHeight() / 8);
    }

    public BufferedImage insertMessage(String message, double alpha) {
        System.out.println("INSERTION");
        int capacity = getContainerCapacity();
        System.out.println("Container capacity = " + capacity + " bits");
        int n = 8;
        int[] bits = getBitArray(message);
        int bitIndex = 0;
        BufferedImage stegoImage = copyImage(image);
        for (int i = 0; i < image.getHeight(); i += 8) {
            for (int j = 0; j < image.getWidth(); j += 8) {
                if (bitIndex < bits.length && bitIndex < capacity) {
                    BufferedImage subImage = image.getSubimage(j, i, n, n);
                    int[][] red = Utils.getComponent(subImage, "red");
                    int[][] green = Utils.getComponent(subImage, "green");
                    int[][] blue = Utils.getComponent(subImage, "blue");
                    int[][] dct = Utils.directDiscreteCosineTransform(red);
                    int[] index = getIndexOfMax(dct);
                    int s = bits[bitIndex] == 0 ? 1 : -1;
                    dct[index[0]][index[1]] = (int) (dct[index[0]][index[1]] + alpha * s);
                    int[][] idctRed = Utils.inverseDiscreteCosineTransform(dct);
                    int[][] rgb = Utils.combineComponents(idctRed, green, blue);
                    setSubImage(stegoImage, rgb, j, i);
                    bitIndex++;
                }
            }
        }
        System.out.println("Embedded " + bitIndex + " bits");
        try {
            Files.write(Path.of("src/main/resources/size.txt"), String.valueOf(bitIndex).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stegoImage;
    }

    public int[] extractMessage(BufferedImage processed) {
        System.out.println("EXTRACTION");
        int n = 8;
        int messageLength = 0;
        try {
            messageLength = Integer.parseInt(Files.readString(Path.of("src/main/resources/size.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int index = 0;
        int[] bits = new int[messageLength];
        for (int i = 0; i < image.getHeight(); i += 8) {
            for (int j = 0; j < image.getWidth(); j += 8) {
                if (index < messageLength) {
                    BufferedImage oSubImage = image.getSubimage(j, i, n, n);
                    BufferedImage pSubImage = processed.getSubimage(j, i, n, n);
                    int[][] oRed = Utils.getComponent(oSubImage, "red");
                    int[][] pRed = Utils.getComponent(pSubImage, "red");
                    int[][] dctOriginalRed = Utils.directDiscreteCosineTransform(oRed);
                    int[][] dctProcessedRed = Utils.directDiscreteCosineTransform(pRed);
                    int[] indexOfMax = getIndexOfMax(dctOriginalRed);
                    int y = indexOfMax[0];
                    int x = indexOfMax[1];
                    bits[index] = dctProcessedRed[y][x] > dctOriginalRed[y][x] ? 0 : 1;
                    index++;
                }
            }
        }
        return bits;
    }
}
