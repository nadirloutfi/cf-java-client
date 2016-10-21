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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.organizations.CreateOrganizationRequest;
import org.cloudfoundry.client.v2.spaces.CreateSpaceRequest;
import org.cloudfoundry.client.v2.stacks.ListStacksRequest;
import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.ProxyConfiguration;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.ClientCredentialsGrantTokenProvider;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.cloudfoundry.uaa.UaaClient;
import org.cloudfoundry.uaa.clients.CreateClientRequest;
import org.cloudfoundry.uaa.groups.AddMemberRequest;
import org.cloudfoundry.uaa.groups.Group;
import org.cloudfoundry.uaa.groups.ListGroupsRequest;
import org.cloudfoundry.uaa.groups.ListGroupsResponse;
import org.cloudfoundry.uaa.groups.MemberType;
import org.cloudfoundry.uaa.users.CreateUserRequest;
import org.cloudfoundry.uaa.users.CreateUserResponse;
import org.cloudfoundry.uaa.users.Email;
import org.cloudfoundry.uaa.users.Name;
import org.cloudfoundry.util.PaginationUtils;
import org.cloudfoundry.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.fail;
import static org.cloudfoundry.uaa.tokens.GrantType.AUTHORIZATION_CODE;
import static org.cloudfoundry.uaa.tokens.GrantType.CLIENT_CREDENTIALS;
import static org.cloudfoundry.uaa.tokens.GrantType.PASSWORD;
import static org.cloudfoundry.uaa.tokens.GrantType.REFRESH_TOKEN;

@Configuration
@EnableAutoConfiguration
public class IntegrationTestConfiguration {

    private static final List<String> GROUPS = Arrays.asList(
        "clients.admin",
        "cloud_controller.admin",
        "idps.write",
        "scim.create",
        "scim.invite",
        "scim.read",
        "scim.userids",
        "scim.write",
        "scim.zones",
        "uaa.admin",
        "zones.read",
        "zones.write");

    private static final List<String> SCOPES = Arrays.asList(
        "clients.admin",
        "cloud_controller.admin",
        "cloud_controller.read",
        "cloud_controller.write",
        "idps.write",
        "password.write",
        "scim.create",
        "scim.invite",
        "scim.read",
        "scim.userids",
        "scim.write",
        "scim.zones",
        "uaa.admin",
        "uaa.user",
        "zones.read",
        "zones.write");

    private final Logger logger = LoggerFactory.getLogger("cloudfoundry-client.test");

    @Bean
    @Qualifier("admin")
    ReactorCloudFoundryClient adminCloudFoundryClient(ConnectionContext connectionContext,
                                                      @Value("${test.admin.password}") String password,
                                                      @Value("${test.admin.username}") String username) {
        return ReactorCloudFoundryClient.builder()
            .connectionContext(connectionContext)
            .tokenProvider(PasswordGrantTokenProvider.builder()
                .password(password)
                .username(username)
                .build())
            .build();
    }

    @Bean
    @Qualifier("admin")
    ReactorUaaClient adminUaaClient(ConnectionContext connectionContext, @Value("${test.admin.clientId}") String clientId, @Value("${test.admin.clientSecret}") String clientSecret) {
        return ReactorUaaClient.builder()
            .connectionContext(connectionContext)
            .tokenProvider(ClientCredentialsGrantTokenProvider.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build())
            .build();
    }

    @Bean(initMethod = "block")
    @DependsOn("cloudFoundryCleaner")
    Mono<Tuple2<String, String>> client(@Qualifier("admin") UaaClient uaaClient, String clientId, String clientSecret) {
        return uaaClient.clients()
            .create(CreateClientRequest.builder()
                .authorizedGrantType(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, PASSWORD, REFRESH_TOKEN)
                .autoApprove(String.valueOf(true))
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUriPattern("/login")
                .scopes(SCOPES)
                .build())
            .then(Mono.just(Tuples.of(clientId, clientSecret)))
            .doOnSubscribe(s -> this.logger.debug(">> CLIENT ({}/{}) <<", clientId, clientSecret))
            .doOnError(Throwable::printStackTrace)
            .doOnSuccess(r -> this.logger.debug("<< CLIENT ({})>>", clientId));
    }

