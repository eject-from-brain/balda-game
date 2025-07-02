package org.ejectfb.balda.mode;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class ModeSelectionView {
    private final Stage stage;

    public ModeSelectionView(Consumer<ModeSelector.Mode> modeConsumer) {
        stage = new Stage();
        stage.setResizable(false);

        Text title = new Text("БАЛДА");
        title.getStyleClass().add("title-text");

        Button serverBtn = createModeButton("⌂ Запустить сервер",
                () -> modeConsumer.accept(ModeSelector.Mode.SERVER));

        Button clientBtn = createModeButton("↻ Подключиться как клиент",
                () -> modeConsumer.accept(ModeSelector.Mode.CLIENT));

        VBox root = new VBox(20, title, serverBtn, clientBtn);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1a1a1a;");

        Scene scene = new Scene(root, 350, 300);
        scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Выбор режима");
    }

    private Button createModeButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("button", "mode-button");
        btn.setOnAction(e -> {
            action.run();
            stage.close();
        });
        return btn;
    }

    public void show() {
        stage.show();
    }
}