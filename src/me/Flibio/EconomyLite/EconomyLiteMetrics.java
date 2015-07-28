package me.Flibio.EconomyLite;

import org.spongepowered.api.Game;

import java.io.File;
import java.io.IOException;


public class EconomyLiteMetrics extends Metrics {

    private Game game = Main.access.game;

    public EconomyLiteMetrics() throws IOException {
        super("EconomyLite", Main.access.version);
    }

    @Override
    public String getFullServerVersion() {
        return "1.8";
    }

    @Override
    public int getPlayersOnline() {
        return game.getServer().getOnlinePlayers().size();
    }

    @Override
    public File getConfigFile() {
        return new File("config/EconomyLite/metrics.yml");
    }
}