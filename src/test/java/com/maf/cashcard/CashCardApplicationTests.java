package com.maf.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)//This will start our Spring Boot application and make it available for our test to perform requests to it.
//@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD) // costly in runtime resources. better to place in PUSH/PUT/DELETE tests only
class CashCardApplicationTests {
    TestRestTemplate restTemplate;//We've asked Spring to inject a test helper that’ll allow us to make HTTP requests to the locally running application.

    @Autowired
    public CashCardApplicationTests(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Test
    void contextLoads() {
    }

    @Test
    void shouldReturnACashCardWhenDataIsSaved() {
        // ARRANGE
        //H2 automatically fill DB with test/resources/data.sql

        // ACT
        ResponseEntity<String> result = restTemplate//We use restTemplate to make an HTTP GET request to our application endpoint /cashcards/99000000000
                .withBasicAuth("sarah1", "admin123")//Simple auth config
                .getForEntity("/cashcards/99000000000", String.class);

        // ASSERT
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotBlank();

        DocumentContext resultBody = JsonPath.parse(result.getBody());
        assertThat(resultBody.read("$.id", Long.class)).isEqualTo(99000000000L);
        assertThat(resultBody.read("$.amount", Double.class)).isEqualTo(123.45);
        assertThat(resultBody.read("$.owner",String.class)).isEqualTo("sarah1");
    }

    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
        // ARRANGE
        //H2 automatically fill DB with test/resources/data.sql

        // ACT
        ResponseEntity<String> result = restTemplate
                .withBasicAuth("sarah1", "admin123")
                .getForEntity("/cashcards/1000", String.class);

        // ASSERT
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isBlank();
    }

