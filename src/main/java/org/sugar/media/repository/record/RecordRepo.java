package org.sugar.media.repository.record;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.sugar.media.enums.MediaServerEnum;
import org.sugar.media.model.record.RecordModel;


@Repository
public interface RecordRepo extends JpaRepository<RecordModel, Long>, JpaSpecificationExecutor<RecordModel> {


}
