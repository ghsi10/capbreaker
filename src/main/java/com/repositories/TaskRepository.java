package com.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.models.Task;
import com.models.TaskStatus;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

	Page<Task> findAllByOrderByIdDesc(Pageable pageable);

	Task findOneByStatus(TaskStatus status);

	List<Task> findAllByStatusOrderByIdAsc(TaskStatus status);

}
