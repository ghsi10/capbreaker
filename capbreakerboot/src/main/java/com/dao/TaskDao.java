package com.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.models.Task;
import com.models.TaskStatus;

public interface TaskDao extends JpaRepository<Task, Integer> {

	List<Task> findTop50ByOrderByIdDesc();

	Task findOneByStatus(TaskStatus status);

	List<Task> findAllByStatusOrderByIdAsc(TaskStatus status);

}
