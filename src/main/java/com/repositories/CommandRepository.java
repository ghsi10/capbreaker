package com.repositories;

import com.models.ScanCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandRepository extends JpaRepository<ScanCommand, Integer> {

    List<ScanCommand> findAllByOrderByPriorityAsc();

}
