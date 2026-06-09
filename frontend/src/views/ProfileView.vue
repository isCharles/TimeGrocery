<template>
  <section class="profile-head">
    <div>
      <el-tag type="success">用户中心</el-tag>
      <h1>我的城市足迹</h1>
      <p>订单、签到、浏览记录和 UV 活跃度集中展示。</p>
    </div>
    <div class="avatar">顾</div>
  </section>

  <section class="metric-grid">
    <div class="metric-card"><small>连续签到</small><strong>12 天</strong></div>
    <div class="metric-card"><small>本月浏览</small><strong>86 次</strong></div>
    <div class="metric-card"><small>UV 活跃度</small><strong>73%</strong></div>
    <div class="metric-card"><small>已省金额</small><strong>¥218</strong></div>
  </section>

  <section class="profile-layout">
    <div class="panel">
      <h2>我的订单</h2>
      <div v-for="order in orders" :key="order.name" class="order-row">
        <span>{{ order.name }}</span>
        <small>{{ order.date }} · {{ order.status }}</small>
      </div>
    </div>
    <div class="panel">
      <h2>我的签到</h2>
      <div class="checkin-grid">
        <span v-for="day in 28" :key="day" :class="{ active: day <= 18 }">{{ day }}</span>
      </div>
    </div>
    <div class="panel">
      <h2>浏览记录</h2>
      <div v-for="shop in shops.slice(0, 3)" :key="shop.id" class="history-row">
        <strong>{{ shop.name }}</strong>
        <small>{{ shop.category }} · {{ shop.distance }} km</small>
      </div>
    </div>
  </section>
</template>

<script setup>
import { orders, shops } from "../mock";
</script>
