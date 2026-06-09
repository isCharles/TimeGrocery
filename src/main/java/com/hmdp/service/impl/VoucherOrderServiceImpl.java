package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;


    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private static final DefaultRedisScript<Long> SECKILL_ROLLBACK_SCRIPT;

    static {
        SECKILL_ROLLBACK_SCRIPT = new DefaultRedisScript<>();
        SECKILL_ROLLBACK_SCRIPT.setLocation(new ClassPathResource("seckill_rollback.lua"));
        SECKILL_ROLLBACK_SCRIPT.setResultType(Long.class);
    }

    @Override
    public Result seckillVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        long orderId = redisIdWorker.nextId("order");
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT, Collections.emptyList(),
                voucherId.toString(), userId.toString());
        int r = result.intValue();
        if (r != 0) {
            return Result.fail(r == 1 ? "库存不足！" : "你已经购买过了！");
        }
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        // MQ 投递失败时回滚 Redis 预扣库存和用户占位，避免库存不一致。
        try {
            SendResult sendResult = rocketMQTemplate.syncSend("seckill_order_topic", voucherOrder, 3000);
            if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
                rollBackSeckill(voucherId, userId);
                log.error("秒杀订单消息发送状态异常，sendResult: {}", sendResult);
                return Result.fail("系统繁忙，请稍后重试！");
            }
        } catch (Exception e) {
            rollBackSeckill(voucherId, userId);
            log.error("秒杀订单消息发送失败，已回滚 Redis，voucherId={}, userId={}", voucherId, userId, e);
            return Result.fail("系统繁忙，请稍后重试！");
        }
        return Result.ok(orderId);
    }

    private void rollBackSeckill(Long voucherId, Long userId) {
        Long rollbackResult = stringRedisTemplate.execute(SECKILL_ROLLBACK_SCRIPT, Collections.emptyList(),
                voucherId.toString(), userId.toString());
        if (rollbackResult == null) {
            log.error("回滚脚本没有返回可转换成 Long 的结果，说明脚本或 Java 的 setResultType(Long.class) 有问题");
            return;
        }
        if (rollbackResult.intValue() != 0) {
            log.error("用户似乎不在订单列表中，回滚库存失败，voucherId: {}, userId: {}", voucherId, userId);
            return;
        }
        log.info("成功回滚库存，voucherId: {}, userId: {}", voucherId, userId);
    }

    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();
        Integer count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        if (count > 0) {
            log.warn("重复购买！用户 {} 已经购买过 {} 了", userId, voucherId);
            return;
        }
        try {
            save(voucherOrder);
        } catch (DuplicateKeyException e) {
            log.warn("唯一索引拦截重复订单，voucherId: {}, userId: {}", voucherId, userId);
            return;
        }
        // 先判断是否MySQL唯一索引拦截，因为下面throw了会回滚，而上面的catch不会回滚
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .gt("stock", 0)
                .eq("voucher_id", voucherId)
                .update();
        if (!success) {
            log.error("MySQL库存扣减失败, voucherId: {}", voucherId);
            throw new IllegalStateException("库存不足！");
        }

    }

}
