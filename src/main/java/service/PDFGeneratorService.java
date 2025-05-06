package service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import entite.Commande;
import entite.Pack;
import entite.User;
import service.PackService;
import service.UserService;

import java.util.Date;

public class PDFGeneratorService {
    private final PackService packService = new PackService();
    private final UserService userService = new UserService();

    public void generatePurchaseReceipt(Commande commande) throws Exception {
        // Fetch user and pack details
        User user = userService.getUserByIdForPack(commande.getUserId());
        Pack pack = packService.getPackById(commande.getPackId());

        // Define output path (e.g., resources/pdf/receipt_userId_packId_date.pdf)
        String fileName = String.format("resources/pdf/receipt_%d_%d_%s.pdf",
                commande.getUserId(), commande.getPackId(), new java.text.SimpleDateFormat("yyyyMMdd").format(new Date()));
        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Add content (example)
        document.add(new Paragraph("Reçu d'achat"));
        document.add(new Paragraph("Utilisateur : " + (user != null ? user.getEmail() : "Inconnu")));
        document.add(new Paragraph("Pack : " + (pack != null ? pack.getName() : "Inconnu")));
        document.add(new Paragraph("Prix : " + commande.getAmount() + " TND"));
        document.add(new Paragraph("Méthode de paiement : " + commande.getPaymentMethod()));
        document.add(new Paragraph("Date d'achat : " + commande.getCommandeDate()));
        document.add(new Paragraph("Date d'expiration : " + commande.getExpiryDate()));


        document.close();
    }
}