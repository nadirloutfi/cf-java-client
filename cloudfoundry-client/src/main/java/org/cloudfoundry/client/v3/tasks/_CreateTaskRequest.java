/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry.client.v3.tasks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cloudfoundry.Nullable;
import org.immutables.value.Value;

import java.util.Map;

/**
 * The request payload for the Create Task endpoint
 */
@Value.Immutable
abstract class _CreateTaskRequest {

    /**
     * The application id
     */
    @JsonIgnore
    abstract String getApplicationId();

    /**
     * The command
     */
    @JsonProperty("command")
    abstract String getCommand();

    /**
     * The droplet id
     */
    @JsonProperty("droplet_guid")
    @Nullable
    abstract String getDropletId();

    /**
     * The environment variables
     */
    @JsonProperty("environment_variables")
    @Nullable
    abstract Map<String, String> getEnvironmentVariables();

    /**
     * The memoryInMb
     */
    @JsonProperty("memory_in_mb")
    @Nullable
    abstract Integer getMemoryInMb();

    /**
     * The name
     */
    @JsonProperty("name")
    abstract String getName();

}
