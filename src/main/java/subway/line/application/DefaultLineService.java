package subway.line.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import subway.Station;
import subway.StationRepository;
import subway.line.application.dto.LineRequest;
import subway.line.application.dto.LineResponse;
import subway.line.application.dto.SectionRequest;
import subway.line.domain.Line;
import subway.line.domain.LineRepository;
import subway.line.domain.LineService;
import subway.line.domain.Section;

@Service
@Transactional(readOnly = true)
public class DefaultLineService implements LineService {
    public static final String LINE_NOT_FOUND_MESSAGE = "노선을 찾을 수 없습니다.";

    public static final String STATION_NOT_FOUND_MESSAGE = "역을 찾을 수 없습니다.";
    
    
    private final LineRepository lineRepository;
    private final StationRepository stationRepository;

    public DefaultLineService(LineRepository lineRepository, StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    @Override
    @Transactional
    public LineResponse saveLine(LineRequest lineRequest) {
        final Station upStation = findStationOrElseThrow(lineRequest.getUpStationId());
        final Station downStation = findStationOrElseThrow(lineRequest.getDownStationId());
        final Section section = new Section.SectionBuilder()
            .upStation(upStation)
            .downStation(downStation)
            .distance(lineRequest.getDistance())
            .build();

        Line line = new Line(
            lineRequest.getName(),
            lineRequest.getColor()
        );

        line.addSection(section);

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
        Line line = findLineOrElseThrow(id);
        return LineResponse.from(line);
    }


    @Override
    @Transactional
    public void updateLine(Long id, LineRequest lineRequest) {
        lineRepository.save(findLineOrElseThrow(id).getUpdated(lineRequest.getName(), lineRequest.getColor()));
    }

    @Override
    @Transactional
    public void deleteLineById(Long id) {
        lineRepository.deleteById(id);
    }


    @Override
    @Transactional
    public void addSection(Long lineId, SectionRequest sectionRequest) {
        Line line = findLineOrElseThrow(lineId);
        Station upStation = findStationOrElseThrow(sectionRequest.getUpStationId());
        Station downStation = findStationOrElseThrow(sectionRequest.getDownStationId());
        
        line.addSection(
            new Section.SectionBuilder()
                .line(line)
                .upStation(upStation)
                .downStation(downStation)
                .distance(sectionRequest.getDistance())
                .build()
        );

        lineRepository.save(line);
    }

    @Override
    @Transactional
    public void removeSection(Long lineId, Long stationId) {
        Line line = findLineOrElseThrow(lineId);
        Station station = findStationOrElseThrow(stationId);

        line.removeSection(station);
        lineRepository.save(line);
    }

    private Line findLineOrElseThrow(Long lineId) {
        return lineRepository.findById(lineId)
            .orElseThrow(() -> new IllegalArgumentException(LINE_NOT_FOUND_MESSAGE));
    }

    private Station findStationOrElseThrow(Long stationId) {
        return stationRepository.findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException(STATION_NOT_FOUND_MESSAGE));
    }
}