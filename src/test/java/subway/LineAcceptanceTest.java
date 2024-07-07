package subway;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import subway.line.application.dto.LineRequest;
import subway.line.application.dto.LineResponse;

@DisplayName("지하철 노선 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class LineAcceptanceTest {
    @DisplayName("지하철 노선을 생성한다")
    @Test
    void createLine() {
        // given
        LineRequest request = new LineRequest("신분당선", "bg-red-600", 1L, 2L, 10);

        // when
        ExtractableResponse<Response> response = createLine(request);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // then
        List<LineResponse> lines = getAllLines().jsonPath().getList(".", LineResponse.class);
        assertThat(lines).hasSize(1);
        assertThat(lines.get(0).getName()).isEqualTo("신분당선");
    }

    @DisplayName("존재하지 않는 역 ID로 지하철 노선을 생성할 때 실패한다")
    @Test
    void createLineWithNonExistentStationId() {
        // given
        LineRequest request = new LineRequest("신분당선", "bg-red-600", 999L, 2L, 10);

        // when
        ExtractableResponse<Response> response = createLine(request);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private ExtractableResponse<Response> createLine(LineRequest request) {
        return RestAssured.given().log().all()
            .body(request)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when().post("/lines")
            .then().log().all()
            .extract();
    }


    private ExtractableResponse<Response> getAllLines() {
        return RestAssured.given().log().all()
            .when().get("/lines")
            .then().log().all()
            .extract();
    }
}
