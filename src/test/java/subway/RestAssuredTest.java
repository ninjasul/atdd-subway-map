package subway;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RestAssuredTest {

    private static final String TARGET = "https://www.google.com";

    @Test
    void 구글_페이지_접근_테스트() {
        ExtractableResponse<Response> response = given().when()
                                                        .get(TARGET)
                                                        .then()
                                                        .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }
}
