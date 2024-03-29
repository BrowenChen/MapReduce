import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.Math;
import java.lang.String;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/*
 * This is the skeleton for CS61c project 1, Fall 2013.
 *
 * Reminder:  DO NOT SHARE CODE OR ALLOW ANOTHER STUDENT TO READ YOURS.
 * EVEN FOR DEBUGGING. THIS MEANS YOU.
 *
 */
public class Proj1{

    /*
     * Inputs is a set of (docID, document contents) pairs.
     */
    public static class Map1 extends Mapper<WritableComparable, Text, Text, DoublePair> {
        /** Regex pattern to find words (alphanumeric + _). */
        final static Pattern WORD_PATTERN = Pattern.compile("\\w+");

        private String targetGram = null;
        private int funcNum = 0;

        /*
         * Setup gets called exactly once for each mapper, before map() gets called the first time.
         * It's a good place to do configuration or setup that can be shared across many calls to map
         */
        @Override
            public void setup(Context context) {
                targetGram = context.getConfiguration().get("targetWord").toLowerCase();
                try {
                    funcNum = Integer.parseInt(context.getConfiguration().get("funcNum"));
                } catch (NumberFormatException e) {
                    /* Do nothing. */
                }
            }

        @Override
            public void map(WritableComparable docID, Text docContents, Context context)
            throws IOException, InterruptedException {
                Matcher matcher = WORD_PATTERN.matcher(docContents.toString());
                Func func = funcFromNum(funcNum);

                // YOUR CODE HERE
		Matcher matcher2 = WORD_PATTERN.matcher(docContents.toString());
		ArrayList<Integer> count = new ArrayList(); //Index of target word
		
		int index = 0;
		int index2 = 0;

		// System.out.println("Starting matcher find");

		// System.out.println("Target word is " + targetGram);
		//Determine the target words in the documents
		while(matcher.find()) { 
			index++;
			
			String word = (matcher.group()).toLowerCase();
			// System.out.println("Index is " + index + " string is " + word);
			//if target word, add to the arrayList an Integer the index the target word is at.
			if (word.equals(targetGram)){
			   // System.out.println("index is: " + index);
			   Integer target = new Integer(index);
			   count.add(target);			
			}
		    			
		}
	
		// System.out.println("Size of the count ArrayList of Target words is : " + count.size());
		// System.out.println("Starting Second matcher find for same doc");
		//For each word, emit (word, DP(occurence (1), co-currence (f(d))
		//need to account if target word is not in context, set d = 0 if so. 
		while(matcher2.find()){
			
			//int d = 0;
			index2++;
			Text word2 = new Text(matcher2.group().toLowerCase());
			/*
			word2.toString().toLowerCase();
*/
			// System.out.println("Word 2 index " + index2 + " is " + word2);
			if (count.size() == 0){
			   double zero = func.f(Double.POSITIVE_INFINITY);
			   // System.out.print("no target word in this document " + zero );
			   
			   context.write(word2, new DoublePair(1.0 , func.f(Double.POSITIVE_INFINITY)));
			   //got rid of break statement to account for occurences
			}else {

				// System.out.print("There is a target word here ");
			//find co-currence with target word.FIX THIS
				if (!(word2.toString().equals(targetGram))){
				    int min = 100000000; 

				 //Distance Forumla
				    	for (int i = 0; i < count.size(); i++){
				        	if (Math.abs(count.get(i) - index2) < min){
				            	min = Math.abs(count.get(i) - index2);
				        	}
				    	}
					    
					

				   
				    // System.out.println("d value for word " + word2 + " index " + index2 + " is " + min);
				    double coFunc = func.f(min);
				    // System.out.println("coFunc of this is " + coFunc);
				    DoublePair outValues = new DoublePair(1.0, coFunc);
				    context.write(word2, outValues); //emit		
				} 
			}
		}

	

	}

        /** Returns the Func corresponding to FUNCNUM*/
        private Func funcFromNum(int funcNum) {
            Func func = null;
            switch (funcNum) {
                case 0:	
                    func = new Func() {
                        public double f(double d) {
                            return d == Double.POSITIVE_INFINITY ? 0.0 : 1.0;
                        }			
                    };	
                    break;
                case 1:
                    func = new Func() {
                        public double f(double d) {
                            return d == Double.POSITIVE_INFINITY ? 0.0 : 1.0 + 1.0 / d;
                        }			
                    };
                    break;
                case 2:
                    func = new Func() {
                        public double f(double d) {
                            return d == Double.POSITIVE_INFINITY ? 0.0 : 1.0 + Math.sqrt(d);
                        }			
                    };
                    break;
            }
            return func;
        }
    }

