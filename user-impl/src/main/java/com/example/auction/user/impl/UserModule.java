package com.example.auction.user.impl;

import com.example.auction.user.api.UserService;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.lightbend.lagom.javadsl.api.ConfigurationServiceLocator;
import com.lightbend.lagom.javadsl.api.ServiceLocator;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.ApplicationLifecycle;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class UserModule extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindServices(serviceBinding(UserService.class, UserServiceImpl.class));

        // Need some implementation. Dummy CRUD service doesn't need much for this. Should be wrapped in an if(isProd) block if we still wanted dev mode to work
        bind(ServiceLocator.class).to(ConfigurationServiceLocator.class);

        // Enable graceful stop
        bind(UserServiceLifecycle.class).asEagerSingleton();
    }

    private static class UserServiceLifecycle {
        private Logger log = LoggerFactory.getLogger(UserServiceLifecycle.class);

        @Inject
        public UserServiceLifecycle(ApplicationLifecycle applicationLifecycle, PersistentEntityRegistry persistentEntityRegistry){
            applicationLifecycle.addStopHook(() -> {
                log.warn("Initiating graceful stop");
                return persistentEntityRegistry.gracefulShutdown(FiniteDuration.create(5, TimeUnit.SECONDS))
                    .thenRun(() -> log.warn("Completed graceful stop"));
            });
        }
    }
}
