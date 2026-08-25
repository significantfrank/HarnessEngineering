import { HookBusImpl } from './hook-bus.js';
import { PluginManager } from './plugin-manager.js';

// 1. 构建钩子总线 + 上下文（logger + config）
const bus = new HookBusImpl();
const manager = new PluginManager(bus, {
  logger: {
    info(msg: string) {
      console.log(`[info] ${msg}`);
    },
  },
  config: {
    lint: { strict: true },
  },
});

async function main(): Promise<void> {
  // 2. 加载插件 → apply(ctx) 挂载回调（ctx.hooks 已内嵌）
  await manager.load('./plugins/lint-plugin.js');
  await manager.load('./plugins/metrics-plugin.js');

  console.log('--- loaded plugins ---');
  console.log(manager.list());

  console.log('--- listenerCount ---');
  console.log('beforeCompile:', bus.listenerCount('beforeCompile')); // 2
  console.log('afterCompile:', bus.listenerCount('afterCompile')); // 1

  // 3. 宿主编译流程：在恰当时机 emit（推模式核心）
  console.log('--- emit beforeCompile ---');
  await bus.emit('beforeCompile', { entry: './src/index.ts' });

  console.log('--- (host compiles...) ---');

  console.log('--- emit afterCompile ---');
  await bus.emit('afterCompile', { output: 'dist/bundle.js', errors: 0 });

  // 4. 卸载插件（插件取消订阅，bus 清空）
  console.log('--- unload metrics ---');
  manager.unload('metrics');
  console.log('afterCompile listeners after unload:', bus.listenerCount('afterCompile')); // 0

  // 5. 卸载剩余
  console.log('--- unloadAll ---');
  manager.unloadAll();
  console.log('beforeCompile listeners after unloadAll:', bus.listenerCount('beforeCompile')); // 0
}

main().catch((err) => {
  console.error('Fatal:', err);
});
