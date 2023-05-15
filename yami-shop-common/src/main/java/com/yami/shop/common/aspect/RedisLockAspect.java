package com.yami.shop.common.aspect;

import cn.hutool.core.util.StrUtil;
import com.yami.shop.common.annotation.RedisLock;
import com.yami.shop.common.util.SpelUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author 北易航
 */
@Aspect
@Component
public class RedisLockAspect {

	@Autowired
	private RedissonClient redissonClient;

	private static final String REDISSON_LOCK_PREFIX = "redisson_lock:";

	@Around("@annotation(redisLock)")
	public Object around(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {
		// 获取注解中的SpEL表达式和锁名称
		String spel = redisLock.key();
		String lockName = redisLock.lockName();

		// 获取Redisson的锁对象
		RLock rLock = redissonClient.getLock(getRedisKey(joinPoint, lockName, spel));

		// 获取锁并设置过期时间
		rLock.lock(redisLock.expire(), redisLock.timeUnit());

		Object result = null;
		try {
			// 执行被拦截的方法
			result = joinPoint.proceed();

		} finally {
			// 释放锁
			rLock.unlock();
		}
		return result;
	}

	/**
	 * 将spel表达式转换为字符串
	 * @param joinPoint 切点
	 * @return redisKey
	 */
	private String getRedisKey(ProceedingJoinPoint joinPoint, String lockName, String spel) {
		// 获取方法的签名信息
		Signature signature = joinPoint.getSignature();
		MethodSignature methodSignature = (MethodSignature) signature;
		Method targetMethod = methodSignature.getMethod();

		// 获取目标对象、方法参数等信息
		Object target = joinPoint.getTarget();
		Object[] arguments = joinPoint.getArgs();

		// 构建Redis锁的键值
		// 键值格式为：REDIS_LOCK_PREFIX + 锁名称 + 冒号 + SpEL解析结果
		return REDISSON_LOCK_PREFIX + lockName + StrUtil.COLON + SpelUtil.parse(target, spel, targetMethod, arguments);
	}

}
