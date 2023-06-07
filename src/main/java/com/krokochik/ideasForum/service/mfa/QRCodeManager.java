package com.krokochik.ideasForum.service.mfa;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class QRCodeManager {
    private HashMap<String, String> savedQrCodes = new HashMap<>();

    /**
     * @param name Username or filename. It is assumed that filenames are unique and there is only one qr-code for user
     */
    public boolean deleteQrCode(String name) {
        HashMap<String, String> newMap = new HashMap<>(savedQrCodes);
        savedQrCodes.forEach((username, filename) -> {
            if (username.equals(name) || filename.equals(name))
                newMap.remove(username);
        });
        if (!savedQrCodes.equals(newMap)) {
            savedQrCodes = newMap;
            return true;
        }
        return false;
    }

    public Optional<String> getQrCode(String username) {
        return Optional.ofNullable(savedQrCodes.get(username));
    }

    public void addQrCode(String content, String filename, String username, int size, int cornerRadius) throws IOException, WriterException {
        BufferedImage qrCodeImage = generateQRCodeImage(content, size);
        BufferedImage roundedImage = roundCorners(qrCodeImage, cornerRadius);

        File outputFile = new File("app/src/main/resources/dynamic/qr/" + filename + ".png"); // Укажите путь и имя файла для сохранения
        System.out.println(System.getProperty("user.dir"));
        ImageIO.write(roundedImage, "png", outputFile);
        savedQrCodes.put(username, filename);
    }

    public void addQrCode(String content, String filename, String username) throws IOException, WriterException {
        addQrCode(content, filename, username, 300, 15);
    }

    public void addQrCode(String content, String filename, String username, int size) throws IOException, WriterException {
        addQrCode(content, filename, username, size, 0);
    }

    private BufferedImage generateQRCodeImage(String text, int size) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size, hints);
        int qrCodeSize = bitMatrix.getWidth();
        BufferedImage qrCodeImage = new BufferedImage(qrCodeSize, qrCodeSize, BufferedImage.TYPE_INT_RGB);
        qrCodeImage.createGraphics();

        Graphics2D graphics = (Graphics2D) qrCodeImage.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, qrCodeSize, qrCodeSize);
        graphics.setColor(Color.BLACK);

        for (int x = 0; x < qrCodeSize; x++) {
            for (int y = 0; y < qrCodeSize; y++) {
                if (bitMatrix.get(x, y)) {
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        }

        return qrCodeImage;
    }

    private BufferedImage roundCorners(BufferedImage image, int cornerRadius) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage roundedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = roundedImage.createGraphics();
        g2.setClip(new RoundRectangle2D.Double(0, 0, width, height, cornerRadius, cornerRadius));
        g2.drawImage(image, 0, 0, width, height, null);
        g2.dispose();

        return roundedImage;
    }

}
