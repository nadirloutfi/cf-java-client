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

package org.cloudfoundry.operations;

import org.cloudfoundry.AbstractIntegrationTest;
import org.cloudfoundry.client.v2.CloudFoundryException;
import org.cloudfoundry.operations.domains.CreateDomainRequest;
import org.cloudfoundry.operations.domains.ShareDomainRequest;
import org.cloudfoundry.operations.domains.UnshareDomainRequest;
import org.cloudfoundry.operations.organizations.CreateOrganizationRequest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.subscriber.ScriptedSubscriber;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public final class DomainsTest extends AbstractIntegrationTest {

    @Autowired
    private CloudFoundryOperations cloudFoundryOperations;

    @Autowired
    private String organizationName;

    @Test
    public void create() throws TimeoutException, InterruptedException {
        String domainName = this.nameFactory.getDomainName();

        ScriptedSubscriber<Void> subscriber = ScriptedSubscriber.<Void>create()
            .expectComplete();

        this.cloudFoundryOperations.domains()
            .create(CreateDomainRequest.builder()
                .domain(domainName)
                .organization(this.organizationName)
                .build())
            .subscribe(subscriber);

        subscriber.verify(Duration.ofMinutes(5));
    }

    @Test
    public void createInvalidDomain() throws TimeoutException, InterruptedException {
        ScriptedSubscriber<Void> subscriber = ScriptedSubscriber.<Void>create()
            .consumeErrorWith(t -> assertThat(t).isInstanceOf(CloudFoundryException.class).hasMessageMatching("CF-DomainInvalid\\([0-9]+\\): The domain is invalid.*"));

        this.cloudFoundryOperations.domains()
            .create(CreateDomainRequest.builder()
                .domain("invalid-domain")
                .organization(this.organizationName)
                .build())
            .subscribe(subscriber);

        subscriber.verify(Duration.ofMinutes(5));
    }

    @Test
    public void share() throws TimeoutException, InterruptedException {
        String domainName = this.nameFactory.getDomainName();
        String targetOrganizationName = this.nameFactory.getOrganizationName();

        ScriptedSubscriber<Void> subscriber = ScriptedSubscriber.<Void>create()
            .expectComplete();

        requestCreateOrganization(this.cloudFoundryOperations, targetOrganizationName)
            .then(requestCreateDomain(this.cloudFoundryOperations, domainName, this.organizationName))
            .then(this.cloudFoundryOperations.domains()
                .share(ShareDomainRequest.builder()
                    .domain(domainName)
                    .organization(targetOrganizationName)
                    .build()))
            .subscribe(subscriber);

        subscriber.verify(Duration.ofMinutes(5));
    }

    @Test
    public void unshare() throws TimeoutException, InterruptedException {
        String domainName = this.nameFactory.getDomainName();
        String targetOrganizationName = this.nameFactory.getOrganizationName();

        ScriptedSubscriber<Void> subscriber = ScriptedSubscriber.<Void>create()
            .expectComplete();

        requestCreateOrganization(this.cloudFoundryOperations, targetOrganizationName)
            .then(requestCreateDomain(this.cloudFoundryOperations, domainName, this.organizationName))
            .then(requestShareDomain(this.cloudFoundryOperations, targetOrganizationName, domainName))
            .then(this.cloudFoundryOperations.domains()
                .unshare(UnshareDomainRequest.builder()
                    .domain(domainName)
                    .organization(targetOrganizationName)
                    .build()))
            .subscribe(subscriber);

        subscriber.verify(Duration.ofMinutes(5));
    }

    private static Mono<Void> requestCreateDomain(CloudFoundryOperations cloudFoundryOperations, String domainName, String organizationName) {
        return cloudFoundryOperations.domains()
            .create(CreateDomainRequest.builder()
                .domain(domainName)
                .organization(organizationName)
                .build());
    }

    private static Mono<Void> requestCreateOrganization(CloudFoundryOperations cloudFoundryOperations, String name) {
        return cloudFoundryOperations.organizations()
            .create(CreateOrganizationRequest.builder()
                .organizationName(name)
                .build());
    }

    private static Mono<Void> requestShareDomain(CloudFoundryOperations cloudFoundryOperations, String organizationName, String domainName) {
        return cloudFoundryOperations.domains()
            .share(ShareDomainRequest.builder()
                .domain(domainName)
                .organization(organizationName)
                .build());
    }

}
