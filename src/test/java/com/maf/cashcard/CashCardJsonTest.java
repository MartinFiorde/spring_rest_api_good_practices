package com.maf.cashcard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CashCardJsonTest {

    @Autowired
    private JacksonTester<CashCard> json;

    @Test
    void cashCardSerializationTest() throws IOException {
        // ARRANGE - Setting up the data that required for the test case
        ClassPathResource resource = new ClassPathResource("static/expected.json");
        String expectedJson = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(expectedJson);
        long expectedId = jsonNode.get("id").asLong();
        double expectedAmount = jsonNode.get("amount").asDouble();

        // ACT - Calling a Method/Unit that is being tested
        CashCard cashCard = new CashCard(99L, 123.45);
        JsonContent<CashCard> resultSerializedJson = json.write(cashCard);

        // ASSERT - Verify that the expected result is correct or not
        assertThat(resultSerializedJson)
                .isStrictlyEqualToJson(expectedJson)
                .hasJsonPathNumberValue("@.id")
                .hasJsonPathNumberValue("@.amount");
        assertThat(resultSerializedJson.getJson())
                .contains("\"id\":" + expectedId)
                .contains("\"amount\":" + expectedAmount);
//        ORIGINAL ASSERTS
//        assertThat(json.write(cashCard)).isStrictlyEqualToJson("expected.json")
//        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id")
//        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id").isEqualTo(99)
//        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount")
//        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount").isEqualTo(123.45)
    }

    @Test
    void cashCardDeserializationTest() throws IOException {
        // ARRANGE - Setting up the data that required for the test case
        CashCard expectedCashCard = new CashCard(99L, 123.45);

        // ACT - Calling a Method/Unit that is being tested
        String baseJson = """
                {
                    "id":99,
                    "amount":123.45
                }
                """;
        CashCard resultDeserializedCashCard = json.parseObject(baseJson); // Deserializing JSON

        // ASSERT - Verify that the expected result is correct or not
        assertThat(resultDeserializedCashCard).isEqualTo(expectedCashCard); // With Junit 5 Assertions.assertEquals(expectedCashCard, resultDeserializedCashCard)
        assertThat(resultDeserializedCashCard.id()).isEqualTo(expectedCashCard.id());
        assertThat(resultDeserializedCashCard.amount()).isEqualTo(expectedCashCard.amount());
//        ORIGINAL ASSERTS
//        assertThat(json.parse(baseJson)).isEqualTo(expectedCashCard)
//        assertThat(json.parseObject(baseJson).id()).isEqualTo(99L)
//        assertThat(json.parseObject(baseJson).amount()).isEqualTo(123.45)
    }
}