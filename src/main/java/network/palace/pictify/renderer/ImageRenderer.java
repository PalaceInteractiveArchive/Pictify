package network.palace.pictify.renderer;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Marc
 * @since 7/2/17
 */
public class ImageRenderer extends MapRenderer {
    @Getter private final World world;
    @Getter private final int id;
    @Getter private final int frameId;
    @Getter private BufferedImage image;
    @Getter public final int xCap;
    @Getter public final int yCap;
    @Getter @Setter private String source;
    @Getter private final byte[] data;
    @Getter @Setter private boolean restored = false;
    private final List<UUID> rendered = new ArrayList<>();
    @Getter @Setter private MapView mapView;

    public ImageRenderer(int id, int frameId, byte[] data, int xCap, int yCap) {
        this(Bukkit.getWorlds().get(0), id, frameId, data, xCap, yCap, "unknown");
    }

    public ImageRenderer(int id, int frameId, byte[] data, int xCap, int yCap, String source) {
        this(Bukkit.getWorlds().get(0), id, frameId, data, xCap, yCap, source);
    }

    public ImageRenderer(World world, int id, int frameId, byte[] data, int xCap, int yCap, String source) {
        this.world = world;
        this.id = id;
        this.frameId = frameId;
        this.xCap = xCap;
        this.yCap = yCap;
        this.data = data;
        this.source = source;
    }

    @Override
    public void render(MapView view, MapCanvas canvas, Player p) {
        // Already sent this player the map, no need to send it again
        if (rendered.contains(p.getUniqueId())) return;

        for (int x2 = 0; x2 < this.xCap; x2++) {
            for (int y2 = 0; y2 < this.yCap; y2++) {
                try {
                    canvas.setPixel(x2, y2, data[(y2 * this.yCap + x2)]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    Core.logMessage("Renderer " + id, e.getMessage());
                    e.printStackTrace(System.out);
                }
            }
        }

        rendered.add(p.getUniqueId());
    }

    public void deactivate() {
        if (mapView != null) mapView.removeRenderer(this);
    }

    public void leave(UUID uuid) {
        rendered.remove(uuid);
    }
}
