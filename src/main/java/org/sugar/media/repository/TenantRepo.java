package org.sugar.media.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.sugar.media.model.TenantModel;

/**
 * Date:2024/12/18 18:15:34
 * Author：Tobin
 * Description:
 */
public interface TenantRepo  extends JpaRepository<TenantModel, Long>, JpaSpecificationExecutor<TenantModel> {


}
