package com.generator;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code128.Code128Constants;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.EnumMap;
import java.util.Map;

@RestController
@RequestMapping("/generate")
public class SampleRestController {

    @GetMapping("/pdf")
    public String qr(@RequestParam String value) throws IOException {
        try {
            String barcodeString = value.trim();
            Document document = new Document();
            File outputFile = new File("generator.pdf");
            outputFile.createNewFile();
            OutputStream outputStream = new FileOutputStream(outputFile);
            PdfWriter pdfWriter = PdfWriter.getInstance(document, outputStream);

            document.open();
            PdfContentByte pdfContentByte = pdfWriter.getDirectContent();

            Barcode128 barcode128 = new Barcode128();
            barcode128.setCode(barcodeString);
            barcode128.setCodeType(Barcode128.CODE128);
            Image code128Image = barcode128.createImageWithBarcode(pdfContentByte, null, null);
            code128Image.setAbsolutePosition(50, 700);
            code128Image.scalePercent(100);
            document.add(code128Image);

            BarcodeQRCode barcodeQrcode = new BarcodeQRCode(barcodeString, 100, 100, null);
            Image qrcodeImage = barcodeQrcode.getImage();
            qrcodeImage.setAbsolutePosition(50, 500);
            qrcodeImage.scalePercent(100);
            document.add(qrcodeImage);

            document.close();
            return document.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return "welcome";
    }

    @GetMapping("/image")
    public String imageFormat(@RequestParam String value) throws IOException {
        String barcodeString = value.trim();
        Code128Bean barcode128Bean = new Code128Bean();
        barcode128Bean.setCodeset(Code128Constants.CODESET_B);
        final int dpi = 100;
        //Configure the barcode generator
        //adjust barcode width here
        barcode128Bean.setModuleWidth(UnitConv.in2mm(5.0f / dpi));
        barcode128Bean.doQuietZone(false);
        //Open output file
        File outputFile = new File("barcode.png");
        outputFile.createNewFile();
        OutputStream out = new FileOutputStream(outputFile);
        try {
            BitmapCanvasProvider canvasProvider = new BitmapCanvasProvider(
                    out, "image/x-png", dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);

            barcode128Bean.generateBarcode(canvasProvider, barcodeString);

            canvasProvider.finish();
            qrCode(value);
            return "success";
        } finally {
            out.close();
        }
    }

    public void qrCode(String value) throws IOException {
        String myCodeText = value.trim();
        int size = 250;
        File myFile = new File("qrcode.png");
        myFile.createNewFile();
        try {

            Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            // Now with zxing version 3.2.1 you could change border size (white border size to just 1)
            hintMap.put(EncodeHintType.MARGIN, 2); /* default = 4 */
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(myCodeText, BarcodeFormat.QR_CODE, size,
                    size, hintMap);
            int CrunchifyWidth = byteMatrix.getWidth();
            BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth,
                    BufferedImage.TYPE_INT_RGB);
            image.createGraphics();

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
            graphics.setColor(Color.BLACK);

            for (int i = 0; i < CrunchifyWidth; i++) {
                for (int j = 0; j < CrunchifyWidth; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            ImageIO.write(image, "png", myFile);
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\n\nYou have successfully created QR Code.");
    }
}
