package me.waifu.anilink;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.waifu.graphquery.GraphQLQuery;
import net.minecraft.server.command.ServerCommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Locale;

public class CommandQuery {

    private final QueryType query;
    private final String name;

    public CommandQuery(QueryType query) {
        this.query = query;
        this.name = query.name().toLowerCase(Locale.ROOT);
    }

    public LiteralArgumentBuilder<ServerCommandSource> create() {
        return ServerCommandManager.literal(name)
                .then(ServerCommandManager.argument(name, StringArgumentType.string())
                        .executes(context -> {
                            String search = context.getArgument(name, String.class);
                            GraphQLQuery query = this.query.getQuery();
                            query.withVariable("name", search);
                            QueryThread.INSTANCE.queue.add(new QueryThread.Query(context.getSource().getPlayer(), query, this.query));
                            return 1;
                        })
                );
    }
}
