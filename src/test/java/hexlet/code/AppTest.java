
package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.parser.ParserUrl;
import io.ebean.DB;
import io.ebean.SqlRow;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public final class AppTest {
    private static String baseUrl;
    private static Javalin app;
    private static Transaction transaction;
    private static MockWebServer mockServer;

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        Url existingUrl = new Url("https://github.com");
        existingUrl.save();
        mockServer = new MockWebServer();
        mockServer.start();
        String content = Files.readString(Paths.get("src/test/resources/responseTest.html"));
        mockServer.enqueue(new MockResponse().setBody(content));
    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        mockServer.close();
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
        void testStore() throws IOException, SQLException {
            String url = mockServer.url("/").toString().replaceAll("/$", "");

            HttpResponse<String> responseAddUrl = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", url)
                    .asEmpty();

            String selectUrl = String.format("SELECT * FROM url WHERE name = '%s';", url);
            SqlRow actualUrl = DB.sqlQuery(selectUrl).findOne();

            assertThat(actualUrl).isNotNull();
            assertThat(actualUrl.getString("name")).isEqualTo(url);

            HttpResponse<String> responseGet = Unirest
                    .get(baseUrl + "/urls")
                    .asString();

            assertThat(responseGet.getStatus()).isEqualTo(200);
            assertThat(responseGet.getBody()).contains("Страница успешно добавлена");

            HttpResponse<String> responseCheck = Unirest
                    .post(baseUrl + "/urls/" + actualUrl.getString("id") + "/checks")
                    .asEmpty();

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/" + actualUrl.getString("id"))
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains("Страница успешно проверена");

            String selectUrlCheck = String.format(
                    "SELECT * FROM url_check WHERE url_id = '%s' ORDER BY created_at DESC;",
                    actualUrl.getString("id")
            );
            SqlRow actualCheckUrl = DB.sqlQuery(selectUrlCheck).findOne();
            assertThat(actualCheckUrl).isNotNull();
            assertThat(actualCheckUrl.getString("status_code")).isEqualTo("200");
            assertThat(actualCheckUrl.getString("title")).isEqualTo("Test page");
            assertThat(actualCheckUrl.getString("h1")).
                    isEqualTo("Do not expect a miracle, miracles yourself!");

            Reader description = ((Clob) actualCheckUrl.get("description")).getCharacterStream();
            String actualDes = new BufferedReader(description).lines()
                    .collect(Collectors.joining(System.lineSeparator()));

            assertThat(actualDes).isEqualTo("Free Web tutorials");

        }
    }
}
