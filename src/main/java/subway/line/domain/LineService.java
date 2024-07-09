package subway.line.domain;

import java.util.List;

import subway.line.application.dto.LineRequest;
import subway.line.application.dto.LineResponse;
import subway.line.application.dto.SectionRequest;

public interface LineService {
    LineResponse findLineById(Long id);

    List<LineResponse> findAllLines();

    LineResponse saveLine(LineRequest lineRequest);

    void updateLine(Long id, LineRequest lineRequest);

    void deleteLineById(Long id);

    void addSection(Long lineId, SectionRequest sectionRequest);

    void removeSection(Long lineId, Long stationId);
}
