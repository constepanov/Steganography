package org.constepanov.util;

import org.constepanov.stego.CoxAlgorithm;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.constepanov.util.Utils.*;

public class ChartBuilder {

    public static void psnrChart(BufferedImage image) {
        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .theme(Styler.ChartTheme.Matlab)
                .xAxisTitle("%")
                .yAxisTitle("PSNR")
                .build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setHasAnnotations(false);


        int capacity = 4096;
        CoxAlgorithm cox = new CoxAlgorithm(image);
        for (int alpha = 2; alpha <= 10; alpha += 2) {
            List<Double> psnr = new ArrayList<>();
            List<Double> numberOfBits = new ArrayList<>();
            for (double i = 0.1; i < 1; i += 0.1) {
                String message = randomString((int) ((capacity * i) / 8));
                BufferedImage processedImage = cox.insertMessage(message, alpha);
                double p = calculatePSNR(image, processedImage);
                numberOfBits.add(i * 100);
                psnr.add(p);
            }
            chart.addSeries("α=" + alpha, numberOfBits, psnr);
        }

        chart.getStyler().setLegendVisible(true);
        try {
            BitmapEncoder.saveBitmap(chart, "psnr", BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void psnrAlphaChart(BufferedImage image, String message) {
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

    public static void errorsAlphaChart(BufferedImage image, String message) throws IOException {
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
