
package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.model.query.QUrl;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public final class AppTest {
    private static String baseUrl;
    private static Javalin app;
    private static Transaction transaction;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    // При использовании БД запускать каждый тест в транзакции -
    // является хорошей практикой
    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    @AfterEach
    void afterEach() {
        transaction.rollback();
    }

    @Test
    void testRoot() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void addUrlTest() {
        String name = "https://leetcode.com";
        HttpResponse<String> httpResponse = Unirest
                .post(baseUrl + "/urls")
                .field("name", name)
                .asEmpty();
        assertThat(httpResponse.getStatus()).isEqualTo(302);

        Url url = new Url("https://leetcode.com");
        url.save();
        Url actualUrl = new QUrl()
                .name.equalTo(name)
                .findOne();
        assertThat(actualUrl).isNotNull();
        assertThat(actualUrl.getName()).isEqualTo(name);
    }

    @Test
    void showUrlTest() {
        HttpResponse<String> httpResponse = Unirest
                .get(baseUrl + "/urls/1")
                .asString();
        assertThat(httpResponse.getStatus()).isEqualTo(200);
        assertThat(httpResponse.getBody()).contains("htmled.it");

    }

    @Test
    void showListUrlTest() {
        HttpResponse<String> httpResponse = Unirest
                .get(baseUrl + "/urls")
                .asString();
        assertThat(httpResponse.getStatus()).isEqualTo(200);
        assertThat(httpResponse.getBody()).contains("htmled.it");
        assertThat(httpResponse.getBody()).contains("https://getbootstrap.com");
    }

    @Test
    void createCheckTest() throws IOException {
        String name = "https://htmled.it";
        HttpResponse<String> httpResponse = Unirest
                .post(baseUrl + "/urls/1/checks")
                .field("name", name)
                .asEmpty();
        assertThat(httpResponse.getStatus()).isEqualTo(302);
        Url actualUrl = new QUrl()
                .name.equalTo(name)
                .findOne();
        assertThat(actualUrl).isNotNull();
        assertThat(actualUrl.getName()).isEqualTo(name);
        assertThat(actualUrl.getLastCheck()).isNotNull();
        assertThat(actualUrl.getLastStatusCode()).isEqualTo(200);

    }
}
