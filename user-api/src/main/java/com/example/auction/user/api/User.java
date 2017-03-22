package com.example.auction.user.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

@Value
// Since this is persisted, it needs to implement Jsonable
public final class User implements Jsonable {

    private final UUID id;
    private final String name;

    @JsonCreator
    private User(@JsonProperty("id") Optional<UUID> id, @JsonProperty("name") String name) {
        this.id = id.orElse(null);
        this.name = name;
    }

    public User(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Used when creating a new user.
     */
    public User(String name) {
        this.id = null;
        this.name = name;
    }
}
