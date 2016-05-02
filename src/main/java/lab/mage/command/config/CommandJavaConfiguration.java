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
package lab.mage.command.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lab.mage.command.util.CommandConstants;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.mapping.BasicCassandraMappingContext;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableJms
@EnableCassandraRepositories(
    basePackages = {
        "lab.mage.command.repository"
    }
)
@ComponentScan(
    basePackages = {
        "lab.mage.command.gateway",
        "lab.mage.command.internal",
        "lab.mage.command.repository",
        "lab.mage.command.domain"
    }
)
public class CommandJavaConfiguration extends AbstractCassandraConfiguration implements AsyncConfigurer {

  @Autowired
  private Environment environment;

  public CommandJavaConfiguration() {
    super();
  }

  @Bean(name = CommandConstants.LOGGER_NAME)
  public Logger logger() {
    return LoggerFactory.getLogger(CommandConstants.LOGGER_NAME);
  }

  @Bean(name = CommandConstants.COMMAND_SERIALIZER)
  public Gson gson() {
    final GsonBuilder gsonBuilder = new GsonBuilder();
    return gsonBuilder.create();
  }

  @Bean
  public PooledConnectionFactory jmsFactory() {
    final PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
    final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
    activeMQConnectionFactory.setBrokerURL(
        this.environment.getProperty(
            CommandConstants.ACTIVEMQ_BROKER_URL_PROP,
            CommandConstants.ACTIVEMQ_BROKER_URL_DEFAULT));
    pooledConnectionFactory.setConnectionFactory(activeMQConnectionFactory);
    return pooledConnectionFactory;
  }

  @Bean
  public JmsListenerContainerFactory jmsListenerContainerFactory(final PooledConnectionFactory jmsFactory) {
    final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    factory.setConnectionFactory(jmsFactory);
    factory.setConcurrency(
        this.environment.getProperty(
            CommandConstants.ACTIVEMQ_CONCURRENCY_PROP,
            CommandConstants.ACTIVEMQ_CONCURRENCY_DEFAULT
        )
    );
    return factory;
  }

  @Bean
  public JmsTemplate jmsTemplate(final PooledConnectionFactory jmsFactory) {
    final ActiveMQQueue activeMQQueue = new ActiveMQQueue(
        this.environment.getProperty(
            CommandConstants.APPLICATION_NAME_PROP,
            CommandConstants.APPLICATION_NAME_DEFAULT));
    final JmsTemplate jmsTemplate = new JmsTemplate();
    jmsTemplate.setConnectionFactory(jmsFactory);
    jmsTemplate.setDefaultDestination(activeMQQueue);
    return jmsTemplate;
  }

  @Override
  protected String getKeyspaceName() {
    return this.environment.getProperty(
        CommandConstants.CASSANDRA_KEYSPACE_PROP,
        CommandConstants.CASSANDRA_KEYSPACE_DEFAULT);
  }

  @Bean
  public CassandraClusterFactoryBean cluster() {
    final CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
    cluster.setContactPoints(
        this.environment.getProperty(
            CommandConstants.CASSANDRA_CONTACT_POINTS_PROP,
            CommandConstants.CASSANDRA_CONTACT_POINTS_DEFAULT));
    cluster.setPort(Integer.valueOf(
        this.environment.getProperty(
            CommandConstants.CASSANDRA_PORT_PROP,
            CommandConstants.CASSANDRA_PORT_DEFAULT)));
    return cluster;
  }

  @Bean
  public CassandraMappingContext cassandraMapping() {
    return new BasicCassandraMappingContext();
  }

  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(
        Integer.valueOf(this.environment.getProperty(
            CommandConstants.EXECUTOR_CORE_POOL_SIZE_PROP,
            CommandConstants.EXECUTOR_CORE_POOL_SIZE_DEFAULT)));
    executor.setMaxPoolSize(
        Integer.valueOf(this.environment.getProperty(
            CommandConstants.EXECUTOR_MAX_POOL_SIZE_PROP,
            CommandConstants.EXECUTOR_MAX_POOL_SIZE_DEFAULT)));
    executor.setQueueCapacity(
        Integer.valueOf(this.environment.getProperty(
            CommandConstants.EXECUTOR_QUEUE_CAPACITY_PROP,
            CommandConstants.EXECUTOR_QUEUE_CAPACITY_DEFAULT
        )));
    executor.setThreadNamePrefix(CommandConstants.EXECUTOR_PREFIX);
    executor.initialize();
    return executor;
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return new SimpleAsyncUncaughtExceptionHandler();
  }
}
