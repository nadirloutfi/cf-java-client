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

package org.cloudfoundry.uaa;

import org.cloudfoundry.AbstractIntegrationTest;
import org.cloudfoundry.uaa.clients.Client;
import org.cloudfoundry.uaa.groups.Group;
import org.cloudfoundry.uaa.groups.ListGroupsRequest;
import org.cloudfoundry.uaa.groups.ListGroupsResponse;
import org.cloudfoundry.uaa.identityproviders.AttributeMappings;
import org.cloudfoundry.uaa.identityproviders.CreateIdentityProviderRequest;
import org.cloudfoundry.uaa.identityproviders.LdapConfiguration;
import org.cloudfoundry.uaa.identityproviders.LdapGroupFile;
import org.cloudfoundry.uaa.identityproviders.LdapProfileFile;
import org.cloudfoundry.uaa.identityproviders.ListIdentityProvidersRequest;
import org.cloudfoundry.uaa.identityproviders.ListIdentityProvidersResponse;
import org.cloudfoundry.uaa.identityzones.CreateIdentityZoneRequest;
import org.cloudfoundry.uaa.identityzones.CreateIdentityZoneResponse;
import org.cloudfoundry.uaa.identityzones.IdentityZone;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.subscriber.ScriptedSubscriber;

import java.time.Duration;
import java.util.Collections;

import static org.cloudfoundry.uaa.identityproviders.Type.LDAP;

public final class IdentityProvidersTest extends AbstractIntegrationTest {

    @Autowired
    private UaaClient adminUaaClient;

    @Autowired
    private UaaClient uaaClient;

    @Autowired
    private Mono<String> userId;

    @Test
    public void createLdap() {
        String identityZoneName = this.nameFactory.getIdentityZoneName();
        String name = this.nameFactory.getIdentityProviderName();
        String subdomainName = this.nameFactory.getDomainName();

        ScriptedSubscriber<ListIdentityProvidersResponse> subscriber = ScriptedSubscriber.<ListIdentityProvidersResponse>create()
            .expectNextCount(1)
            .expectComplete();

        this.userId
            .then(userId -> requestCreateIdentityZone(this.uaaClient, identityZoneName, subdomainName)
                .map(CreateIdentityZoneResponse::getId))
//            .then(function((identityZoneId, userId) -> Mono
//                .when(
//                    getGroupId(this.uaaClient, identityZoneName),
//                    Mono.just(userId))))
//            .then(function((groupId, userId) -> this.adminUaaClient.groups()
//                .addMember(AddMemberRequest.builder()
//                    .memberId(userId)
//                    .groupId(groupId)
//                    .identityZoneId(identityZoneName)
//                    .origin("uaa")
//                    .type(MemberType.USER)
//                    .build())))
            .then(ignore -> this.uaaClient.identityProviders()
                .create(CreateIdentityProviderRequest.builder()
                    .active(true)
                    .configuration(LdapConfiguration.builder()
                        .autoAddGroups(true)
                        .baseUrl(String.format("ldap://%s.url", name))
                        .ldapProfileFile(LdapProfileFile.SimpleBind)
                        .ldapGroupFile(LdapGroupFile.NoGroup)
                        .skipSSLVerification(true)
                        .build())
                    .identityZoneId(identityZoneName)
                    .name(name)
                    .originKey("ldap")
                    .type(LDAP)
                    .build()))
            .then(this.uaaClient.identityProviders()
                .list(ListIdentityProvidersRequest.builder()
                    .build()))
            .subscribe(subscriber);

        subscriber.verify(Duration.ofMinutes(5));
    }

    @Ignore("TODO: Await https://www.pivotaltracker.com/story/show/")
    @Test
    public void createOAuth() {
        //
    }

    @Ignore("TODO: Await https://www.pivotaltracker.com/story/show/")
    @Test
    public void createSaml() {
        //
    }

    @Ignore("TODO: Await https://www.pivotaltracker.com/story/show/")
    @Test
    public void delete() {
        //
    }

    @Ignore("TODO: Await https://www.pivotaltracker.com/story/show/")
    @Test
    public void get() {
        //
    }

    @Ignore("TODO: Await https://www.pivotaltracker.com/story/show/")
    @Test
    public void list() {
        //
    }

    @Ignore("TODO: Await https://www.pivotaltracker.com/story/show/")
    @Test
    public void update() {
        //
    }

    private static Mono<String> getGroupId(UaaClient uaaClient, String identityZoneName) {
        return requestListGroups(uaaClient, identityZoneName)
            .flatMapIterable(ListGroupsResponse::getResources)
            .single()
            .map(Group::getId);
    }

    private static Mono<CreateIdentityZoneResponse> requestCreateIdentityZone(UaaClient uaaClient, String identityZoneName, String subdomainName) {
        return uaaClient.identityZones()
            .create(CreateIdentityZoneRequest.builder()
                .identityZoneId(identityZoneName)
                .name(identityZoneName)
                .subdomain(subdomainName)
                .build());
    }

    private static Mono<ListGroupsResponse> requestListGroups(UaaClient uaaClient, String identityZoneName) {
        return uaaClient.groups()
            .list(ListGroupsRequest.builder()
                .filter(String.format("displayName eq \"zones.%s.admin\"", identityZoneName))
                .build());
    }

}
