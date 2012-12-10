package net.semanticmetadata.lire.imageanalysis;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * User: mlux
 * Date: 04.12.12
 * Time: 11:01
 */
public class TestJointHistogram extends TestCase {
    public void testExtract() throws IOException {
        int[][] histogram = new int[64][9];
        for (int i = 0; i < histogram.length; i++) {
            for (int j = 0; j < histogram[i].length; j++)
                histogram[i][j] = 0;
        }
        BufferedImage img = ImageIO.read(new FileInputStream("C:\\Java\\Projects\\LireSVN\\testdata\\ferrari\\black\\2828686873_2fa36f83d7_b.jpg"));
        WritableRaster raster = img.getRaster();
        int[] px = new int[3];
        for (int x = 1; x < raster.getWidth() - 1; x++) {
            for (int y = 1; y < raster.getHeight() - 1; y++) {
                raster.getPixel(x, y, px);
                int colorPos = (int) Math.round((double) px[2] / 85d) +
                        (int) Math.round((double) px[1] / 85d) * 4 +
                        (int) Math.round((double) px[0] / 85d) * 4 * 4;
                int rank = 0;
                double intensity = getIntensity(px);
                if (getIntensity(raster.getPixel(x - 1, y - 1, px)) > intensity) rank++;
                if (getIntensity(raster.getPixel(x, y - 1, px)) > intensity) rank++;
                if (getIntensity(raster.getPixel(x + 1, y - 1, px)) > intensity) rank++;
                if (getIntensity(raster.getPixel(x - 1, y + 1, px)) > intensity) rank++;
                if (getIntensity(raster.getPixel(x, y + 1, px)) > intensity) rank++;
                if (getIntensity(raster.getPixel(x + 1, y + 1, px)) > intensity) rank++;
                if (getIntensity(raster.getPixel(x - 1, y, px)) > intensity) rank++;
                if (getIntensity(raster.getPixel(x + 1, y, px)) > intensity) rank++;
                histogram[colorPos][rank]++;
            }
        }
    }

    private double getIntensity(int[] px) {
        return (0.3 * px[0] + 0.6 * px[1] + 0.1 * px[2]);
    }
}