    @Test
    @DirtiesContext
    void shouldCreateANewCashCard() {
        // ARRANGE
        CashCard newCashCard = new CashCard(null, 250.00, null, true);

        // ASSERT
        ResponseEntity<Void> result = restTemplate
                .withBasicAuth("sarah1", "admin123")
                .postForEntity("/cashcards", newCashCard, Void.class);

        // ASSERT
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        URI locationOfNewCashCard = result.getHeaders().getLocation();
        assertThat(locationOfNewCashCard).isNotNull();

        ResponseEntity<String> resultGet = restTemplate
                .withBasicAuth("sarah1", "admin123")
                .getForEntity(locationOfNewCashCard, String.class);
        DocumentContext resultGetBody = JsonPath.parse(resultGet.getBody());
        String path = locationOfNewCashCard.getPath();
        Long expectedId = Long.valueOf(path.substring(path.lastIndexOf("/") + 1));

        assertThat(resultGet.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long resultId = resultGetBody.read("$.id", Long.class);
        assertThat(resultId).isEqualTo(expectedId);
        Double resultAmount = resultGetBody.read("$.amount", Double.class);
        assertThat(resultAmount).isEqualTo(newCashCard.amount());
    }

    @Test
    void shouldReturnAllCashCardsWhenListIsRequested() {
        ResponseEntity<String> result = restTemplate
                .withBasicAuth("sarah1", "admin123")
                .getForEntity("/cashcards", String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext resultGetBody = JsonPath.parse(result.getBody());
        int cashCardCount = resultGetBody.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        List<?> tempIds = resultGetBody.read("$..id", List.class);// Originall JSONArray fails because low numbers are interpreted as int
        List<Long> resultIds = tempIds.stream().map(x -> ((Number) x).longValue()).toList();
        JSONArray resultAmounts = resultGetBody.read("$..amount");

        assertThat(resultIds).containsExactlyInAnyOrder(99000000000L, 100L, 101L);
        assertThat(resultAmounts).containsExactlyInAnyOrder(123.45, 1.0, 150.00);
    }

    @Test
    void shouldReturnAPageOfCashCards() {
        ResponseEntity<String> result = restTemplate
                .withBasicAuth("sarah1", "admin123")
                .getForEntity("/cashcards?page=0&size=1", String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext resultGetBody = JsonPath.parse(result.getBody());
        int resultLength = resultGetBody.read("$[*]", List.class).size();
        assertThat(resultLength).isEqualTo(1);
    }

    @Test
    void shouldReturnASortedPageOfCashCards() {
        ResponseEntity<String> result = restTemplate
                .withBasicAuth("sarah1", "admin123")
                .getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext resultGetBody = JsonPath.parse(result.getBody());
        int resultLength = ((JSONArray) resultGetBody.read("$[*]")).size();
        double resultFirstAmount = resultGetBody.read("$[0].amount");

        assertThat(resultLength).isEqualTo(1);
        assertThat(resultFirstAmount).isEqualTo(150.00);
    }

    @Test
    void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
        ResponseEntity<String> result = restTemplate
                .withBasicAuth("sarah1", "admin123")
                .getForEntity("/cashcards", String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext resultGetBody = JsonPath.parse(result.getBody());
        int resultLength = ((JSONArray) resultGetBody.read("$[*]")).size();
        assertThat(resultLength).isEqualTo(3);

        JSONArray resultAmounts = resultGetBody.read("$..amount");
        assertThat(resultAmounts).containsExactly(1.00, 123.45, 150.00);
    }

    @Test
    void shouldNotReturnACashCardWhenUsingBadCredentials() {
        ResponseEntity<String> result = restTemplate
                .withBasicAuth("BAD-USER", "admin123")
                .getForEntity("/cashcards/99000000000", String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        result = restTemplate
                .withBasicAuth("sarah1", "BAD-PASSWORD")
                .getForEntity("/cashcards/99000000000", String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUsersWhoAreNotCardOwners() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("hank-owns-no-cards", "admin123")
                .getForEntity("/cashcards/99000000000", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldNotAllowAccessToCashCardsTheyDoNotOwn() {
        ResponseEntity<String> result = restTemplate
                .withBasicAuth("sarah1", "admin123")
                .getForEntity("/cashcards/102", String.class); // kumar2's data
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldUpdateAnExistingCashCard() {
        CashCard cashCardUpdate = new CashCard(null, 19.99, null, true);
        HttpEntity<CashCard> request = new HttpEntity<>(cashCardUpdate);
        ResponseEntity<Void> result = restTemplate
                .withBasicAuth("sarah1", "admin123")
                .exchange("/cashcards/99000000000", HttpMethod.PUT, request, Void.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> resultGet = restTemplate
                .withBasicAuth("sarah1", "admin123")
                .getForEntity("/cashcards/99000000000", String.class);
        assertThat(resultGet.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext resultGetBody = JsonPath.parse(resultGet.getBody());
        Number id = resultGetBody.read("$.id");
        Double amount = resultGetBody.read("$.amount");
        String owner = resultGetBody.read("$.owner");
        assertThat(id).isEqualTo(99000000000L);
        assertThat(amount).isEqualTo(19.99);
        assertThat(owner).isEqualTo("sarah1");
    }

    @Test
    void shouldNotUpdateACashCardThatDoesNotExist() {
        CashCard unknownCard = new CashCard(null, 19.99, null, true);
        HttpEntity<CashCard> request = new HttpEntity<>(unknownCard);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("sarah1", "admin123")
                .exchange("/cashcards/99999", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotUpdateACashCardThatIsOwnedBySomeoneElse() {
        CashCard kumarsCard = new CashCard(null, 333.33, null, true);
        HttpEntity<CashCard> request = new HttpEntity<>(kumarsCard);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("sarah1", "admin123")
                .exchange("/cashcards/102", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotUpdateACashCardInvalidId() {
        CashCard unknownCard = new CashCard(null, 19.99, null, true);
        HttpEntity<CashCard> request = new HttpEntity<>(unknownCard);
        ResponseEntity<Void> result = restTemplate
                .withBasicAuth("sarah1", "admin123")
                .exchange("/cashcards/invalidid", HttpMethod.PUT, request, Void.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DirtiesContext
    void shouldDeleteAnExistingCashCard() {
        ResponseEntity<Void> result = restTemplate
                .withBasicAuth("sarah1", "admin123")
                .exchange("/cashcards/99000000000", HttpMethod.DELETE, null, Void.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> resultGet = restTemplate
                .withBasicAuth("sarah1", "admin123")
                .getForEntity("/cashcards/99000000000", String.class);
        assertThat(resultGet.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotDeleteACashCardThatDoesNotExist() {
        ResponseEntity<Void> result = restTemplate
                .withBasicAuth("sarah1", "admin123")
                .exchange("/cashcards/99999", HttpMethod.DELETE, null, Void.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteAnExistingCashCardThatIsOwnedBySomeoneElse() {
        ResponseEntity<Void> result = restTemplate
                .withBasicAuth("sarah1", "admin123")
                .exchange("/cashcards/102", HttpMethod.DELETE, null, Void.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> resultGet = restTemplate
                .withBasicAuth("kumar2", "admin123")
                .getForEntity("/cashcards/102", String.class);
        assertThat(resultGet.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}

/*
al ejecutar tests, en el log se ven los resultados de los test desordenados. A que se debe esto?
--------------------------------------------
La diferencia en el orden de ejecución y en el registro de los resultados de tus pruebas se debe a cómo JUnit
maneja las pruebas y sus configuraciones. Aquí te explico los detalles:

    CashCardApplicationTests: Usa @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT),
    que inicia un contexto completo de Spring Boot. Este tipo de prueba se ejecuta al principio porque
    generalmente necesita un tiempo de configuración más largo, ya que se carga tod0 el contexto de la aplicación.

    CashCardJson2Test: Usa @ContextConfiguration(classes = CashCardJson2Test.Config.class) para definir un
    contexto de prueba específico. Esto puede involucrar menos configuración comparado con @SpringBootTest,
    y puede ejecutarse en un orden diferente dependiendo de cómo se configuren las tareas en el proyecto.

    CashCardJsonTest: Está anotada con @JsonTest, que está diseñada para pruebas que se centran en la
    serialización y deserialización de JSON. Esta anotación carga solo los componentes necesarios para
    estas pruebas específicas, lo cual es más rápido y puede hacer que estas pruebas se ejecuten al final
    cuando el proceso de carga de contexto es más eficiente.

El orden en el log puede depender del tipo de prueba y de cómo JUnit o el entorno de construcción (Gradle)
organiza y ejecuta las pruebas.

Si el orden de ejecución es importante para ti, puedes intentar ajustar el orden con configuraciones adicionales,
pero generalmente, el comportamiento que estás viendo es normal dado el tipo de prueba y la configuración
de Spring Boot.
 */