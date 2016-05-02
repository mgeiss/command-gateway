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
package lab.mage.command.internal;

import com.google.gson.Gson;
import lab.mage.command.annotation.Aggregate;
import lab.mage.command.annotation.CommandHandler;
import lab.mage.command.domain.CommandProcessingException;
import lab.mage.command.repository.CommandSource;
import lab.mage.command.repository.CommandSourceKey;
import lab.mage.command.repository.CommandSourceRepository;
import lab.mage.command.util.CommandConstants;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Component
public class CommandBus implements ApplicationContextAware {

  private final Environment environment;
  private final Logger logger;
  private final Gson gson;
  private final CommandSourceRepository commandSourceRepository;
  private final ConcurrentHashMap<Class, Method> cachedCommandHandlers = new ConcurrentHashMap<>();
  private ApplicationContext applicationContext;

  @Autowired
  public CommandBus(final Environment environment,
                    @Qualifier(CommandConstants.LOGGER_NAME) final Logger logger,
                    @Qualifier(CommandConstants.COMMAND_SERIALIZER) final Gson gson,
                    final CommandSourceRepository commandSourceRepository) {
    super();
    this.environment = environment;
    this.logger = logger;
    this.gson = gson;
    this.commandSourceRepository = commandSourceRepository;
  }

  @Async
  public <C> void dispatch(final C command) {
    this.logger.debug("CommandBus::dispatch-async called.");
    final CommandSourceKey commandSourceKey = this.storeCommand(command);
    try {
      final Method commandHandler = this.findCommandHandler(command);
      final Object aggregate = this.applicationContext.getBean(commandHandler.getDeclaringClass());
      commandHandler.invoke(aggregate, command);
      this.updateCommandSource(commandSourceKey, null);
    } catch (final Throwable th) {
      this.handle(th, commandSourceKey);
    }
  }

  @Async
  public <C, T> Future<T> dispatch(final C command, final Class<T> clazz) throws CommandProcessingException {
    this.logger.debug("CommandBus::dispatch-sync called.");
    final CommandSourceKey commandSourceKey = this.storeCommand(command);
    try {
      final Method commandHandler = this.findCommandHandler(command);
      final Object aggregate = this.applicationContext.getBean(commandHandler.getDeclaringClass());
      final T result = clazz.cast(commandHandler.invoke(aggregate, command));
      this.updateCommandSource(commandSourceKey, null);
      return new AsyncResult<>(result);
    } catch (final Throwable th) {
      throw this.handle(th, commandSourceKey);
    }
  }

  private <C> Method findCommandHandler(final C command) {
    this.logger.debug("CommandBus::findCommandHandler called for {}.", command.getClass().getSimpleName());
    final Class<?> commandClass = command.getClass();
    this.cachedCommandHandlers.computeIfAbsent(commandClass, findHandler -> {
      final Map<String, Object> aggregates = this.applicationContext.getBeansWithAnnotation(Aggregate.class);
      for (Object aggregate : aggregates.values()) {
        final Method[] methods = aggregate.getClass().getDeclaredMethods();
        for (final Method method : methods) {
          if (method.isAnnotationPresent(CommandHandler.class)
              && method.getParameterCount() == 1
              && method.getParameterTypes()[0].isAssignableFrom(commandClass)) {
            this.logger.debug("CommandBus::findCommandHandler added method for {}.",commandClass.getSimpleName());
            return method;
          }
        }
      }
      this.logger.error("Could not find command handler for {}.", commandClass.getSimpleName());
      throw new IllegalArgumentException("No command handler found!");
    });
    return this.cachedCommandHandlers.get(commandClass);
  }

  private <C> CommandSourceKey storeCommand(final C command) {
    this.logger.debug("CommandBus::storeCommand called.");
    final LocalDateTime now = LocalDateTime.now();

    final CommandSourceKey commandSourceKey = new CommandSourceKey();
    commandSourceKey.setSource(
        this.environment.getProperty(
            CommandConstants.APPLICATION_NAME_PROP,
            CommandConstants.APPLICATION_NAME_DEFAULT
        )
    );
    commandSourceKey.setBucket(now.format(DateTimeFormatter.ISO_LOCAL_DATE));
    commandSourceKey.setCreatedOn(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));

    final CommandSource commandSource = new CommandSource();
    commandSource.setCommandSourceKey(commandSourceKey);
    commandSource.setCommand(this.gson.toJson(command));

    this.commandSourceRepository.save(commandSource);

    return commandSourceKey;
  }

  private void updateCommandSource(final CommandSourceKey commandSourceKey, final String failureMessage) {
    this.logger.debug("CommandBus::updateCommandSource called.");
    final CommandSource commandSource = this.commandSourceRepository.findOne(commandSourceKey);
    if (failureMessage != null) {
      commandSource.setFailed(Boolean.TRUE);
      commandSource.setFailureMessage(failureMessage);
    } else {
      commandSource.setProcessed(Boolean.TRUE);
    }

    this.commandSourceRepository.save(commandSource);
  }

  private CommandProcessingException handle(final Throwable th, final CommandSourceKey commandSourceKey) {
    final Throwable cause;
    if (th.getClass().isAssignableFrom(InvocationTargetException.class)) {
      cause = th.getCause();
    } else {
      cause = th;
    }
    this.logger.error(cause.getMessage(), cause);
    this.updateCommandSource(commandSourceKey, cause.getMessage());
    return new CommandProcessingException(cause.getMessage(), cause);
  }

  @Override
  public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
