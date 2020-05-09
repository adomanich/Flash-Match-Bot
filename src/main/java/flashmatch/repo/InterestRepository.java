package flashmatch.repo;

import flashmatch.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface InterestRepository extends JpaRepository<Interest, Long> {

    Interest getInterestById(int id);

}
