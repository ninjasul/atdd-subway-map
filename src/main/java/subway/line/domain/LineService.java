package subway.line.domain;

import java.util.List;

import subway.line.application.dto.LineRequest;
import subway.line.application.dto.LineResponse;

public interface LineService {
    LineResponse findLineById(Long id);

    List<LineResponse> findAllLines();

    LineResponse saveLine(LineRequest lineRequest);
}
