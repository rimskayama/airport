package ui;

import core.Airport;
import entity.Fare;
import entity.FareClass;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;

public class MainViewController {
    private final Airport airport;
    private final TableView<Fare> tableView;
    private final ObservableList<Fare> fareData;
    private final VBox root;

    public MainViewController(Airport airport) {
        this.airport = airport;
        // данные для таблицы тарифов
        this.fareData = FXCollections.observableArrayList(airport.getFares());
        // таблица данных тарифов
        tableView = new TableView<>(fareData);
        setupColumns();

        // кнопки
        Button btnAdd = new Button("Добавить");
        Button btnImport = new Button("Импорт");
        Button btnExport = new Button("Экспорт");

        btnAdd.setOnAction(e -> openEditor());
        btnImport.setOnAction(e -> importFromFile());
        btnExport.setOnAction(e -> exportToFile());

        //horizontal box - горизонтальная панель
        HBox toolbar = new HBox(10, btnAdd, btnImport, btnExport);
        //отступы
        toolbar.setPadding(new Insets(10));

        // vertical box (вертикальная компоновка): панель кнопок + таблица
        root = new VBox(10, toolbar, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        root.setPadding(new Insets(10));
    }

    // колонки
    private void setupColumns() {
        TableColumn<Fare, String> colFrom = new TableColumn<>("Откуда");
        colFrom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().fromLocation()));

        TableColumn<Fare, String> colTo = new TableColumn<>("Куда");
        colTo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().toLocation()));

        TableColumn<Fare, Double> colPrice = new TableColumn<>("Цена");
        colPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrice()));

        TableColumn<Fare, String> colClass = new TableColumn<>("Класс");
        colClass.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getClassName()));

        tableView.getColumns().addAll(colFrom, colTo, colPrice, colClass);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public VBox getRoot() {
        return root;
    }

    // добавление тарифа
    private void openEditor() {
        // создание модального редактора
        FareEditorController editor = new FareEditorController(airport);
        Fare result = editor.showAndWait();

        // если пользователь нажал "сохранить"
        if (result != null) {
            airport.addFare(result); // добавление в БД
            fareData.add(result); // добавление в таблицу
        }
    }

    // экспорт в csv
    private void exportToFile() {
        // системное окно для сохранения
        System.out.println("🔹 exportToFile() вызван");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить данные в файл");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        // Получаем Stage (окно) для показа диалога
        Stage stage = (Stage) tableView.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                // Заголовки
                writer.write("Откуда,Куда,Цена,Класс,Скидка\n");

                for (Fare fare : airport.getFares()) {
                    // Формат: Откуда, Куда, Цена, Класс(строка), Скидка(число)
                    writer.write(String.format("%s,%s,%.2f,%s,%d\n",
                            fare.fromLocation(),
                            fare.toLocation(),
                            fare.getPrice(),
                            fare.getClassChoice().name(),
                            fare.getRouteDiscountPercent()));
                }
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Данные успешно экспортированы в " + file.getName());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось сохранить файл: " + e.getMessage());
            }
        }
    }

    // импорт из CSV
    private void importFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Загрузить данные из файла");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        // получение родительского окна
        Stage stage = (Stage) tableView.getScene().getWindow();
        // блокировка выполнения во время выбора файла CSV
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            // чтение файла
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
                String line = reader.readLine(); // пропуск заголовка
                int importedCount = 0;

                while ((line = reader.readLine()) != null) {
                    // разбивка строки по запятым: ["Москва", "Питер", "5000.00", "ECONOMY", "10"]
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        // парсинг полей из CSV
                        String from = parts[0].trim();
                        String to = parts[1].trim();
                        double price = Double.parseDouble(parts[2].trim());

                        // enum класса (ожидается "ECONOMY", "BUSINESS" или "FIRST")
                        FareClass fareClass = FareClass.valueOf(parts[3].trim());

                        // стратегия скидки
                        int discountPercent = Integer.parseInt(parts[4].trim());
                        strategy.DiscountStrategy routeDiscount = (discountPercent > 0)
                                ? new strategy.RouteDiscount(discountPercent)
                                : new strategy.NoDiscount();

                        // создание объекта и добавление в систему
                        Fare importedFare = new Fare(from, to, price, fareClass, routeDiscount);
                        airport.addFare(importedFare);
                        // автоматическое обновление TableView из-за ObservableList
                        fareData.add(importedFare);

                        importedCount++;
                    }
                }
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Импортировано тарифов: " + importedCount);
            } catch (IllegalArgumentException e) {
                // ошибки парсинга
                showAlert(Alert.AlertType.ERROR, "Ошибка формата",
                        "Не удалось распарсить строку. Проверьте, что файл соответствует формату:\n" +
                                "Откуда, Куда, Цена, Класс(имя константы), Скидка\n" +
                                "Детали: " + e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось прочитать файл: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

}