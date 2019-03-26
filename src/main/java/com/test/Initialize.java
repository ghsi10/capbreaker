package com.test;

import com.models.Handshake;
import com.models.Task;
import com.models.User;
import com.models.UserRole;
import com.repositories.TaskRepository;
import com.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class Initialize {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    private void init() {
        Handshake hs = new Handshake("D0:0E:D9:5E:DD:2B");
        hs.setEssid("LeeHee2");
        hs.setSnonce("18:CF:5E:C4:EC:00");
        hs.setSnonce("66225D43A6F5836453B2FFABE843B3D45CB6E44B4165CA3BFF475D479D94290C");
        hs.setAnonce("E2276B7DC42EADEBF19A6F73467185D9262DBDE7D80F3DA12D469EBE919A51C8");
        hs.setEapol(
                "0103007502010A0000000000000000000166225D43A6F5836453B2FFABE843B3D45CB6E44B4165CA3BFF475D479D94290C000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001630140100000FAC040100000FAC040100000FAC023C00");
        hs.setKeyVersion("02");
        hs.setKeyMic("3A6C3A697FB1300B219CA5DB30E7732D");

        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Task task = new Task(hs);
            task.setEssid("testTask " + i);
            tasks.add(task);
        }

        User user1 = new User("uuser", "u123", UserRole.ROLE_USER, true);
        User user2 = new User("uadmin", "a123", UserRole.ROLE_ADMIN, true);

        taskRepository.save(tasks);
        userRepository.save(user1);
        userRepository.save(user2);
    }
}
