package uk.ac.soton.ecs.webs2002a;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * This class implements a Hadoop {@link Mapper} that will run on every
 * available CPU of each machine in the cluster. The class is responsible for
 * processing a subset of all the images (which are assigned in such a way to
 * minimise the amount of data transferred between machines). The data bytes
 * representing each image are handed to the
 * {@link #map(Text, BytesWritable, Context)} method for processing.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class FaceDetectorMapper extends Mapper<Text, BytesWritable, Text, Text> {
	enum Counters {
		SUCCESS_NO_FACES, SUCCESS_FACES, FAILURE
	}

	final HaarCascadeDetector faceDetector;

	public FaceDetectorMapper() {
		faceDetector = HaarCascadeDetector.BuiltInCascade.frontalface_alt2.load();
		faceDetector.setMinSize(35); // 35px min size as agreed
	}

	/**
	 * This method processes a single image, by loading it into memory, running
	 * the face detector & then forming the output as a list of comma separated
	 * values:
	 * 
	 * <pre>
	 * <num_faces>,<image_width>,<image_height>,[<x1>;<y1>;<size1>:<x2>;<y2>;<size2>:...]
	 * </pre>
	 * 
	 * The id will be automatically inserted as the first column.
	 * 
	 * @see org.apache.hadoop.mapreduce.Mapper#map(KEYIN, VALUEIN,
	 *      org.apache.hadoop.mapreduce.Mapper.Context)
	 */
	@Override
	protected void map(Text key, BytesWritable value, Context context)
			throws IOException, InterruptedException
	{
		try {
			// read the image from the array of bytes given to us by the hadoop
			// framework
			final MBFImage img = ImageUtilities.readMBF(new ByteArrayInputStream(value.getBytes()));

			// convert to grey-scale
			final FImage grey = img.flatten();

			// run the face detector
			final List<DetectedFace> faces = faceDetector.detectFaces(grey);

			// format the csv string:
			final String csv = buildCSV(grey, faces);

			// this is just for logging purposes so we can see how far along
			// processing is in the web UI
			if (faces == null || faces.size() == 0)
				context.getCounter(Counters.SUCCESS_NO_FACES).increment(1);
			else
				context.getCounter(Counters.SUCCESS_FACES).increment(1);

			context.write(key, new Text(csv));
		} catch (final Exception e) {
			// In case there was an problem with an image, we just log it
			context.getCounter(Counters.FAILURE).increment(1);
			System.err.println(key);
			e.printStackTrace();
		}
	}

	private String buildCSV(FImage img, List<DetectedFace> faces) {
		// handle the count and width/height of the image
		String csv = String.format("%d,%d,%d,", faces == null ? 0 : faces.size(), img.width, img.height);

		// then add the bounding boxes for each face
		if (faces != null && faces.size() >= 1) {
			Rectangle bounds = faces.get(0).getBounds();
			csv += String.format("%d;%d;%d", (int) bounds.x, (int) bounds.y, (int) bounds.width);

			for (int i = 1; i < faces.size(); i++) {
				bounds = faces.get(i).getBounds();
				csv += String.format(":%d;%d;%d", (int) bounds.x, (int) bounds.y, (int) bounds.width);
			}
		}

		return csv;
	}
}
