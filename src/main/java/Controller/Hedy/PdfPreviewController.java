package Controller.Hedy;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import java.io.InputStream;
import java.net.URL;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Label;
public class PdfPreviewController {
    public void setPdfFile(String pdfFileName) {
        try {
            if (pdfPagination == null) {
                System.err.println("pdfPagination is null. Check FXML loading and fx:id.");
                return;
            }

            // Load and render PDF pages
            pdfPages = renderAllPdfPages("resources/pdf/" + pdfFileName); // Make sure to pass the correct path

            // Set up pagination
            pdfPagination.setPageCount(pdfPages.size());
            pdfPagination.setCurrentPageIndex(0);

            // Set the page factory for pagination
            pdfPagination.setPageFactory(this::createPage);

        } catch (Exception e) {
            System.err.println("Error rendering PDF: " + e.getMessage());
        }
    }
    @FXML private Pagination pdfPagination;
    private List<Image> pdfPages; // Stores all rendered pages of the PDF


    private List<Image> renderAllPdfPages(String pdfPath) throws Exception {
        List<Image> pages = new ArrayList<>();
        PDDocument document = null;

        try {
            if (pdfPath.startsWith("http://") || pdfPath.startsWith("https://")) {
                // Handle Dropbox or any web-based PDF
                System.out.println("Loading PDF from URL: " + pdfPath);
                java.net.URL url = new java.net.URL(pdfPath.replace("?dl=0", "?raw=1")); // Force direct download
                try (InputStream inputStream = url.openStream()) {
                    document = PDDocument.load(inputStream);
                }
            } else {
                // Handle local PDF file
                document = PDDocument.load(new File(pdfPath));
            }

            PDFRenderer pdfRenderer = new PDFRenderer(document);
            System.out.println("Rendering PDF with " + document.getNumberOfPages() + " pages.");

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(i, 300);
                Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                pages.add(fxImage);
                System.out.println("Successfully rendered page " + (i + 1));
            }

        } catch (Exception e) {
            System.err.println("Error rendering PDF: " + e.getMessage());
            throw e;
        } finally {
            if (document != null) {
                document.close();
            }
        }

        return pages;
    }

    private Node createPage(int pageIndex) {
        try {
            if (pdfPages == null || pdfPages.isEmpty()) {
                System.err.println("No pages available to display.");
                return new Label("No pages available.");
            }

            if (pageIndex < 0 || pageIndex >= pdfPages.size()) {
                System.err.println("Invalid page index: " + pageIndex);
                return new Label("Invalid page index.");
            }

            ImageView imageView = new ImageView(pdfPages.get(pageIndex));
            imageView.setFitWidth(900); // Set the width of the ImageView
            imageView.setPreserveRatio(true); // Preserve aspect ratio

            return imageView;
        } catch (Exception e) {
            System.err.println("Error creating page: " + e.getMessage());
            return new Label("Error displaying page.");
        }
    }

}