    /** Here's where you'll be implementing your combiner. It must be non-trivial for you to receive credit. */
    public static class Combine1 extends Reducer<Text, DoublePair, Text, DoublePair> {

        @Override
            public void reduce(Text key, Iterable<DoublePair> values,
                    Context context) throws IOException, InterruptedException {

                 // YOUR CODE HERE
		                 
                double sumOccurence = 0.0;
                double sumCocurrence = 0.0;
                for (DoublePair value : values){
                    sumOccurence += value.getDouble1();
                    sumCocurrence += value.getDouble2();
                }

                context.write(key, new DoublePair(sumOccurence, sumCocurrence));
            }
    }


//change the data types, Text to DoublePairs. 
    public static class Reduce1 extends Reducer<Text, DoublePair, Text, DoublePair> {
        @Override
            public void reduce(Text key, Iterable<DoublePair> values,
                    Context context) throws IOException, InterruptedException {
                // YOUR CODE HERE
		//for each key, sum up occurence and cocurrence values
		double sumOccurence = 0.0;
		double sumCocurrence = 0.0;
		double coCurrenceRate = 0.0;
		// System.out.println("Starting Reduce 1 ");


		//limit output to 100.
		for (DoublePair text : values){
		    sumOccurence += text.getDouble1();
		    sumCocurrence += text.getDouble2();		
	
		}

		// System.out.print("Word " + key.toString() + " Occurence: " + sumOccurence + " and Cocurrence "  + sumCocurrence);

		//do the cocurrence rate calculation
		if (sumCocurrence > 0.0){
			coCurrenceRate = (sumCocurrence * Math.pow(Math.log(sumCocurrence), 3)) / sumOccurence;	
		}

		// System.out.println(" coCurrenceRate is " + coCurrenceRate);
		context.write(key, new DoublePair(sumOccurence, coCurrenceRate));
		


            }
    }

    public static class Map2 extends Mapper<Text, DoublePair, DoubleWritable, Text> {
        //maybe do something, maybe don't
	//sort by value. DoublePair key and sort by Cocurrence rate. Secondary Sortying
	
	//for each key, emit sumCocurrence as the key, and word as value
	//Co-Currence Rate is Sw * (log(Sw))^3 / Aw
	
       //got rid of override
            public void map(Text key, DoublePair values, Context context)
               throws IOException, InterruptedException {
		double CoCurrenceRate = values.getDouble2();
		// System.out.println("Cocurrence Rate for word " + key.toString() + " is " + CoCurrenceRate);		
	        /*
		if (values.getDouble2() > 0){
		   //cocurrence rate calculation
		   CoCurrenceRate = (values.getDouble2() * Math.pow(Math.log(values.getDouble2()), 3)) / values.getDouble1();
		   
		}else {
		   CoCurrenceRate = 0;
		}
		*/
		//emit the rate to be sorted, and the word
		context.write(new DoubleWritable(CoCurrenceRate * -1.0), key);

	    }
	


    }

    public static class Reduce2 extends Reducer<DoubleWritable, Text, DoubleWritable, Text> {

        int n = 0;
        static int N_TO_OUTPUT = 100;

        /*
         * Setup gets called exactly once for each reducer, before reduce() gets called the first time.
         * It's a good place to do configuration or setup that can be shared across many calls to reduce
         */
        @Override
            protected void setup(Context c) {
                n = 0;
            }

        /*
         * Your output should be a in the form of (DoubleWritable score, Text word)
         * where score is the co-occurrence value for the word. Your output should be

         * sorted from largest co-occurrence to smallest co-occurrence.
         */
        @Override
            public void reduce(DoubleWritable key, Iterable<Text> values,
                    Context context) throws IOException, InterruptedException {

                 // YOUR CODE HERE
		//for each key, emit (coccurence score, word)
		//reverse the order
		
		//iterate through the list of Text words.
		//write out DoubleWritable Key cocurrence rate, first iterable<Text> in values for the word)
		
		DoubleWritable score = new DoubleWritable(key.get() * -1.0);
		for (Text word : values){
			if (N_TO_OUTPUT != 0){
				context.write(score, word);
				N_TO_OUTPUT--;
			}
		}
	
		

            }
    }

