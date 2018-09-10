package me.waifu.anilink;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = AniLink.MODID, name = AniLink.NAME, version = "${VERSION}", acceptableRemoteVersions = "*")
public class AniLink {

    public static final String MODID = "anilink";
    public static final String NAME = "AniLink";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandQuery(QueryType.ANIME));
        event.registerServerCommand(new CommandQuery(QueryType.MANGA));
        event.registerServerCommand(new CommandQuery(QueryType.CHARACTER));

        if (!QueryThread.INSTANCE.isAlive()) {
            LOGGER.info("Starting AniLink query queue thread");
            QueryThread.INSTANCE.start();
        }
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {
        QueryThread.INSTANCE.interrupt();
        LOGGER.info("Stopping AniLink query queue thread");
    }

    @Config(modid = MODID)
    @Mod.EventBusSubscriber(modid = MODID)
    public static class Settings {
        @Config.Comment("Hides NSFW descriptions, but still allows the message to be sent.")
        public static boolean blockNsfw = true;
        @Config.Comment("Blocks any NSFW media from being displayed.")
        public static boolean hardBlockNsfw;

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(MODID))
                ConfigManager.sync(MODID, Config.Type.INSTANCE);
        }
    }
}
