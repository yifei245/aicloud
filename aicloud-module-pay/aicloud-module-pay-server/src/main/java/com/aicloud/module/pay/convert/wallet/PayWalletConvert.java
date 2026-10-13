package com.aicloud.module.pay.convert.wallet;

import com.aicloud.framework.common.pojo.PageResult;
import com.aicloud.module.pay.controller.admin.wallet.vo.wallet.PayWalletRespVO;
import com.aicloud.module.pay.controller.app.wallet.vo.wallet.AppPayWalletRespVO;
import com.aicloud.module.pay.dal.dataobject.wallet.PayWalletDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PayWalletConvert {

    PayWalletConvert INSTANCE = Mappers.getMapper(PayWalletConvert.class);

    AppPayWalletRespVO convert(PayWalletDO bean);

    PayWalletRespVO convert02(PayWalletDO bean);

    PageResult<PayWalletRespVO> convertPage(PageResult<PayWalletDO> page);

}
