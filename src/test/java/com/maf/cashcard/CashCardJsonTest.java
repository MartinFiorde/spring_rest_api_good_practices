package com.maf.cashcard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
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

    private final JacksonTester<CashCard> jsonTester;
    CashCard cashCardBase = new CashCard(99000000000L, 123.45);

    @Autowired
    public CashCardJsonTest(JacksonTester<CashCard> jsonTester) {
        this.jsonTester = jsonTester;
    }

    @Test
    void cashCardSerializationTest() throws IOException, JSONException {
        // ARRANGE - Setting up the data that required for the test case
        CashCard cashCard = cashCardBase;

        // ACT - Calling a Method/Unit that is being tested
        JsonContent<CashCard> resultSerializedJson = jsonTester.write(cashCard);

        // ASSERT - Verify that the expected result is correct or not
        ClassPathResource resource = new ClassPathResource("static/expected.json");
        String expectedJson = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);

        assertThat(resultSerializedJson)
                .isStrictlyEqualToJson(expectedJson)
                .hasJsonPath("@.id")
                .hasJsonPath("@.amount");
        assertThat(resultSerializedJson.getJson())
                .contains("\"id\":" + cashCard.id())
                .contains("\"amount\":" + cashCard.amount());
//        ORIGINAL ASSERTS
//        assertThat(resultSerializedJson).isStrictlyEqualToJson(new ClassPathResource("static/expected.json").getFile())
//        assertThat(resultSerializedJson).hasJsonPathNumberValue("@.id")
//        assertThat(resultSerializedJson).extractingJsonPathNumberValue("@.id").isEqualTo(expectedId)
//        assertThat(resultSerializedJson).hasJsonPathNumberValue("@.amount")
//        assertThat(resultSerializedJson).extractingJsonPathNumberValue("@.amount").isEqualTo(expectedAmount)
    }

    @Test
    void cashCardDeserializationTest() throws IOException {
        // ARRANGE - Setting up the data that required for the test case
        ClassPathResource resource = new ClassPathResource("static/expected.json");
        String baseJson = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);

        // ACT - Calling a Method/Unit that is being tested
        CashCard result = jsonTester.parseObject(baseJson); // Deserializing JSON

        // ASSERT - Verify that the expected result is correct or not
        CashCard expectedCashCard = cashCardBase;

        assertThat(result).isEqualTo(expectedCashCard); // With Junit 5 Assertions.assertEquals(expectedCashCard, result)
        assertThat(result.id()).isEqualTo(expectedCashCard.id());
        assertThat(result.amount()).isEqualTo(expectedCashCard.amount());
//        ORIGINAL ASSERTS
//        assertThat(jsonTester.parse(baseJson)).isEqualTo(expectedCashCard)
//        assertThat(jsonTester.parseObject(baseJson).id()).isEqualTo(99L)
//        assertThat(jsonTester.parseObject(baseJson).amount()).isEqualTo(123.45)
    }
}
