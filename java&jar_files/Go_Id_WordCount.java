// Importing required libraries and packages for the program
import java.io.IOException;
import java.util.*;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

// Class for the main program
public class Go_Id_WordCount {
	
	// Mapper class for the program
    public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        private Text go = new Text();
		
		// Map function for the mapper class
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			// Splitting the input value by tab
            String[] fields = value.toString().split("\t");
			// Checking if the input value has at least 5 fields
            if (fields.length >= 5) {
                String go_id = fields[4];
                go.set(go_id);
                context.write(go, one);
            }
        }
    }
	
	// Combiner class for the program
    public static class Combine extends Reducer<Text, IntWritable, Text, IntWritable> {
		// Reduce function for the combiner class
        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
			// Calculating the sum of the values
            for (IntWritable val : values) {
                sum += val.get();
            }
			// Writing the output to context
            context.write(key, new IntWritable(sum));
        }
    }
	
	// Reducer class for the program
    public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {
		// TreeMap for storing the count of each word
        private Map<Integer, String> countMap = new TreeMap<>(Collections.reverseOrder());
		
		// Reduce function for the reducer class
        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
			// Calculating the sum of the values
            for (IntWritable val : values) {
                sum += val.get();
            }
			// Storing the count of each word in the TreeMap
            countmap.put(sum, key.toString());
        }
		
		// Cleanup function for the reducer class
        protected void cleanup(Context context) throws IOException, InterruptedException {
            int counter = 0;
			// Writing the top 10 words to the output
            for (Map.Entry<Integer, String> entry : countMap.entrySet()) {
                if (counter++ == 10) {
                    break;
                }
                context.write(new Text(entry.getValue()), new IntWritable(entry.getKey()));
            }
        }
    }
	
	// Main function for the program
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
		// Creating a new job
        Job job = Job.getInstance(conf, "GO Count");
		
		// Setting the jar file for the job
        job.setJarByClass(Go_Id_WordCount.class);
		// Setting the mapper class for the job
        job.setMapperClass(Map.class);
		// Setting the combiner class for the job
        job.setCombinerClass(Combine.class);
		// Setting the reducer class for the job
        job.setReducerClass(Reduce.class);
		// Setting the output key class for the job
        job.setOutputKeyClass(Text.class);
		// Setting the output value class for the Reducer
        job.setOutputValueClass(IntWritable.class);
		// Setting the input and output paths
        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
		// Submitting the job and waiting for its completion
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
