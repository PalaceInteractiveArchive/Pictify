package network.palace.pictify;

import lombok.Getter;
import network.palace.core.plugin.Plugin;
import network.palace.core.plugin.PluginInfo;
import network.palace.pictify.commands.PictifyCommand;
import network.palace.pictify.listeners.PlayerLeave;
import network.palace.pictify.renderer.ImageRenderer;
import network.palace.pictify.renderer.RendererManager;

/**
 * @author Marc
 * @since 6/27/17
 */
@PluginInfo(name = "Pictify", version = "1.0.8", depend = {"Core", "ProtocolLib"}, canReload = true)
public class Pictify extends Plugin {
    @Getter private static Pictify instance;
    @Getter private static RendererManager rendererManager;

    @Override
    protected void onPluginEnable() throws Exception {
        instance = this;
        rendererManager = new RendererManager();
        // Un-comment for 1.13
        // registerListener(new MapListener());
        registerListener(new PlayerLeave());
        registerCommand(new PictifyCommand());
    }

    @Override
    protected void onPluginDisable() throws Exception {
        for (ImageRenderer image : rendererManager.getImages()) {
            image.deactivate();
        }
    }
}
