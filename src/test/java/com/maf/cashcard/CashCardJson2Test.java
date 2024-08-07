package com.maf.cashcard;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.json.JSONObject;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

//Same tests as CashCardJsonTest, but configuring manually the @JsonTest annotation and using JSONObject for more clear asserts
@ContextConfiguration(classes = CashCardJson2Test.Config.class)
class CashCardJson2Test {

    @SuppressWarnings("unused")
    private JacksonTester<CashCard> json;
    CashCard cashCardBase = new CashCard(99000000000L, 123.45);


    @Configuration
    static class Config {

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @BeforeEach
    public void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        JacksonTester.initFields(this, objectMapper);
    }

    @Test
    void testSerialize() throws IOException, JSONException {
        // ARRANGE
        CashCard cashCard = cashCardBase;

        // ACT
        JsonContent<CashCard> result = json.write(cashCard);
        JSONObject resultAsObject = new JSONObject(result.getJson());

        // ASSERT
        File expected = new ClassPathResource("static/single.json").getFile();
        JSONObject expectedAsObject = new JSONObject(new String(Files.readAllBytes(expected.toPath())));

        assertThat(result)
                .isStrictlyEqualToJson(expected)//assertThat(resultAsObject.toString()).hasToString(expectedAsObject.toString())
                .hasJsonPath("$.id")//assertThat(resultAsObject.has("id")).isTrue()
                .hasJsonPath("$.amount");//assertThat(resultAsObject.has("amount")).isTrue()
        assertThat(resultAsObject.get("id")).isEqualTo(expectedAsObject.get("id"));
        assertThat(resultAsObject.get("amount")).isEqualTo(expectedAsObject.get("amount"));
    }

    @Test
    void testDeserialize() throws IOException {
        // ARRANGE
        String jsonContent = new String(Files.readAllBytes(new ClassPathResource("static/single.json").getFile().toPath()));

        // ACT
        CashCard result = json.parseObject(jsonContent);

        // ASSERT
        CashCard expected = cashCardBase;

        assertThat(result).isEqualTo(expected);
        assertThat(result.id()).isEqualTo(expected.id());
        assertThat(result.amount()).isEqualTo(expected.amount());
    }
}
