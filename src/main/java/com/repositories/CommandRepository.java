package com.repositories;

import com.models.ScanCommand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommandRepository extends JpaRepository<ScanCommand, Integer> {

    List<ScanCommand> findAllByOrderByPriorityAsc();

}
