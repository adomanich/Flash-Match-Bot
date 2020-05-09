package flashmatch.service;

import flashmatch.entity.User;
import flashmatch.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    @Qualifier("userRepository")
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUsersByChatId(long chatId) {
        return userRepository.getUserByChatId(chatId);
    }

    public User getWaitedUser(int interestId, long chatId) {
        return userRepository.getWaitedUser(interestId, chatId);
    }

    public List<User> getAlreadyMatchedUsers(int interestId, long chatId) {
        return userRepository.getMatchedUser(interestId, chatId);
    }

    public List<User> getUsersWithTime() {
        return userRepository.getUsersWitTime();
    }

    public User addNew(User user) {
        return userRepository.save(user);
    }

    public User update(User user) {
        return userRepository.save(user);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    public boolean isUserAdmin(long chatId, String userName) {
        User user = getUsersByChatId(chatId);
        if (user == null) {
            return false;
        } else {
            return userName.equals("");
        }
    }
}
