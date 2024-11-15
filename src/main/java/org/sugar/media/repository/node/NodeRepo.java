package org.sugar.media.repository.node;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.sugar.media.model.UserModel;
import org.sugar.media.model.node.NodeModel;

@Repository
public interface NodeRepo extends JpaRepository<NodeModel, Long>, JpaSpecificationExecutor<NodeModel> {
}
