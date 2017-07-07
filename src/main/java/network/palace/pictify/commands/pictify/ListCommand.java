package network.palace.pictify.commands.pictify;

import network.palace.core.command.CommandException;
import network.palace.core.command.CommandMeta;
import network.palace.core.command.CoreCommand;
import org.bukkit.command.CommandSender;

/**
 * @author Marc
 * @since 7/7/17
 */
@CommandMeta(description = "List all pictify images")
public class ListCommand extends CoreCommand {

    public ListCommand() {
        super("list");
    }

    @Override
    protected void handleCommandUnspecific(CommandSender sender, String[] args) throws CommandException {
    }
}
