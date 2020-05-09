package flashmatch.repo;

import flashmatch.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByInterestId(int interesid);

    @Query(value = "SELECT u from User u where u.chatId = :chat_id")
    User getUserByChatId(@Param("chat_id") Long chat_id);

    @Query(value = "SELECT u from User u where u.interestId = :interest_id AND u.stateId = 2 AND u.chatId != :chat_id")
    User getWaitedUser(@Param("interest_id") int interest_id, @Param("chat_id") Long chat_id);

    @Query(value = "SELECT u from User u where u.interestId = :interest_id AND u.stateId = 3 AND u.chatId != :chat_id")
    List<User> getMatchedUser(@Param("interest_id") int interest_id, @Param("chat_id") Long chat_id);

    @Query(value = "SELECT u from User u where time != 0")
    List<User> getUsersWitTime();
}
