package flashmatch.service;

import flashmatch.entity.Interest;
import flashmatch.repo.InterestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InterestService {

    @Autowired
    @Qualifier("interestRepository")
    private InterestRepository interestRepository;

    @Transactional(readOnly = true)
    public List<Interest> getAllInterests() {
        return interestRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Interest getInterestById(int interest_id) {
        return interestRepository.getInterestById(interest_id);
    }

    public Interest addInterest(Interest interest) {
        return interestRepository.save(interest);
    }

    public void delete(Interest interest) {
        interestRepository.delete(interest);
    }
}
