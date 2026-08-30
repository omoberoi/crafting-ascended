import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public final class MakeTransparent {
    public static void main(String[] args) throws Exception {
        BufferedImage source = ImageIO.read(new File(args[0]));
        BufferedImage output = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int rgb = source.getRGB(x, y);
                int r = (rgb >> 16) & 255;
                int g = (rgb >> 8) & 255;
                int b = rgb & 255;
                int brightest = Math.max(r, Math.max(g, b));
                int alpha = brightest <= 22 ? 0 : brightest >= 65 ? 255 : (brightest - 22) * 255 / 43;
                output.setRGB(x, y, (alpha << 24) | (rgb & 0xFFFFFF));
            }
        }
        ImageIO.write(output, "png", new File(args[1]));
    }
}
