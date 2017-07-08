package network.palace.pictify.commands.pictify;

import network.palace.core.command.CommandException;
import network.palace.core.command.CoreCommand;
import network.palace.core.player.CPlayer;
import network.palace.pictify.utils.RestoreUtil;

/**
 * @author Marc
 * @since 7/7/17
 */
public class RestoreCommand extends CoreCommand {

    public RestoreCommand() {
        super("restore");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        RestoreUtil.toggle(player);
    }
}
