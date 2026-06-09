<template>
  <section class="hero-section">
    <div class="hero-copy">
      <el-tag effect="dark" type="warning">今日城市灵感</el-tag>
      <h1>时光杂货铺</h1>
      <p>发现附近值得停留的小店，抢一张刚刚好的限时权益。</p>
      <div class="search-card">
        <el-input v-model="keyword" size="large" placeholder="搜索咖啡、书店、甜品或商圈">
          <template #append>
            <el-button type="primary" @click="$router.push('/shops')">搜索</el-button>
          </template>
        </el-input>
      </div>
      <div class="hero-stats">
        <span><strong>118</strong> 家精选小店</span>
        <span><strong>32</strong> 个今日权益</span>
        <span><strong>4.8</strong> 平均评分</span>
      </div>
    </div>
    <div class="hero-visual">
      <div class="ticket">
        <small>限时权益</small>
        <strong>咖啡双人券</strong>
        <span>剩余 64 份</span>
      </div>
    </div>
  </section>

  <section class="section">
    <div class="section-head">
      <h2>分类入口</h2>
      <router-link to="/shops">查看全部</router-link>
    </div>
    <div class="category-grid">
      <router-link v-for="item in categories" :key="item.name" class="category-card" to="/shops">
        <span :class="['category-dot', item.tone]"></span>
        <strong>{{ item.name }}</strong>
        <small>{{ item.count }} 家可逛</small>
      </router-link>
    </div>
  </section>

  <section class="section">
    <div class="section-head">
      <h2>推荐店铺</h2>
      <router-link to="/shops">按距离浏览</router-link>
    </div>
    <div class="shop-grid">
      <ShopCard v-for="shop in shops.slice(0, 3)" :key="shop.id" :shop="shop" />
    </div>
  </section>

  <section class="section deal-band">
    <div>
      <el-tag type="danger" effect="dark">限时权益</el-tag>
      <h2>今晚 20:00 前可用的城市小确幸</h2>
      <p>库存、倒计时和下单结果均使用 Mock 数据，适合展示秒杀链路产品形态。</p>
    </div>
    <router-link to="/deals">
      <el-button type="danger" size="large">去抢购</el-button>
    </router-link>
  </section>
</template>

<script setup>
import { ref } from "vue";
import { categories, shops } from "../mock";
import ShopCard from "../components/ShopCard.vue";

const keyword = ref("");
</script>
