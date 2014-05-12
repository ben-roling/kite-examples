package org.kitesdk.examples.oozie;

import java.net.URI;
import java.util.StringTokenizer;

import org.apache.crunch.MapFn;
import org.apache.crunch.PCollection;
import org.apache.crunch.PipelineResult;
import org.apache.crunch.io.From;
import org.apache.crunch.types.avro.Avros;
import org.apache.crunch.util.CrunchTool;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ToolRunner;
import org.kitesdk.data.Dataset;
import org.kitesdk.data.Datasets;
import org.kitesdk.data.crunch.CrunchDatasets;

public class PersonTool extends CrunchTool {

  @Override
  public int run(String[] args) throws Exception {
    final Path inputPath = new Path(args[0]);
    final URI outputDatasetUri = new URI(args[1]);

    final Dataset<Person> outputDataset = Datasets.createPartition(outputDatasetUri);

    final PCollection<String> rawPersons = read(From.textFile(inputPath));
    final PCollection<Person> processedPersons = doSomeProcessing(rawPersons);

    write(processedPersons, CrunchDatasets.asTarget(outputDataset, true));

    final PipelineResult result = run();
    if (!result.succeeded()) {
        throw new RuntimeException("Pipeline run failed!");
    }

    return 0;
  }

  private PCollection<Person> doSomeProcessing(final PCollection<String> rawPersons) {
    return rawPersons.parallelDo(new MapFn<String, Person>() {

      @Override
      public Person map(final String input) {
        final StringTokenizer tokenizer = new StringTokenizer(input, ",");
        final String id = tokenizer.nextToken();
        final String firstName = tokenizer.nextToken();
        final String lastName = tokenizer.nextToken();
        final int age = Integer.parseInt(tokenizer.nextToken());
        return Person.newBuilder().setId(id).setFirstName(firstName)
            .setLastName(lastName).setAge(age).build();
      }
    }, Avros.records(Person.class));
  }

  public static void main(final String[] args) throws Exception {
      ToolRunner.run(new PersonTool(), args);
  }

}
