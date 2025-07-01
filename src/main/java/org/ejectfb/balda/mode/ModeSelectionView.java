package org.ejectfb.balda.mode;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class ModeSelectionView {
    private Stage stage;
    private Consumer<ModeSelector.Mode> modeConsumer;

    public ModeSelectionView(Consumer<ModeSelector.Mode> modeConsumer) {
        this.modeConsumer = modeConsumer;
        stage = new Stage();

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        Button serverBtn = new Button("Запустить сервер");
        serverBtn.setOnAction(e -> {
            modeConsumer.accept(ModeSelector.Mode.SERVER);
            stage.close();
        });

        Button clientBtn = new Button("Подключиться как клиент");
        clientBtn.setOnAction(e -> {
            modeConsumer.accept(ModeSelector.Mode.CLIENT);
            stage.close();
        });

        root.getChildren().addAll(serverBtn, clientBtn);
        Scene scene = new Scene(root, 300, 200);
        stage.setScene(scene);
        stage.setTitle("Выберите режим");
    }

    public void show() {
        stage.show();
    }
}