
package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.model.query.QUrl;
import hexlet.code.parser.ParserUrl;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.assertj.core.api.Assertions.assertThat;

public final class AppTest {
    private static String baseUrl;
    private static Javalin app;
    private static Transaction transaction;
    private static Url existingUrl;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        existingUrl = new Url("https://github.com");
        existingUrl.save();
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
    void parserTest() throws MalformedURLException {
        String example = "https://codeclimate.com/velocity/signup/";
        String actual = "https://codeclimate.com";
        String exampleWithPort = "https://some-domain.org:8080/example/path";
        String actual2 = "https://some-domain.org:8080";
        assertThat(actual).isEqualTo(ParserUrl.parse(example));
        assertThat(actual2).isEqualTo(ParserUrl.parse(exampleWithPort));
    }

    @Test
    void testRoot() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Nested
    class UrlTest {
        @Test
        void addUrlTest() {
            String name = "https://codebattle.hexlet.io";
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("input", name)
                    .asEmpty();
            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            Url actualUrl = new QUrl()
                    .name.equalTo(name)
                    .findOne();
            assertThat(actualUrl).isNotNull();
            assertThat(actualUrl.getName()).isEqualTo(name);

            HttpResponse<String> responseGet = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = responseGet.getBody();
            assertThat(body).contains("страница успешно добавлена");
        }

        @Test
        void showUrlTest() {
            HttpResponse<String> actualHttpResponse = Unirest
                    .get(baseUrl + "/urls/" + existingUrl.getId())
                    .asString();
            String body = actualHttpResponse.getBody();
            assertThat(actualHttpResponse.getStatus()).isEqualTo(200);
            assertThat(body).contains(existingUrl.getName());

        }

        @Test
        void showListUrlTest() {
            HttpResponse<String> httpResponse = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            assertThat(httpResponse.getStatus()).isEqualTo(200);
            assertThat(httpResponse.getBody()).contains(existingUrl.getName());
        }

        @Test
        void createCheckTest() {
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls/" + existingUrl.getId() + "/checks")
                    .asEmpty();
            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls/1");

            HttpResponse<String> responseGet = Unirest
                    .get(baseUrl + "/urls/1")
                    .asString();
            assertThat(responseGet.getStatus()).isEqualTo(200);
            assertThat(responseGet.getBody()).contains("проверка создана");
            assertThat(responseGet.getBody()).contains("title");


        }
    }

//    @Test
//    void testTest() throws IOException, InterruptedException {
//        MockWebServer server = new MockWebServer();
//
//        // Schedule some responses.
//        server.enqueue(new MockResponse().setBody("hello, world!"));
//        server.enqueue(new MockResponse().setBody("страница успешно добавлена"));
//        server.enqueue(new MockResponse().setBody("yo dog"));
//        server.start();
//        // Start the server.
//
//        // Ask the server for its URL. You'll need this to make HTTP requests.
//        HttpUrl baseUrl = server.url("/urls");
//        HttpResponse<String> response = Unirest.get(String.valueOf(baseUrl)).asString();
//        assertThat(response.getBody()).isEqualTo("hello, world!");
//
//        RecordedRequest request1 = server.takeRequest();
//        Assertions.assertEquals("/urls", request1.getPath());
//
//        server.shutdown();
//    }
}
