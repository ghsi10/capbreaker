package com.repositories;

import com.models.Task;
import com.models.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    Page<Task> findAllByOrderByIdDesc(Pageable pageable);

    List<Task> findAllByStatusOrderByIdAsc(TaskStatus status);

}
