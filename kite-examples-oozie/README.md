# Kite Oozie Versioned Datasets Example

This example demonstrates creating immutable versioned Datasets as described in https://groups.google.com/a/cloudera.org/d/msg/cdk-dev/uUm-wOv1B3o/Sm6cDVBMusoJ.

This example uses some different Entity models than discussed in the thread.  This example uses 3 entity models:

* Person
* PersonOutcomes
* PersonSummary

For each entity model there is a corresponding Oozie coordinator that produces "version" partitions in a Dataset
oriented around that model.

The data flow is Person -> PersonOutcomes -> PersonSummary.  The model is inspired by a processing system in the healthcare
arena where raw person data is processed into a form containing a number of outcomes for each person.  Later, each
person's outcomes might be reduced into a record representing a high-level summary of the person.

The entity models used in the example are bare skeletons and realistic processing logic to transform between the models
is absent.  What those might look like in a more realistic system is left to the reader's imagination.

Person is the first dataset in this example.  The input used to produce that Dataset is a simple text file.

## Prerequisites

* Running instance of [CDH5.0 Quickstart VM](http://www.cloudera.com/content/support/en/downloads/download-components/download-products.html?productID=F6mO278Rvo)

* Build [dataset uris prototype](https://github.com/ben-roling/kite/tree/dataset_uri_prototype) branch of kite

* Drop the following jars in /var/lib/oozie
    * kite-data-core-0.13.1-SNAPSHOT.jar
    * kite-data-hcatalog-0.13.1-SNAPSHOT.jar
    * kite-data-oozie-0.13.1-SNAPSHOT.jar
    * commons-jexl-2.1.1.jar
    * jackson-core-2.3.1.jar
    * jackson-databind-2.3.1.jar

* Add the following to oozie-site.xml Safety Valve:

        <property>
          <name>oozie.service.URIHandlerService.uri.handlers</name>
          <value>org.apache.oozie.dependency.FSURIHandler,org.apache.oozie.dependency.HCatURIHandler,org.kitesdk.data.oozie.KiteURIHandler</value>
        </property>

* You may also need/want to add the following to ensure jobs don't fail due to improper oozie whitelisting of jobtracker
  and namenode

        <property>
            <name>oozie.service.HadoopAccessorService.jobTracker.whitelist</name>
            <value> </value>
            <description>
                Whitelisted job tracker for Oozie service.
            </description>
        </property>
        <property>
            <name>oozie.service.HadoopAccessorService.nameNode.whitelist</name>
            <value> </value>
            <description>
                Whitelisted job tracker for Oozie service.
            </description>
        </property>

* Build [oozie branch](https://github.com/ben-roling/oozie-cloudera/tree/OOZIE-1829) with fix for [OOZIE-1829](https://issues.apache.org/jira/browse/OOZIE-1829)
    * Add oozie-core-4.0.0-cdh5.0.0-OOZIE-1829.jar from previous step to /var/lib/oozie

* Tweak YARN config (in service yarn -> Gateway Base Group -> Resource Management) in Cloudera Manager
    * ApplicationMaster Memory: 128
    * ApplicationMaster Java Maximum Heap Size: 100
    * Map Task Memory: 128
    * Reduce Task Memory: 128
    * Map Task Max Heap: 100
    * Reduce Task Max Heap: 100

* Deploy YARN client configuration via Service -> yarn -> Actions -> Deploy Client Configuration

* Tweak YARN service configurations:
    * Service -> yarn -> Configuration -> ResourceManager Base Group -> Resource Management -> Container Memory Increment: 256
    * Service -> yarn -> Configuration -> NodeManager Base Group -> Resource Management -> Container Virtual CPU Cores: 16

* Restart yarn

* Tweak Oozie configuration: Service -> oozie -> Configuration -> Oozie Server Base Group -> Resource Management -> Java Heap Size of Oozie Server: 100

* Restart oozie

* Consider stopping Cloudera Management Services in Cloudera Manager to free more resources on the VM

* Copy persons.txt from src/main/resources to /user/cloudera directory in HDFS

* Copy datasets.xml from src/main/resources to /user/cloudera/apps directory in HDFS

## Running
* Run "mvn clean package -Pdeploy-example"
    * this will create the Person, PersonOutcomes, and PersonSummary base Datasets and deploy and start the associated
      coordinators that populate "version" partitions of the datasets.  Look at the coordinators and workflows in Hue to
      see them progress.

## Re-deploying
After making any code/configuration changes, kill the running coordinators from Hue and re-run "mvn clean package -Pdeploy-example"
