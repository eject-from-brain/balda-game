package org.ejectfb.balda;

import java.io.IOException;
import java.util.function.Consumer;

public interface NetworkService {
    void connect(String address) throws IOException;
    void disconnect();
    void sendGameState(BaldaGame game);
    void setGameStateListener(Consumer<BaldaGame> listener);
}