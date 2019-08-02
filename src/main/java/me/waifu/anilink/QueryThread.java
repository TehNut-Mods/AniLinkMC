package me.waifu.anilink;

import com.google.gson.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;

public class QueryThread extends Thread {

    public static final QueryThread INSTANCE = new QueryThread();
    public final Queue<Query> queue = new ConcurrentLinkedQueue<>();
    public MinecraftServer server;

    private QueryThread() {
        super("AniList Query");
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            Query nextQuery = queue.poll();
            if (nextQuery != null) {
                FutureTask<String> task = nextQuery.graphQLQuery.submit();
                task.run();

                server.execute(() -> {
                    try {
                        String response = task.get();
                        try {
                            JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
                            if (jsonObject.get("data").isJsonNull()) {
                                nextQuery.sender.addChatMessage(getErrorText(jsonObject.get("errors").getAsJsonArray()), false);
                                return;
                            }

                            Text toSend = nextQuery.query.getText(jsonObject.getAsJsonObject("data"));
                            if (toSend != null) {
                                Text Text = new TranslatableText("chat.anilink.has_linked", nextQuery.sender.getDisplayName(), new TranslatableText("chat.anilink.type_" + nextQuery.query.name().toLowerCase(Locale.ROOT)), toSend);
                                server.getPlayerManager().sendToAll(Text);
                            } else
                                nextQuery.sender.addChatMessage(new TranslatableText("chat.anilink.unable_to_send"), false);
                        } catch (JsonSyntaxException e) {
                            AniLink.LOGGER.error(response);
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            try {
                sleep(500);
            } catch (InterruptedException e) {

            }
        }
    }

    private static Text getErrorText(JsonArray errors) {
        Text Text = new TranslatableText("chat.anilink.error").setStyle(new Style().setColor(Formatting.RED));
        Text hover = Text.copy().setStyle(new Style().setColor(Formatting.RED));
        for (JsonElement element : errors) {
            if (!hover.getSiblings().isEmpty())
                hover.append("\n");
            JsonObjectWrapper json = new JsonObjectWrapper(element.getAsJsonObject());
            hover.append(json.getString("message"));
        }

        Text.setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)));
        return Text;
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
