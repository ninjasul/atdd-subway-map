package nextstep.subway.domain;

import nextstep.subway.exception.SectionException;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class Sections {
    private static String DOWN_STATION_REGISTERED_ERROR_MASSAGE = "하행역만 상행역으로 등록될 수 있습니다.";
    private static String SECTION_STATION_REGISTERED_ERROR_MASSAGE = "이미 구간에 등록되어 있습니다.";
    private static String NOT_LAST_STATION_ERROR_MASSAGE = "마지막 하행역이 아닙니다.";
    private static String DELETE_SECTION_SIZE_ERROR_MASSAGE = "구간이 하나라 삭제가 불가능합니다.";
    private static int MINIMUM_SIZE = 1;

    @OneToMany(mappedBy = "line", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    protected Sections() { }

    public Sections(List<Section> sections) {
        this.sections = sections;
    }

    public List<Station> getStations() {
        List<Station> result = new ArrayList<>();
        if (sections.size() > 0) {
            sections.forEach(sec
                    -> result.add(sec.getUpStation()));
            result.add(sections.get(sections.size() - 1).getDownStation());
        }
        return result;
    }

    public void validationSectionStation(Station upStation, Station downStation) {
        if (!matchDownStation(upStation)) {
            throw new SectionException(DOWN_STATION_REGISTERED_ERROR_MASSAGE);
        }

        if (matchAllStation(downStation)) {
            throw new SectionException(SECTION_STATION_REGISTERED_ERROR_MASSAGE);
        }
    }

    public void deleteStation(Station station) {
        validationDeleteStation(station);
        Section lastSection = sections.get(lastIndex());
        sections.remove(lastSection);
    }

    private void validationDeleteStation(Station station) {
        if (!isLastDownStation(station)) {
            throw new SectionException(NOT_LAST_STATION_ERROR_MASSAGE);
        }

        if (isMinimumSectionSize()) {
            throw new SectionException(DELETE_SECTION_SIZE_ERROR_MASSAGE);
        }
    }

    private boolean isMinimumSectionSize() {
        return sections.size() == MINIMUM_SIZE;
    }

    private boolean isLastDownStation(Station station) {
        return sections.get(lastIndex()).getDownStation().equals(station);
    }

    private int lastIndex() {
        return sections.size() - 1;
    }

    private boolean matchDownStation(Station station) {
        return sections.size() == 0
                || sections.stream()
                        .anyMatch(sec -> sec.matchDownStation(station));
    }

    private boolean matchAllStation(Station station) {
        return sections.stream()
                        .anyMatch(sec -> sec.matchAllStation(station));
    }
}
