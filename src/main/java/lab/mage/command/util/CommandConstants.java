/*
 * Copyright 2016 Markus Geiss.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lab.mage.command.util;

public interface CommandConstants {

  String LOGGER_NAME = "command-logger";
  String COMMAND_SERIALIZER = "command-serializer";

  String APPLICATION_NAME_PROP = "spring.application.name";
  String APPLICATION_NAME_DEFAULT = "mage-command-v1";

  String ACTIVEMQ_BROKER_URL_PROP = "activemq.brokerUrl";
  String ACTIVEMQ_BROKER_URL_DEFAULT = "vm://localhost?broker.persistent=false";
  String ACTIVEMQ_CONCURRENCY_PROP = "activemq.concurrency";
  String ACTIVEMQ_CONCURRENCY_DEFAULT = "3-10";

  String CASSANDRA_CONTACT_POINTS_PROP = "cassandra.contactPoints";
  String CASSANDRA_CONTACT_POINTS_DEFAULT = "127.0.0.1";
  String CASSANDRA_PORT_PROP = "cassandra.port";
  String CASSANDRA_PORT_DEFAULT = "9142";
  String CASSANDRA_KEYSPACE_PROP = "cassandra.keyspace";
  String CASSANDRA_KEYSPACE_DEFAULT = "commands";

  String EXECUTOR_PREFIX = "command-executor-";
  String EXECUTOR_CORE_POOL_SIZE_PROP = "executor.corePoolSize";
  String EXECUTOR_CORE_POOL_SIZE_DEFAULT = "32";
  String EXECUTOR_MAX_POOL_SIZE_PROP = "executor.maxPoolSize";
  String EXECUTOR_MAX_POOL_SIZE_DEFAULT = "16384";
  String EXECUTOR_QUEUE_CAPACITY_PROP = "executor.queueCapacity";
  String EXECUTOR_QUEUE_CAPACITY_DEFAULT = "8";

  String COMMAND_SOURCE_TABLE_NAME = "command_source";
}
