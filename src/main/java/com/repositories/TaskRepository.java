package com.repositories;

import com.models.Handshake;
import com.models.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    Page<Task> findAllByOrderByIdDesc(Pageable pageable);

    Optional<Task> findByHandshake(Handshake handshake);

    @Transactional(readOnly = true)
    @Query("select t from Task t where t.id = (select min(t.id) from Task t where t.status = 0 and t.pulled = false)")
    Optional<Task> getNextTask();

    @Transactional
    @Modifying
    @Query("update Task t set t.pulled = true where t.id = ?1")
    void markAsPulled(Integer id);

    @Transactional
    @Modifying
    @Query("update Task t set t.status = 0, t.progress = 0, t.pulled = false where t.status = 0 or t.status = 1")
    void resetTasks();

    @Transactional
    @Modifying
    @Query("update Task t set t.status = 2, t.wifiPassword = ?2 where t.id = ?1")
    void reportTheResult(Integer id, String password);

    @Transactional
    @Modifying
    @Query("update Task t set t.status = 1 where t.id = ?1 and t.status = 0")
    void updateStatusToWorking(Integer id);

    @Transactional
    @Modifying
    @Query("update Task t set t.status = 2 where t.id = ?1")
    void updateStatusToCompleted(Integer id);

    @Transactional
    @Modifying
    @Query("update Task t set t.progress = t.progress+?2 where t.id = ?1")
    void addProgress(Integer id, BigDecimal progress);
}
