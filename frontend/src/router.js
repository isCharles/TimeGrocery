import { createRouter, createWebHistory } from "vue-router";
import HomeView from "./views/HomeView.vue";
import ShopListView from "./views/ShopListView.vue";
import ShopDetailView from "./views/ShopDetailView.vue";
import DealsView from "./views/DealsView.vue";
import ProfileView from "./views/ProfileView.vue";

const routes = [
  { path: "/", name: "home", component: HomeView },
  { path: "/shops", name: "shops", component: ShopListView },
  { path: "/shops/:id", name: "shop-detail", component: ShopDetailView },
  { path: "/deals", name: "deals", component: DealsView },
  { path: "/profile", name: "profile", component: ProfileView },
];

export default createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
});
