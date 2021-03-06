package com.generator;

import com.google.common.base.Strings;
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
import org.krysalis.barcode4j.HumanReadablePlacement;
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
import java.util.Random;

@RestController
@RequestMapping("/generate")
public class SampleRestController {

    @GetMapping("/pdf")
    public String pdfFormat(@RequestParam(defaultValue = "") String value) throws IOException {
        Document document = new Document();
        try {
            String barcodeString = value;
            if(Strings.isNullOrEmpty(barcodeString)) {
                return "empty value";
            }
            //Adding filename
            File outputFile = new File("target/classes/static/" + "barcode.pdf");
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

            BarcodeQRCode barcodeQrcode = new BarcodeQRCode(barcodeString, 50, 50, null);
            Image qrcodeImage = barcodeQrcode.getImage();
            qrcodeImage.setAbsolutePosition(50, 500);
            qrcodeImage.scalePercent(100);
            document.add(qrcodeImage);


            return "success";
        } catch (FileNotFoundException e) {
            System.out.println("Exception: " + e.toString());
        } catch (DocumentException e) {
            System.out.println("Exception: " + e.toString());
        } catch (RuntimeException e) {
            System.out.println("Exception: " + e.toString());
        } finally {
            document.close();
        }
        return "error";
    }

    @GetMapping("/image")
    public String imageFormat(@RequestParam(defaultValue = "") String value) throws IOException {
        String barcodeString = value;
        if(Strings.isNullOrEmpty(barcodeString)) {
            return "empty value";
        }
        Code128Bean barcode128Bean = new Code128Bean();
        barcode128Bean.setCodeset(Code128Constants.CODESET_B);
        final int dpi = 100;

        barcode128Bean.setBarHeight(15.0);
        barcode128Bean.setFontSize(8);
        barcode128Bean.setQuietZone(5.0);
        barcode128Bean.doQuietZone(true);
        barcode128Bean.setModuleWidth(UnitConv.in2mm(3.2f / dpi));
        barcode128Bean.setMsgPosition(HumanReadablePlacement.HRP_BOTTOM);

        File outputFile = new File("target/classes/static/" + "barcode.png");
        outputFile.createNewFile();
        OutputStream out = new FileOutputStream(outputFile);
        try {
            BitmapCanvasProvider canvasProvider = new BitmapCanvasProvider(
                    out, "image/x-png", dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);
            barcode128Bean.generateBarcode(canvasProvider, barcodeString);

            canvasProvider.finish();
            qrCode(value);
            return "success";
        } catch (FileNotFoundException e) {
            System.out.println("Exception: " + e.toString());
        } catch (RuntimeException e) {
            System.out.println("Exception: " + e.toString());
        } finally {
            out.close();
        }
        return "error";
    }

    public void qrCode(String value) throws IOException {
        String myCodeText = value.trim();
        int size = 250;
        File myFile = new File("target/classes/static/" + "qrcode.png");
        myFile.createNewFile();
        try {

            Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            hintMap.put(EncodeHintType.MARGIN, 2); /* default = 4 */
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(myCodeText, BarcodeFormat.QR_CODE, size,
                    size, hintMap);
            int width = byteMatrix.getWidth();
            BufferedImage image = new BufferedImage(width, width,
                    BufferedImage.TYPE_INT_RGB);
            image.createGraphics();

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, width);
            graphics.setColor(Color.BLACK);

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < width; j++) {
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
        } catch (RuntimeException e) {
            System.out.println("Exception: " + e.toString());
        }
        System.out.println("\n\nYou have successfully created QR Code.");
    }

}
