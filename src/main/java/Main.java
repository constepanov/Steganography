import algorithm.CoxAlgorithm;
import algorithm.Utils;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static algorithm.Utils.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter container name:");
        String name = scanner.nextLine();
        File f = new File(name);
        BufferedImage image = ImageIO.read(f);
        CoxAlgorithm cox = new CoxAlgorithm(image);
        System.out.println("Select mode:");
        System.out.println("\t1 - insertion");
        System.out.println("\t2 - extraction");
        System.out.println("\t3 - check the resistance of the method to lossy compression");
        System.out.println("\t4 - build charts");
        int mode = Integer.parseInt(scanner.nextLine());
        if (mode == 1) {
            System.out.println("Enter message to insert:");
            String message = scanner.nextLine();
            System.out.println("Enter alpha:");
            int alpha = Integer.parseInt(scanner.nextLine());
            BufferedImage processedImage = cox.insertMessage(message, alpha);
            ImageIO.write(processedImage, "BMP", new File("out.bmp"));
            System.out.println("PSNR = " + Utils.calculatePSNR(image, processedImage));
        } else if (mode == 2) {
            BufferedImage processedImage = ImageIO.read(new File("out.bmp"));
            int[] extractedBits = cox.extractMessage(processedImage);
            String extractedMessage = getMessageFromBits(extractedBits);
            System.out.println("Extracted message: " + extractedMessage);
        } else if (mode == 3) {
            System.out.println("Enter message to insert:");
            String message = scanner.nextLine();
            System.out.println("Enter alpha:");
            int alpha = Integer.parseInt(scanner.nextLine());
            BufferedImage processedImage = cox.insertMessage(message, alpha);
            BufferedImage restoredImage = Utils.compressBMP(processedImage);
            int[] messageBits = getBitArray(message);
            int[] extractedBits = cox.extractMessage(restoredImage);
            String extractedMessage = getMessageFromBits(extractedBits);
            System.out.println("Extracted message: " + extractedMessage);
            System.out.println("Number of errors: " + getErrorsNumber(messageBits, extractedBits));
            System.out.println("PSNR = " + Utils.calculatePSNR(image, restoredImage));
        } else if (mode == 4) {
            String message = Utils.randomString(100);
            System.out.println("Message: " + message);
            psnrAlphaChart(image, message);
            errorsAlphaChart(image, message);
        }
        else {
            throw new IllegalArgumentException("Unknown mode");
        }
    }

    private static void psnrAlphaChart(BufferedImage image, String message) {
        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .theme(Styler.ChartTheme.Matlab)
                .xAxisTitle("α")
                .yAxisTitle("PSNR")
                .build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setHasAnnotations(false);
        chart.getStyler().setLegendVisible(false);
        List<Double> psnrValues = new ArrayList<>();
        List<Integer> alphaValues = new ArrayList<>();
        CoxAlgorithm cox = new CoxAlgorithm(image);
        for (int i = 2; i < 50; i++) {
            BufferedImage processedImage = cox.insertMessage(message, i);
            psnrValues.add(Utils.calculatePSNR(image, processedImage));
            alphaValues.add(i);
        }
        chart.addSeries("mesage", alphaValues, psnrValues);
        try {
            BitmapEncoder.saveBitmap(chart, "psnrAlpha[message=" + message.length() * 8 + "]", BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void errorsAlphaChart(BufferedImage image, String message) throws IOException {
        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .theme(Styler.ChartTheme.Matlab)
                .xAxisTitle("α")
                .yAxisTitle("Number of errors")
                .build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setHasAnnotations(false);
        chart.getStyler().setLegendVisible(false);
        CoxAlgorithm cox = new CoxAlgorithm(image);
        List<Integer> errorsNumberValues = new ArrayList<>();
        List<Integer> alphaValues = new ArrayList<>();
        for (int i = 2; i < 50; i++) {
            BufferedImage processedImage = cox.insertMessage(message, i);
            BufferedImage restoredImage = Utils.compressBMP(processedImage);
            int[] messageBits = getBitArray(message);
            int[] extractedBits = cox.extractMessage(restoredImage);
            int errorsNumber = getErrorsNumber(messageBits, extractedBits);
            alphaValues.add(i);
            errorsNumberValues.add(errorsNumber);
        }
        chart.addSeries("1", alphaValues, errorsNumberValues);
        try {
            BitmapEncoder.saveBitmap(chart, "errorsAlpha[message=" + message.length() * 8 + "]", BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
