package org.sugar.media.repository.stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.model.stream.StreamPushModel;

/**
 * (MStreamPull)
 *
 * @author Tobin
 * @since 2024-11-27 12:42:56
 */
@Repository
public interface StreamPushRepo extends JpaRepository<StreamPushModel, Long>, JpaSpecificationExecutor<StreamPushModel> {


    StreamPushModel findAllByAppAndStreamAndTenantId(String app,String stream,Long tenantId);
    StreamPushModel findAllByRelevanceId(Long relevanceId);
}
