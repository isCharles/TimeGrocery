package com.hmdp.mq;

import com.hmdp.entity.VoucherOrder;
import com.hmdp.service.IVoucherOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = "seckill_order_topic",
        consumerGroup = "hmdp_seckill_consumer"
)
public class VoucherOrderConsumer implements RocketMQListener<VoucherOrder> {
    @Resource
    private IVoucherOrderService voucherOrderService;

    @Override
    public void onMessage(VoucherOrder voucherOrder) {
        try {
            log.info("接收到秒杀订单消息，voucherOrder={}", voucherOrder);
            voucherOrderService.createVoucherOrder(voucherOrder);
            log.info("处理秒杀订单完成，voucherOrder={}", voucherOrder);
        } catch (Exception e) {
            log.error("处理秒杀订单失败，RocketMQ正在重试，voucherOrder={}, error={}", voucherOrder, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
