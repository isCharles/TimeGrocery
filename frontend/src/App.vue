<template>
  <el-config-provider>
    <div class="app-shell">
      <header class="topbar">
        <router-link class="brand" to="/">
          <span class="brand-mark">时</span>
          <span>
            <strong>时光杂货铺</strong>
            <small>城市小店与限时权益</small>
          </span>
        </router-link>
        <nav>
          <router-link to="/">首页</router-link>
          <router-link to="/shops">店铺</router-link>
          <router-link to="/deals">限时权益</router-link>
          <router-link to="/profile">我的</router-link>
        </nav>
      </header>
      <main>
        <router-view />
      </main>
    </div>
  </el-config-provider>
</template>
