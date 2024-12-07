package org.sugar.media.repository.node;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.sugar.media.enums.MediaServerEnum;
import org.sugar.media.model.UserModel;
import org.sugar.media.model.node.NodeModel;

import java.util.Date;
import java.util.List;

@Repository
public interface NodeRepo extends JpaRepository<NodeModel, Long>, JpaSpecificationExecutor<NodeModel> {

    List<NodeModel> findAllByZidAndTypesOrderByIdDesc(Long zid, MediaServerEnum types);
    List<NodeModel> findAllByIdIn(List<Long> ids);


    @Modifying
    @Transactional
    @Query("update NodeModel m set m.syncConfigTime=?2 where  m.id=?1")
    void updateConfigTimeById(Long id, Date syncConfigTime);


    @Modifying
    @Transactional
    @Query("update NodeModel m set m.syncHeartbeatTime=?2 where  m.id=?1")
    void updateHeartbeatTimeById(Long id, Date syncHeartbeatTime);
}
