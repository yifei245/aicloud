package com.aicloud.module.bpm.service.message.dto;

import lombok.Data;
import java.util.Map;

@Data
public class BpmNoticeDeliveryDTO {
    private Long userId;
    private String templateCode;
    private Map<String, Object> templateParams;
}
