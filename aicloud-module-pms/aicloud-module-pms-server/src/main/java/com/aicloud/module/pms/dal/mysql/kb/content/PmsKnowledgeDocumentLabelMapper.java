package com.aicloud.module.pms.dal.mysql.kb.content;

import com.aicloud.framework.mybatis.core.mapper.BaseMapperX;
import com.aicloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.aicloud.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentLabelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PmsKnowledgeDocumentLabelMapper extends BaseMapperX<PmsKnowledgeDocumentLabelDO> {

    default List<PmsKnowledgeDocumentLabelDO> selectList() {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeDocumentLabelDO>()
                .orderByAsc(PmsKnowledgeDocumentLabelDO::getCreateTime)
                .orderByAsc(PmsKnowledgeDocumentLabelDO::getId));
    }

}
