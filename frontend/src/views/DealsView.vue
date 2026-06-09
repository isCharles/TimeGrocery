<template>
  <section class="page-head">
    <div>
      <el-tag type="danger">限时权益</el-tag>
      <h1>抢一份今天刚好用得上的优惠</h1>
    </div>
    <div class="countdown">距本场结束 {{ featured.endsIn }}</div>
  </section>

  <section class="deal-grid">
    <article v-for="deal in deals" :key="deal.id" class="deal-card">
      <div class="deal-top">
        <el-tag type="danger" effect="dark">{{ deal.badge }}</el-tag>
        <span>{{ deal.endsIn }}</span>
      </div>
      <h2>{{ deal.title }}</h2>
      <p>{{ deal.shop }}</p>
      <div class="price-row">
        <strong>¥{{ deal.price }}</strong>
        <span>¥{{ deal.originPrice }}</span>
      </div>
      <el-progress :percentage="stockPercent(deal)" :stroke-width="10" :show-text="false" />
      <small>剩余 {{ deal.stock }} / {{ deal.total }}</small>
      <el-button type="danger" size="large" @click="buy(deal)">立即抢购</el-button>
    </article>
  </section>
</template>

<script setup>
import { computed } from "vue";
import { ElMessage } from "element-plus";
import { deals } from "../mock";

const featured = computed(() => deals[0]);
const stockPercent = (deal) => Math.round((deal.stock / deal.total) * 100);

function buy(deal) {
  if (deal.stock <= 0) {
    ElMessage.error("库存不足，请看看其他权益");
    return;
  }
  deal.stock -= 1;
  ElMessage.success(`抢购成功，订单已进入异步处理：${deal.title}`);
}
</script>
