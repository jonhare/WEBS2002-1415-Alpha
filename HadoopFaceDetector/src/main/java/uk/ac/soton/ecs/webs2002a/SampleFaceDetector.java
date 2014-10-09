package uk.ac.soton.ecs.webs2002a;

import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

/**
 * Demonstrating face detection (with a webcam).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SampleFaceDetector {
	public static void main(String[] args) throws VideoCaptureException {
		final VideoCapture vc = new VideoCapture(640, 480);
		final HaarCascadeDetector faceDetector = HaarCascadeDetector.BuiltInCascade.frontalface_alt2.load();
		// faceDetector.setMinSize(80);

		for (final MBFImage frame : vc) {
			final FImage image = frame.flatten();

			final List<DetectedFace> faces = faceDetector.detectFaces(image);

			for (final DetectedFace face : faces) {
				frame.drawShape(face.getBounds(), RGBColour.RED);
			}
			DisplayUtilities.displayName(frame, "faces");
		}
	}
}
