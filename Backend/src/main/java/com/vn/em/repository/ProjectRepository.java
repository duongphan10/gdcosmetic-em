package com.vn.em.repository;

import com.vn.em.domain.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {

    @Query(value = "SELECT p.* FROM projects p " +
            "WHERE " +
            "   (?1 = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', ?1, '%'))) " +
            "   AND (?2 IS NULL OR p.status_id = ?2) ", nativeQuery = true)
    Page<Project> getAll(String keyword, Integer statusId, Pageable pageable);

    @Query(value = "SELECT p.* FROM projects p " +
            "INNER JOIN users u ON p.project_manager_id = u.employee_id " +
            "WHERE " +
            "   (?1 = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', ?1, '%'))) " +
            "   AND u.id = ?2 " +
            "   AND (?3 IS NULL OR p.status_id = ?3) ", nativeQuery = true)
    Page<Project> getAllByUserId(String keyword, Integer userId, Integer statusId, Pageable pageable);

}
