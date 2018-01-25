package com.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.models.Task;
import com.models.TaskStatus;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

	List<Task> findTop50ByOrderByIdDesc();

	Task findOneByStatus(TaskStatus status);

	List<Task> findAllByStatusOrderByIdAsc(TaskStatus status);

}
