package network.palace.pictify.commands.pictify;

import network.palace.core.Core;
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
        if (args.length == 0) {
            RestoreUtil.toggle(player);
        } else {
            CPlayer p = Core.getPlayerManager().getPlayer(args[0]);
            if (p != null)
                RestoreUtil.toggle(p);
        }
    }
}
