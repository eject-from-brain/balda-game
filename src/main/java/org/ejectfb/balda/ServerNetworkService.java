package org.ejectfb.balda;

import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class ServerNetworkService implements NetworkService {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Consumer<BaldaGame> gameStateListener;
    private volatile boolean isRunning = false;
    private volatile boolean isClientConnected = false;

    @Override
    public void connect(String address) throws IOException {
        serverSocket = new ServerSocket(5555);
        isRunning = true;
        new Thread(this::waitForConnection).start();
    }

    @Override
    public void disconnect() {
        try {
            isRunning = false;
            isClientConnected = false;
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error while disconnecting: " + e.getMessage());
        }
    }

    @Override
    public void setGameStateListener(Consumer<BaldaGame> listener) {
        this.gameStateListener = listener;
    }

    private void waitForConnection() {
        try {
            clientSocket = serverSocket.accept();
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            isClientConnected = true;

            // Поток для получения обновлений от клиента
            new Thread(this::receiveUpdates).start();
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Error waiting for connection: " + e.getMessage());
            }
        }
    }

    private void receiveUpdates() {
        try {
            while (isClientConnected) {
                BaldaGame gameState = (BaldaGame) in.readObject();
                if (gameStateListener != null) {
                    Platform.runLater(() -> gameStateListener.accept(gameState));
                }
            }
        } catch (Exception e) {
            if (isClientConnected) {
                System.err.println("Connection error: " + e.getMessage());
                isClientConnected = false;
            }
        }
    }

    @Override
    public void sendGameState(BaldaGame game) {
        if (!isClientConnected) {
            System.err.println("Cannot send game state - no client connected");
            return;
        }

        try {
            out.writeObject(game);
            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to send game state: " + e.getMessage());
            isClientConnected = false;
        }
    }
}