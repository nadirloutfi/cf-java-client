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

package org.cloudfoundry;

/**
 * A factory for creating names
 */
public interface NameFactory {

    String APPLICATION_PREFIX = "test-application-";

    String BUILDPACK_PREFIX = "test-buildpack-";

    String CLIENT_ID_PREFIX = "test-client-id-";

    String CLIENT_SECRET_PREFIX = "test-client-secret-";

    String DOMAIN_PREFIX = "test.domain.";

    String GROUP_PREFIX = "test-group-";

    String HOST_PREFIX = "test-host-";

    String IDENTITY_PROVIDER_PREFIX = "test-identity-provider-";

    String IDENTITY_ZONE_PREFIX = "test-identity-zone-";

    String ORGANIZATION_PREFIX = "test-organization-";

    String PASSWORD_PREFIX = "test-password-";

    String PATH_PREFIX = "/test-path-";

    String QUOTA_DEFINITION_PREFIX = "test-quota-definition-";

    String SERVICE_INSTANCE_PREFIX = "test-service-instance-";

    String SPACE_PREFIX = "test-space-";

    String USER_PREFIX = "test-user-";

    String VARIABLE_NAME_PREFIX = "test-variable-name-";

    String VARIABLE_VALUE_PREFIX = "test-variable-value-";

    /**
     * Creates an application name
     *
     * @return the application name
     */
    default String getApplicationName() {
        return getName(APPLICATION_PREFIX);
    }

    /**
     * Creates a buildpack name
     *
     * @return the buildpack name
     */
    default String getBuildpackName() {
        return getName(BUILDPACK_PREFIX);
    }

    /**
     * Creates a client id
     *
     * @return the client id
     */
    default String getClientId() {
        return getName(CLIENT_ID_PREFIX);
    }

    /**
     * Creates a client secret
     *
     * @return the client secret
     */
    default String getClientSecret() {
        return getName(CLIENT_SECRET_PREFIX);
    }

    /**
     * Creates a domain name
     *
     * @return the domain name
     */
    default String getDomainName() {
        return getName(DOMAIN_PREFIX);
    }

    /**
     * Creates a group name
     *
     * @return the group name
     */
    default String getGroupName() {
        return getName(GROUP_PREFIX);
    }

    /**
     * Creates a host name
     *
     * @return the host name
     */
    default String getHostName() {
        return getName(HOST_PREFIX);
    }

    /**
     * Creates an identity provider name
     *
     * @return the identity provider name
     */
    default String getIdentityProviderName() {
        return getName(IDENTITY_PROVIDER_PREFIX);
    }

    /**
     * Creates a identity zone name
     *
     * @return the identity zone name
     */
    default String getIdentityZoneName() {
        return getName(IDENTITY_ZONE_PREFIX);
    }

    /**
     * Creates a name
     *
     * @param prefix the prefix to the name
     * @return the name
     */
    String getName(String prefix);

    /**
     * Creates a organization name
     *
     * @return the organization name
     */
    default String getOrganizationName() {
        return getName(ORGANIZATION_PREFIX);
    }

    /**
     * Creates a password
     *
     * @return the password
     */
    default String getPassword() {
        return getName(PASSWORD_PREFIX);
    }

    /**
     * Creates a path
     *
     * @return the path
     */
    default String getPath() {
        return getName(PATH_PREFIX);
    }

    /**
     * Creates a quota definition name
     *
     * @return the quota definition name
     */
    default String getQuotaDefinitionName() {
        return getName(QUOTA_DEFINITION_PREFIX);
    }

    /**
     * Creates a service instance name
     *
     * @return the service instance name
     */
    default String getServiceInstanceName() {
        return getName(SERVICE_INSTANCE_PREFIX);
    }

    /**
     * Creates a space name
     *
     * @return the space name
     */
    default String getSpaceName() {
        return getName(SPACE_PREFIX);
    }

    /**
     * Creates a user name
     *
     * @return the user name
     */
    default String getUserName() {
        return getName(USER_PREFIX);
    }

    /**
     * Creates a variable name
     *
     * @return the variable name
     */
    default String getVariableName() {
        return getName(VARIABLE_NAME_PREFIX);
    }

    /**
     * Creates a variable value
     *
     * @return the variable value
     */
    default String getVariableValue() {
        return getName(VARIABLE_VALUE_PREFIX);
    }

    /**
     * Tests a name to determine if it is an application name
     *
     * @param candidate the candidate name
     * @return {@code true} if the name is an application name, {@code false} otherwise
     */
    default boolean isApplicationName(String candidate) {
        return isName(APPLICATION_PREFIX, candidate);
    }

