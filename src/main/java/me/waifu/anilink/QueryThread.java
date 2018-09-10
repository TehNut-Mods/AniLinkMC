package me.waifu.anilink;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

public class QueryThread extends Thread {

    public static final QueryThread INSTANCE = new QueryThread();
    public final Queue<Query> queue = new ConcurrentLinkedQueue<>();

    private QueryThread() {
        super("AniList Query");
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            Query nextQuery = queue.poll();
            if (nextQuery != null ) {
                nextQuery.task.run();

                try {
                    JsonObject jsonObject = new JsonParser().parse(nextQuery.task.get()).getAsJsonObject();
                    if (jsonObject.get("data").isJsonNull())
                        nextQuery.sender.sendMessage(getErrorComponent(jsonObject.get("errors").getAsJsonArray()));

                    ITextComponent toSend = nextQuery.query.getTextComponent(jsonObject.getAsJsonObject("data"));
                    if (toSend != null) {
                        ITextComponent component = new TextComponentTranslation("chat.anilink:has_linked", nextQuery.sender.getDisplayName(), new TextComponentTranslation("chat.anilink:type_" + nextQuery.query.name().toLowerCase(Locale.ROOT)), toSend);
                        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(component);
                    } else
                        nextQuery.sender.sendMessage(new TextComponentTranslation("chat.anilink:unable_to_send"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static ITextComponent getErrorComponent(JsonArray errors) {
        ITextComponent component = new TextComponentTranslation("chat.anilink:error").setStyle(new Style().setColor(TextFormatting.RED));
        ITextComponent hover = new TextComponentString("").setStyle(new Style().setColor(TextFormatting.RED));
        for (JsonElement element : errors) {
            if (!hover.getSiblings().isEmpty())
                hover.appendSibling(new TextComponentString("\n"));
            JsonObjectWrapper json = new JsonObjectWrapper(element.getAsJsonObject());
            hover.appendSibling(new TextComponentString(json.getString("message")));
        }

        component.setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)));
        return component;
    }

    public static class Query {
        private final ICommandSender sender;
        private final FutureTask<String> task;
        private final QueryType query;

        public Query(ICommandSender sender, FutureTask<String> task, QueryType query) {
            this.sender = sender;
            this.task = task;
            this.query = query;
        }
    }
}
