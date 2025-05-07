package Controller.Ham;

import entite.StoreItem;
import entite.Title;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import service.StoreItemService;
import service.TitleService;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ListStoreItemsAdminController implements Initializable {

    @FXML private TableView<StoreItem> storeItemsTable;
    @FXML private TableColumn<StoreItem, Integer> idColumn;
    @FXML private TableColumn<StoreItem, String> titleColumn;
    @FXML private TableColumn<StoreItem, String> nameColumn;
    @FXML private TableColumn<StoreItem, String> descriptionColumn;
    @FXML private TableColumn<StoreItem, Integer> priceColumn;
    @FXML private TableColumn<StoreItem, String> imageColumn;
    @FXML private TableColumn<StoreItem, Integer> stockColumn;
    @FXML private TableColumn<StoreItem, Void> updateColumn;
    @FXML private TableColumn<StoreItem, Void> deleteColumn;
    @FXML private Button createTitleButton;
    @FXML private Button createStoreItemButton;
    @FXML private Label errorLabel;

    private StoreItemService storeItemService;
    private TitleService titleService;
    private ObservableList<StoreItem> storeItemsList;
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$"
    );

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        storeItemService = new StoreItemService();
        titleService = new TitleService();
        storeItemsList = FXCollections.observableArrayList();

        // Initialize Table Columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(cellData -> {
            StoreItem item = cellData.getValue();
            boolean isTitleItem = item.getTitle() != null && item.getTitle().getId() != 0;
            return new SimpleStringProperty(isTitleItem ? "Title item." : "Not a title item.");
        });
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Update Column
        updateColumn.setCellFactory(param -> new TableCell<>() {
            private final Button updateButton = new Button();

            {
                setupButton(updateButton, "https://img.icons8.com/?size=100&id=7z7iEsDReQvk&format=png&color=000000", "Update Item", "");
            }
            // ... rest of the cell factory code remains unchanged


            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    StoreItem storeItem = getTableView().getItems().get(getIndex());
                    updateButton.setOnAction(event -> {
                        Dialog<StoreItem> dialog = createUpdateDialog(storeItem);
                        Optional<StoreItem> result = dialog.showAndWait();
                        result.ifPresent(updatedItem -> {
                            try {
                                storeItemService.update(updatedItem);
                                refreshTable();
                                setMessage("Item updated successfully.", true);
                            } catch (Exception e) {
                                setMessage("Update failed: " + e.getMessage(), false);
                            }
                        });
                    });
                    setGraphic(updateButton);
                    setText(null);
                }
            }
        });

        // Delete Column
        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button();

            {
                setupButton(deleteButton, "https://img.icons8.com/?size=100&id=97745&format=png&color=000000", "Delete Item", "");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    deleteButton.setOnAction(event -> {
                        StoreItem storeItem = getTableView().getItems().get(getIndex());
                        storeItemService.delete(storeItem);
                        storeItemsList.remove(storeItem);
                        setMessage("Item deleted successfully.", true);
                    });
                    setGraphic(deleteButton);
                    setText(null);
                }
            }
        });

        // Configure buttons with icons
        Platform.runLater(() -> {
            setupButton(createTitleButton, "https://img.icons8.com/?size=100&id=91226&format=png&color=000000", "Create Title", "Create Title");
            setupButton(createStoreItemButton, "https://img.icons8.com/?size=100&id=91226&format=png&color=000000", "Create Store Item", "Create Store Item");
        });

        // Load data
        refreshTable();
    }

    private void setupButton(Button button, String iconUrl, String tooltipText, String buttonText) {
        try {
            ImageView icon = new ImageView(new Image(iconUrl));
            icon.setFitWidth(48);
            icon.setFitHeight(48);

            if (buttonText != null && !buttonText.isEmpty()) {
                Label textLabel = new Label(buttonText);
                textLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                HBox content = new HBox(5, icon, textLabel);
                content.setAlignment(javafx.geometry.Pos.CENTER);
                button.setGraphic(content);
                button.setText("");
                button.setMinSize(120, 60); // Increased width for text
                button.getStyleClass().add("icon-button-with-text");
            } else {
                button.setGraphic(icon);
                button.setText("");
                button.setMinSize(60, 60);
                button.getStyleClass().add("icon-button");
            }

            button.setTooltip(new Tooltip(tooltipText));
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8;");
        } catch (Exception e) {
            System.out.println("Failed to load icon from " + iconUrl + ": " + e.getMessage());
            button.setText(buttonText != null && !buttonText.isEmpty() ? buttonText : tooltipText);
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(buttonText != null && !buttonText.isEmpty() ? 120 : 60, 60);
            button.getStyleClass().add(buttonText != null && !buttonText.isEmpty() ? "icon-button-with-text" : "icon-button");
        }
    }
    @FXML
    private void handleCreateTitle() {
        Dialog<Title> dialog = createTitleDialog();
        Optional<Title> result = dialog.showAndWait();
        result.ifPresent(newTitle -> {
            try {
                titleService.createPst(newTitle);
                if (newTitle.getPrice() > 0 && newTitle.getpoints_required() == 0) {
                    StoreItem storeItem = new StoreItem(
                            newTitle,
                            newTitle.getName(),
                            "A Title that Suits you!",
                            newTitle.getPrice(),
                            "https://cdn.textstudio.com/output/sample/normal/8/3/3/6/title-logo-73-16338.png",
                            999
                    );
                    storeItemService.createPst(storeItem);
                    setMessage("Title and store item created successfully!", true);
                } else {
                    setMessage("Title created successfully!", true);
                }
                refreshTable();
            } catch (Exception e) {
                setMessage("Failed to create title or store item: " + e.getMessage(), false);
            }
        });
    }

    @FXML
    private void handleCreateStoreItem() {
        Dialog<StoreItem> dialog = createStoreItemDialog();
        Optional<StoreItem> result = dialog.showAndWait();
        result.ifPresent(newItem -> {
            try {
                storeItemService.createPst(newItem);
                refreshTable();
                setMessage("Store item created successfully!", true);
            } catch (Exception e) {
                setMessage("Failed to create store item: " + e.getMessage(), false);
            }
        });
    }

    private Dialog<Title> createTitleDialog() {
        Dialog<Title> dialog = new Dialog<>();
        dialog.setTitle("Create New Title");
        dialog.setHeaderText("Enter details for the new title");

        ButtonType saveButtonType = new ButtonType("", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Title Name");
        TextField pointsField = new TextField();
        pointsField.setPromptText("Points Required");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Points Required:"), 0, 1);
        grid.add(pointsField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        setupButton(saveButton, "https://img.icons8.com/?size=100&id=7z7iEsDReQvk&format=png&color=000000", "Save","Save");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        setupButton(cancelButton, "https://img.icons8.com/?size=100&id=85853&format=png&color=000000", "Cancel","Cancel");

        pointsField.textProperty().addListener((obs, oldValue, newValue) -> {
            String trimmed = newValue.trim();
            if (!trimmed.isEmpty() && !trimmed.equals("0")) {
                if (!priceField.getText().equals("0")) {
                    priceField.setText("0");
                }
                priceField.setDisable(true);
            } else if (priceField.getText().trim().isEmpty() || priceField.getText().equals("0")) {
                priceField.setDisable(false);
            }
        });

        priceField.textProperty().addListener((obs, oldValue, newValue) -> {
            String trimmed = newValue.trim();
            if (!trimmed.isEmpty() && !trimmed.equals("0")) {
                if (!pointsField.getText().equals("0")) {
                    pointsField.setText("0");
                }
                pointsField.setDisable(true);
            } else if (pointsField.getText().trim().isEmpty() || pointsField.getText().equals("0")) {
                pointsField.setDisable(false);
            }
        });

        saveButton.setDisable(true);
        nameField.textProperty().addListener((obs, old, newValue) -> {
            boolean isValid = !newValue.trim().isEmpty() && isValidTitleInput(pointsField, priceField);
            saveButton.setDisable(!isValid);
        });
        pointsField.textProperty().addListener((obs, old, newValue) -> {
            boolean isValid = !nameField.getText().trim().isEmpty() && isValidTitleInput(pointsField, priceField);
            saveButton.setDisable(!isValid);
        });
        priceField.textProperty().addListener((obs, old, newValue) -> {
            boolean isValid = !nameField.getText().trim().isEmpty() && isValidTitleInput(pointsField, priceField);
            saveButton.setDisable(!isValid);
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                String pointsText = pointsField.getText().trim();
                String priceText = priceField.getText().trim();

                if (name.isEmpty()) {
                    setMessage("Name is required.", false);
                    return null;
                }

                int points = 0;
                if (!pointsText.isEmpty()) {
                    try {
                        points = Integer.parseInt(pointsText);
                        if (points < 0) {
                            setMessage("Points Required must be non-negative.", false);
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        setMessage("Points Required must be a valid number.", false);
                        return null;
                    }
                }

                int price = 0;
                if (!priceText.isEmpty()) {
                    try {
                        price = Integer.parseInt(priceText);
                        if (price < 0) {
                            setMessage("Price must be non-negative.", false);
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        setMessage("Price must be a valid number.", false);
                        return null;
                    }
                }

                if (points > 0 && price > 0) {
                    setMessage("Only one of Points Required or Price can be set.", false);
                    return null;
                }

                Title title = new Title();
                title.setName(name);
                title.setpoints_required(points);
                title.setPrice(price);
                return title;
            }
            return null;
        });

        return dialog;
    }

    private Dialog<StoreItem> createStoreItemDialog() {
        Dialog<StoreItem> dialog = new Dialog<>();
        dialog.setTitle("Create New Store Item");
        dialog.setHeaderText("Enter details for the new store item");

        ButtonType saveButtonType = new ButtonType("", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        TextField imageField = new TextField();
        imageField.setPromptText("Image URL");
        TextField stockField = new TextField();
        stockField.setPromptText("Stock");

        ComboBox<Title> titleComboBox = new ComboBox<>();
        ObservableList<Title> titleOptions = FXCollections.observableArrayList();
        titleOptions.add(null);
        titleOptions.addAll(titleService.readAll());
        titleComboBox.setItems(titleOptions);
        titleComboBox.setPromptText("Select Title (optional)");
        titleComboBox.setConverter(new javafx.util.StringConverter<Title>() {
            @Override
            public String toString(Title title) {
                return title == null ? "None" : title.getName();
            }

            @Override
            public Title fromString(String string) {
                return titleOptions.stream()
                        .filter(t -> t != null && t.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Image URL:"), 0, 3);
        grid.add(imageField, 1, 3);
        grid.add(new Label("Stock:"), 0, 4);
        grid.add(stockField, 1, 4);
        grid.add(new Label("Title:"), 0, 5);
        grid.add(titleComboBox, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        setupButton(saveButton, "https://img.icons8.com/?size=100&id=7z7iEsDReQvk&format=png&color=000000", "Save","");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        setupButton(cancelButton, "https://img.icons8.com/?size=100&id=85853&format=png&color=000000", "Cancel","");

        saveButton.setDisable(true);
        nameField.textProperty().addListener((obs, old, newValue) -> {
            saveButton.setDisable(!isValidStoreItemInput(nameField, descriptionArea, priceField, imageField, stockField));
        });
        descriptionArea.textProperty().addListener((obs, old, newValue) -> {
            saveButton.setDisable(!isValidStoreItemInput(nameField, descriptionArea, priceField, imageField, stockField));
        });
        priceField.textProperty().addListener((obs, old, newValue) -> {
            saveButton.setDisable(!isValidStoreItemInput(nameField, descriptionArea, priceField, imageField, stockField));
        });
        imageField.textProperty().addListener((obs, old, newValue) -> {
            saveButton.setDisable(!isValidStoreItemInput(nameField, descriptionArea, priceField, imageField, stockField));
        });
        stockField.textProperty().addListener((obs, old, newValue) -> {
            saveButton.setDisable(!isValidStoreItemInput(nameField, descriptionArea, priceField, imageField, stockField));
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                String description = descriptionArea.getText().trim();
                String priceText = priceField.getText().trim();
                String image = imageField.getText().trim();
                String stockText = stockField.getText().trim();

                if (name.isEmpty() || name.length() < 3) {
                    setMessage("Name must be at least 3 characters long.", false);
                    return null;
                }

                if (!description.isEmpty() && description.length() < 5) {
                    setMessage("Description, if provided, must be at least 5 characters.", false);
                    return null;
                }

                int price;
                try {
                    price = Integer.parseInt(priceText);
                    if (price <= 0) {
                        setMessage("Price must be greater than 0.", false);
                        return null;
                    }
                } catch (NumberFormatException e) {
                    setMessage("Price must be a valid number.", false);
                    return null;
                }

                if (!image.isEmpty() && !URL_PATTERN.matcher(image).matches()) {
                    setMessage("Image URL, if provided, must be valid.", false);
                    return null;
                }

                int stock;
                try {
                    stock = Integer.parseInt(stockText);
                    if (stock < 0) {
                        setMessage("Stock cannot be negative.", false);
                        return null;
                    }
                } catch (NumberFormatException e) {
                    setMessage("Stock must be a valid number.", false);
                    return null;
                }

                return new StoreItem(
                        titleComboBox.getValue(),
                        name,
                        description.isEmpty() ? null : description,
                        price,
                        image.isEmpty() ? null : image,
                        stock
                );
            }
            return null;
        });

        return dialog;
    }

    private Dialog<StoreItem> createUpdateDialog(StoreItem storeItem) {
        Dialog<StoreItem> dialog = new Dialog<>();
        dialog.setTitle("Update Store Item");
        dialog.setHeaderText("Edit item: " + storeItem.getName());

        ButtonType saveButtonType = new ButtonType("", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(storeItem.getName());
        nameField.setPromptText("Name");
        TextArea descriptionArea = new TextArea(storeItem.getDescription());
        descriptionArea.setPromptText("Description");
        TextField priceField = new TextField(String.valueOf(storeItem.getPrice()));
        priceField.setPromptText("Price");
        TextField imageField = new TextField(storeItem.getImage());
        imageField.setPromptText("Image URL");
        TextField stockField = new TextField(String.valueOf(storeItem.getStock()));
        stockField.setPromptText("Stock");

        ComboBox<Title> titleComboBox = new ComboBox<>();
        ObservableList<Title> titleOptions = FXCollections.observableArrayList();
        titleOptions.add(null);
        titleOptions.addAll(titleService.readAll());
        titleComboBox.setItems(titleOptions);
        titleComboBox.setPromptText("Select Title (optional)");
        titleComboBox.setValue(storeItem.getTitle() != null && storeItem.getTitle().getId() != 0 ? storeItem.getTitle() : null);
        titleComboBox.setConverter(new javafx.util.StringConverter<Title>() {
            @Override
            public String toString(Title title) {
                return title == null ? "None" : title.getName();
            }

            @Override
            public Title fromString(String string) {
                return titleOptions.stream()
                        .filter(t -> t != null && t.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Image URL:"), 0, 3);
        grid.add(imageField, 1, 3);
        grid.add(new Label("Stock:"), 0, 4);
        grid.add(stockField, 1, 4);
        grid.add(new Label("Title:"), 0, 5);
        grid.add(titleComboBox, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        setupButton(saveButton, "https://img.icons8.com/?size=100&id=7z7iEsDReQvk&format=png&color=000000", "Save","");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        setupButton(cancelButton, "https://img.icons8.com/?size=100&id=85853&format=png&color=000000", "Cancel","");

        saveButton.setDisable(true);
        nameField.textProperty().addListener((obs, old, newValue) -> {
            saveButton.setDisable(!isValidStoreItemInput(nameField, descriptionArea, priceField, imageField, stockField));
        });
        descriptionArea.textProperty().addListener((obs, old, newValue) -> {
            saveButton.setDisable(!isValidStoreItemInput(nameField, descriptionArea, priceField, imageField, stockField));
        });
        priceField.textProperty().addListener((obs, old, newValue) -> {
            saveButton.setDisable(!isValidStoreItemInput(nameField, descriptionArea, priceField, imageField, stockField));
        });
        imageField.textProperty().addListener((obs, old, newValue) -> {
            saveButton.setDisable(!isValidStoreItemInput(nameField, descriptionArea, priceField, imageField, stockField));
        });
        stockField.textProperty().addListener((obs, old, newValue) -> {
            saveButton.setDisable(!isValidStoreItemInput(nameField, descriptionArea, priceField, imageField, stockField));
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                String description = descriptionArea.getText().trim();
                String priceText = priceField.getText().trim();
                String image = imageField.getText().trim();
                String stockText = stockField.getText().trim();

                if (name.isEmpty() || name.length() < 3) {
                    setMessage("Name must be at least 3 characters long.", false);
                    return null;
                }

                if (!description.isEmpty() && description.length() < 5) {
                    setMessage("Description, if provided, must be at least 5 characters.", false);
                    return null;
                }

                int price;
                try {
                    price = Integer.parseInt(priceText);
                    if (price <= 0) {
                        setMessage("Price must be greater than 0.", false);
                        return null;
                    }
                } catch (NumberFormatException e) {
                    setMessage("Price must be a valid number.", false);
                    return null;
                }

                if (!image.isEmpty() && !URL_PATTERN.matcher(image).matches()) {
                    setMessage("Image URL, if provided, must be valid.", false);
                    return null;
                }

                int stock;
                try {
                    stock = Integer.parseInt(stockText);
                    if (stock < 0) {
                        setMessage("Stock cannot be negative.", false);
                        return null;
                    }
                } catch (NumberFormatException e) {
                    setMessage("Stock must be a valid number.", false);
                    return null;
                }

                return new StoreItem(
                        titleComboBox.getValue(),
                        name,
                        description.isEmpty() ? null : description,
                        price,
                        image.isEmpty() ? null : image,
                        stock
                );
            }
            return null;
        });

        return dialog;
    }

    private boolean isValidTitleInput(TextField pointsField, TextField priceField) {
        String pointsText = pointsField.getText().trim();
        String priceText = priceField.getText().trim();

        int points = 0;
        if (!pointsText.isEmpty()) {
            try {
                points = Integer.parseInt(pointsText);
                if (points < 0) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        int price = 0;
        if (!priceText.isEmpty()) {
            try {
                price = Integer.parseInt(priceText);
                if (price < 0) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return !(points > 0 && price > 0);
    }

    private boolean isValidStoreItemInput(TextField nameField, TextArea descriptionArea, TextField priceField, TextField imageField, TextField stockField) {
        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();
        String priceText = priceField.getText().trim();
        String image = imageField.getText().trim();
        String stockText = stockField.getText().trim();

        if (name.isEmpty() || name.length() < 3) {
            return false;
        }

        if (!description.isEmpty() && description.length() < 5) {
            return false;
        }

        try {
            int price = Integer.parseInt(priceText);
            if (price <= 0) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        if (!image.isEmpty() && !URL_PATTERN.matcher(image).matches()) {
            return false;
        }

        try {
            int stock = Integer.parseInt(stockText);
            if (stock < 0) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private void setMessage(String message, boolean isSuccess) {
        errorLabel.setText(message);
        errorLabel.getStyleClass().removeAll("success", "error");
        errorLabel.getStyleClass().add(isSuccess ? "success" : "error");
    }

    private void refreshTable() {
        storeItemsList.clear();
        storeItemsList.addAll(storeItemService.readAll());
        storeItemsTable.setItems(storeItemsList);
    }
}