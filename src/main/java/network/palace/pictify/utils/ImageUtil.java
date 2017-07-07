package network.palace.pictify.utils;

import network.palace.core.Core;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * @author Marc
 * @since 7/7/17
 */
public class ImageUtil {

    public static BufferedImage loadImage(int id, URL u) {
        try {
            return ImageIO.read(u);
        } catch (IOException e) {
            Core.logMessage("Pictify URL Request", "IOException for ID: " + id + " URL: " + u.toString());
        }
        return null;
    }
}
