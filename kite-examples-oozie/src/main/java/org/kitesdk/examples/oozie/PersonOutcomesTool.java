package org.kitesdk.examples.oozie;

import java.net.URI;

import org.apache.crunch.MapFn;
import org.apache.crunch.PCollection;
import org.apache.crunch.PipelineResult;
import org.apache.crunch.types.avro.Avros;
import org.apache.crunch.util.CrunchTool;
import org.apache.hadoop.util.ToolRunner;
import org.kitesdk.data.Dataset;
import org.kitesdk.data.Datasets;
import org.kitesdk.data.crunch.CrunchDatasets;

public class PersonOutcomesTool extends CrunchTool {

  @Override
  public int run(String[] args) throws Exception {
    final URI inputDatasetUri = new URI(args[0]);
    final URI outputDatasetUri = new URI(args[1]);

    Dataset<Person> inputDataset = Datasets.load(inputDatasetUri);
    final Dataset<PersonOutcomes> outputDataset = Datasets.createPartition(outputDatasetUri);
    
    final PCollection<Person> persons = read(CrunchDatasets.asSource(inputDataset, Person.class));
    
    final PCollection<PersonOutcomes> personOutcmes = doSomeProcessing(persons);

    write(personOutcmes, CrunchDatasets.asTarget(outputDataset, true));

    final PipelineResult result = run();
    if (!result.succeeded()) {
        throw new RuntimeException("Pipeline run failed!");
    }

    return 0;
  }

  private PCollection<PersonOutcomes> doSomeProcessing(final PCollection<Person> persons) {
    // a real example would have some more interesting logic here
    return persons.parallelDo(new MapFn<Person, PersonOutcomes>() {

      @Override
      public PersonOutcomes map(final Person input) {
        return PersonOutcomes.newBuilder().setPersonId(input.getId()).build();
      }
    }, Avros.records(PersonOutcomes.class));
  }


  public static void main(final String[] args) throws Exception {
      ToolRunner.run(new PersonOutcomesTool(), args);
  }

}
