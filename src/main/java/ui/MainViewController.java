package ui;

import core.Airport;
import entity.Fare;
import entity.FareClass;
import exceptions.NoFaresAvailableException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import strategy.DiscountStrategy;

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
        Button btnEdit = new Button("Редактировать");
        Button btnDelete = new Button("Удалить");
        Button btnStats = new Button("Статистика");
        Button btnImport = new Button("Импорт");
        Button btnExport = new Button("Экспорт");

        btnAdd.setOnAction(e -> openEditor(null));
        btnEdit.setOnAction(e -> openEditor(tableView.getSelectionModel().getSelectedItem()));
        btnDelete.setOnAction(e -> deleteSelectedFare());
        btnStats.setOnAction(e -> showStatistics());
        btnImport.setOnAction(e -> importFromFile());
        btnExport.setOnAction(e -> exportToFile());

        //horizontal box - горизонтальная панель
        HBox toolbar = new HBox(10, btnAdd, btnEdit, btnDelete, btnStats, btnImport, btnExport);
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

    // добавление или редактирование тарифа
    private void openEditor(Fare fareToEdit) {
        // создание модального редактора
        FareEditorController editor = new FareEditorController(airport, fareToEdit);
        Fare result = editor.showAndWait();

        if (result == null) {
            return;
        }

        if (fareToEdit != null) {
            // редактирование
            int index = fareData.indexOf(fareToEdit);
            if (index != -1) {
                // тариф найден → обновляем
                if (airport.updateFare(fareToEdit, result)) {
                    fareData.set(index, result);
                }
            } else {
                // тариф не найден
                showAlert(Alert.AlertType.WARNING, "Внимание",
                        "Исходный тариф не найден. Проверьте данные или добавьте тариф заново.");
            }
        } else {
            // добавление (fareToEdit == null)
            airport.addFare(result);
            fareData.add(result);
        }
    }

    // удаление тарифа
    private void deleteSelectedFare() {
        Fare selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Выберите тариф для удаления");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Удалить тариф?");
        confirm.setContentText("Маршрут: " + selected.fromLocation() + " → " + selected.toLocation());

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                if (airport.removeFare(selected)) {
                    fareData.remove(selected);
                    showAlert(Alert.AlertType.INFORMATION, "Успех", "Тариф удалён");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить тариф из базы");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при удалении: " + e.getMessage());
            }
        }
    }

    //  статистика
    private void showStatistics() {
        if (fareData.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Статистика", "Нет данных для расчёта");
            return;
        }

        try {
            int totalPassengers = airport.calculatePassengerTotal();
            double totalRevenue = airport.calculateTotalRevenue();
            Fare maxPrice = airport.findMaxPriceFare();

            String msg = String.format(
                    "Сводка по тарифам:\n\n" +
                            "• Всего маршрутов: %d\n" +
                            "• Суммарная стоимость: %.2f ₽\n" +
                            "• Максимальная цена: %.2f ₽\n" +
                            "• Пассажиров: %d",
                    fareData.size(), totalRevenue, maxPrice.getPrice(), totalPassengers
            );
            showAlert(Alert.AlertType.INFORMATION, "Статистика", msg);

        } catch (NoFaresAvailableException e) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Данные изменились. Попробуйте снова.");
        }
    }

    // экспорт в csv
    private void exportToFile() {
        // системное окно для сохранения
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить данные в файл");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        // получение родительского окна
        Stage stage = (Stage) tableView.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                // заголовки
                writer.write("Откуда,Куда,Цена,Класс,Скидка\n");

                for (Fare fare : airport.getFares()) {
                    // формат: Откуда, Куда, Цена, Класс(строка), Скидка(число)
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
                int lineNumber = 1;
                StringBuilder errors = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    // разбивка строки по запятым: ["Москва", "Питер", "5000.00", "ECONOMY", "10"]
                    lineNumber++;

                    try {
                        String[] parts = line.split(",");
                        if (parts.length < 5) {
                            errors.append("Строка ").append(lineNumber).append(": мало данных\n");
                            continue;
                        }
                            // парсинг полей из CSV
                            String from = parts[0].trim();
                            String to = parts[1].trim();

                            // валидация цены
                            double price = Double.parseDouble(parts[2].trim());
                            if (price < 1 || price > 1_000_000) {
                                errors.append("Строка ").append(lineNumber).append(": цена ").append(price).append(" вне диапазона\n");
                                continue;
                            }

                            // валидация скидки
                            int discountPercent = Integer.parseInt(parts[4].trim());
                            if (discountPercent < 0 || discountPercent > 100) {
                                errors.append("Строка ").append(lineNumber).append(": скидка ").append(discountPercent).append("% вне диапазона\n");
                                continue;
                            }

                        // enum класса (ожидается "ECONOMY", "BUSINESS" или "FIRST")
                        FareClass fareClass = FareClass.valueOf(parts[3].trim());
                        DiscountStrategy routeDiscount = Fare.createDiscountStrategy(discountPercent);

                        // создание объекта и добавление в систему
                        Fare importedFare = new Fare(from, to, price, fareClass, routeDiscount);
                        airport.addFare(importedFare);
                        // автоматическое обновление TableView из-за ObservableList
                        fareData.add(importedFare);

                        importedCount++;

                    } catch (Exception e) {
                        errors.append("Строка ").append(lineNumber).append(": ошибка (").append(e.getMessage()).append(")\n");
                    }
                }

                if (importedCount == 0 && errors.length() == 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Импорт завершён", "Файл пуст или не содержит данных для импорта");
                }

                if (importedCount > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Успех", "Импортировано тарифов: " + importedCount);
                }

                if (errors.length() > 0) {
                    showAlert(Alert.AlertType.WARNING, "Обнаружены ошибки",
                            "Импортировано: " + importedCount + "\n\nОшибки в файле:\n" + errors.toString());
                }

                } catch (IOException e) {
                    showAlert(
                            Alert.AlertType.ERROR, "Ошибка",
                            "Не удалось прочитать файл: " + e.getMessage());
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