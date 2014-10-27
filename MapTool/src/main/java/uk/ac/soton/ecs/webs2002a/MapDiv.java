package uk.ac.soton.ecs.webs2002a;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.util.iterator.TextLineIterable;

public class MapDiv {
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

			if (i++ % 1000 == 0) {
				System.err.println(i);
			}
		}

		logNorm(all);
		logNorm(lte2);
		logNorm(gte3);

		// DisplayUtilities.display(all, "ALL");
		// DisplayUtilities.display(lte2, "LTE2");
		// DisplayUtilities.display(gte3, "GTE3");
		ImageUtilities.write(all, new File("./all.png"));
		ImageUtilities.write(lte2, new File("./lte2.png"));
		ImageUtilities.write(gte3, new File("./gte3.png"));
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
