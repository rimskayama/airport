package ui;

import core.Airport;
import entity.Fare;
import entity.FareClass;
import strategy.NoDiscount;
import strategy.RouteDiscount;
import strategy.DiscountStrategy;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FareEditorController {
    private final Stage dialog;
    private Fare resultFare;
    private boolean confirmed = false;

    public FareEditorController(Airport airport) {
        this.dialog = new Stage();

        dialog.initModality(Modality.APPLICATION_MODAL); // блокировка главного окна
        dialog.setTitle("Добавление тарифа");

        VBox root = buildUI();
        dialog.setScene(new Scene(root, 400, 350));
    }

    // построение формы
    private VBox buildUI() {
        // поля формы
        TextField fromField = new TextField();
        fromField.setPromptText("Пункт отправления");

        TextField toField = new TextField();
        toField.setPromptText("Пункт назначения");

        TextField priceField = new TextField();
        priceField.setPromptText("Цена (1 - 1 000 000)");

        // выпадающий список
        ComboBox<FareClass> classCombo = new ComboBox<>();
        classCombo.getItems().addAll(FareClass.values());
        classCombo.setValue(FareClass.ECONOMY);

        TextField discountField = new TextField("0");
        discountField.setPromptText("Скидка направления (0-100)");

        // валидация - запрет на введение букв в поля цены и скидки
        priceField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) priceField.setText(old);
        });
        discountField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*")) discountField.setText(old);
        });

        // кнопки
        Button btnSave = new Button("Сохранить");
        Button btnCancel = new Button("Отмена");
        btnSave.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"); // зелёный фон, белый текст

        // валидация и создание объекта при сохранении
        btnSave.setOnAction(e -> {
            String from = fromField.getText().trim();
            String to = toField.getText().trim();
            String priceStr = priceField.getText().trim();
            String discountStr = discountField.getText().trim();

            // проверки на пустоту
            if (from.isEmpty() || to.isEmpty()) {
                showAlert("Ошибка", "Поля 'Откуда' и 'Куда' не могут быть пустыми");
                return;
            }
            if (priceStr.isEmpty()) {
                showAlert("Ошибка", "Укажите цену тарифа");
                return;
            }

            // парсинг и валидация цены
            double price;
            try {
                price = Double.parseDouble(priceStr);
                if (price < 1 || price > 1_000_000) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showAlert("Ошибка", "Цена должна быть числом от 1 до 1 000 000");
                return;
            }

            // парсинг и валидация скидки
            int discountPercent;
            try {
                discountPercent = discountStr.isEmpty() ? 0 : Integer.parseInt(discountStr);
                if (discountPercent < 0 || discountPercent > 100) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showAlert("Ошибка", "Скидка должна быть от 0 до 100");
                return;
            }

            // создание объекта тарифа
            FareClass selectedClass = classCombo.getValue();
            DiscountStrategy routeDiscount = (discountPercent > 0)
                    ? new RouteDiscount(discountPercent)
                    : new NoDiscount();

            resultFare = new Fare(from, to, price, selectedClass, routeDiscount);
            confirmed = true;
            dialog.close();
        });

        btnCancel.setOnAction(e -> dialog.close());

        // панель кнопок: выравнивание вправо, отступ сверху
        HBox buttons = new HBox(10, btnSave, btnCancel);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        // сборка формы
        GridForm grid = new GridForm();
        grid.addRow("Откуда:", fromField);
        grid.addRow("Куда:", toField);
        grid.addRow("Цена:", priceField);
        grid.addRow("Класс:", classCombo);
        grid.addRow("Скидка (%):", discountField);

        // добавление кнопок
        VBox root = new VBox(15, grid, buttons);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        return root;
    }

    // форма: отступы, подписи, выравнивание полей
    private static class GridForm extends VBox {
        public GridForm() { setSpacing(10); }
        public void addRow(String label, Control control) {
            HBox row = new HBox(10);
            Label lbl = new Label(label);
            lbl.setMinWidth(100);
            lbl.setAlignment(Pos.CENTER_RIGHT);
            control.setMinWidth(200);
            row.getChildren().addAll(lbl, control);
            getChildren().add(row);
        }
    }

    private void showAlert(String title, String message) {
        // объект алерта с типом предупреждение
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // показать модальное окно и ждать закрытия
    public Fare showAndWait() {
        dialog.showAndWait();
        return confirmed ? resultFare : null;
    }
}