    /*
     *  You shouldn't need to modify this function much. If you think you have a good reason to,
     *  you might want to discuss with staff.
     *
     *  The skeleton supports several options.
     *  if you set runJob2 to false, only the first job will run and output will be
     *  in TextFile format, instead of SequenceFile. This is intended as a debugging aid.
     *
     *  If you set combiner to false, the combiner will not run. This is also
     *  intended as a debugging aid. Turning on and off the combiner shouldn't alter
     *  your results. Since the framework doesn't make promises about when it'll
     *  invoke combiners, it's an error to assume anything about how many times
     *  values will be combined.
     */
    public static void main(String[] rawArgs) throws Exception {
        GenericOptionsParser parser = new GenericOptionsParser(rawArgs);
        Configuration conf = parser.getConfiguration();
        String[] args = parser.getRemainingArgs();

        boolean runJob2 = conf.getBoolean("runJob2", true);
        boolean combiner = conf.getBoolean("combiner", false);

        System.out.println("Target word: " + conf.get("targetWord"));
        System.out.println("Function num: " + conf.get("funcNum"));

        if(runJob2)
            System.out.println("running both jobs");
        else
            System.out.println("for debugging, only running job 1");

        if(combiner)
            System.out.println("using combiner");
        else
            System.out.println("NOT using combiner");

        Path inputPath = new Path(args[0]);
        Path middleOut = new Path(args[1]);
        Path finalOut = new Path(args[2]);
        FileSystem hdfs = middleOut.getFileSystem(conf);
        int reduceCount = conf.getInt("reduces", 32);

        if(hdfs.exists(middleOut)) {
            System.err.println("can't run: " + middleOut.toUri().toString() + " already exists");
            System.exit(1);
        }
        if(finalOut.getFileSystem(conf).exists(finalOut) ) {
            System.err.println("can't run: " + finalOut.toUri().toString() + " already exists");
            System.exit(1);
        }

        {
            Job firstJob = new Job(conf, "job1");

            firstJob.setJarByClass(Map1.class);

            /* You may need to change things here */
            firstJob.setMapOutputKeyClass(Text.class);
            firstJob.setMapOutputValueClass(DoublePair.class);
            firstJob.setOutputKeyClass(Text.class);
            firstJob.setOutputValueClass(DoublePair.class);
            /* End region where we expect you to perhaps need to change things. */

            firstJob.setMapperClass(Map1.class);
            firstJob.setReducerClass(Reduce1.class);
            firstJob.setNumReduceTasks(reduceCount);


            if(combiner)
                firstJob.setCombinerClass(Combine1.class);

            firstJob.setInputFormatClass(SequenceFileInputFormat.class);
            if(runJob2)
                firstJob.setOutputFormatClass(SequenceFileOutputFormat.class);

            FileInputFormat.addInputPath(firstJob, inputPath);
            FileOutputFormat.setOutputPath(firstJob, middleOut);

            firstJob.waitForCompletion(true);
        }

        if(runJob2) {
            Job secondJob = new Job(conf, "job2");

            secondJob.setJarByClass(Map1.class);
            /* You may need to change things here */
            secondJob.setMapOutputKeyClass(DoubleWritable.class);
            secondJob.setMapOutputValueClass(Text.class);
            secondJob.setOutputKeyClass(DoubleWritable.class);
            secondJob.setOutputValueClass(Text.class);
            /* End region where we expect you to perhaps need to change things. */

            secondJob.setMapperClass(Map2.class);
            secondJob.setReducerClass(Reduce2.class);

            secondJob.setInputFormatClass(SequenceFileInputFormat.class);
            secondJob.setOutputFormatClass(TextOutputFormat.class);
            secondJob.setNumReduceTasks(1);


            FileInputFormat.addInputPath(secondJob, middleOut);
            FileOutputFormat.setOutputPath(secondJob, finalOut);

            secondJob.waitForCompletion(true);
        }
    }

}

