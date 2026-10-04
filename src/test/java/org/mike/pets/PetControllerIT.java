package org.mike.pets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class PetControllerIT {

  @Autowired private RestTestClient restTestClient;

  @Test
  void getPetsReturnsAllPets() {
    restTestClient
        .get()
        .uri("/pets")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(7)
        .jsonPath("$[6].petName")
        .isEqualTo("tweety");
  }
}
