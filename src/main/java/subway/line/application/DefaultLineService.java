package subway.line.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import subway.Station;
import subway.StationRepository;
import subway.line.application.dto.LineRequest;
import subway.line.application.dto.LineResponse;
import subway.line.domain.Line;
import subway.line.domain.LineRepository;
import subway.line.domain.LineService;

@Service
@Transactional(readOnly = true)
public class DefaultLineService implements LineService {
    private final LineRepository lineRepository;
    private final StationRepository stationRepository;

    public DefaultLineService(LineRepository lineRepository, StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    @Override
    @Transactional
    public LineResponse saveLine(LineRequest lineRequest) {
        Station upStation = stationRepository.findById(lineRequest.getUpStationId())
            .orElseThrow(() -> new IllegalArgumentException("상행 종점역을 찾을 수 없습니다."));

        Station downStation = stationRepository.findById(lineRequest.getDownStationId())
            .orElseThrow(() -> new IllegalArgumentException("하행 종점역을 찾을 수 없습니다."));

        Line line = new Line(
            lineRequest.getName(),
            lineRequest.getColor(),
            upStation,
            downStation,
            lineRequest.getDistance()
        );

        Line savedLine = lineRepository.save(line);
        return LineResponse.from(savedLine);
    }

    @Override
    public List<LineResponse> findAllLines() {
        return lineRepository.findAll().stream()
            .map(LineResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    public LineResponse findLineById(Long id) {
        Line line = lineRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("노선을 찾을 수 없습니다."));
        return LineResponse.from(line);
    }


    @Override
    @Transactional
    public void updateLine(Long id, LineRequest lineRequest) {
        lineRepository.save(
            lineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("노선을 찾을 수 없습니다."))
                .getUpdated(lineRequest.getName(), lineRequest.getColor())
        );
    }

    @Override
    @Transactional
    public void deleteLineById(Long id) {
        lineRepository.deleteById(id);
    }
}