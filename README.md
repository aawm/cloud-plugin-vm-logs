Apache CloudStack Plugin for virtual machine logs
==============

This plugin provides API plugin for Apache CloudStack to process and view virtual machine logs which are handled by ELK and delivered by Filebeat. 
The version of the plugin matches Apache CloudStack version that it is build for.

The plugin is developed and tested only with Apache CloudStack 4.11.1

* [Installing into CloudStack](#installing-into-cloudstack)
* [Plugin settings](#plugin-settings)
* [ELK Configuration](#elk-configuration)
* [API](#api)

# Installing into CloudStack

Create lib directory in your ACS management server hierarchy. In Ubuntu installation which is based on deb package:

```
mkdir -p /usr/share/cloudstack-management/webapp/WEB-INF/lib
cd /usr/share/cloudstack-management/webapp/WEB-INF/lib
```

Download the plugin jar with dependencies file from OSS Nexus (https://oss.sonatype.org/content/groups/public/com/bwsw/cloud-plugin-vm-logs/) which corresponds to your ACS version (e.g. 4.11.1). 

E.g
```
cloud-plugin-vm-logs-4.11.1.0-20180727.075513-11-jar-with-dependencies.jar
```

 
# Plugin settings

| Name | Description | Default value |
| -------------- | ----------- | -------- |
| vm.log.elasticsearch.list | comma separated list of ElasticSearch HTTP hosts; e.g. http://localhost,http://localhost:9201 | |
| vm.log.elasticsearch.username | Elasticsearch username for authentication; should be empty if authentication is disabled | |
| vm.log.elasticsearch.password | Elasticsearch password for authentication; should be empty if authentication is disabled | |
| vm.log.page.size.default | the default page size for VM log listing | 100 |

*default.page.size* is used as a default value for pagesize parameter in [listVmLogFiles](#listvmlogfiles) command. Its value should be less or equal to Elasticsearch 
*index.max_result_window* otherwise listVmLogFiles requests without pagesize parameter will fail.
  
# ELK Configuration

Following components should be deployed:

## ElasticSearch 6.2.4

```
Version recommended: 6.2.4
```

The official documentation can be found at https://www.elastic.co/guide/en/elasticsearch/reference/6.2/index.html

If customization for _log_ and _file_ tags in responses for [getVmLogs](#getvmlogs) command is required a new template based on _logstash_ template for an index pattern 
*vmlog-** with an adjusted mapping for _message_ and _source_ properties correspondingly should be created.

## Logstash 6.3

```
Version recommended: 6.3.2
```

The official documentation can be found at https://www.elastic.co/guide/en/logstash/6.3/index.html.

The [log pipeline](deployment/vmlogs-logstash.conf) should be used for VM log processing.

In the template above following placeholders should be replaced with real values:

| Name | Description |
| -------------- | ---------- |
| %PORT% | the port to process incoming beats from virtual machines |
| %JDBC_DRIVER_PATH% | the path to JDBC driver library |
| %JDBC_URL% | JDBC connection URL for Apache CloudStack database |
| %JDBC_USER% | the user for Apache CloudStack database |
| %JDBC_PASSWORD% | the user's password for Apache CloudStack database |
| %ELASTICSEARCH_HOSTS% | Elasticsearch hosts to store VM logs |

If SSL or user authentification are required Elasticsearch output plugin should be adjusted (see https://www.elastic.co/guide/en/logstash/6.2/plugins-outputs-elasticsearch.html).

If throttling for VM logs are required Throttle filter plugin should be used (see https://www.elastic.co/guide/en/logstash/6.2/plugins-filters-throttle.html). 

Configuration example:

```
input {
  beats {
    port => 5045
  }
}

# GRANT SELECT on cloud.vm_instance TO logstash@'localhost' IDENTIFIED BY 'xxxxxxxxxx';

filter {
  jdbc_streaming {
    jdbc_driver_library => "/usr/share/java/mysql-connector-java-5.1.38.jar"
    jdbc_driver_class => "com.mysql.jdbc.Driver"
    jdbc_connection_string => "jdbc:mysql://localhost:3306/cloud"
    jdbc_user => "logstash"
    jdbc_password => "xxxxxxxxxx"
    jdbc_validate_connection => true
    statement => "select id from vm_instance WHERE uuid = :uuid"
    parameters => { "uuid" => "vm_uuid"}
    target => "vm_id"
    tag_on_failure => ["vm_uuid_failure"]
    tag_on_default_use => ["vm_uuid_unknown"]
  }
  if "vm_uuid_unknown" in [tags] and "vm_uuid_failure" not in [tags] {
    drop {
    }
  }
}

output {
  elasticsearch {
    hosts => "localhost:9200"
    index => "vmlog-%{[vm_uuid]}-%{+YYYY-MM-dd}"
    ssl => false
  }
}

```

## Filebeat 6.3

```
Version recommended: 6.3.2
```


Filebeat should be used in virtual machines for log processing.

The official documentation can be found at https://www.elastic.co/guide/en/beats/filebeat/6.3/index.html

Filebeat configuration should contain a field *vm_uuid* that is the ID of the virtual machine, *fields_under_root* equal to true and Logstash output.

A configuration example can be find [here](deployment/vmlogs-filebeat.yml).

## Curator 5.5

Curator is used to delete old virtual machine logs.

The official documentation can be found at https://www.elastic.co/guide/en/elasticsearch/client/curator/5.5/index.html

The [action file](deployment/vmlogs-curator.yml) should be used for VM log processing.

In the template following placeholders should be replaced with real values:

| Name | Description |
| -------------- | ---------- |
| %TIMEOUT% | a client timeout in seconds |
| %DAYS% | a number of days to store VM logs |

# API

The plugin provides following API commands to view virtual machine logs:

* [listVmLogFiles](#listvmlogfiles)
* [getVmLogs](#getvmlogs)
* [scrollVmLogs](#scrollvmlogs)

## Commands

### listVmLogFiles

Lists available log files for the virtual machine.

**Request parameters**

| Parameter Name | Description | Required |
| -------------- | ----------- | -------- |
| id | the ID of the virtual machine | true |
| startdate | the start date/time in UTC, yyyy-MM-ddTHH:mm:ss | false |
| enddate | the end date/time in UTC, yyyy-MM-ddTHH:mm:ss | false |
| page | the requested page of the result listing | false |
| pagesize | the size for result listing | false | 

**Response tags**

| Response Name | Description |
| -------------- | ---------- |
| file | the log file name |

### getVmLogs

Retrieves logs for the virtual machine.

**Request parameters**

| Parameter Name | Description | Required |
| -------------- | ----------- | -------- |
| id | the ID of the virtual machine | true |
| startdate | the start date/time in UTC, yyyy-MM-ddTHH:mm:ss | false |
| enddate | the end date/time in UTC, yyyy-MM-ddTHH:mm:ss | false |
| keywords | comma separated list of keywords (AND logical operator is used if multiple keywords are specified) | false |
| logfile | the log file | false |
| sort | comma separated list of response tags optionally prefixed with - for descending order | false |
| page | the requested page of the result listing | false |
| pagesize | the size for result listing | false |
| scroll | timeout in ms for subsequent scroll requests | false | 

If both page/pagesize and scroll parameters are specified scroll is used.

Sorting and filtering for _file_ and _log_ tags in responses is applied to 256 first characters. 
The information how to change the limit can be found at [deployment section](#deployment).  

**Response tags**

See [VM log response tags](#vm-log-response-tags).

### scrollVmLogs

Retrieves next batch of logs for the virtual machine.

**Request parameters**

| Parameter Name | Description | Required |
| -------------- | ----------- | -------- |
| scrollid | the tag to request next batch of logs | true |
| timeout | timeout in ms for subsequent scroll requests | true | 

**Response tags**

See [VM log response tags](#vm-log-response-tags).

## Response tags

### VM log response tags

| Response Name | Description |
| -------------- | ---------- |
| vmlogs | the log listing |
| &nbsp;&nbsp;&nbsp;&nbsp;count | the total number of log entries |
| &nbsp;&nbsp;&nbsp;&nbsp;items(*) | log entries |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;timestamp | the date/time of log event registration |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;file | the log file |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;log | the log data |
| &nbsp;&nbsp;&nbsp;&nbsp;scrollid | the tag to request next batch of logs |
