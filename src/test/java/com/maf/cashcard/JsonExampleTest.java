package com.maf.cashcard;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class JsonExampleTest {

    @Autowired
    private JacksonTester<CashCard> json;

    @Autowired
    private JacksonTester<CashCard[]> jsonList;

    private CashCard[] cashCards;

    @BeforeEach
    void setUp() {
        cashCards = Arrays.array(
                new CashCard(99000000000L, 123.45, "sarah1", true),
                new CashCard(100L, 1.00, "sarah1", true),
                new CashCard(101L, 150.00, "sarah1", true));
    }

    @Test
    void cashCardSerializationTest() throws IOException {
        CashCard cashCard = cashCards[0];
        ClassPathResource resource = new ClassPathResource("static/single.json");
        String expected = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);

        JsonContent<CashCard> result = json.write(cashCard);
        System.out.println("MAF: "+result.getJson().toString());
        assertThat(result)
                .isStrictlyEqualToJson(expected)
                .hasJsonPathNumberValue("@.id")
                .hasJsonPathNumberValue("@.amount")
                .hasJsonPathStringValue("@.owner");
        assertThat(result.getJson())
                .contains("\"id\":" + cashCard.id())
                .contains("\"amount\":" + cashCard.amount())
                .contains("\"owner\":\"" + cashCard.owner()+"\"")
                .contains("\"isActive\":" + cashCard.isActive());
    }

    @Test
    void cashCardDeserializationTest() throws IOException {
        // ARRANGE
        String expected = """
                {
                    "id": 99000000000,
                    "amount": 123.45,
                    "owner": "sarah1",
                    "isActive": true
                }
                """;
        // ACT
        CashCard result = json.parseObject(expected);
        // ASSERT
        assertThat(result).isEqualTo(new CashCard(99000000000L, 123.45, "sarah1", true));
        assertThat(result.id()).isEqualTo(99000000000L);
        assertThat(result.amount()).isEqualTo(123.45);
        assertThat(result.owner()).isEqualTo("sarah1");
    }

    @Test
    void cashCardListSerializationTest() throws IOException {
        File expected = new ClassPathResource("static/list.json").getFile();
        JsonContent<CashCard[]> result = jsonList.write(cashCards);
        assertThat(result).isStrictlyEqualToJson(expected);
    }

    @Test
    void cashCardListDeserializationTest() throws IOException {
        // ARRANGE
        String baseJson="""
         [
            { "id": 99000000000, "amount": 123.45 , "owner": "sarah1", "isActive": true },
            { "id": 100, "amount": 1.00, "owner": "sarah1", "isActive": true },
            { "id": 101, "amount": 150.00, "owner": "sarah1", "isActive": true }
         ]
         """;
        // ACT
        CashCard[] result = jsonList.parseObject(baseJson);
        // ASSERT
        assertThat(result).isEqualTo(cashCards);
    }
}
