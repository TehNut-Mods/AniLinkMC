package me.waifu.anilink;

import me.waifu.graphquery.GraphQLQuery;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;

import java.util.Locale;

public class CommandQuery extends CommandBase {

    private final QueryType query;
    private final String name;

    public CommandQuery(QueryType query) {
        this.query = query;
        this.name = query.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/" + name + " [search_string]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0)
            throw new CommandException(getUsage(sender));

        GraphQLQuery query = this.query.getQuery();
        query.withVariable("name", String.join(" ", args));
        try {
            QueryThread.INSTANCE.queue.add(new QueryThread.Query(sender, query.createRequest(), this.query));
        } catch (Exception e) {
            throw new CommandException(e.getMessage());
        }
    }
}
