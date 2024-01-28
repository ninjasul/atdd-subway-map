package subway.line.service.dto;

import org.springframework.util.StringUtils;
import subway.line.exception.CreateRequestNotValidException;

import java.util.Objects;

public class LineCreateRequest {
    private String name;
    private String color;
    private Long upStationId;
    private Long downStationId;
    private long distance;

    private LineCreateRequest() {
    }

    public LineCreateRequest(final String name, final String color, final Long upStationId, final Long downStationId, final long distance) {
        this.name = name;
        this.color = color;
        this.upStationId = upStationId;
        this.downStationId = downStationId;
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public Long getUpStationId() {
        return upStationId;
    }

    public Long getDownStationId() {
        return downStationId;
    }

    public long getDistance() {
        return distance;
    }

    public void validate() {
        if(!StringUtils.hasLength(name)) {
            throw new CreateRequestNotValidException("name can not be empty");
        }
        if(!StringUtils.hasLength(color)) {
            throw new CreateRequestNotValidException("color can not be empty");
        }
        if(Objects.isNull(upStationId)) {
            throw new CreateRequestNotValidException("upStationId can not be null");
        }
        if(Objects.isNull(downStationId)) {
            throw new CreateRequestNotValidException("downStationId can not be null");
        }
        if(downStationId.equals(upStationId)) {
            throw new CreateRequestNotValidException("upStationId and downStationId can not be the same");
        }
        if(distance <= 0) {
            throw new CreateRequestNotValidException("distance must be greater than 0");
        }
    }
}