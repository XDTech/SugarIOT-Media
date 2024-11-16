package org.sugar.media.service.node;


import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.repository.node.NodeRepo;

@Service
public class NodeService {

    @Resource
    private NodeRepo nodeRepo;


    @Resource
    private StringRedisTemplate stringRedisTemplate;



    public NodeModel getNode(Long id){

        return this.nodeRepo.findById(id).get();
    }

    @Transactional
    public NodeModel createNode(NodeModel nodeModel){


        return this.nodeRepo.save(nodeModel);

    }

    @Transactional
    public void createMediaSync(NodeModel nodeModel){

    }
}
