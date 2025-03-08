package org.sugar.media.repository.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.sugar.media.model.system.DepartmentModel;

import java.util.List;

/**
 * (Department)
 *
 * @author Tobin
 * @since 2025-01-22 21:32:55
 */
@Repository
public interface DepartmentRepo extends JpaRepository<DepartmentModel, Long>, JpaSpecificationExecutor<DepartmentModel> {


    /**
     * 递归查询
     * @param id
     * @return
     */
    @Query(value = """
            WITH RECURSIVE department_tree AS (
                SELECT d.id, d.name, d.parent_id
                FROM department d
                WHERE d.id = ?1
                UNION ALL
                SELECT d.id, d.name, d.parent_id
                FROM department d
                INNER JOIN department_tree dt ON d.parent_id = dt.id
            )
            SELECT * FROM department_tree
            """, nativeQuery = true)
    List<DepartmentModel> findAllSubDepartments(Long id);
}
