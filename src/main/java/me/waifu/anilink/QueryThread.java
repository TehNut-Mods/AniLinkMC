package me.waifu.anilink;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.waifu.graphquery.GraphQLQuery;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.*;
import net.minecraft.text.event.HoverEvent;

import java.io.IOException;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;

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
                try {
                    FutureTask<String> task = nextQuery.graphQLQuery.createRequest();
                    task.run();

                    FabricLoader.INSTANCE.getEnvironmentHandler().getServerInstance().execute(() -> {
                        try {
                            JsonObject jsonObject = new JsonParser().parse(task.get()).getAsJsonObject();
                            if (jsonObject.get("data").isJsonNull())
                                nextQuery.sender.addChatMessage(getErrorComponent(jsonObject.get("errors").getAsJsonArray()), false);

                            TextComponent toSend = nextQuery.query.getTextComponent(jsonObject.getAsJsonObject("data"));
                            if (toSend != null) {
                                TextComponent component = new TranslatableTextComponent("chat.anilink.has_linked", nextQuery.sender.getDisplayName(), new TranslatableTextComponent("chat.anilink.type_" + nextQuery.query.name().toLowerCase(Locale.ROOT)), toSend);
                                FabricLoader.INSTANCE.getEnvironmentHandler().getServerInstance().getPlayerManager().sendToAll(component);
                            } else
                                nextQuery.sender.addChatMessage(new TranslatableTextComponent("chat.anilink.unable_to_send"), false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (IOException e) {
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

    private static TextComponent getErrorComponent(JsonArray errors) {
        TextComponent component = new TranslatableTextComponent("chat.anilink.error").setStyle(new Style().setColor(TextFormat.RED));
        TextComponent hover = new StringTextComponent("").setStyle(new Style().setColor(TextFormat.RED));
        for (JsonElement element : errors) {
            if (!hover.getChildren().isEmpty())
                hover.append("\n");
            JsonObjectWrapper json = new JsonObjectWrapper(element.getAsJsonObject());
            hover.append(json.getString("message"));
        }

        component.setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)));
        return component;
    }

    public static class Query {
        private final PlayerEntity sender;
        private final GraphQLQuery graphQLQuery;
        private final QueryType query;

        public Query(PlayerEntity sender, GraphQLQuery graphQLQuery, QueryType query) {
            this.sender = sender;
            this.graphQLQuery = graphQLQuery;
            this.query = query;
        }
    }
}
