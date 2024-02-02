package subway.line.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import subway.line.repository.domain.Line;

import java.util.List;
import java.util.Optional;

public interface LineRepository extends JpaRepository<Line, Long> {
    @Query("select line from Line line left join fetch line.sections.sections section join fetch section.upStation join fetch section.downStation")
    List<Line> findAllWithLines();

    @Query("select line from Line line left join fetch line.sections.sections section join fetch section.upStation join fetch section.downStation where line.id = :id")
    Optional<Line> findByIdWithSection(@Param("id") Long id);
}
