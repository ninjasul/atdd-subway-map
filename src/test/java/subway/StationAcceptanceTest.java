package subway;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철역 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class StationAcceptanceTest {
    /**
     * When 지하철역을 생성하면
     * Then 지하철역이 생성된다
     * Then 지하철역 목록 조회 시 생성한 역을 찾을 수 있다
     */
    @DisplayName("지하철역을 생성한다.")
    @Test
    void createStation() {
        // when
        Map<String, String> params = new HashMap<>();
        params.put("name", "강남역");

        ExtractableResponse<Response> response =
                RestAssured.given().log().all()
                        .body(params)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .when().post("/stations")
                        .then().log().all()
                        .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // then
        List<String> stationNames =
                RestAssured.given().log().all()
                        .when().get("/stations")
                        .then().log().all()
                        .extract().jsonPath().getList("name", String.class);
        assertThat(stationNames).containsAnyOf("강남역");
    }

    /**
     * Given 2개의 지하철역을 생성하고
     * When 지하철역 목록을 조회하면
     * Then 2개의 지하철역을 응답 받는다
     */
    @DisplayName("모든 지하철역 목록을 조회한다.")
    @Test
    void readStations() {
        // Given
        createStations();
        int expectedCountOfStation = 2;

        // When
        List<String> stationsNames = RestAssured.given().log().all()
                .when().get("/stations")
                .then().log().all()
                .extract().jsonPath().getList("name", String.class);

        // Then
        assertThat(stationsNames).contains("숙대입구", "서울역");
        assertThat(stationsNames.size()).isEqualTo(expectedCountOfStation);
    }

    /**
     * Given 지하철역을 생성하고
     * When 그 지하철역을 삭제하면
     * Then 그 지하철역 목록 조회 시 생성한 역을 찾을 수 없다
     */
    @DisplayName("지하철역을 삭제한다")
    @Test
    void deleteStation() {
        // Given
        List<ExtractableResponse<Response>> stations = createStations();
        ExtractableResponse<Response> responseExtractableResponse = stations.get(0);
        Long stationId = responseExtractableResponse.jsonPath().getObject("id", Long.class);
        String stationName = responseExtractableResponse.jsonPath().getObject("name", String.class);
        int expectedCountOfStation = 1;

        // When
        RestAssured.given().log().all()
                .when()
                .delete("/stations/" + stationId)
                .then().log().all();

        // Then
        List<String> stationsNames = RestAssured.given().log().all()
                .when().get("/stations")
                .then().log().all()
                .extract().jsonPath().getList("name", String.class);
        assertThat(stationsNames).doesNotContain(stationName);
        assertThat(stationsNames.size()).isEqualTo(expectedCountOfStation);
    }

    private List<ExtractableResponse<Response>> createStations() {
        Map<String, String> stationParamsOfSookmyungEntranceStation = createStationParams("숙대입구");
        Map<String, String> stationParamsOfSeoulStation = createStationParams("서울역");
        ExtractableResponse<Response> responseOfSookmyungEntraceStation = requestCreateStation(
                stationParamsOfSookmyungEntranceStation);
        ExtractableResponse<Response> responseOfSeoulStation = requestCreateStation(stationParamsOfSeoulStation);
        return List.of(responseOfSookmyungEntraceStation, responseOfSeoulStation);
    }

    private Map<String, String> createStationParams(String name) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        return params;
    }

    private ExtractableResponse<Response> requestCreateStation(Map<String, String> params) {
        return RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/stations")
                .then().log().all()
                .extract();
    }
}