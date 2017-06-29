package network.palace.pictify;

import lombok.Getter;
import network.palace.core.plugin.Plugin;
import network.palace.core.plugin.PluginInfo;

/**
 * @author Marc
 * @since 6/27/17
 */
@PluginInfo(name = "Pictify", version = "1.0.0", depend = "Core", canReload = true)
public class Pictify extends Plugin {
    @Getter private static Pictify instance;

    @Override
    protected void onPluginEnable() throws Exception {
        instance = this;
    }

    @Override
    protected void onPluginDisable() throws Exception {
    }
}
