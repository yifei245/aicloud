package com.aicloud.module.oa.dal.mysql.mail;

import com.aicloud.framework.mybatis.core.mapper.BaseMapperX;
import com.aicloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.aicloud.module.oa.dal.dataobject.mail.OaMailProviderDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * OA 企业邮箱服务配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaMailProviderMapper extends BaseMapperX<OaMailProviderDO> {

    default List<OaMailProviderDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<OaMailProviderDO>()
                .eqIfPresent(OaMailProviderDO::getStatus, status)
                .orderByAsc(OaMailProviderDO::getId));
    }

}
