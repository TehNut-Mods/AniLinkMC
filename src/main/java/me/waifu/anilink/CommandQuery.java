package me.waifu.anilink;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Locale;

public class CommandQuery {

    private final QueryType type;
    private final String name;

    public CommandQuery(QueryType type) {
        this.type = type;
        this.name = type.name().toLowerCase(Locale.ROOT);
    }

    public LiteralArgumentBuilder<ServerCommandSource> create() {
        return CommandManager.literal(name)
                .then(CommandManager.argument(name, StringArgumentType.string())
                        .executes(context -> {
                            String search = context.getArgument(name, String.class);
                            GraphQLQuery query = this.type.getQuery();
                            this.type.getDefaultVariables().forEach(query::withVariable);
                            query.withVariable("name", search);
                            QueryThread.INSTANCE.queue.add(new QueryThread.Query(context.getSource().getPlayer(), query, this.type));
                            return 1;
                        })
                );
    }
}
