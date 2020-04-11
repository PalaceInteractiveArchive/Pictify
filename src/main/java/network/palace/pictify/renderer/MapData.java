package network.palace.pictify.renderer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.World;

@Getter
@AllArgsConstructor
public class MapData {
    private World world;
    private int previousId;
    private int nextId;
}
