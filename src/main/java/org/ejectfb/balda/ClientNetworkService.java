package org.ejectfb.balda;

import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class ClientNetworkService implements NetworkService {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Consumer<BaldaGame> gameStateListener;
    private volatile boolean isConnected = false;

    @Override
    public void connect(String address) throws IOException {
        socket = new Socket(address, 5555);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        isConnected = true;

        // Поток для получения обновлений от сервера
        new Thread(this::receiveUpdates).start();
    }

    @Override
    public void disconnect() {
        try {
            isConnected = false;
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error while disconnecting: " + e.getMessage());
        }
    }

    @Override
    public void setGameStateListener(Consumer<BaldaGame> listener) {
        this.gameStateListener = listener;
    }

    private void receiveUpdates() {
        try {
            while (isConnected) {
                BaldaGame gameState = (BaldaGame) in.readObject();
                if (gameStateListener != null) {
                    Platform.runLater(() -> gameStateListener.accept(gameState));
                }
            }
        } catch (Exception e) {
            if (isConnected) {
                System.err.println("Connection error: " + e.getMessage());
                disconnect();
            }
        }
    }

    @Override
    public void sendGameState(BaldaGame game) {
        if (!isConnected) {
            System.err.println("Cannot send game state - not connected");
            return;
        }

        try {
            out.writeObject(game);
            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to send game state: " + e.getMessage());
            disconnect();
        }
    }
}