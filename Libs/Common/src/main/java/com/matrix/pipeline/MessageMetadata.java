package com.matrix.pipeline;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * The metadata of an application message sent on the pipeline. A message is uniquely identified by its URI({@link #uri}). The uri would typically refer a blob but need not be limited to that.
 * Any number of properties can be associated with a message. {@link MessageMetadata} should be transformed to JSON before publishing - {@link #toJson()} is available for convenience.
 * </p>
 *  Reasons to use {@link MessageMetadata} over sending raw messages
 * <ul>
 *     <li>The message can be stored as soon as it is received to avoid any loss.</li>
 *     <li>For large messages there is lesser burden on the messaging infrastructure.</li>
 *     <li>A standardization of message exchange within the application. The is the most compelling reason.</li>
 * </ul>
 *
 * There cannot be two properties with the same name. Property values must be JSON serializable.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class MessageMetadata {

    /**
     * Location of the message - blob, doc etc
     */
    private final String uri;

    /**
     * The content type - text/fix, text/json etc
     */
    private final String contentType;

    /**
     * The type of message. This is optional. For example 'ExecutionReport' is a type of FIX message.
     */
    private final String messageType;

    /**
     * Additional properties associated with the message. Optional.
     */
    private final Set<Property> properties = new HashSet<>();

    public static MessageMetadata fromJSON(String json) {
        Preconditions.checkNotNull(json, "json is null");
        try {
            return new ObjectMapper().readValue(json, MessageMetadata.class);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Error marshalling from JSON [%s]", json), e);
        }
    }

    @JsonCreator
    public MessageMetadata(@JsonProperty("uri") String uri, @JsonProperty("contentType") String contentType, @JsonProperty("messageType") String messageType) {
        Preconditions.checkNotNull(uri, "uri is null");
        Preconditions.checkNotNull(contentType, "contentType is null");
        this.uri = uri;
        this.contentType = contentType;
        this.messageType = messageType;
    }

    public String getUri() {
        return uri;
    }

    public String getContentType() {
        return contentType;
    }

    public String getMessageType() {
        return messageType;
    }

    public Set<Property> getProperties() {
        return Collections.unmodifiableSet(properties);
    }

    public void addProperty(Property property) {
        properties.add(property);
    }

    public void addProperties(Property... props) {
        for(Property property : props) {
            properties.add(property);
        }
    }

    public void addProperties(Set<Property> props) {
        for(Property property : props) {
            properties.add(property);
        }
    }

    public void addProperty(String name, Object value) {
        properties.add(new Property(name, value));
    }

    public <T> T getProperty(String name) {
        T ret = getProperty(name, null);
        Preconditions.checkState(ret != null, "No property [%s] found", name);
        return ret;
    }

    public <T> T getProperty(String name, T defaultVal) {
        for(Property p : properties) {
            if(p.getName().equals(name)) {
                return (T) p.getValue();
            }
        }
        return defaultVal;
    }

    public static class Property {
        private final String name;
        private final Object value;

        @JsonCreator
        public Property(@JsonProperty("name") String name, @JsonProperty("value") Object value) {
            Preconditions.checkNotNull(name, "name is null");
            Preconditions.checkNotNull(value, "value is null");
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Property property = (Property) o;

            return name.equals(property.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    public String toJson() {
        return toJson(new ObjectMapper());
    }

    public String toJson(ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error marshalling to JSON", e);
        }
    }


}
