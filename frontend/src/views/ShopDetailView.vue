<template>
  <section class="detail-hero" :style="{ background: shop.cover }">
    <router-link to="/shops" class="back-link">返回店铺列表</router-link>
    <div>
      <el-tag effect="dark">{{ shop.category }}</el-tag>
      <h1>{{ shop.name }}</h1>
      <p>{{ shop.description }}</p>
    </div>
  </section>

  <section class="detail-layout">
    <div class="detail-main">
      <div class="info-grid">
        <div><small>评分</small><strong>{{ shop.rating }}</strong></div>
        <div><small>距离</small><strong>{{ shop.distance }} km</strong></div>
        <div><small>人均</small><strong>¥{{ shop.avgPrice }}</strong></div>
      </div>
      <div class="panel">
        <h2>店铺信息</h2>
        <p>{{ shop.address }}</p>
        <div class="tag-row">
          <el-tag v-for="tag in shop.tags" :key="tag">{{ tag }}</el-tag>
        </div>
      </div>
      <div class="panel">
        <h2>热门评论</h2>
        <article v-for="comment in comments" :key="comment.user" class="comment">
          <strong>{{ comment.user }} · {{ comment.score }} 分</strong>
          <p>{{ comment.text }}</p>
        </article>
      </div>
    </div>
    <aside class="deal-side">
      <h2>限时权益入口</h2>
      <p>店铺权益、库存预扣和异步下单链路可以在这里形成完整产品闭环。</p>
      <router-link to="/deals">
        <el-button type="danger" size="large">查看可抢权益</el-button>
      </router-link>
    </aside>
  </section>
</template>

<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";
import { comments, shops } from "../mock";

const route = useRoute();
const shop = computed(() => shops.find((item) => item.id === Number(route.params.id)) ?? shops[0]);
</script>
