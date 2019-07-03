package network.palace.pictify.listeners;

import network.palace.core.Core;
import network.palace.pictify.Pictify;
import network.palace.pictify.renderer.ImageRenderer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class MapListener implements Listener {

    @EventHandler
    public void onMapInitialize(MapInitializeEvent event) {
        MapView view = event.getMap();
        if (view.isVirtual()) return;
        ImageRenderer imageRenderer = Pictify.getRendererManager().getLocalImage(view.getId());
        if (imageRenderer == null) return;
        Core.runTask(Pictify.getInstance(), () -> {
            for (MapRenderer renderer : view.getRenderers()) {
                view.removeRenderer(renderer);
            }
            imageRenderer.setMapView(view);
            view.addRenderer(imageRenderer);
        });
    }
}
