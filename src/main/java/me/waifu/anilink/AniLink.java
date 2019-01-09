package me.waifu.anilink;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.commands.CommandRegistry;
import net.fabricmc.fabric.events.ServerEvent;
import net.fabricmc.loader.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Supplier;

public class AniLink implements ModInitializer {

    public static final String MODID = "anilink";
    public static final String NAME = "AniLink";
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    public static final Settings CONFIG = ((Supplier<Settings>) () -> {
        File file = new File(FabricLoader.INSTANCE.getConfigDirectory(), "anilink.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (!file.exists()) {
            Settings settings = new Settings();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(gson.toJson(settings));
            } catch (IOException e) {
                LOGGER.warn("Failed to create default config file.");
            }

            return settings;
        }

        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, Settings.class);
        } catch (IOException e) {
            LOGGER.warn("Failed to read config file.");
            return new Settings();
        }
    }).get();

    @Override
    public void onInitialize() {
        CommandRegistry.INSTANCE.register(false, dispatcher -> {
            dispatcher.register(new CommandQuery(QueryType.ANIME).create());
            dispatcher.register(new CommandQuery(QueryType.MANGA).create());
            dispatcher.register(new CommandQuery(QueryType.CHARACTER).create());
        });

        ServerEvent.START.register(server -> {
            if (!QueryThread.INSTANCE.isAlive()) {
                LOGGER.info("Starting AniLink query queue thread");
                QueryThread.INSTANCE.start();
            }
        });

        ServerEvent.STOP.register(server -> {
            QueryThread.INSTANCE.interrupt();
            LOGGER.info("Stopping AniLink query queue thread");
        });
    }

    public static class Settings {
        public boolean blockNsfw = true;
        public boolean hardBlockNsfw;
    }
}
