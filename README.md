# KillrWeather

KillrWeather is a reference application (which is adopted from Datastax's https://github.com/killrweather/killrweather) showing how to easily leverage and integrate [Apache Spark](http://spark.apache.org), [Apache Cassandra](http://cassandra.apache.org), [Apache Kafka](http://kafka.apache.org), [Akka](https://akka.io), and [InfluxDB](https://www.influxdata.com/) for fast, streaming computations. This application focuses on the use case of **[time series data](https://github.com/killrweather/killrweather/wiki/4.-Time-Series-Data-Model)**.

This application also can be viewed as a prototypical IoT (or sensors) data collection application, which stores data in the form of a time series.

## Sample Use Case

_I need fast access to historical data on the fly for predictive modeling with real time data from the stream._

The application does not quite do that, it stops at capturing real-time and cumulative information.

## Reference Application

[KillrWeather Main App](https://github.com/killrweather/killrweather/tree/master/killrweather-app/src/main/scala/com/datastax/killrweather) is the entry point.

## Time Series Data

The use of time series data for business analysis is not new. What is new is the ability to collect and analyze massive volumes of data in sequence at extremely high velocity to get the clearest picture to predict and forecast future market changes, user behavior, environmental conditions, resource consumption, health trends and much, much more.

Apache Cassandra is a NoSQL database platform particularly suited for these types of Big Data challenges. Cassandra’s data model is an excellent fit for handling data in sequence regardless of data type or size. When writing data to Cassandra, data is sorted and written sequentially to disk. When retrieving data by row key and then by range, you get a fast and efficient access pattern due to minimal disk seeks – time series data is an excellent fit for this type of pattern. Apache Cassandra allows businesses to identify meaningful characteristics in their time series data as fast as possible to make clear decisions about expected future outcomes.

There are many flavors of time series data. Some are windows into the stream, others cannot be windows, because the queries are not by time slice but by specific year, month, day, hour, etc. Spark Streaming lets you do both.

In addition to Cassandra, the application also demonstrates integration with InfluxDB and Grafana. This toolset is typically used for DevOps monitoring purposes, but it can also be very useful for real-time visualization of stream processing. Data is written to influxDB as it gets ingested in the application and Grafana is used to provide a real time view of temperature, pressure, and dewpoint values, as the reports come in. Additionally, the application provides results of data rollups - daily and monthly mean, low, and high values.

## Start Here

The original [KillrWeather Wiki](https://github.com/killrweather/killrweather/wiki) is still a great source of information.

## Build the code

We recommend using [IntelliJ IDEA](https://www.jetbrains.com/idea/) for managing and building the code. The project is organized as several modules:

* `data` - some data files for running the applications, including commands for initializing Cassandra and the actual measurement data
* `diagrams` - original diagrams for overall architecture (now obsolete)
* `killrweather-app` - actual Spark application for data processing - ready to run in Spark. It's used for both local mode and cluster execution. If running locally in IntelliJ, make sure that you use the setting `use classpath of module` for this module
* `killrweather-app-local` - An empty directory that's convenient for running locally when using IntelliJ.
* `killrweatherCore` - some support code used throughout an application. Also includes Embedded Kafka allowing you to run everything locally
* `killrweather-grcpclient` - client for exposing KillrWeather app over [GRPC](https://grpc.io/). This client accepts GRPC messages and publishes them to Kafka for application consumption.
* `killrweather-httpclient` - client for exposing KillrWeather app over HTTP. This client accepts HTTP messages (in JSON) and publishes them to Kafka for application consumption.
* `killrWeather-loader` - a collection of loaders for reading the reports data (from the `data` directory) and publishing it (via Kafka, GRPC, and HTTP) to the application.

The build is done via SBT

    cd killrweather
    sbt compile
    # For IntelliJ users, just import a project and use IntelliJ commands


## Run Locally on Linux or Mac

For testing convenience, you can run KillrWeather locally on your workstation, although only Linux and Mac are currently supported. It's still easier to connect to running Cassandra and Kafka clusters, as we'll see.

Do the first three steps _if_ you want to run Cassandra locally. If you already have Cassandra running somewhere, skip to step #4, _Run the Application_.

#### 1. Download the latest Cassandra

[Download the latest Cassandra](http://cassandra.apache.org/download/) and open the compressed file. For Mac users, you could try using [HomeBrew](https://brew.sh/). However, the 3.11.0 build ("bottle") appears to be misconfigured; the wrong version of the `jamm` library is provided. (You'll see this if you try running one of the start commands below.) Unfortunately, in attempting to refactor HomeBrew modules to add built-in support for installing different versions, it appears to be mostly broken now. Good luck if you can install an earlier version that hopefully works. Otherwise, [download from apache.org](  https://github.com/Homebrew/homebrew-versions) and expand it somewhere convenient.

#### 2.Start Cassandra

On Mac, if you successfully installed a working Cassandra distribution using Homebrew, use `brew services start cassandra` if you want to start Cassandra and install it as a service that will always start on reboot. (Use `brew services stop cassandra` to explicitly stop it.) If just want to run Cassandra in a terminal, use `cassandra -f`. This command also works if you just downloaded a build of Cassandra.

#### 3. Run the Setup CQL Scripts

Run the setup CQL scripts to create the schema and populate the weather stations table.
On the command line start a cqlsh shell:

    cd /path/to/killrweather/data
    path/to/apache-cassandra-{version}/bin/cqlsh

Run the CQL commands in the [Cassandra Setup](#cassandra-setup) section below.

#### 4. Run the application

By default `KillrWeather` defaults to looking for Kafka, Cassandra, and Spark using standard DC/OS domain names local to each cluster. It also assumes you want to use InfluxDB. So, to run the application locally, you have to pass several flags to the command:

* `--master local[*]`: You need to specify the Spark master.
* `--without-influxdb`: so it doesn't try to write to InfluxDB, which is the default behavior. (For "symmetry", there is a `--with-influxdb` flag, too, but it's the default.)
* `--kafka-brokers kbs`: If running Kafka locally, then `localhost:9092` is probably right for the `kbs` host and port value. If running elsewhere in a cluster, then use the appropriate `ip:port` (or comma-separated list of them).
* `--cassandra-hosts chs`: If running Kafka locally, then `localhost` is right for `chs`.

At the SBT prompt, use this command sequence to switch to the `killrWeatherApp` subproject and run `com.lightbend.killrweather.app.KillrWeather`. Here we'll use IP addresses in a fictitious cluster that are routable from the workstation running SBT:

```bash
project killrWeatherApp
run --master local[*] --without-influxdb --kafka-brokers 10.8.0.10:1025 --cassandra-hosts 10.8.0.33
```

There is only one "main" class in the app project, `...KillrWeather`, so SBT's `run` tasks finds and runs it.

> If you want to run these commands from `bash`, quote as follows:
>
>```
> sbt "project killrWeatherApp" "run --master local[*] --without-influxdb ..."
>```


You should see lots of log messages printed to the console, then a sequence of Spark Streaming diagnostic headers like the following:

```
-------------------------------------------
Time: 1505870840000 ms
-------------------------------------------

-------------------------------------------
Time: 1505870845000 ms
-------------------------------------------
```

Note there will be no data written between these "banners", because there is no data flowing into the system yet. To get data, we need to run one of the "loaders".

There are several "mains" for project `loader`, so we use `runMain`:

```
project loader
runMain com.lightbend.killrweather.loader.kafka.KafkaDataIngester --kafka-brokers 10.8.0.10:1025
```

(You could pass all the same arguments we passed to `KillrWeather`, but only the `--kafka-brokers` argument is useful. You'll notice invalid, default values printed to the console for the Cassandra Hosts, etc., which you can ignore.)

Actually, you can you just `run` with the arguments and you'll be prompted for which `ingester` to run:

```
run --kafka-brokers 10.8.0.10:1025
[warn] Multiple main classes detected.  Run 'show discoveredMainClasses' to see the list

Multiple main classes detected, select one to run:

 [1] com.lightbend.killrweather.loader.grpc.KafkaDataIngesterGRPC
 [2] com.lightbend.killrweather.loader.http.KafkaDataIngesterRest
 [3] com.lightbend.killrweather.loader.kafka.KafkaDataIngester
 [4] com.lightbend.killrweather.loader.utils.test.TestFilesIterator

Enter number:
```

Enter `3`.

There are also `-h` and `--help` options that show a help message and exit for each of these commands.

TODO: RUNNING IN IntelliJ


### Cassandra Setup

Use these CQL commands whether running Cassandra locally or in a cluster.

Start `CQLSH`. You should see something similar to:

     Connected to Test Cluster at 127.0.0.1:9042.
     [cqlsh {latest.version} | Cassandra {latest.version} | CQL spec {latest.version} | Native protocol {latest.version}]
     Use HELP for help.
     cqlsh>

Run the scripts, then keep the cql shell open querying once the apps are running:

     cqlsh> source 'create-timeseries.cql';
     cqlsh> source 'load-timeseries.cql';

Verify the keyspace was added:

     describe keyspaces;

Switch active keyspace context:

     use isd_weather_data ;
List out all tables installed:

     describe tables;

Weather stations table should be populated

     select * from weather_station limit 5;

To clean up data in Cassandra, type:


     cqlsh> DROP KEYSPACE isd_weather_data;

To close the cql shell:

     cqlsh> quit;


## DC/OS Deployment

To run KillrWeather in an FDP Cluster, you'll need to do the following steps.

1. Install Kafka, InfluxDB, Grafana and Cassandra (from DC/OS universe)

2. Application and clients are set up using FDP-Lab as described [here](https://docs.google.com/document/d/1eMG8I4z6mQ0C4Llg1VHnpV7isnVAtnk-pOkDo8tIubI/edit#heading=h.izl4k6rmh4c0)


#### Application itself

1. Copy `./deploy.conf.template` to `./deploy.conf` and edit the settings as appropriate.
2. Run `sbt 'deploySsh killrWeather'`. This will package an uberJar spark.jar and push it to FDP lab.  The parameter to `deploySsh` must match your server configuration in `./deploy.conf`.
3. Use `KillrWeatherApp/src/main/resource/KillrweatherApp.json` to run it as a marathon job.  With the DC/OS CLI: `dcos marathon app add < killrweather-app/src/main/resource/killrweatherApp.json`.
4. Use http://killrweatherapp.marathon.mesos:4040/jobs/ to see execution
5. Go to http://leader.mesos/mesos/#/frameworks and search for KillrweatherApp to get more info about executors

#### Clients

The application contains 2 clients:

1. HTTP client - Rest API, Rest interface on top of Kafka. It can be deployed on the cluster as a marathon service using `killrweatherHTTPClient.json`.
Deploying it as a Marathon service allows to scale it (behind Marathon-LB) to increase scalability and fail over.
KafkaDataIngesterRest.scala is a local client that can communicate with Rest APIs to send weather reports.
2. Google RPC client, Google RPC interface on top of Kafka. It can be deployed on the cluster as a marathon service using `killrweatherGRPCClient.json`.
Deploying it as a Marathon service allows to scale it (behind Marathon-LB) to increase scalability and fail over.
KafkaDataIngesterGRPC.scala is a local client that can communicate with GRPC APIs to send weather reports.



## Loading data
Application can use 3 different loaders:
1. Direct Kafka loader `com.lightbend.killrweather.loader.kafka.KafkaDataIngester` pushes data directly to the Kafka queue
that an application is listening on.
2. HTTP loader `com.lightbend.killrweather.loader.kafka.KafkaDataIngesterRest` writes data to KillrWeather HTTP client.
3. GRPC loader `com.lightbend.killrweather.loader.kafka.KafkaDataIngesterGRPC` writes data to KillrWeather GRPC client.

## Monitoring and viewing results

Monitoring is done using InfluxDB/Grafana (Grafana definition is in `grafana.json`). For setting up
Grafana/InfluxDB see this [article](https://mesosphere.com/blog/monitoring-dcos-cadvisor-influxdb-grafana/)


Viewing of the execution results is based on Zeppelin - see current support
for [Cassandra in Zeppelin](https://zeppelin.apache.org/docs/0.7.0/interpreter/cassandra.html).

To configure Zeppeling for use of Cassandra, make sure that interpreter is configured correctly. The most important ones are:

```

name	                            value
cassandra.cluster                   cassandra
cassandra.hosts	                    node.cassandra.l4lb.thisdcos.directory
cassandra.native.port	            9042

```

A sample notebook can look something like follows

```
%cassandra
use isd_weather_data;

%cassandra
select day, wsid, high, low, mean,stdev,variance from daily_aggregate_temperature WHERE year=2008 and month=6 allow filtering;

%cassandra
select wsid, day, high, low, mean,stdev,variance from daily_aggregate_pressure  WHERE year=2008 and month=6 allow filtering;

%cassandra
select wsid, month, high, low, mean,stdev,variance from monthly_aggregate_temperature WHERE year=2008  allow filtering;

%cassandra
select wsid, month, high, low, mean,stdev,variance from monthly_aggregate_pressure WHERE year=2008  allow filtering;

```
