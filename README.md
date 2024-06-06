# Computational Analysis Platform for Comparing Bacterial Organisms

## Overview

This project aims to develop a scalable application using Hadoop and MapReduce to analyze and compare various bacterial organisms based on their protein functions. The application processes input files containing protein information, extracts Gene Ontology (GO) terms, and identifies the most commonly used GO terms across the input files.

## Task Description

A biopharmaceutical company requires a computational analysis platform to compare various bacterial organisms based on their features. The goal is to investigate how cloud resources can be utilized to develop a scalable application to efficiently analyze several organisms as and when required. The application will use Hadoop and MapReduce to process protein data and report the ten most commonly used GO terms for each input file and across all input files.

## Input Files

The input files provided are:

1. Bacillus_amyloliquefaciens_FZB42-326423.gaf
2. Bacillus_licheniformis_ATCC_14580-279010.gaf
3. Bacillus_megaterium_DSM_319-592022.gaf
4. Bacillus_subtilis_168-224308.gaf
5. Escherichia_coli_K-12_ecocyc_83333.gaf
6. Geobacillus_kaustophilus_HTA426-235909.gaf
7. Geobacillus_thermodenitrificans_NG80_2-420246.gaf

Each line in these files includes an identifier for a protein in the second column and information about the protein’s function in the fifth column. A protein’s function is specified via a related Gene Ontology (GO) term.

## Application Workflow

1. **Mapper**: Extracts GO terms from each line of the input files.
2. **Combiner**: Aggregates counts of GO terms locally before sending data to the Reducer.
3. **Reducer**: Aggregates the counts from all Mappers, sorts the GO terms by count, and selects the top ten GO terms.
4. **Driver**: Configures and runs the Hadoop job.

## Code Implementation

### Mapper Class

The `Map` class reads the input files, extracts the GO terms, and emits each GO term with a count of 1.

```java
import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private Text go = new Text();

    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] fields = value.toString().split("\t");
        if (fields.length >= 5) {
            String go_id = fields[4];
            go.set(go_id);
            context.write(go, one);
        }
    }
}
```

### Combiner Class

The Combine class aggregates the counts of GO terms locally to reduce the amount of data transferred to the Reducer.

```
import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public static class Combine extends Reducer<Text, IntWritable, Text, IntWritable> {
    public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        int sum = 0;
        for (IntWritable val : values) {
            sum += val.get();
        }
        context.write(key, new IntWritable(sum));
    }
}
```
### Reducer Class
```
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {
    private Map<Integer, String> countMap = new TreeMap<>(Collections.reverseOrder());

    public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        int sum = 0;
        for (IntWritable val : values) {
            sum += val.get();
        }
        countMap.put(sum, key.toString());
    }

    protected void cleanup(Context context) throws IOException, InterruptedException {
        int counter = 0;
        for (Map.Entry<Integer, String> entry : countMap.entrySet()) {
            if (counter++ == 10) {
                break;
            }
            context.write(new Text(entry.getValue()), new IntWritable(entry.getKey()));
        }
    }
}
```
### Driver Class
The Go_Id_WordCount class configures and runs the Hadoop job.
```
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Go_Id_WordCount {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "GO Count");

        job.setJarByClass(Go_Id_WordCount.class);
        job.setMapperClass(Map.class);
        job.setCombinerClass(Combine.class);
        job.setReducerClass(Reduce.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
```

## Output Files
The output files contain the top ten GO terms for each input file and across
all input files:

    Output_Bacillus_amyloliquefaciens_FZB42-326423_Top_10_GO_terms
    Output_Bacillus_licheniformis_ATCC_14580-279010_Top_10_GO_terms
    Output_Bacillus_megaterium_DSM_319-592022_Top_10_GO_terms
    Output_Bacillus_subtilis_168-224308_Top_10_GO_terms
    Output_Escherichia_coli_K-12_ecocyc_83333_Top_10_GO_terms
    Output_Geobacillus_kaustophilus_HTA426-235909_Top_10_GO_terms
    Output_Geobacillus_thermodenitrificans_NG80_2-420246_Top_10_GO_terms
    Output_accross_all_Input_files_Top_10_GO_terms


## Running the Application

To run the application, use the following command:
```
hadoop jar Go_Id_WordCount.jar Go_Id_WordCount /path/to/input /path/to/output
```
Replace /path/to/input with the directory containing the input files and /path/to/output with the directory where you want to store the output files.

## Conclusion

This application leverages Hadoop and MapReduce to efficiently process large
datasets of bacterial protein information. By using cloud resources, the application can scale to analyze thousands of organisms, providing valuable insights into their protein functions through the identification of common GO terms.
