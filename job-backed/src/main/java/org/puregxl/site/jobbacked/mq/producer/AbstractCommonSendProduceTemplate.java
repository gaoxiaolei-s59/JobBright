package org.puregxl.site.jobbacked.mq.producer;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.puregxl.site.jobbacked.mq.base.BaseSendExtendDTO;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;


/**
 * 发送消息通用模块
 */
@Component
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractCommonSendProduceTemplate<T> {
    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 构建扩展消息体
     *
     * @return
     */
    protected abstract BaseSendExtendDTO buildBaseSendExtendDTO(T messageSendEvent);

    /**
     * 构建基础消息体
     *
     * @return
     */
    protected abstract Message<?> buildMessage(T messageSendEvent, BaseSendExtendDTO requestParam);


    /**
     * 消息事件通用发送
     *
     * @param messageSendEvent 消息发送事件
     * @return 消息发送返回结果
     */
    public SendResult sendMessage(T messageSendEvent) {
        //构建基础消息体
        BaseSendExtendDTO baseSendExtendDTO = buildBaseSendExtendDTO(messageSendEvent);
        SendResult sendResult;

        //执行发送消息逻辑
        try {
            StringBuilder destinationBuilder = StrUtil.builder().append(baseSendExtendDTO.getTopic());

            if (!StrUtil.isBlank(baseSendExtendDTO.getTag())) {
                destinationBuilder.append(":").append(baseSendExtendDTO.getTag());
            }
            //延迟消息为空 说明需要发送普通消息

            sendResult = rocketMQTemplate.syncSend(
                    destinationBuilder.toString(),
                    buildMessage(messageSendEvent, baseSendExtendDTO),
                    baseSendExtendDTO.getSentTimeout()
            );

            log.info("[生产者] {} - 发送结果：{}，消息ID：{}，消息Keys：{}", baseSendExtendDTO.getEventName(), sendResult.getSendStatus(), sendResult.getMsgId(), baseSendExtendDTO.getKeys());
            return sendResult;
        } catch (Exception e) {
            log.error("[生产者] {} - 消息发送失败，消息体：{}", baseSendExtendDTO.getEventName(), JSON.toJSONString(messageSendEvent), e);
            throw e;
        }
    }

}
