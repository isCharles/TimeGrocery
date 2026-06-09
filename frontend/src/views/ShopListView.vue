<template>
  <section class="page-head">
    <div>
      <el-tag>附近推荐</el-tag>
      <h1>找到今天想去的小店</h1>
    </div>
    <el-segmented v-model="sortBy" :options="sortOptions" />
  </section>

  <div class="filter-row">
    <el-check-tag v-for="item in categories" :key="item.name" :checked="activeCategory === item.name" @change="activeCategory = item.name">
      {{ item.name }}
    </el-check-tag>
    <el-button link @click="activeCategory = '全部'">全部</el-button>
  </div>

  <section class="shop-grid">
    <ShopCard v-for="shop in sortedShops" :key="shop.id" :shop="shop" />
  </section>
</template>

<script setup>
import { computed, ref } from "vue";
import { categories, shops } from "../mock";
import ShopCard from "../components/ShopCard.vue";

const sortBy = ref("距离优先");
const activeCategory = ref("全部");
const sortOptions = ["距离优先", "热度优先"];

const sortedShops = computed(() => {
  const list = shops.filter((shop) => activeCategory.value === "全部" || shop.category === activeCategory.value);
  return [...list].sort((a, b) => (sortBy.value === "距离优先" ? a.distance - b.distance : b.heat - a.heat));
});
</script>
