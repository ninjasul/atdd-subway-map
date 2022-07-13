package nextstep.subway.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.applicaion.exception.ExceptionMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("지하철 구간 관련 기능")
@Sql(scripts = {
        "classpath:sql/truncate.sql",
        "classpath:sql/createStations.sql",
        "classpath:sql/createLine.sql"})
class SectionAcceptanceTest extends BaseAcceptanceTest{

    /**
     * When     새로운 구간을 지하철 노선의 하행 종점역에 등록하면
     * Then     지하철 노선의 구간 목록에서 생성된 구간을 찾을 수 있다.
     * Then     지하철 노선의 하행 종점역이 변경된다.
     */
    @Test
    @DisplayName("지하철 노선의 하행 종점역에 구간을 등록한다.")
    void registerSectionTest() {
        //when
        Map<String, Object> requestBody = getRequestBody("2","3");
        ExtractableResponse<Response> createdSection = createSection(requestBody);

        //then
        ExtractableResponse<Response> sections = RestAssured
                .given().log().all()
                .when().get("/lines/{id}/sections",1)
                .then().log().all()
                .extract();
        int sectionId = createdSection.jsonPath().getInt("id");
        assertThat(sections.jsonPath().getList("id",Integer.class)).contains(sectionId);

        //then
        ExtractableResponse<Response> line = RestAssured
            .given().log().all()
            .when().get("/lines/{id}",1)
            .then().log().all()
            .extract();
        assertThat(line.jsonPath().getInt("stations[1].id")).isEqualTo(3);
    }

    /**
     * When     새로운 구간의 상행역이 해당 노선에 등록된 하행 종점역이 아니면
     * Then     에러처리한다.
     */
    @Test
    @DisplayName("지하철 노선의 하행 종점역이 아닌 역에 구간을 등록한다.")
    void registerIllegalUpStationSectionTest() {
        //when
        Map<String, Object> requestBody = getRequestBody("1","3");
        ExtractableResponse<Response> createdSection = createSection(requestBody);

        //then
        assertThat(createdSection.jsonPath().getString("message"))
            .isEqualTo(ExceptionMessages.getNoEndpointInputExceptionMessage(1,2));
    }

    /**
     * Given    지하철 노선의 하행 종점역에 새로운 구간을 등록하고
     * When     해당 노선의 하행 종점역의 구간을 제거하면
     * Then     지하철 노선의 구간 목록에서 제거한 구간을 찾을 수 없다.
     * Then     지하철 노선의 하행 종점역이 변경된다.
     */
    @Test
    @DisplayName("지하철 노선의 하행 종점역의 구간을 제거한다.")
    void removeSectionTest() {
        //given


    }

    /**
     * Given    지하철 노선의 상행 종점역에 새로운 구간을 등록하고
     * When     삭제할 구간이 하행 종점역이 아니면
     * Then     에러처리한다.
     */
    @Test
    @DisplayName("지하철 노선의 하행 종점역이 아닌 역의 구간을 제거한다.")
    void removeIllegalDownStationSectionTest() {

    }

    /**
     * When     삭제할 지하철 노선의 구간이 1개이면
     * Then     에러처리한다.
     */
    @Test
    @DisplayName("지하철 노선의 구간이 1개일 때 구간을 제거한다.")
    void remainAtLeastOneSectionTest() {

    }

    private ExtractableResponse<Response> createSection(Map<String, Object> requestBody) {
        return RestAssured
            .given().log().all()
            .body(requestBody)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when().post("/lines/{id}/sections",1)
            .then().log().all()
            .extract();
    }

    private Map<String, Object> getRequestBody(String upStationId, String downStationId) {
        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put("upStationId", upStationId);
        requestBody.put("downStationId",downStationId);
        requestBody.put("distance", 10);
        return requestBody;
    }

}
