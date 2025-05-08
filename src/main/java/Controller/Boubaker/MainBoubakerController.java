package Controller.Boubaker;

import entite.Commande;
import entite.Pack;
import entite.Session;
import entite.User;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import service.CommandeService;
import service.PackService;
import service.UserService;
import service.PDFGeneratorService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class MainBoubakerController {
    private final PackService packService = new PackService();
    private final CommandeService commandeService = new CommandeService();
    private final PDFGeneratorService pdfService = new PDFGeneratorService();
    private User currentUser;
    private boolean isProcessingBalanceUpdate = false;
    private static final Gson gson = new Gson();
    private static final Type listType = new TypeToken<List<String>>() {}.getType();
    private static final String STRIPE_SECRET_KEY = "sk_test_51QyM2SJTeskTAr7Fk3CfBeSC52BJt0GNGH2c1RzEJCkIDbkUWlptN61QhE7DB51dVJ1EPBmTH2bamMwI2ddJtXod005yWQQL7C";
    private static final String CHAT_HISTORY_DIR = "chat_histories";
    private String userChatHistory = "";

    // Known payment codes for simulation
    private static final Map<String, String> PAYMENT_METHOD_CODES = new HashMap<>();
    static {
        PAYMENT_METHOD_CODES.put("42424242424242", "PayPal");
        PAYMENT_METHOD_CODES.put("55555555555544", "Mastercard");
    }

    @FXML private HBox packContainer;
    @FXML private Label balanceLabel;
    @FXML private Button addBalanceButton;

    @FXML
    public void initialize() {
        Stripe.apiKey = STRIPE_SECRET_KEY;
        System.out.println("MainBoubakerController: initialize() started");

        Session session = Session.getInstance();
        System.out.println("Session state: userId=" + session.getUserId() +
                ", email=" + session.getEmail() +
                ", isActive=" + session.isActive());

        if (!session.isActive() || session.getUserId() <= 0) {
            System.err.println("Aucun utilisateur connecté.");
            showAlert(Alert.AlertType.ERROR, "Connexion Requise", "Veuillez vous connecter pour accéder aux packs.");
            return;
        }

        UserService userService = new UserService();
        currentUser = userService.getUserByIdForPack(session.getUserId());
        if (currentUser == null) {
            System.err.println("Utilisateur ID " + session.getUserId() + " non trouvé.");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non trouvé. Veuillez vous reconnecter.");
            session.clearSession();
            return;
        }

        List<String> roles = currentUser.getRoles() != null ? currentUser.getRoles() : List.of();
        if (!roles.contains("ROLE_PARENT")) {
            System.err.println("Accès refusé : Utilisateur ID " + session.getUserId() + " n'a pas le rôle ROLE_PARENT.");
            showAlert(Alert.AlertType.ERROR, "Accès Refusé", "Seuls les parents peuvent acheter des packs.");
            return;
        }

        // Load chat history on reconnection
        loadChatHistory(session.getUserId());

        updateBalanceDisplay();

        if (packContainer == null) {
            System.err.println("Erreur : packContainer est null. Vérifiez fx:id dans main.fxml.");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement du conteneur de packs.");
            return;
        }

        packContainer.setAlignment(Pos.CENTER);
        packContainer.setSpacing(40);
        packContainer.setPadding(new Insets(10));
        packContainer.getChildren().clear();

        try {
            List<Pack> packs = packService.getAllPacks();
            if (packs == null) {
                System.err.println("PackService a retourné null.");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement des packs depuis la base de données.");
                return;
            }

            packs = packs.stream().filter(pack -> pack != null).collect(Collectors.toList());
            System.out.println("Chargé " + packs.size() + " packs valides depuis la base de données.");
            if (packs.isEmpty()) {
                System.out.println("Aucun pack valide trouvé dans la base de données.");
                showAlert(Alert.AlertType.WARNING, "Aucun Pack", "Aucun pack disponible dans la base de données.");
                return;
            }

            if (packs.size() != 2) {
                System.err.println("Erreur : Exactement 2 packs requis (Basic et Premium).");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Les achats sont désactivés car il doit y avoir exactement deux packs disponibles.");
                return;
            }

            Pack basic = packs.stream().filter(p -> p.getName().equals("Basic")).findFirst().orElse(null);
            Pack premium = packs.stream().filter(p -> p.getName().equals("PREMIUM")).findFirst().orElse(null);
            if (basic == null || premium == null || basic.getPrice() >= premium.getPrice()) {
                System.err.println("Erreur : Packs invalides ou Basic >= Premium.");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Configuration des packs invalide.");
                return;
            }

            for (int i = 0; i < packs.size(); i++) {
                Pack pack = packs.get(i);
                System.out.println("Ajout de la carte de pack : id=" + pack.getId() + ", nom=" + pack.getName() +
                        ", prix=" + pack.getPrice() + ", fonctionnalités=" + pack.getFeatures());
                String styleClass = i == 1 ? "pack-card premium-card" : "pack-card";
                VBox card = createCard(pack, styleClass);
                packContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des packs : " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement des packs : " + e.getMessage());
        }
    }

    private void updateBalanceDisplay() {
        if (currentUser != null) {
            double balance = currentUser.getBalance() != null ? currentUser.getBalance() : 0.0;
            balanceLabel.setText(String.format("Solde : %.2f TND", balance));
            System.out.println("Affichage mis à jour : Solde=" + balance + " TND (stored as USD)");
        }
    }

    private VBox createCard(Pack pack, String styleClass) {
        VBox card = new VBox();
        card.getStyleClass().addAll(styleClass.split(" "));
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setSpacing(15);
        card.setMinWidth(250);
        card.setPrefWidth(250);
        card.setMaxWidth(300);

        Text nameText = new Text(pack.getName());
        nameText.getStyleClass().add("pack-name");
        if (styleClass.contains("premium-card")) {
            nameText.getStyleClass().add("premium-title");
        }

        Text priceText = new Text(String.format("%.2f TND", pack.getPrice()));
        priceText.getStyleClass().add("pack-price");

        String featuresDisplay = "Aucune fonctionnalité";
        if (pack.getFeatures() != null && !pack.getFeatures().isEmpty()) {
            List<String> featuresList = Arrays.stream(pack.getFeatures().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            featuresDisplay = String.join(", ", featuresList);
        }
        Text featuresText = new Text(featuresDisplay);
        featuresText.getStyleClass().add("pack-features");
        featuresText.setWrappingWidth(200);

        Button button = new Button("Commander");
        button.getStyleClass().add("order-button");
        // Add icon to the button
        try {
            ImageView iconView = new ImageView(new Image("https://img.icons8.com/?size=100&id=116993&format=png&color=000000"));
            iconView.setFitWidth(60);
            iconView.setFitHeight(60);
            iconView.setPreserveRatio(true);
            button.setGraphic(iconView);
        } catch (Exception e) {
            System.err.println("Error loading order icon: " + e.getMessage());
        }
        button.setOnAction(event -> orderPack(pack));

        card.getChildren().addAll(nameText, priceText, featuresText, button);
        return card;
    }

    @FXML
    private void addBalance() {
        if (isProcessingBalanceUpdate) {
            System.out.println("Mise à jour du solde déjà en cours, ignorée.");
            return;
        }

        Dialog<String> codeDialog = new Dialog<>();
        codeDialog.setTitle("Méthode de Paiement");
        codeDialog.setHeaderText("Entrez votre code de paiement");

        ButtonType confirmButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        codeDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Customize Confirm button with icon
        Button confirmButton = (Button) codeDialog.getDialogPane().lookupButton(confirmButtonType);
        try {
            ImageView iconView = new ImageView(new Image("https://img.icons8.com/?size=100&id=94194&format=png&color=000000"));
            iconView.setFitWidth(55);
            iconView.setFitHeight(55);
            iconView.setPreserveRatio(true);
            confirmButton.setGraphic(iconView);
        } catch (Exception e) {
            System.err.println("Error loading confirm button icon: " + e.getMessage());
        }

        // Customize Cancel button with icon
        Button cancelButton = (Button) codeDialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        try {
            ImageView iconView = new ImageView(new Image("https://img.icons8.com/?size=100&id=97745&format=png&color=000000"));
            iconView.setFitWidth(55);
            iconView.setFitHeight(55);
            iconView.setPreserveRatio(true);
            cancelButton.setGraphic(iconView);
        } catch (Exception e) {
            System.err.println("Error loading cancel button icon: " + e.getMessage());
        }

        TextField codeField = new TextField();
        codeField.setPromptText("Entrez un code de paiement à 14 chiffres");
        codeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                codeField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 14) {
                codeField.setText(newValue.substring(0, 14));
            }
        });

        VBox codeContent = new VBox(10);
        codeContent.getChildren().addAll(new Label("Code de paiement (14 chiffres):"), codeField);
        codeDialog.getDialogPane().setContent(codeContent);

        codeDialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        codeDialog.getDialogPane().getStyleClass().add("dialog-pane");

        codeDialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return codeField.getText().trim();
            }
            return null;
        });

        Optional<String> codeResult = codeDialog.showAndWait();
        if (!codeResult.isPresent() || codeResult.get().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le code de paiement est requis.");
            return;
        }

        String paymentCode = codeResult.get();
        if (paymentCode.length() != 14 || !paymentCode.matches("\\d{14}")) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le code de paiement doit contenir exactement 14 chiffres.");
            return;
        }

        String paymentMethod = PAYMENT_METHOD_CODES.get(paymentCode);
        if (paymentMethod == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Code de paiement invalide. Veuillez vérifier et réessayer.");
            return;
        }

        TextInputDialog amountDialog = new TextInputDialog();
        amountDialog.setTitle("Ajouter Solde");
        amountDialog.setHeaderText("Paiement via " + paymentMethod + "\nEntrez le montant à ajouter (TND)");
        amountDialog.setContentText("Montant :");

        // Customize OK button with icon
        Button okButton = (Button) amountDialog.getDialogPane().lookupButton(ButtonType.OK);
        try {
            ImageView iconView = new ImageView(new Image("https://img.icons8.com/?size=100&id=97745&format=png&color=000000"));
            iconView.setFitWidth(20);
            iconView.setFitHeight(20);
            iconView.setPreserveRatio(true);
            okButton.setGraphic(iconView);
        } catch (Exception e) {
            System.err.println("Error loading OK button icon: " + e.getMessage());
        }

        amountDialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        amountDialog.getDialogPane().getStyleClass().add("dialog-pane");

        Optional<String> amountResult = amountDialog.showAndWait();
        amountResult.ifPresent(amountStr -> {
            try {
                isProcessingBalanceUpdate = true;
                double amountTND = Double.parseDouble(amountStr);
                if (amountTND <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Le montant doit être positif.");
                    return;
                }

                double amountUSD = amountTND;
                boolean paymentSuccess = processPayment(amountUSD, paymentMethod);
                if (!paymentSuccess) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du paiement.");
                    return;
                }

                UserService userService = new UserService();
                double currentBalance = currentUser.getBalance() != null ? currentUser.getBalance() : 0.0;
                System.out.println("Mise à jour du solde : userId=" + currentUser.getId() +
                        ", ancien solde=" + currentBalance + " TND (stored as USD), ajout=" + amountTND + " TND (stored as USD)");
                userService.addBalance(currentUser.getId(), amountTND);

                currentUser = userService.getUserByIdForPack(currentUser.getId());
                if (currentUser == null) {
                    System.err.println("Erreur : Utilisateur non trouvé après mise à jour du solde.");
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour du solde. Veuillez réessayer.");
                    return;
                }

                updateBalanceDisplay();
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        String.format("Solde mis à jour : %.2f TND ajouté via %s.", amountTND, paymentMethod));
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Montant invalide.");
            } catch (Exception e) {
                System.err.println("Erreur lors de l'ajout de solde : " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout de solde : " + e.getMessage());
            } finally {
                isProcessingBalanceUpdate = false;
            }
        });
    }

    private boolean processPayment(double amount, String paymentMethod) {
        System.out.println("Simulation de paiement réussie : Méthode=" + paymentMethod +
                ", montant=" + amount + " USD (equivalent à " + amount + " TND), statut=succeeded");
        return mockStripePayment(amount);
    }

    private boolean mockStripePayment(double amount) {
        try {
            long amountInCents = (long) (amount * 100);

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("USD")
                    .setPaymentMethod("pm_card_visa")
                    .setConfirm(true)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            System.out.println("Paiement Stripe réussi : PaymentIntent ID=" + paymentIntent.getId() +
                    ", montant=" + (paymentIntent.getAmount() / 100.0) + " USD (equivalent to " + amount + " TND), statut=" + paymentIntent.getStatus());
            return "succeeded".equals(paymentIntent.getStatus());
        } catch (StripeException e) {
            System.err.println("Erreur lors du paiement Stripe : " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur de Paiement", "Échec du paiement via Stripe : " + e.getMessage());
            return false;
        }
    }

    private void orderPack(Pack pack) {
        try {
            Session session = Session.getInstance();
            int userId = session.getUserId();
            System.out.println("Tentative de commande du pack : " + pack.getName() +
                    ", userId=" + userId + ", isActive=" + session.isActive());

            if (userId <= 0 || !session.isActive()) {
                System.err.println("Impossible de commander le pack : Aucun utilisateur connecté.");
                showAlert(Alert.AlertType.ERROR, "Connexion Requise", "Veuillez vous connecter pour passer une commande.");
                return;
            }

            UserService userService = new UserService();
            currentUser = userService.getUserByIdForPack(userId);
            if (currentUser == null) {
                System.err.println("Impossible de commander le pack : Utilisateur ID " + userId + " non trouvé.");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non trouvé. Veuillez vous reconnecter.");
                session.clearSession();
                return;
            }

            List<String> roles = currentUser.getRoles() != null ? currentUser.getRoles() : List.of();
            if (!roles.contains("ROLE_PARENT")) {
                System.err.println("Accès refusé : Utilisateur ID " + userId + " n'a pas le rôle ROLE_PARENT.");
                showAlert(Alert.AlertType.ERROR, "Accès Refusé", "Seuls les parents peuvent acheter des packs.");
                return;
            }

            List<Commande> commandes = commandeService.getAllCommandes();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date currentDate = new Date();
            for (Commande cmd : commandes) {
                if (cmd.getUserId() == userId && "Active".equals(cmd.getStatus())) {
                    try {
                        Date expiryDate = sdf.parse(cmd.getExpiryDate());
                        if (expiryDate.before(currentDate)) {
                            cmd.setStatus("Cancelled");
                            commandeService.updateCommande(cmd);
                            userService.resetFeaturesUnlocked(userId);
                            System.out.println("Commande ID=" + cmd.getId() + " marquée comme Cancelled, fonctionnalités réinitialisées.");
                        }
                    } catch (ParseException e) {
                        System.err.println("Erreur lors de la vérification de l'expiration : " + e.getMessage());
                    }
                }
            }

            List<Commande> activeCommandes = getActiveCommandes(userId);
            if (activeCommandes.isEmpty()) {
                userService.resetFeaturesUnlocked(userId);
                System.out.println("Aucune commande active pour userId=" + userId + ", fonctionnalités réinitialisées.");
            }

            boolean hasBasic = activeCommandes.stream()
                    .anyMatch(c -> {
                        Pack p = packService.getPackById(c.getPackId());
                        return p != null && p.getName().equals("Basic");
                    });
            boolean hasPremium = activeCommandes.stream()
                    .anyMatch(c -> {
                        Pack p = packService.getPackById(c.getPackId());
                        return p != null && p.getName().equals("PREMIUM");
                    });

            if (hasPremium && pack.getName().equals("Basic")) {
                System.err.println("Impossible de commander le pack : Rétrogradation non autorisée.");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Vous ne pouvez pas passer du pack Premium au pack Basic.");
                return;
            }

            if (activeCommandes.stream().anyMatch(c -> {
                Pack p = packService.getPackById(c.getPackId());
                return p != null && p.getName().equals(pack.getName());
            })) {
                System.err.println("Impossible de commander le pack : Pack " + pack.getName() + " déjà actif.");
                showAlert(Alert.AlertType.ERROR, "Pack Actif",
                        "Vous possédez déjà le pack " + pack.getName() + ".");
                return;
            }

            double priceToPayTND = pack.getPrice();
            double priceToPayUSD = priceToPayTND;
            String expiryDateStr = sdf.format(new Date(currentDate.getTime() + (pack.getValidityPeriod() * 24L * 60 * 60 * 1000)));
            boolean isUpgrade = hasBasic && pack.getName().equals("PREMIUM");

            if (pack.getName().equals("Basic")) {
                Pack premiumPack = packService.getAllPacks().stream()
                        .filter(p -> p.getName().equals("PREMIUM"))
                        .findFirst().orElse(null);
                if (premiumPack != null) {
                    String message = String.format(
                            "Vous achetez le Pack Basic. Si vous passez au Pack Premium plus tard, vous pouvez payer la différence (%.2f TND), mais la durée du Premium sera réduite selon les jours utilisés.",
                            premiumPack.getPrice() - pack.getPrice()
                    );
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Avertissement");
                    alert.setHeaderText(null);
                    TextArea textArea = new TextArea(message);
                    textArea.setEditable(false);
                    textArea.setWrapText(true);
                    textArea.setMaxWidth(400);
                    textArea.setMaxHeight(100);
                    alert.getDialogPane().setContent(textArea);
                    alert.getDialogPane().setMinWidth(450);
                    alert.showAndWait();
                }
            }

            if (isUpgrade) {
                Pack basicPack = packService.getAllPacks().stream()
                        .filter(p -> p.getName().equals("Basic"))
                        .findFirst().orElse(null);
                if (basicPack != null) {
                    Commande basicCommande = activeCommandes.stream()
                            .filter(c -> {
                                Pack p = packService.getPackById(c.getPackId());
                                return p != null && p.getName().equals("Basic");
                            })
                            .findFirst().orElse(null);
                    if (basicCommande != null) {
                        try {
                            Date basicCommandeDate = sdf.parse(basicCommande.getCommandeDate());
                            LocalDate commandeLocalDate = basicCommandeDate.toInstant()
                                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                            long daysUsed = ChronoUnit.DAYS.between(commandeLocalDate, LocalDate.now());
                            long remainingPremiumDays = pack.getValidityPeriod() - daysUsed;
                            if (remainingPremiumDays <= 0) {
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Le Pack Basic est presque expiré. Veuillez acheter le Pack Premium directement.");
                                return;
                            }
                            priceToPayTND = pack.getPrice() - basicPack.getPrice();
                            priceToPayUSD = priceToPayTND;
                            expiryDateStr = sdf.format(new Date(
                                    basicCommandeDate.getTime() + (pack.getValidityPeriod() * 24L * 60 * 60 * 1000)
                            ));
                            System.out.println("Upgrade margin: daysUsed=" + daysUsed + ", remainingPremiumDays=" + remainingPremiumDays +
                                    ", price=" + priceToPayTND + " TND (stored as USD), expiryDate=" + expiryDateStr);
                        } catch (ParseException e) {
                            System.err.println("Erreur lors du calcul de l'expiration pour la mise à niveau : " + e.getMessage());
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du calcul de la date d'expiration.");
                            return;
                        }
                    }
                    if (priceToPayTND < 0) {
                        System.err.println("Impossible de commander le pack : Prix de mise à niveau invalide.");
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Mise à niveau non autorisée (prix inférieur).");
                        return;
                    }
                }
            }

            double balance = currentUser.getBalance() != null ? currentUser.getBalance() : 0.0;
            System.out.println("Vérification du solde : userId=" + userId +
                    ", solde=" + balance + " TND (stored as USD), requis=" + priceToPayTND + " TND (stored as USD)");
            if (balance < priceToPayTND) {
                System.err.println("Impossible de commander le pack : Solde insuffisant pour l'utilisateur ID " + userId +
                        ". Requis : " + priceToPayTND + " TND (stored as USD), Disponible : " + balance + " TND (stored as USD)");
                showAlert(Alert.AlertType.WARNING, "Solde Insuffisant",
                        String.format("Vous avez besoin de %.2f TND pour le pack %s. Ajoutez au moins %.2f TND.",
                                priceToPayTND, pack.getName(), priceToPayTND - balance));
                addBalanceForPack(priceToPayTND);
                return;
            }

            String paymentMethod = "Balance";

            List<String> currentFeaturesList = new ArrayList<>();
            String currentFeatures = currentUser.getFeaturesUnlocked();
            if (currentFeatures != null && !currentFeatures.isEmpty()) {
                try {
                    List<String> parsedFeatures = gson.fromJson(currentFeatures, listType);
                    if (parsedFeatures != null) {
                        currentFeaturesList.addAll(parsedFeatures);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur de parsing des fonctionnalités actuelles : " + e.getMessage());
                    currentFeaturesList = new ArrayList<>();
                }
            }

            List<String> newFeaturesList = new ArrayList<>();
            if (pack.getFeatures() != null && !pack.getFeatures().isEmpty()) {
                newFeaturesList = Arrays.stream(pack.getFeatures().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }

            Set<String> mergedFeatures = new LinkedHashSet<>();
            mergedFeatures.addAll(currentFeaturesList);
            mergedFeatures.addAll(newFeaturesList);
            String newFeaturesJson = gson.toJson(mergedFeatures);

            System.out.println("Déduction du solde : userId=" + userId +
                    ", montant=" + priceToPayTND + " TND (stored as USD), nouvelles fonctionnalités=" + newFeaturesJson);

            try {
                userService.addBalance(userId, -priceToPayTND);
            } catch (Exception e) {
                System.err.println("Erreur lors de la déduction du solde pour le pack : " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour du solde. Veuillez réessayer.");
                return;
            }

            String originalFeatures = currentUser.getFeaturesUnlocked();
            try {
                userService.updateFeaturesUnlocked(userId, newFeaturesJson);
            } catch (Exception e) {
                userService.addBalance(userId, priceToPayTND);
                System.err.println("Erreur lors de la mise à jour des fonctionnalités : " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour des fonctionnalités. Le solde a été restauré.");
                return;
            }

            try {
                Commande commande = new Commande(
                        userId,
                        pack.getId(),
                        priceToPayTND,
                        sdf.format(currentDate),
                        paymentMethod,
                        expiryDateStr,
                        "Active"
                );
                commandeService.addCommande(commande);
                pdfService.generatePurchaseReceipt(commande);

                saveChatHistory(userId, pack.getName(), priceToPayTND, paymentMethod, sdf.format(currentDate), expiryDateStr);
                loadChatHistory(userId);
            } catch (Exception e) {
                userService.addBalance(userId, priceToPayTND);
                userService.updateFeaturesUnlocked(userId, originalFeatures);
                System.err.println("Erreur lors de l'ajout de la commande ou génération du PDF : " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'enregistrement de la commande ou génération du reçu PDF. Le solde et les fonctionnalités ont été restaurés.");
                return;
            }

            currentUser = userService.getUserByIdForPack(userId);
            if (currentUser == null) {
                System.err.println("Erreur : Utilisateur non trouvé après mise à jour.");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour. Veuillez réessayer.");
                return;
            }

            System.out.println("Pack commandé : " + pack.getName() + " pour l'utilisateur ID : " + userId);
            showAlert(Alert.AlertType.INFORMATION, "Commande Passée",
                    "Commande du pack " + pack.getName() + " réussie via " + paymentMethod + " ! Reçu généré et historique sauvegardé.");
            updateBalanceDisplay();
        } catch (Exception e) {
            System.err.println("Erreur lors de la commande du pack : " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la commande : " + e.getMessage());
        }
    }

    private void saveChatHistory(int userId, String packName, double price, String paymentMethod, String purchaseDate, String expiryDate) {
        File dir = new File(CHAT_HISTORY_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        String fileName = String.format("%s/user_%d.txt", CHAT_HISTORY_DIR, userId);
        String entry = String.format("Achat - Pack: %s, Prix: %.2f TND, Méthode: %s, Date: %s, Expiration: %s%n",
                packName, price, paymentMethod, purchaseDate, expiryDate);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(entry);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'enregistrement de l'historique : " + e.getMessage());
        }
    }

    private void loadChatHistory(int userId) {
        String fileName = String.format("%s/user_%d.txt", CHAT_HISTORY_DIR, userId);
        File file = new File(fileName);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder history = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    history.append(line).append("\n");
                }
                userChatHistory = history.toString();
                System.out.println("Historique chargé pour user " + userId + ":\n" + userChatHistory);
            } catch (IOException e) {
                System.err.println("Erreur lors du chargement de l'historique : " + e.getMessage());
                userChatHistory = "Erreur lors du chargement de l'historique.";
            }
        } else {
            userChatHistory = "Aucun historique trouvé.";
            System.out.println("Aucun historique pour user " + userId);
        }
    }

    private void addBalanceForPack(double requiredAmount) {
        if (isProcessingBalanceUpdate) {
            System.out.println("Mise à jour du solde déjà en cours, ignorée.");
            return;
        }

        double currentBalance = currentUser.getBalance() != null ? currentUser.getBalance() : 0.0;
        double neededAmount = requiredAmount - currentBalance;

        Dialog<String> codeDialog = new Dialog<>();
        codeDialog.setTitle("Méthode de Paiement");
        codeDialog.setHeaderText("Solde insuffisant. Ajoutez au moins " + neededAmount + " TND.");

        ButtonType confirmButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        codeDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        TextField codeField = new TextField();
        codeField.setPromptText("Entrez un code de paiement à 14 chiffres");
        codeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                codeField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 14) {
                codeField.setText(newValue.substring(0, 14));
            }
        });

        VBox codeContent = new VBox(10);
        codeContent.getChildren().addAll(new Label("Code de paiement (14 chiffres):"), codeField);
        codeDialog.getDialogPane().setContent(codeContent);

        codeDialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        codeDialog.getDialogPane().getStyleClass().add("dialog-pane");

        codeDialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return codeField.getText().trim();
            }
            return null;
        });

        Optional<String> codeResult = codeDialog.showAndWait();
        if (!codeResult.isPresent() || codeResult.get().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le code de paiement est requis.");
            return;
        }

        String paymentCode = codeResult.get();
        if (paymentCode.length() != 14 || !paymentCode.matches("\\d{14}")) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le code de paiement doit contenir exactement 14 chiffres.");
            return;
        }

        String paymentMethod = PAYMENT_METHOD_CODES.get(paymentCode);
        if (paymentMethod == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Code de paiement invalide. Veuillez vérifier et réessayer.");
            return;
        }

        TextInputDialog amountDialog = new TextInputDialog();
        amountDialog.setTitle("Ajouter Solde pour Pack");
        amountDialog.setHeaderText("Paiement via " + paymentMethod + "\nAjoutez au moins " + neededAmount + " TND pour atteindre " + requiredAmount + " TND.");
        amountDialog.setContentText("Montant :");
        amountDialog.getEditor().setText(String.format("%.2f", neededAmount));

        amountDialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        amountDialog.getDialogPane().getStyleClass().add("dialog-pane");

        Optional<String> amountResult = amountDialog.showAndWait();
        amountResult.ifPresent(amountStr -> {
            try {
                isProcessingBalanceUpdate = true;
                double amountTND = Double.parseDouble(amountStr);
                if (amountTND <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Montant Insuffisant", "Le montant doit être positif.");
                    addBalanceForPack(requiredAmount);
                    return;
                }
                if (currentBalance + amountTND < requiredAmount) {
                    showAlert(Alert.AlertType.ERROR, "Montant Insuffisant",
                            String.format("Le solde total (%.2f + %.2f = %.2f TND) est inférieur à %.2f TND requis.",
                                    currentBalance, amountTND, currentBalance + amountTND, requiredAmount));
                    addBalanceForPack(requiredAmount);
                    return;
                }

                double amountUSD = amountTND;
                boolean paymentSuccess = processPayment(amountUSD, paymentMethod);
                if (!paymentSuccess) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du paiement.");
                    return;
                }

                UserService userService = new UserService();
                System.out.println("Mise à jour du solde : userId=" + currentUser.getId() +
                        ", ancien solde=" + currentBalance + " TND (stored as USD), ajout=" + amountTND + " TND (stored as USD)");
                userService.addBalance(currentUser.getId(), amountTND);

                currentUser = userService.getUserByIdForPack(currentUser.getId());
                if (currentUser == null) {
                    System.err.println("Erreur : Utilisateur non trouvé après mise à jour du solde.");
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour du solde. Veuillez réessayer.");
                    return;
                }

                updateBalanceDisplay();
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        String.format("Solde mis à jour : %.2f TND ajouté via %s. Vous pouvez maintenant acheter le pack.", amountTND, paymentMethod));
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Montant invalide.");
            } catch (Exception e) {
                System.err.println("Erreur lors de l'ajout de solde : " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout de solde : " + e.getMessage());
            } finally {
                isProcessingBalanceUpdate = false;
            }
        });
    }

    private List<Commande> getActiveCommandes(int userId) {
        List<Commande> commandes = commandeService.getAllCommandes(); // Force fresh fetch
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date();
        return commandes.stream()
                .filter(c -> c.getUserId() == userId && "Active".equals(c.getStatus()))
                .filter(c -> {
                    try {
                        Date expiryDate = sdf.parse(c.getExpiryDate());
                        return !expiryDate.before(currentDate);
                    } catch (ParseException e) {
                        System.err.println("Erreur de parsing de la date d'expiration : " + e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setMinWidth(400);

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-pane");

        alert.showAndWait();
    }

    public boolean hasChatbotAccess() {
        if (currentUser == null) {
            System.err.println("hasChatbotAccess: currentUser is null");
            return false;
        }
        UserService userService = new UserService();
        if (userService.isAdmin(currentUser)) {
            System.out.println("hasChatbotAccess: User " + currentUser.getId() + " is an admin, granting access");
            System.out.println("Chatbot accédé. Historique de l'utilisateur :\n" + userChatHistory);
            return true;
        }
        List<Commande> activeCommandes = getActiveCommandes(currentUser.getId()); // Use fresh list
        System.out.println("hasChatbotAccess: Found " + activeCommandes.size() + " active commandes for user " + currentUser.getId());
        boolean hasAccess = activeCommandes.stream().anyMatch(c -> {
            Pack p = packService.getPackById(c.getPackId());
            if (p == null) return false;
            boolean hasPremium = p.getName().equals("PREMIUM");
            boolean hasChatbotFeature = p.getFeatures() != null && p.getFeatures().contains("chatbot");
            System.out.println("hasChatbotAccess: Commande ID=" + c.getId() + ", Pack=" + p.getName() +
                    ", Has Premium=" + hasPremium + ", Has Chatbot Feature=" + hasChatbotFeature + ", Expiry=" + c.getExpiryDate());
            return hasPremium || (hasChatbotFeature && p.getName().equals("PREMIUM"));
        });
        System.out.println("hasChatbotAccess: Final result for user " + currentUser.getId() + ": " + hasAccess);
        if (hasAccess) {
            System.out.println("Chatbot accédé. Historique de l'utilisateur :\n" + userChatHistory);
        }
        return hasAccess;
    }
}