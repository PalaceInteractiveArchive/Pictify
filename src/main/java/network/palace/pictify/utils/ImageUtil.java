package network.palace.pictify.utils;

import network.palace.core.Core;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
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

    public static BufferedImage scale(BufferedImage b, int width, int height) {
        if ((b.getWidth() == width) && (b.getHeight() == height)) {
            return b;
        }
        AffineTransform a = AffineTransform.getScaleInstance((double) width / b.getWidth(), (double) height / b.getHeight());
        AffineTransformOp o = new AffineTransformOp(a, 2);
        return o.filter(b, new BufferedImage(width, height, b.getType()));
    }
}
