package com.home.model;

import com.home.bot.State;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends AbstractBaseEntity {

    @Column(name = "chat_id", unique = true, nullable = false)
    @NotNull
    private Long chatId;

    @Column(name = "name", unique = true, nullable = false)
    @NotBlank
    private String name;

    @Column(name = "name_from_telegram", nullable = false)
    @NotBlank
    private String nameFromTelegram;

    @Column(name = "tel_number")
    @NotBlank
    private String telNumber;

    @Column(name = "score", nullable = false)
    @NotNull
    private int currentTestNumber;

    @Column(name = "last_result", nullable = false)
    @NotNull
    private String lastResult;

    @Column(name = "last_motivation")
    private String lastNumberMotivation;

    @Column(name = "bot_state", nullable = false)
//    @NotBlank
    @Enumerated(EnumType.STRING)
    private State botState;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> resultsList;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    public User(Long chatId, String nameFromTelegram) {
        this.chatId = chatId;
        this.name = String.valueOf(chatId);
        this.nameFromTelegram = nameFromTelegram;
        this.telNumber = "no number";
        this.currentTestNumber = 0;
        this.lastResult = "no result";
        this.lastNumberMotivation = "0";
        this.botState = State.START;
        this.resultsList = new ArrayList<>();
        this.role = Role.USER;
    }

    @Override
    public String toString() {
        return "<strong>User</strong> {" +
                "id=<u>" + id +
                "</u>, chatId=" + chatId +
                ", name=<b><i>'" + name + '\'' +
                "</i></b>, nameFromTelegram=<a href=\"tg://user?id=" + chatId + "\">" + nameFromTelegram + '\'' +
                "</a>, telNumber='" + telNumber + '\'' +
                ", currentTestNumber=" + currentTestNumber +
                ", lastResult=" + lastResult +
                ", lastMotivation=" + lastNumberMotivation +
                ", botState=" + botState +
                ", role=" + role +
                '}';
    }
}
