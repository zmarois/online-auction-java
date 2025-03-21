package com.example.auction.user.impl;

import com.example.auction.user.api.User;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.example.auction.user.impl.UserCommand.*;
import com.example.auction.user.impl.UserEvent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class UserEntity extends PersistentEntity<UserCommand, UserEvent, Optional<User>> {
    private static final Logger log = LoggerFactory.getLogger(UserEntity.class);

    @Override
    public Behavior initialBehavior(Optional<Optional<User>> snapshotState) {
        Optional<User> user = snapshotState.flatMap(Function.identity());

        if (user.isPresent()) {
            return created(user.get());
        } else {
            return notCreated();
        }
    }

    private Behavior created(User user) {
        BehaviorBuilder b = newBehaviorBuilder(Optional.of(user));

        b.setReadOnlyCommandHandler(GetUser.class, (get, ctx) ->
                ctx.reply(state())
        );

        b.setReadOnlyCommandHandler(CreateUser.class, (create, ctx) ->
            ctx.invalidCommand("User already exists.")
        );

        return b.build();
    }

    private Behavior notCreated() {
        BehaviorBuilder b = newBehaviorBuilder(Optional.empty());

        b.setReadOnlyCommandHandler(GetUser.class, (get, ctx) ->
                ctx.reply(state())
        );

        b.setCommandHandler(CreateUser.class, (create, ctx) -> {
            log.warn("Creating user " + entityId());
            User user = new User(UUID.fromString(entityId()), create.getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return ctx.thenPersist(new UserCreated(user), (e) -> ctx.reply(user));
        });

        b.setEventHandlerChangingBehavior(UserCreated.class, user -> created(user.getUser()));

        return b.build();
    }
}
