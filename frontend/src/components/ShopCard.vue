<template>
  <router-link class="shop-card" :to="`/shops/${shop.id}`">
    <div class="shop-cover" :style="{ background: shop.cover }">
      <span>{{ shop.category }}</span>
    </div>
    <div class="shop-body">
      <div class="shop-title-row">
        <h3>{{ shop.name }}</h3>
        <strong>{{ shop.rating }}</strong>
      </div>
      <p>{{ shop.description }}</p>
      <div class="shop-meta">
        <span>{{ shop.distance }} km</span>
        <span>人均 ¥{{ shop.avgPrice }}</span>
        <span>{{ shop.heat }} 热度</span>
      </div>
      <div class="tag-row">
        <el-tag v-for="tag in shop.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
      </div>
    </div>
  </router-link>
</template>

<script setup>
defineProps({
  shop: {
    type: Object,
    required: true,
  },
});
</script>
