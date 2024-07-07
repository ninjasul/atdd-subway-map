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
        ExtractableResponse<Response> response = createStation("강남역");

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
    @DisplayName("지하철역을 조회한다.")
    @Test
    void getStations() {
        // given
        String stationName1 = "강남역";
        String stationName2 = "역삼역";

        createStation(stationName1);
        createStation(stationName2);

        // when
        ExtractableResponse<Response> response = getAllStations();

        // then
        List<String> stationNames = response.jsonPath().getList("name", String.class);
        assertThat(stationNames).containsExactlyInAnyOrder(stationName1, stationName2);
    }

    /**
     * Given 지하철역을 생성하고
     * When 그 지하철역을 삭제하면
     * Then 그 지하철역 목록 조회 시 생성한 역을 찾을 수 없다
     */
    @DisplayName("지하철역을 삭제한다.")
    @Test
    void deleteStation() {
        // given
        String stationName = "강남역";
        ExtractableResponse<Response> response = createStation(stationName);

        String location = response.header("Location");
        Long stationId = Long.parseLong(location.split("/stations/")[1]);

        // when
        deleteStation(stationId);

        // then
        ExtractableResponse<Response> allStations = getAllStations();
        List<String> stationNames = allStations.jsonPath().getList("name", String.class);
        assertThat(stationNames).doesNotContain(stationName);
    }

    private ExtractableResponse<Response> createStation(String stationName) {
        Map<String, String> station = new HashMap<>();
        station.put("name", stationName);

        return RestAssured
            .given()
            .log().all()
            .body(station)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/stations")
            .then()
            .log().all()
            .extract();
    }

    private ExtractableResponse<Response> getAllStations() {
        return RestAssured
            .given()
            .log().all()
            .when()
            .get("/stations")
            .then()
            .log().all()
            .extract();
    }

    private ExtractableResponse<Response> deleteStation(Long stationId) {
        return RestAssured
            .given()
            .log().all()
            .when()
            .delete("/stations/" + stationId)
            .then()
            .log().all()
            .extract();
    }
}