    /**
     * Tests a name to determine if it is a buildpack name
     *
     * @param candidate the candidate name
     * @return {@code true} if the name is a buildpack name, {@code false} otherwise
     */
    default boolean isBuildpackName(String candidate) {
        return isName(BUILDPACK_PREFIX, candidate);
    }

    /**
     * Tests a name to determine if it is a client id
     *
     * @param candidate the candidate name
     * @return {@code true} if the name is a client id, {@code false} otherwise
     */
    default boolean isClientId(String candidate) {
        return isName(CLIENT_ID_PREFIX, candidate);
    }

    /**
     * Tests a name to determine if it is a client secret
     *
     * @param candidate the candidate name
     * @return {@code true} if the name is a client secret, {@code false} otherwise
     */
    default boolean isClientSecret(String candidate) {
        return isName(CLIENT_SECRET_PREFIX, candidate);
    }

    /**
     * Tests a name to determine if it is a domain name
     *
     * @param candidate the candidate name
     * @return {@code true} if the name is a domain name, {@code false} otherwise
     */
    default boolean isDomainName(String candidate) {
        return isName(DOMAIN_PREFIX, candidate);
    }

    /**
     * Tests a name to determine if it is a group name
     *
     * @param candidate the candidate name
     * @return {@code true} if the name is a group name, {@code false} otherwise
     */
    default boolean isGroupName(String candidate) {
        return isName(GROUP_PREFIX, candidate);
    }

    /**
     * Tests a name to determine if it is a host name
     *
     * @param candidate the candidate name
     * @return {@code true} if the name is a host name, {@code false} otherwise
     */
    default boolean isHostName(String candidate) {
        return isName(HOST_PREFIX, candidate);
    }

    /**
     * Tests a name to determine if it is an identity zone name
     *
     * @param candidate the candidate name
     * @return {@code true} if the name is an identity zone name, {@code false} otherwise
     */
    default boolean isIdentityZoneName(String candidate) {
        return isName(IDENTITY_ZONE_PREFIX, candidate);
    }

    /**
     * Tests a name to determine if it starts with a prefix
     *
     * @param prefix    the prefix to the name
     * @param candidate the candidate name
     * @return {@code true} if the name starts with the prefix, {@code false} otherwise
     */
    boolean isName(String prefix, String candidate);

    /**
     * Tests a name to determine if it is an organization name
     *
     * @param candidate the candidate name
     * @return {@code true} if the name is an organization name, {@code false} otherwise
     */
    default boolean isOrganizationName(String candidate) {
        return isName(ORGANIZATION_PREFIX, candidate);
    }

    /**
     * Tests a name to determine if it is a password
     *
     * @param candidate the candidate name
     * @return {@code true} if the name is a password, {@code false} otherwise
     */
    default boolean isPassword(String candidate) {
        return isName(PASSWORD_PREFIX, candidate);
    }

    /**
     * Tests a name to determine if it is a path
     *
     * @param candidate the candidate name
     * @return {@code true} if the name is a path, {@code false} otherwise
     */
    default boolean isPath(String candidate) {
        return isName(PATH_PREFIX, candidate);
    }

    /**
     * Tests a name to determine if it is a quota definition name
     *
     * @param candidate the candidate name
     * @return {@code true} if the name is a quota definition name, {@code false} otherwise
     */
    default boolean isQuotaDefinitionName(String candidate) {
        return isName(QUOTA_DEFINITION_PREFIX, candidate);
    }

    /**
     * Tests a name to determine if it is a service instance name
     *
     * @param candidate the candidate name
     * @return {@code true} if the name is a service instance name, {@code false} otherwise
     */
    default boolean isServiceInstanceName(String candidate) {
        return isName(SERVICE_INSTANCE_PREFIX, candidate);
    }

    /**
     * Tests a name to determine if it is a space name
     *
     * @param candidate the candidate name
     * @return {@code true} if the name is a space name, {@code false} otherwise
     */
    default boolean isSpaceName(String candidate) {
        return isName(SPACE_PREFIX, candidate);
    }

    /**
     * Tests a name to determine if it is a user name
     *
     * @param candidate the candidate name
     * @return {@code true} if the name is a user name, {@code false} otherwise
     */
    default boolean isUserName(String candidate) {
        return isName(USER_PREFIX, candidate);
    }

    /**
     * Tests a name to determine if it is a variable name
     *
     * @param candidate the candidate name
     * @return {@code true} if the name is a variable name, {@code false} otherwise
     */
    default boolean isVariableName(String candidate) {
        return isName(VARIABLE_NAME_PREFIX, candidate);
    }

    /**
     * Tests a name to determine if it is a variable value
     *
     * @param candidate the candidate name
     * @return {@code true} if the name is a variable value, {@code false} otherwise
     */
    default boolean isVariableValue(String candidate) {
        return isName(VARIABLE_VALUE_PREFIX, candidate);
    }

}
