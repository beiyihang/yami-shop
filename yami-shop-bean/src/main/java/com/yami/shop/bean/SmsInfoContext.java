package com.yami.shop.bean;

import java.util.ArrayList;
import java.util.List;

import com.yami.shop.bean.bo.SmsInfoBo;

import cn.hutool.core.collection.CollectionUtil;

/**
 * @author 北易航
 */
public class SmsInfoContext {

	/** The request holder. */
	private static ThreadLocal<List<SmsInfoBo>> smsInfoHolder = new ThreadLocal<List<SmsInfoBo>>();


	public static List<SmsInfoBo> get() {
		// 从smsInfoHolder中获取存储的SmsInfoBo列表
		List<SmsInfoBo> list = smsInfoHolder.get();

		// 检查列表是否为空
		if (CollectionUtil.isEmpty(list)) {
			// 如果列表为空，返回一个空的ArrayList
			return new ArrayList<>();
		}

		// 如果列表不为空，直接返回列表
		return smsInfoHolder.get();
	}


	public static void set(List<SmsInfoBo> smsInfoBos){
		 smsInfoHolder.set(smsInfoBos);
	}
	
	public static void put(SmsInfoBo smsInfoBo){
		List<SmsInfoBo> smsInfoBos = smsInfoHolder.get();
		if (CollectionUtil.isEmpty(smsInfoBos)) {
			smsInfoBos = new ArrayList<>();
		}
		smsInfoBos.add(smsInfoBo);
		smsInfoHolder.set(smsInfoBos);
	}
	
	public static void clean() {
		if (smsInfoHolder.get() != null) {
			smsInfoHolder.remove();
		}
	}
}