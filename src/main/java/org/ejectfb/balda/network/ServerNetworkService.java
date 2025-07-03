package org.ejectfb.balda.network;

import javafx.application.Platform;
import org.ejectfb.balda.game.BaldaGame;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerNetworkService implements NetworkService {
    private static final Logger logger = Logger.getLogger(ServerNetworkService.class.getName());

    private BaldaGame currentGame;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Consumer<BaldaGame> gameStateListener;
    private volatile boolean isRunning = false;
    private volatile boolean isClientConnected = false;

    public ServerNetworkService(BaldaGame currentGame) {
        this.currentGame = currentGame;
    }

    @Override
    public void connect(String address) throws IOException {
        try {
            logger.info("Запуск сервера на порту 5555");
            serverSocket = new ServerSocket(5555);
            isRunning = true;

            logger.info("Ожидание подключения клиента...");
            new Thread(this::waitForConnection).start();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка запуска сервера: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void disconnect() {
        logger.info("Остановка сервера...");
        try {
            isRunning = false;
            isClientConnected = false;
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                logger.info("Клиентский сокет закрыт");
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                logger.info("Серверный сокет закрыт");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при остановке сервера: " + e.getMessage(), e);
        }
    }

    @Override
    public void setGameStateListener(Consumer<BaldaGame> listener) {
        logger.info("Установка слушателя состояния игры на сервере");
        this.gameStateListener = listener;
    }

    private void waitForConnection() {
        try {
            clientSocket = serverSocket.accept();
            logger.log(Level.INFO, "Клиент подключен: " + clientSocket.getInetAddress());

            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            isClientConnected = true;

            sendGameState(currentGame);

            logger.info("Запуск потока для получения обновлений от клиента");
            new Thread(this::receiveUpdates).start();

        } catch (IOException e) {
            if (isRunning) {
                logger.log(Level.SEVERE, "Ошибка ожидания подключения: " + e.getMessage(), e);
            }
        }
    }

    private void receiveUpdates() {
        logger.info("Поток получения обновлений от клиента запущен");
        try {
            while (isClientConnected) {
                BaldaGame gameState = (BaldaGame) in.readObject();
                logger.log(Level.INFO, "Получено новое состояние игры от клиента");

                if (gameStateListener != null) {
                    Platform.runLater(() -> {
                        logger.info("Обновление UI сервера с новым состоянием игры");
                        gameStateListener.accept(gameState);
                    });
                }
            }
        } catch (Exception e) {
            if (isClientConnected) {
                logger.log(Level.SEVERE, "Ошибка в потоке получения обновлений: " + e.getMessage(), e);
                isClientConnected = false;
            }
        }
        logger.info("Поток получения обновлений от клиента завершен");
    }

    @Override
    public void sendGameState(BaldaGame game) {
        if (!isClientConnected) {
            logger.warning("Попытка отправить состояние игры без подключенного клиента");
            return;
        }

        try {
            logger.info("Отправка состояния игры клиенту");
            out.writeObject(game);
            out.flush();
            logger.info("Состояние игры успешно отправлено клиенту");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка отправки состояния игры: " + e.getMessage(), e);
            isClientConnected = false;
        }
    }
}