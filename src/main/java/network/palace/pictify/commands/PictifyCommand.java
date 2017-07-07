package network.palace.pictify.commands;

import network.palace.core.command.CommandMeta;
import network.palace.core.command.CommandPermission;
import network.palace.core.command.CoreCommand;
import network.palace.core.player.Rank;
import network.palace.pictify.commands.pictify.ListCommand;

/**
 * @author Marc
 * @since 7/7/17
 */
@CommandMeta(aliases = "pic", description = "Default pictify command")
@CommandPermission(rank = Rank.KNIGHT)
public class PictifyCommand extends CoreCommand {

    public PictifyCommand() {
        super("pictify");

        registerSubCommand(new ListCommand());
    }

    @Override
    protected boolean isUsingSubCommandsOnly() {
        return true;
    }
}
