package network.palace.pictify.renderer;

import lombok.Getter;
import network.palace.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
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
@SuppressWarnings("deprecation")
public class ImageRenderer extends MapRenderer {
    @Getter private int id;
    @Getter private BufferedImage image;
    @Getter public int xCap = 0;
    @Getter public int yCap = 0;
    @Getter private byte[] data;
    private List<UUID> rendered = new ArrayList<>();

    public ImageRenderer(int id, BufferedImage image) {
        this.id = id;
        this.image = image;
        this.data = MapPalette.imageToBytes(image);
        this.xCap = image.getWidth(null);
        this.yCap = image.getHeight(null);
        activate();
    }

    @Override
    public void render(MapView view, MapCanvas canvas, Player p) {
        if (rendered.contains(p.getUniqueId())) {
            // Already sent this player the map, no need to send it again
            return;
        }
        for (int x2 = 0; x2 < this.xCap; x2++) {
            for (int y2 = 0; y2 < this.yCap; y2++) {
                try {
                    canvas.setPixel(x2, y2, data[(y2 * this.yCap + x2)]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    Core.logMessage("Renderer " + id, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        rendered.add(p.getUniqueId());
        p.sendMap(view);
    }

    public void deactivate() {
        MapView m = Bukkit.getMap((short) this.id);
        for (MapRenderer mr : m.getRenderers())
            m.removeRenderer(mr);
    }

    public void activate() {
        deactivate();
        Bukkit.getMap((short) this.id).addRenderer(this);
    }

    public void leave(UUID uuid) {
        rendered.remove(uuid);
    }
}
