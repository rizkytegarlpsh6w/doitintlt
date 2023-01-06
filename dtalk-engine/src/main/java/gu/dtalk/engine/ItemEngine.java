package gu.dtalk.engine;

import gu.simplemq.redis.JedisPoolLazy;


/**
 * 消息驱动的菜单引擎redis实现<br>
 * @author guyadong
 * @deprecated replaced by {@link ItemEngineRedisImpl}
 */
public class ItemEngine extends ItemEngineRedisImpl{
	public ItemEngine(JedisPoolLazy pool) {
		super(pool);
	}
}