    @Bean
    String clientId(NameFactory nameFactory) {
        return nameFactory.getClientId();
    }

    @Bean
    String clientSecret(NameFactory nameFactory) {
        return nameFactory.getClientSecret();
    }

    @Bean(initMethod = "clean", destroyMethod = "clean")
    CloudFoundryCleaner cloudFoundryCleaner(@Qualifier("admin") CloudFoundryClient cloudFoundryClient, NameFactory nameFactory, @Qualifier("admin") UaaClient uaaClient) {
        return new CloudFoundryCleaner(cloudFoundryClient, nameFactory, uaaClient);
    }

    @Bean(initMethod = "checkCompatibility")
    ReactorCloudFoundryClient cloudFoundryClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorCloudFoundryClient.builder()
            .connectionContext(connectionContext)
            .tokenProvider(tokenProvider)
            .build();
    }

    @Bean
    DefaultCloudFoundryOperations cloudFoundryOperations(CloudFoundryClient cloudFoundryClient, DopplerClient dopplerClient, UaaClient uaaClient, String organizationName, String spaceName) {
        return DefaultCloudFoundryOperations.builder()
            .cloudFoundryClient(cloudFoundryClient)
            .dopplerClient(dopplerClient)
            .uaaClient(uaaClient)
            .organization(organizationName)
            .space(spaceName)
            .build();
    }

    @Bean
    DefaultConnectionContext connectionContext(@Value("${test.apiHost}") String apiHost,
                                               @Value("${test.proxy.host:}") String proxyHost,
                                               @Value("${test.proxy.password:}") String proxyPassword,
                                               @Value("${test.proxy.port:8080}") Integer proxyPort,
                                               @Value("${test.proxy.username:}") String proxyUsername,
                                               @Value("${test.skipSslValidation:false}") Boolean skipSslValidation) {

        DefaultConnectionContext.Builder connectionContext = DefaultConnectionContext.builder()
            .apiHost(apiHost)
            .skipSslValidation(skipSslValidation)
            .sslHandshakeTimeout(Duration.ofSeconds(30));

        if (StringUtils.hasText(proxyHost)) {
            ProxyConfiguration.Builder proxyConfiguration = ProxyConfiguration.builder()
                .host(proxyHost)
                .port(proxyPort);

            if (StringUtils.hasText(proxyUsername)) {
                proxyConfiguration
                    .password(proxyPassword)
                    .username(proxyUsername);
            }

            connectionContext.proxyConfiguration(proxyConfiguration.build());
        }

        return connectionContext.build();
    }

    @Bean
    DopplerClient dopplerClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorDopplerClient.builder()
            .connectionContext(connectionContext)
            .tokenProvider(tokenProvider)
            .build();
    }

    @Bean
    RandomNameFactory nameFactory(Random random) {
        return new RandomNameFactory(random);
    }

    @Bean(initMethod = "block")
    @DependsOn("cloudFoundryCleaner")
    Mono<String> organizationId(CloudFoundryClient cloudFoundryClient, String organizationName) throws InterruptedException {
        return cloudFoundryClient.organizations()
            .create(CreateOrganizationRequest.builder()
                .name(organizationName)
                .build())
            .map(ResourceUtils::getId)
            .doOnSubscribe(s -> this.logger.debug(">> ORGANIZATION ({}) <<", organizationName))
            .doOnError(Throwable::printStackTrace)
            .doOnSuccess(id -> this.logger.debug("<< ORGANIZATION ({}) >>", id))
            .cache();
    }

    @Bean
    String organizationName(NameFactory nameFactory) {
        return nameFactory.getOrganizationName();
    }

    @Bean
    String password(NameFactory nameFactory) {
        return nameFactory.getPassword();
    }

    @Bean
    SecureRandom random() {
        return new SecureRandom();
    }

    @Bean(initMethod = "block")
    @DependsOn("cloudFoundryCleaner")
    Mono<String> spaceId(CloudFoundryClient cloudFoundryClient, Mono<String> organizationId, String spaceName) throws InterruptedException {
        return organizationId
            .then(orgId -> cloudFoundryClient.spaces()
                .create(CreateSpaceRequest.builder()
                    .name(spaceName)
                    .organizationId(orgId)
                    .build()))
            .map(ResourceUtils::getId)
            .doOnSubscribe(s -> this.logger.debug(">> SPACE ({}) <<", spaceName))
            .doOnError(Throwable::printStackTrace)
            .doOnSuccess(id -> this.logger.debug("<< SPACE ({}) >>", id))
            .cache();
    }

    @Bean
    String spaceName(NameFactory nameFactory) {
        return nameFactory.getSpaceName();
    }

    @Bean(initMethod = "block")
    @DependsOn("cloudFoundryCleaner")
    Mono<String> stackId(CloudFoundryClient cloudFoundryClient, String stackName) throws InterruptedException {
        return PaginationUtils
            .requestClientV2Resources(page -> cloudFoundryClient.stacks()
                .list(ListStacksRequest.builder()
                    .name(stackName)
                    .page(page)
                    .build()))
            .single()
            .map(ResourceUtils::getId)
            .doOnSubscribe(s -> this.logger.debug(">> STACK ({}) <<", stackName))
            .doOnError(Throwable::printStackTrace)
            .doOnSuccess(id -> this.logger.debug("<< STACK ({})>>", id))
            .cache();
    }

    @Bean
    String stackName() {
        return "cflinuxfs2";
    }

    @Bean
    @DependsOn({"client", "userId"})
    PasswordGrantTokenProvider tokenProvider(String clientId, String clientSecret, String password, String username) {
        return PasswordGrantTokenProvider.builder()
            .clientId(clientId)
            .clientSecret(clientSecret)
            .password(password)
            .username(username)
            .build();
    }

    @Bean
    ReactorUaaClient uaaClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorUaaClient.builder()
            .connectionContext(connectionContext)
            .tokenProvider(tokenProvider)
            .build();
    }

    @Bean(initMethod = "block")
    @DependsOn("cloudFoundryCleaner")
    Mono<String> userId(@Qualifier("admin") UaaClient uaaClient, String password, String username) {
        return uaaClient.users()
            .create(CreateUserRequest.builder()
                .email(Email.builder()
                    .primary(true)
                    .value(String.format("%s@%s.com", username, username))
                    .build())
                .name(Name.builder()
                    .givenName("Test")
                    .familyName("User")
                    .build())
                .password(password)
                .userName(username)
                .build())
            .map(CreateUserResponse::getId)
            .then(userId -> Flux.fromIterable(GROUPS)
                .flatMap(group -> uaaClient.groups()
                    .list(ListGroupsRequest.builder()
                        .filter(String.format("displayName eq \"%s\"", group))
                        .build())
                    .flatMapIterable(ListGroupsResponse::getResources)
                    .single()
                    .map(Group::getId)
                    .then(groupId -> uaaClient.groups()
                        .addMember(AddMemberRequest.builder()
                            .groupId(groupId)
                            .memberId(userId)
                            .origin("uaa")
                            .type(MemberType.USER)
                            .build())))
                .then()
                .then(Mono.just(userId)))
            .doOnSubscribe(s -> this.logger.debug(">> USER ({}/{}) <<", username, password))
            .doOnError(Throwable::printStackTrace)
            .doOnSuccess(id -> this.logger.debug("<< USER ({})>>", id))
            .cache();
    }

    @Bean
    String username(NameFactory nameFactory) {
        return nameFactory.getUserName();
    }

    private static final class FailingDeserializationProblemHandler extends DeserializationProblemHandler {

        @Override
        public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser jp, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) {
            fail(String.format("Found unexpected property %s in payload for %s", propertyName, beanOrClass.getClass().getName()));
            return false;
        }

    }

}
