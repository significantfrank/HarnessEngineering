import type { PluginContext } from './types.js';
import { PluginManager } from './plugin-manager.js';

// 1. 构建插件上下文（注入 logger 与 config）
const ctx: PluginContext = {
  logger: {
    info(msg: string) {
      console.log(`[info] ${msg}`);
    },
  },
  config: {
    greet: { punctuation: '!!!' },
  },
};

async function main(): Promise<void> {
  const manager = new PluginManager(ctx);

  // 2. 动态加载插件（按需装配，互不耦合）
  await manager.load('./plugins/greet-plugin.js');
  await manager.load('./plugins/metrics-plugin.js');

  console.log('--- loaded plugins ---');
  console.log(manager.list());

  // 3. 执行所有已加载插件（宿主主动拉取）
  console.log('--- executeAll ---');
  manager.executeAll();

  // 4. 执行单个插件
  console.log('--- execute(greet) ---');
  manager.execute('greet');

  // 5. 卸载单个插件（触发 destroy）
  console.log('--- unload metrics ---');
  manager.unload('metrics');

  // 6. 卸载剩余插件（优雅关闭）
  console.log('--- unloadAll ---');
  manager.unloadAll();
}

main().catch((err) => {
  console.error('Fatal:', err);
});
