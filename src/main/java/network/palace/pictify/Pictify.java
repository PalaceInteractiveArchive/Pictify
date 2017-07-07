package network.palace.pictify;

import lombok.Getter;
import network.palace.core.plugin.Plugin;
import network.palace.core.plugin.PluginInfo;
import network.palace.pictify.listeners.PlayerJoinAndLeave;
import network.palace.pictify.renderer.RendererManager;

/**
 * @author Marc
 * @since 6/27/17
 */
@PluginInfo(name = "Pictify", version = "1.0.0", depend = "Core", canReload = true)
public class Pictify extends Plugin {
    @Getter private static Pictify instance;
    @Getter private RendererManager rendererManager;

    @Override
    protected void onPluginEnable() throws Exception {
        instance = this;
        rendererManager = new RendererManager();
        registerListener(new PlayerJoinAndLeave());
    }

    @Override
    protected void onPluginDisable() throws Exception {
    }
}
