import fs from "node:fs";
import { performance } from "node:perf_hooks";

const args = parseArgs(process.argv.slice(2));
const voucherId = args.voucherId ?? args.v ?? "16";
const url = args.url ?? `http://127.0.0.1:8080/api/voucher-order/seckill/${voucherId}`;
const tokenFile = args.tokens ?? "src/main/resources/tokens.txt";
const total = Number(args.total ?? 1000);
const concurrency = Number(args.concurrency ?? 100);
const timeoutMs = Number(args.timeout ?? 10000);

if (!Number.isInteger(total) || total <= 0) {
  throw new Error(`Invalid --total: ${args.total}`);
}
if (!Number.isInteger(concurrency) || concurrency <= 0) {
  throw new Error(`Invalid --concurrency: ${args.concurrency}`);
}

const tokens = fs
  .readFileSync(tokenFile, "utf8")
  .split(/\r?\n/)
  .map((line) => line.trim())
  .filter(Boolean)
  .slice(0, total);

if (tokens.length < total) {
  throw new Error(`Only ${tokens.length} tokens found in ${tokenFile}, but --total is ${total}`);
}

const stats = {
  ok: 0,
  businessFail: 0,
  httpFail: 0,
  networkFail: 0,
  byMessage: new Map(),
  latencies: [],
};

let nextIndex = 0;
const startedAt = performance.now();

console.log(`URL: ${url}`);
console.log(`tokens: ${tokens.length}`);
console.log(`concurrency: ${concurrency}`);

await Promise.all(
  Array.from({ length: Math.min(concurrency, tokens.length) }, async () => {
    while (true) {
      const index = nextIndex++;
      if (index >= tokens.length) {
        return;
      }
      await sendOne(tokens[index], index);
    }
  }),
);

const elapsedMs = performance.now() - startedAt;
stats.latencies.sort((a, b) => a - b);

console.log("");
console.log("Result");
console.log(`requests: ${tokens.length}`);
console.log(`ok: ${stats.ok}`);
console.log(`businessFail: ${stats.businessFail}`);
console.log(`httpFail: ${stats.httpFail}`);
console.log(`networkFail: ${stats.networkFail}`);
console.log(`elapsedMs: ${elapsedMs.toFixed(0)}`);
console.log(`qps: ${(tokens.length / (elapsedMs / 1000)).toFixed(2)}`);
console.log(`avgMs: ${average(stats.latencies).toFixed(1)}`);
console.log(`p50Ms: ${percentile(stats.latencies, 0.5).toFixed(1)}`);
console.log(`p95Ms: ${percentile(stats.latencies, 0.95).toFixed(1)}`);
console.log(`p99Ms: ${percentile(stats.latencies, 0.99).toFixed(1)}`);

console.log("");
console.log("Messages");
for (const [message, count] of [...stats.byMessage.entries()].sort((a, b) => b[1] - a[1])) {
  console.log(`${count}\t${message}`);
}

async function sendOne(token, index) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), timeoutMs);
  const started = performance.now();

  try {
    const response = await fetch(url, {
      method: "POST",
      headers: {
        authorization: token,
      },
      signal: controller.signal,
    });

    const text = await response.text();
    const latency = performance.now() - started;
    stats.latencies.push(latency);

    if (!response.ok) {
      stats.httpFail++;
      addMessage(`HTTP ${response.status}: ${text.slice(0, 120)}`);
      return;
    }

    let body;
    try {
      body = JSON.parse(text);
    } catch {
      stats.httpFail++;
      addMessage(`Invalid JSON: ${text.slice(0, 120)}`);
      return;
    }

    if (body.success) {
      stats.ok++;
      return;
    }

    stats.businessFail++;
    addMessage(body.errorMsg ?? body.message ?? JSON.stringify(body));
  } catch (error) {
    const latency = performance.now() - started;
    stats.latencies.push(latency);
    stats.networkFail++;
    addMessage(`${error.name}: ${error.message}`);
    if (index < 5) {
      console.error(`request ${index} failed:`, error.message);
    }
  } finally {
    clearTimeout(timeout);
  }
}

function addMessage(message) {
  stats.byMessage.set(message, (stats.byMessage.get(message) ?? 0) + 1);
}

function average(values) {
  if (values.length === 0) {
    return 0;
  }
  return values.reduce((sum, value) => sum + value, 0) / values.length;
}

function percentile(values, p) {
  if (values.length === 0) {
    return 0;
  }
  const index = Math.min(values.length - 1, Math.ceil(values.length * p) - 1);
  return values[index];
}

function parseArgs(argv) {
  const parsed = {};
  for (let i = 0; i < argv.length; i++) {
    const arg = argv[i];
    if (!arg.startsWith("--")) {
      continue;
    }
    const [rawKey, inlineValue] = arg.slice(2).split("=", 2);
    parsed[rawKey] = inlineValue ?? argv[++i];
  }
  return parsed;
}
