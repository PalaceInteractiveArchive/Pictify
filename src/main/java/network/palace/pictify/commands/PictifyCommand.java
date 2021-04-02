package network.palace.pictify.commands;

import network.palace.core.command.CommandMeta;
import network.palace.core.command.CoreCommand;
import network.palace.core.player.Rank;
import network.palace.pictify.commands.pictify.*;

/**
 * @author Marc
 * @since 7/7/17
 */
@CommandMeta(aliases = "pic", description = "Default pictify command", rank = Rank.CM)
public class PictifyCommand extends CoreCommand {

    public PictifyCommand() {
        super("pictify");
        registerSubCommand(new GiveCommand());
        registerSubCommand(new InfoCommand());
        registerSubCommand(new ListCommand());
        registerSubCommand(new ReloadCommand());
        registerSubCommand(new RemoveCommand());
    }

    @Override
    protected boolean isUsingSubCommandsOnly() {
        return true;
    }
}
