package com.aicloud.module.iot.gateway.protocol.mqtt.handler.downstream;

import com.aicloud.module.iot.core.messagebus.core.IotMessageBus;
import com.aicloud.module.iot.core.mq.message.IotDeviceMessage;
import com.aicloud.module.iot.gateway.protocol.AbstractIotProtocolDownstreamSubscriber;
import com.aicloud.module.iot.gateway.protocol.mqtt.IotMqttProtocol;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 MQTT 协议：接收下行给设备的消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttDownstreamSubscriber extends AbstractIotProtocolDownstreamSubscriber {

    private final IotMqttDownstreamHandler downstreamHandler;

    public IotMqttDownstreamSubscriber(IotMqttProtocol protocol,
                                       IotMqttDownstreamHandler downstreamHandler,
                                       IotMessageBus messageBus) {
        super(protocol, messageBus);
        this.downstreamHandler = downstreamHandler;
    }

    @Override
    protected void handleMessage(IotDeviceMessage message) {
        downstreamHandler.handle(message);
    }

}
