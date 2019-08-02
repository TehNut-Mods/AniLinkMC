package me.waifu.anilink;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public enum QueryType {
    ANIME(() -> readResource("media"), $ -> $.put("type", "ANIME"), QueryType::createMediaComponent),
    MANGA(() -> readResource("media"), $ -> $.put("type", "MANGA"), QueryType::createMediaComponent),
    CHARACTER(() -> readResource("character"), $ -> { }, json -> {
        if (json.isNull("Character"))
            return null;

        JsonObjectWrapper charData = json.getObject("Character");

        String name = charData.getObject("name").getString("full");
        Text hoverData = new LiteralText("");
        hoverData.append(new LiteralText(name + "\n").setStyle(new Style().setUnderline(true)));
        hoverData.append(new LiteralText(parseDescription(charData.getString("description"))));

        return new LiteralText("[" + name + "]").setStyle(new Style()
                .setColor(Formatting.DARK_AQUA)
                .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, charData.getString("url")))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverData))
        );
    }),
    ;

    private final GraphQLQuery query;
    private final Map<String, Object> defaultVariables;
    private final Function<JsonObjectWrapper, Text> componentProvider;

    QueryType(Supplier<String> resourceReader, Consumer<ImmutableMap.Builder<String, Object>> defaultVariables, Function<JsonObjectWrapper, Text> componentProvider) {
        this.query = new GraphQLQuery(resourceReader.get());
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        defaultVariables.accept(builder);
        this.defaultVariables = builder.build();
        this.componentProvider = componentProvider;
    }

    public Text getText(JsonObject jsonObject) {
        return componentProvider.apply(new JsonObjectWrapper(jsonObject));
    }

    public GraphQLQuery getQuery() {
        return query.reset();
    }

    public Map<String, Object> getDefaultVariables() {
        return defaultVariables;
    }

    private static Text createMediaComponent(JsonObjectWrapper json) {
        if (json.isNull("Media"))
            return null;

        JsonObjectWrapper mediaData = json.getObject("Media");
        if (AniLink.CONFIG.hardBlockNsfw && mediaData.getBoolean("isAdult"))
            return null;

        Text hoverData = new LiteralText("");
        hoverData.append(new LiteralText(mediaData.getObject("title").getString("romaji") + "\n").setStyle(new Style().setUnderline(true)));

        if (AniLink.CONFIG.blockNsfw && mediaData.getBoolean("isAdult"))
            hoverData.append(new TranslatableText("chat.anilink.nsfw_link").setStyle(new Style().setColor(Formatting.RED)));
        else
            hoverData.append(new LiteralText(parseDescription(mediaData.getString("description"))));

        return new LiteralText("[" + mediaData.getObject("title").getString("romaji") + "]").setStyle(new Style()
                .setColor(Formatting.DARK_AQUA)
                .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, mediaData.getString("url")))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverData))
        );
    }

    private static String parseDescription(String description) {
        description = description.replace("<br>", "");
        if (description.contains("~!"))
            description = description.substring(0, description.indexOf("~!"));
        if (description.length() > 450)
            description = description.substring(0, 450) + "...";

        return description;
    }

    private static String readResource(String name) {
        InputStream stream = QueryType.class.getResourceAsStream("/assets/anilink/graphql/" + name + ".graphql");
        try {
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}
