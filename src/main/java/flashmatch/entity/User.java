package flashmatch.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.common.aliasing.qual.Unique;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "User")
public class User {

    @Id
    @Unique
    @Column(name = "chat_id")
    private long chatId;

    @Column(name = "interest_id")
    private int interestId;

    @Column(name = "username")
    private String userName;

    @Column(name = "state_id")
    private int stateId;

    @Column(name = "notification")
    private boolean notification;

    @Column(name = "time")
    private long time;

    public User(long chatId, int interestId, int stateId, String userName) {
        this.chatId = chatId;
        this.interestId = interestId;
        this.stateId = stateId;
        this.userName = userName;
        this.time = 0L;
        this.notification = true;
    }

    public User(long chatId, int interestId, int stateId, String userName, long time) {
        this.chatId = chatId;
        this.interestId = interestId;
        this.stateId = stateId;
        this.userName = userName;
        this.time = time;
        this.notification = true;
    }
}
