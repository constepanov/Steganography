import org.constepanov.stego.CoxAlgorithm;
import org.constepanov.util.Utils;
import org.constepanov.util.ChartBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import static org.constepanov.util.Utils.*;

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
            //ChartBuilder.psnrAlphaChart(image, message);
            //ChartBuilder.errorsAlphaChart(image, message);
            ChartBuilder.psnrChart(image);
        }
        else {
            throw new IllegalArgumentException("Unknown mode");
        }
    }
}
