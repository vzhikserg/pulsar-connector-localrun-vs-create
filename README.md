# Description 

This project shows inconsistency between `localrun` and `create` commands when running Pulsar source connector publishing AVRO messages.
When the connector was tested using the `localrun` command, the created topic had a correct schema which was not the case when the connector was started with the `create` command.    

You can reproduce the issue following the 

## Generate nar file

The connector is packaged as nar file using the following command:

```
mvn clean package
```

## Start container

Run a standalone instance of Pulsar:

```
docker run -d -it -p 6650:6650 -p 8080:8080 --name pulsar apachepulsar/pulsar:2.4.2 bin/pulsar standalone
```

## Copy nar

The created nar file is copied to the running container:

```
docker cp target/pulsar-connector-localrun-vs-create-1.0.0-SNAPSHOT.nar pulsar:/pulsar/
```

## Connect to the running container

```
docker exec -it pulsar /bin/bash
```

## Start the connector with localrun

The connector is started using the localrun command:

```
./bin/pulsar-admin sources localrun --archive pulsar-connector-localrun-vs-create-1.0.0-SNAPSHOT.nar --tenant public --namespace default --destination-topic-name example1 --name pulsar-source-connector1 -st avro
```

## Get the schema for the topic

Display schema for the topic created by the container:

``` 
./bin/pulsar-admin schemas get persistent://public/default/example1
```

Output after running the previous command:

```json
{
  "version": 0,
  "schemaInfo": {
    "name": "example1",
    "schema": {
      "type": "record",
      "name": "ExampleMessage",
      "namespace": "com.zhevzhyk.model",
      "doc": "A schema for publishing messages.",
      "fields": [
        {
          "name": "uuid",
          "type": {
            "type": "string",
            "avro.java.string": "String"
          },
          "doc": "The unique identifier of the event. The string has to conform with RFC-4122. This field is mandatory and can be used for reference.",
          "logicalType": "uuid"
        },
        {
          "name": "format",
          "type": {
            "type": "enum",
            "name": "Format",
            "symbols": [
              "UNKNOWN",
              "UNSPECIFIED"
            ],
            "default": "UNKNOWN"
          },
          "doc": "The format. This field is mandatory."
        },
        {
          "name": "sourceApplication",
          "type": [
            "null",
            {
              "type": "string",
              "avro.java.string": "String"
            }
          ],
          "doc": "The identifier of the entity generating the record. This field is optional."
        }
      ]
    },
    "type": "AVRO",
    "properties": {
      "__alwaysAllowNull": "true"
    }
  }
}
```

The schema is correct and corresponds to the schema provided in the AVRO definition. 

## Start the connector with create

The connector is started using the create command:

```
./bin/pulsar-admin sources create --archive pulsar-connector-localrun-vs-create-1.0.0-SNAPSHOT.nar --tenant public --namespace default --destination-topic-name example2 --name pulsar-source-connector2 -st avro
```

## Get the schema for the topic 

Display schema for the topic created by the container:

```
./bin/pulsar-admin schemas get persistent://public/default/example2
```

Output after running the previous command:

```json
{
  "version": 0,
  "schemaInfo": {
    "name": "example2",
    "schema": {
      "type": "record",
      "name": "ExampleMessage",
      "namespace": "com.zhevzhyk.model",
      "fields": [
        {
          "name": "uuid",
          "type": [
            "null",
            "string"
          ]
        },
        {
          "name": "format",
          "type": [
            "null",
            {
              "type": "enum",
              "name": "Format",
              "symbols": [
                "UNKNOWN",
                "UNSPECIFIED"
              ]
            }
          ]
        },
        {
          "name": "sourceApplication",
          "type": [
            "null",
            "string"
          ]
        }
      ]
    },
    "type": "AVRO",
    "properties": {
      "__alwaysAllowNull": "true"
    }
  }
}
```

## Problem

It seems this schema was generated using reflection and it is not the same schema which was provided in the AVRO definition:
* All fields are marked as optional
* Default value was removed from the enum
* The doc fields are not there
