package com.maf.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)//This will start our Spring Boot application and make it available for our test to perform requests to it.
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
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
        ResponseEntity<String> result = restTemplate.getForEntity("/cashcards/99000000000", String.class);//We use restTemplate to make an HTTP GET request to our application endpoint /cashcards/99000000000

        // ASSERT
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotBlank();

        DocumentContext resultBody = JsonPath.parse(result.getBody());
        assertThat(resultBody.read("$.id", Long.class)).isEqualTo(99000000000L);
        assertThat(resultBody.read("$.amount", Double.class)).isEqualTo(123.45);
    }

    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
        // ARRANGE
        //H2 automatically fill DB with test/resources/data.sql

        // ACT
        ResponseEntity<String> result = restTemplate.getForEntity("/cashcards/1000", String.class);

        // ASSERT
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isBlank();
    }

    @Test
    //@DirtiesContext
    void shouldCreateANewCashCard() {
        // ARRANGE
        CashCard newCashCard = new CashCard(null, 250.00);

        // ASSERT
        ResponseEntity<Void> result = restTemplate.postForEntity("/cashcards", newCashCard, Void.class);

        // ASSERT
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        URI locationOfNewCashCard = result.getHeaders().getLocation();
        assertThat(locationOfNewCashCard).isNotNull();

        ResponseEntity<String> resultGet = restTemplate.getForEntity(locationOfNewCashCard, String.class);
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
        ResponseEntity<String> result = restTemplate.getForEntity("/cashcards", String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext resultGetBody = JsonPath.parse(result.getBody());
        int cashCardCount = resultGetBody.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        List<?> ids = resultGetBody.read("$..id", List.class);// Originall JSONArray fails because low numbers are interpreted as int
        List<Long> convertedIds = ids.stream().map(x -> ((Number) x).longValue()).toList();
        assertThat(convertedIds).containsExactlyInAnyOrder(99000000000L, 100L, 101L);

        JSONArray amounts = resultGetBody.read("$..amount");
        assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.0, 150.00);
    }

    @Test
    void shouldReturnAPageOfCashCards() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards?page=0&size=1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page).hasSize(1);
    }

    @Test
    void shouldReturnASortedPageOfCashCards() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray read = documentContext.read("$[*]");
        assertThat(read).hasSize(1);

        double amount = documentContext.read("$[0].amount");
        assertThat(amount).isEqualTo(150.00);
    }
}

/*

al ejecutar los tests de mi aplicación Java Spring, se me genera este log:

> Task :test
CashCardApplicationTests > shouldReturnACashCardWhenDataIsSaved() PASSED
CashCardApplicationTests > contextLoads() PASSED
CashCardJson2Test > testSerialize() PASSED
CashCardJson2Test > testDeserialize() PASSED
<SPRING LOGO AND INFO TAGS>
CashCardJsonTest > cashCardSerializationTest() PASSED
CashCardJsonTest > cashCardDeserializationTest() PASSED
BUILD SUCCESSFUL in 7s
5 actionable tasks: 2 executed, 3 up-to-date
12:32:56: Execution finished 'test'.


se puede ver que los resultados de los tests contenidos en la clase CashCardApplicationTests figuran al
principio, pero los resultados de los tests contenidos en la clase CashCardJson2Test se muestran casi
al final del log. A que se debe esto? te paso la cabecera de las 3 clases:

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashCardApplicationTests

@ContextConfiguration(classes = CashCardJson2Test.Config.class)
class CashCardJson2Test

@JsonTest
class CashCardJsonTest

--------------------------------------------

La diferencia en el orden de ejecución y en el registro de los resultados de tus pruebas se debe a cómo JUnit
maneja las pruebas y sus configuraciones. Aquí te explico los detalles:

    CashCardApplicationTests: Está anotada con @SpringBootTest, que inicia un contexto completo de Spring Boot.
    Este tipo de prueba se ejecuta al principio porque generalmente necesita un tiempo de configuración más largo,
    ya que se carga tod0 el contexto de la aplicación.

    CashCardJson2Test: Usa @ContextConfiguration para definir un contexto de prueba específico. Esto puede
    involucrar menos configuración comparado con @SpringBootTest, y puede ejecutarse en un orden diferente
    dependiendo de cómo se configuren las tareas en el proyecto.

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