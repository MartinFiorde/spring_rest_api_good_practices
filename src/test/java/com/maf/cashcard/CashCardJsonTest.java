package com.maf.cashcard;

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
    CashCard cashCardBase = new CashCard(99000000000L, 123.45, "sarah1");

    @Autowired
    public CashCardJsonTest(JacksonTester<CashCard> jsonTester) {
        this.jsonTester = jsonTester;
    }

    @Test
    void cashCardSerializationTest() throws IOException {
        // ARRANGE - Setting up the data that required for the test case
        CashCard cashCard = cashCardBase;

        // ACT - Calling a Method/Unit that is being tested
        JsonContent<CashCard> result = jsonTester.write(cashCard);

        // ASSERT - Verify that the expected result is correct or not
        ClassPathResource resource = new ClassPathResource("static/single.json");
        String expected = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);

        assertThat(result)
                .isStrictlyEqualToJson(expected)
                .hasJsonPath("@.id")
                .hasJsonPath("@.amount");
        assertThat(result.getJson())
                .contains("\"id\":" + cashCard.id())
                .contains("\"amount\":" + cashCard.amount());
//        ORIGINAL ASSERTS
//        assertThat(result).isStrictlyEqualToJson(resource.getFile())
//        assertThat(result).hasJsonPathNumberValue("@.id")
//        assertThat(result).extractingJsonPathNumberValue("@.id").isEqualTo(cashCard.id())
//        assertThat(result).hasJsonPathNumberValue("@.amount")
//        assertThat(result).extractingJsonPathNumberValue("@.amount").isEqualTo(cashCard.amount())
    }

    @Test
    void cashCardDeserializationTest() throws IOException {
        // ARRANGE - Setting up the data that required for the test case
        ClassPathResource resource = new ClassPathResource("static/single.json");
        String baseJson = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);

        // ACT - Calling a Method/Unit that is being tested
        CashCard result = jsonTester.parseObject(baseJson);

        // ASSERT - Verify that the expected result is correct or not
        CashCard expected = cashCardBase;

        assertThat(result).isEqualTo(expected);//Junit 5: Assertions.assertEquals(expected, result)
        assertThat(result.id()).isEqualTo(expected.id());
        assertThat(result.amount()).isEqualTo(expected.amount());
    }
}
