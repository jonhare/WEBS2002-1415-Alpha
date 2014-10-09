package uk.ac.soton.ecs.webs2002a;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openimaj.hadoop.mapreduce.TextBytesJobUtil;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;

/**
 * The main method required to launch the job on the cluster
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class FaceDetectorApp extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {
		// this is just dealing with where the images are loaded from
		final Path[] p = SequenceFileUtility.getFilePaths(args[0], "part");

		// configure the job and set the output location
		final Job job = TextBytesJobUtil.createJob(p, new Path(args[1]), null, this.getConf());

		// more configuration
		job.setJarByClass(this.getClass());
		job.setMapperClass(FaceDetectorMapper.class); // use our
														// FaceDetectorMapper!
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setReducerClass(Reducer.class);
		job.setNumReduceTasks(1);
		job.setOutputFormatClass(TextOutputFormat.class);

		// use a comma to separate the id from the rest of the csv
		job.getConfiguration().set("mapred.textoutputformat.separator", ",");

		// run the job & then wait for it to finish
		// when it's running, loads of information will be printed to the
		// screen...
		job.waitForCompletion(true);

		return 0;
	}

	/**
	 * Run the code!
	 * 
	 * Note that the arguments should be <input_path> <output_path>
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ToolRunner.run(new FaceDetectorApp(), args);
	}
}
