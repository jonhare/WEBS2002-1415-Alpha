package uk.ac.soton.ecs.webs2002a;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourMap;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.util.iterator.TextLineIterable;

public class MapTool {
	public final static int WIDTH = 1440 * 4;
	public final static int HEIGHT = 720 * 4;

	public static void main(String[] args) throws IOException {
		final File file = new File("/Volumes/SSD/webs2002/faces.csv");

		final FImage all = new FImage(WIDTH, HEIGHT);
		final FImage lte2 = new FImage(WIDTH, HEIGHT);
		final FImage gte3 = new FImage(WIDTH, HEIGHT);

		int i = 0;
		for (final String line : new TextLineIterable(file)) {
			final String[] parts = line.split(",");
			final double lat = Double.parseDouble(parts[4]);
			final double lon = Double.parseDouble(parts[5]);
			final int count = Integer.parseInt(parts[6]);

			final Point2dImpl p = createPoint(lat, lon);

			all.pixels[Math.round(p.y)][Math.round(p.x)]++;

			if (count > 0 && count <= 2)
				lte2.pixels[Math.round(p.y)][Math.round(p.x)]++;
			if (count >= 3)
				gte3.pixels[Math.round(p.y)][Math.round(p.x)]++;

			if (i++ % 10000 == 0) {
				System.err.println(i);
			}
		}

		// final float meanlte2 = FloatArrayStatsUtils.mean(lte2.pixels);
		// lte2.subtractInplace(meanlte2);
		// final float stdlte2 = FloatArrayStatsUtils.std(lte2.pixels);
		// lte2.divideInplace(stdlte2);
		//
		// final float meangte3 = FloatArrayStatsUtils.mean(gte3.pixels);
		// gte3.subtractInplace(meangte3);
		// final float stdgte3 = FloatArrayStatsUtils.std(gte3.pixels);
		// gte3.divideInplace(stdgte3);
		//
		// final FImage diff =
		// lte2.subtract(gte3).divideInplace(2).addInplace(0.5f);
		// DisplayUtilities.display(diff);
		// DisplayUtilities.display(diff.normalise());

		final MBFImage div = new MBFImage(WIDTH, HEIGHT, 3);
		for (int j = 0; j < HEIGHT; j++) {
			for (i = 0; i < WIDTH; i++) {
				// div.pixels[j][i] = gte3.pixels[j][i] == 0 ? 0 :
				// lte2.pixels[j][i] / gte3.pixels[j][i];

				if (gte3.pixels[j][i] == 0 && lte2.pixels[j][i] == 0)
					div.setPixel(i, j, RGBColour.GRAY);
				else if (gte3.pixels[j][i] != 0) {
					final float val = gte3.pixels[j][i] / (lte2.pixels[j][i] + gte3.pixels[j][i]);

					div.setPixel(i, j, ColourMap.Hot.apply(val));

				}
			}
		}

		// div.normalise();

		// DisplayUtilities.display(div);
		ImageUtilities.write(div, new File("./ratio.png"));
	}

	public static Point2dImpl createPoint(double lat, double lon) {
		final double x = (lon + 180) / 361; // now goes from 0..<1
		final double y = (90 - lat) / 181; // now goes from 0..<1

		return new Point2dImpl(x * WIDTH, y * HEIGHT);
	}

	public static void logNorm(final FImage img) {
		for (int y = 0; y < img.height; y++)
			for (int x = 0; x < img.width; x++)
				img.pixels[y][x] = img.pixels[y][x] == 0 ? 0 : (float) Math.log(img.pixels[y][x]);

		img.normalise();
	}
}
