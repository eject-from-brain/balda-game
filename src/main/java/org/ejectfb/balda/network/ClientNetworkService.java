package org.ejectfb.balda.network;

import javafx.application.Platform;
import org.ejectfb.balda.game.BaldaGame;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientNetworkService implements NetworkService {
    private static final Logger logger = Logger.getLogger(ClientNetworkService.class.getName());
    private volatile boolean isFirstRun = true;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Consumer<BaldaGame> gameStateListener;
    private volatile boolean isConnected = false;

    @Override
    public void connect(String address) throws IOException {
        try {
            logger.log(Level.INFO, "Попытка подключения к серверу по адресу: " + address + ":5555");
            socket = new Socket(address, 5555);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            isConnected = true;

            logger.info("Успешное подключение к серверу");
            logger.info("Запуск потока для получения обновлений от сервера");

            new Thread(this::receiveUpdates).start();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка подключения к серверу: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void disconnect() {
        logger.info("Отключение клиента...");
        try {
            isConnected = false;
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
                logger.info("Сокет клиента успешно закрыт");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при отключении: " + e.getMessage(), e);
        }
    }

    @Override
    public void setGameStateListener(Consumer<BaldaGame> listener) {
        logger.info("Установка слушателя состояния игры");
        this.gameStateListener = listener;
    }

    private void receiveUpdates() {
        logger.info("Поток получения обновлений запущен");
        try {
            while (isConnected) {
                BaldaGame gameState = (BaldaGame) in.readObject();
                gameState.setClientConnected(true);

                if (gameStateListener != null) {
                    Platform.runLater(() -> {
                        logger.info("Обновление UI с новым состоянием игры");
                        gameStateListener.accept(gameState);
                    });
                }

                if (isFirstRun && gameState.isServerTurn()) { //так как в механике синк для клиента тоже ход, возвращаем ход
                    sendGameState(gameState);
                    isFirstRun = false;
                } else isFirstRun = false;
            }
        } catch (Exception e) {
            if (isConnected) {
                logger.log(Level.SEVERE, "Ошибка в потоке получения обновлений: " + e.getMessage(), e);
                disconnect();
            }
        }
        logger.info("Поток получения обновлений завершен");
    }

    @Override
    public void sendGameState(BaldaGame game) {
        if (!isConnected) {
            logger.warning("Попытка отправить состояние игры без подключения");
            return;
        }

        try {
            logger.info("Отправка состояния игры на сервер");
            out.writeObject(game);
            out.flush();
            logger.info("Состояние игры успешно отправлено");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка отправки состояния игры: " + e.getMessage(), e);
            disconnect();
        }
    }
}