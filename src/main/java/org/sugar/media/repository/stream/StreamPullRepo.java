package org.sugar.media.repository.stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.sugar.media.model.stream.StreamPullModel;

/**
 * (MStreamPull)
 *
 * @author Tobin
 * @since 2024-11-27 12:42:56
 */
@Repository
public interface StreamPullRepo extends JpaRepository<StreamPullModel, Long>, JpaSpecificationExecutor<StreamPullModel> {


    StreamPullModel findAllByAppAndStream(String app,String stream);
}
