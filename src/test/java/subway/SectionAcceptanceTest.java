package subway;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import subway.line.dto.LineResponse;
import subway.station.dto.StationResponse;

@DisplayName("지하철 구간 관련 기능")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SectionAcceptanceTest {
    private LineResponse newBunDangLine;
    private StationResponse gangNamStation;
    private StationResponse deokSoStation;
    private StationResponse daSanStation;

    static ExtractableResponse<Response> 노선_구간_삭제(Long lineId, Long stationId) {
        return RestAssured.given()
            .queryParam("stationId", stationId).log().all()
            .when()
            .delete("/lines/" + lineId + "/sections")
            .then().log().all()
            .extract();
    }

    static ExtractableResponse<Response> 노선_구간_생성(Long lineId, Map<String, Object> sectionParams) {
        return RestAssured.given().log().all()
            .body(sectionParams)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when().post("/lines/" + lineId)
            .then().log().all()
            .extract();
    }

    @BeforeEach
    void setUp() {
        Map<String, String> gangNamParam = new HashMap<>();
        gangNamParam.put("name", "강남역");
        Map<String, String> deokSoParam = new HashMap<>();
        deokSoParam.put("name", "덕소역");
        Map<String, String> daSanParam = new HashMap<>();
        daSanParam.put("name", "다산역");

        gangNamStation = StationAcceptanceTest.지하철역_생성(gangNamParam).as(StationResponse.class);
        deokSoStation = StationAcceptanceTest.지하철역_생성(deokSoParam).as(StationResponse.class);
        daSanStation = StationAcceptanceTest.지하철역_생성(daSanParam).as(StationResponse.class);

        Map<String, Object> lineParams = new HashMap<>();
        lineParams.put("name", "신분당선");
        lineParams.put("color", "bg-red-600");
        lineParams.put("upStationId", 1);
        lineParams.put("downStationId", 2);
        lineParams.put("distance", 10);

        newBunDangLine = LineAcceptanceTest.지하철_노선_생성(lineParams).as(LineResponse.class);
    }

    /**
     * When
     * 해당 노선에 새로운 구간을 생성한다.
     * Then
     * 정상적으로 구간이 생성되고,
     * 생성된 구간의 하행 종점역이 기존 노선의 하행 종점역으로 변경된다.
     */
    @DisplayName("지하철 구간을 생성한다")
    @Test
    void 지하철_노선을_생성하면_목록_조회_시_생성한_노선을_찾을_수_있다() {
        // When
        Map<String, Object> sectionParams = new HashMap<>();
        sectionParams.put("upStationId", deokSoStation.getId());
        sectionParams.put("downStationId", daSanStation.getId());
        sectionParams.put("distance", 10);

        Long lineId = newBunDangLine.getId();

        ExtractableResponse<Response> response = 노선_구간_생성(lineId, sectionParams);

        // Then
        응답_상태_코드_검증(response, HttpStatus.CREATED);
        Long downStationId = LineAcceptanceTest.지하철_노선_조회(lineId).body().jsonPath().getLong("downStationId");
        assertThat(downStationId).isEqualTo(deokSoStation.getId());
    }

    /**
     * Given
     * 기준 노선에 구간을 생성되고
     * When
     * 해당 노선의 구간을 삭제하면
     * Then
     * 정상적으로 구간이 삭제되고,
     * 삭제된 지하철역을 하행 종점역으로 갖는 구간의 상행 종점역이 노선의 하행 종점역이 된다.
     */
    @DisplayName("지하철 구간을 삭제한다.")
    @Test
    void 지하철_노선을_생성하고_생성한_노선을_삭제하면_해당_노선_정보는_삭제된다() {
        // Given
        Map<String, Object> sectionParams = new HashMap<>();
        sectionParams.put("upStationId", deokSoStation.getId());
        sectionParams.put("downStationId", daSanStation.getId());
        sectionParams.put("distance", 10);

        Long lineId = newBunDangLine.getId();

        응답_상태_코드_검증(노선_구간_생성(lineId, sectionParams), HttpStatus.CREATED);

        // When
        ExtractableResponse<Response> response = 노선_구간_삭제(lineId, daSanStation.getId());

        // Then
        응답_상태_코드_검증(response, HttpStatus.NO_CONTENT);
        Long downStationId = LineAcceptanceTest.지하철_노선_조회(lineId).body().jsonPath().getLong("downStationId");
        assertThat(downStationId).isEqualTo(deokSoStation.getId());
    }

    private void 응답_상태_코드_검증(ExtractableResponse<Response> response, HttpStatus httpStatus) {
        assertThat(response.statusCode()).isEqualTo(httpStatus.value());
    }
}
