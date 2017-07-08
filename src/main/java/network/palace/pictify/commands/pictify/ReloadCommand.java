package network.palace.pictify.commands.pictify;

import network.palace.core.command.CommandException;
import network.palace.core.command.CoreCommand;
import network.palace.pictify.Pictify;
import network.palace.pictify.renderer.RendererManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * @author Marc
 * @since 7/7/17
 */
public class ReloadCommand extends CoreCommand {

    public ReloadCommand() {
        super("reload");
    }

    @Override
    protected void handleCommandUnspecific(CommandSender sender, String[] args) throws CommandException {
        RendererManager manager = Pictify.getInstance().getRendererManager();
        sender.sendMessage(ChatColor.GREEN + "Reloading Pictify image map...");
        manager.load();
        sender.sendMessage(ChatColor.GREEN + "Pictify image map reloaded!");
    }
}
