package com.krokochik.ideasForum.service.mfa;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.krokochik.ideasForum.repository.UserRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class QRCodeManager {

    @Autowired
    UserRepository userRepository;

    private final ArrayList<String> usersWithQrCode = new ArrayList<>();

    public boolean hasUserQrCode(@NonNull String username) {
        return usersWithQrCode.contains(username);
    }


    /**
     * Generates and saves qr code into db for user
     * */
    public void addQrCode(@NonNull String content, @NonNull String username, int size, int cornerRadius) throws IOException, WriterException {
        BufferedImage qrCodeImage = generateQRCodeImage(content, size);
        BufferedImage roundedImage = roundCorners(qrCodeImage, cornerRadius);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(roundedImage, "png", outputStream);
        byte[] imageBytes = outputStream.toByteArray();

        usersWithQrCode.add(username);
        userRepository.setQRCodeById(imageBytes,
                userRepository.findByUsername(username).getId());
    }

    public void addQrCode(String content, String username) throws IOException, WriterException {
        addQrCode(content, username, 300, 15);
    }

    public void addQrCode(String content, String username, int size) throws IOException, WriterException {
        addQrCode(content, username, size, 0);
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
