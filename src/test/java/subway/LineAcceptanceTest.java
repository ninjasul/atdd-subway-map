package subway;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

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
        TestFixture.createStation("강남역");
        TestFixture.createStation("역삼역");
        LineRequest request = new LineRequest("신분당선", "bg-red-600", 1L, 2L, 10);

        // when
        ExtractableResponse<Response> response = TestFixture.createLine(request);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // then
        List<LineResponse> lines = TestFixture.getAllLines().jsonPath().getList(".", LineResponse.class);
        assertThat(lines).hasSize(1);
        assertThat(lines.get(0).getName()).isEqualTo("신분당선");
    }

    @DisplayName("존재하지 않는 역 ID로 지하철 노선을 생성할 때 실패한다")
    @Test
    void createLineWithNonExistentStationId() {
        // given
        TestFixture.createStation("강남역");
        LineRequest request = new LineRequest("신분당선", "bg-red-600", 999L, 2L, 10);

        // when
        ExtractableResponse<Response> response = TestFixture.createLine(request);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @DisplayName("지하철 노선 목록을 조회한다")
    @Test
    void getLines() {
        // given
        TestFixture.createStation("강남역");
        TestFixture.createStation("역삼역");
        TestFixture.createLine(new LineRequest("신분당선", "bg-red-600", 1L, 2L, 10));

        TestFixture.createStation("수서역");
        TestFixture.createStation("가천대역");
        TestFixture.createLine(new LineRequest("분당선", "bg-green-600", 3L, 4L, 20));

        // when
        ExtractableResponse<Response> response = TestFixture.getAllLines();

        // then
        List<LineResponse> lines = response.jsonPath().getList(".", LineResponse.class);
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0).getName()).isEqualTo("신분당선");
        assertThat(lines.get(1).getName()).isEqualTo("분당선");
    }

    @DisplayName("지하철 노선을 조회한다")
    @Test
    void getLine() {
        // given
        TestFixture.createStation("강남역");
        TestFixture.createStation("역삼역");
        ExtractableResponse<Response> createResponse = TestFixture.createLine(new LineRequest("신분당선", "bg-red-600", 1L, 2L, 10));
        Long lineId = createResponse.jsonPath().getLong("id");

        // when
        ExtractableResponse<Response> response = TestFixture.getLine(lineId);

        // then
        LineResponse line = response.jsonPath().getObject(".", LineResponse.class);
        assertThat(line.getName()).isEqualTo("신분당선");
    }

    @DisplayName("존재하지 않는 지하철 노선을 조회할 때 실패한다")
    @Test
    void getNonExistentLine() {
        // when
        ExtractableResponse<Response> response = TestFixture.getLine(999L);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
