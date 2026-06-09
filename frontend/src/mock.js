export const categories = [
  { name: "咖啡", tone: "amber", count: 24 },
  { name: "餐食", tone: "green", count: 36 },
  { name: "书店", tone: "blue", count: 12 },
  { name: "甜品", tone: "rose", count: 18 },
  { name: "生活服务", tone: "gray", count: 29 },
];

export const shops = [
  {
    id: 1,
    name: "梧桐树下咖啡",
    category: "咖啡",
    rating: 4.8,
    distance: 0.6,
    avgPrice: 42,
    heat: 982,
    address: "新河路 88 号一层",
    tags: ["手冲", "安静办公", "宠物友好"],
    cover: "linear-gradient(135deg, #6d4c41, #d7a86e)",
    description: "社区转角处的精品咖啡小店，下午有稳定的自然光和轻音乐。",
  },
  {
    id: 2,
    name: "南巷面包事务所",
    category: "餐食",
    rating: 4.7,
    distance: 1.2,
    avgPrice: 36,
    heat: 860,
    address: "南巷 16 号",
    tags: ["早午餐", "现烤贝果", "可外带"],
    cover: "linear-gradient(135deg, #9f6b3e, #f1c27d)",
    description: "主打开放式厨房和当日现烤面包，适合周末慢早餐。",
  },
  {
    id: 3,
    name: "纸页之间独立书店",
    category: "书店",
    rating: 4.9,
    distance: 1.8,
    avgPrice: 58,
    heat: 734,
    address: "青石街 21 号二层",
    tags: ["选书好", "读书会", "咖啡角"],
    cover: "linear-gradient(135deg, #355c7d, #a7c5bd)",
    description: "以城市、人文和设计类图书为主，每周有小型沙龙。",
  },
  {
    id: 4,
    name: "云朵甜品铺",
    category: "甜品",
    rating: 4.6,
    distance: 2.4,
    avgPrice: 31,
    heat: 690,
    address: "花园路 120 号",
    tags: ["低糖", "季节限定", "下午茶"],
    cover: "linear-gradient(135deg, #e85d75, #ffd6a5)",
    description: "小份甜品和季节水果塔，适合两三人轻量分享。",
  },
];

export const deals = [
  {
    id: 101,
    title: "咖啡双人分享券",
    shop: "梧桐树下咖啡",
    price: 39,
    originPrice: 68,
    stock: 64,
    total: 100,
    endsIn: "02:18:42",
    badge: "今日热抢",
  },
  {
    id: 102,
    title: "早午餐 8 折权益",
    shop: "南巷面包事务所",
    price: 19,
    originPrice: 29,
    stock: 27,
    total: 80,
    endsIn: "05:42:10",
    badge: "限量",
  },
  {
    id: 103,
    title: "书店咖啡阅读套票",
    shop: "纸页之间独立书店",
    price: 49,
    originPrice: 76,
    stock: 18,
    total: 60,
    endsIn: "01:06:33",
    badge: "低库存",
  },
];

export const comments = [
  { user: "林小满", text: "拿铁稳定，靠窗座位很舒服，适合下午整理周计划。", score: 5 },
  { user: "阿澈", text: "店员会认真介绍豆子，甜品不腻，整体体验很松弛。", score: 5 },
  { user: "橙子", text: "周末人稍多，建议工作日下午去。", score: 4 },
];

export const orders = [
  { name: "咖啡双人分享券", status: "待使用", date: "2026-06-08" },
  { name: "书店咖啡阅读套票", status: "已完成", date: "2026-06-02" },
];
