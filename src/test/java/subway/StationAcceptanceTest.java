package subway;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

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
        Map<String, String> params = makeStationRequestBody("강남역");

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
    @DisplayName("지하철 역 목록을 조회하면 모든 지하철 역 정보를 응답받습니다.")
    @Test
    void showStations() {
        // given
        Map<String, String> body1 = makeStationRequestBody("station1");
        Map<String, String> body2 = makeStationRequestBody("station2");

        RestAssured
                .given()
                .log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(body1)
                .when()
                .post("/stations");

        RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(body2)
                .when()
                .post("/stations");

        // when
        List<String> stationList = RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/stations")
                .then().log().all()
                .extract().jsonPath().getList("name", String.class);

        // then
        assertThat(stationList).hasSize(2);
        assertThat(stationList).containsExactly("station1", "station2");
    }

    /**
     * Given 지하철역을 생성하고
     * When 그 지하철역을 삭제하면
     * Then 그 지하철역 목록 조회 시 생성한 역을 찾을 수 없다
     */
    @DisplayName("특정 지하철 역을 삭제하면 해당 지하철역은 조회되지 않습니다.")
    @Test
    void deleteStations() {
        // given
        Map<String, String> body1 = makeStationRequestBody("station1");
        Map<String, String> body2 = makeStationRequestBody("station2");

        long stationId = RestAssured
                .given()
                .log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(body1)
                .when()
                .post("/stations")
                .then().extract().jsonPath().getLong("id");

        RestAssured
                .given()
                .log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(body2)
                .when()
                .post("/stations");

        RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", stationId)
                .when()
                .delete("/stations/{id}");

        // when
        List<String> result = RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/stations")
                .then().log().all()
                .extract().jsonPath().getList("name", String.class);

        // then
        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result).containsExactly("station2");
    }

    private Map<String, String> makeStationRequestBody(String name) {
        return Map.of("name", name);
    }
}