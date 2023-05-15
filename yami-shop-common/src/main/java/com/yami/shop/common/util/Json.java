

package com.yami.shop.common.util;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author 北易航
 */
@Slf4j
public class Json {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 如果为空则不输出
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        // 对于空的对象转json的时候不抛出错误
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 禁用序列化日期为timestamps
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 禁用遇到未知属性抛出异常
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 取消对非ASCII字符的转码
        objectMapper.configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), false);
        
    }

	/**
	 * 对象转json
	 * @param object
	 * @return
	 */
	public static String toJsonString(Object object) {
		try {
			// 将对象转换为 JSON 字符串
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			// 如果转换过程中发生异常，则记录错误日志
			log.error("对象转json错误：", e);
		}
		// 返回 null 表示转换失败
		return null;
	}


	/**
	 * json转换换成对象
	 * @param json
	 * @param clazz
	 * @return
	 */
	public static <T> T parseObject(String json, Class<T> clazz) {
		T result = null;
		try {
			// 将 JSON 字符串转换为指定类型的对象
			result = objectMapper.readValue(json, clazz);
		} catch (Exception e) {
			// 如果转换过程中发生异常，则记录错误日志
			log.error("对象转json错误：", e);
		}
		// 返回转换后的对象，如果转换失败则为 null
		return result;
	}


	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}


	/**
	 * 	 * https://stackoverflow.com/questions/6349421/how-to-use-jackson-to-deserialise-an-array-of-objects
	 * 	 * List<MyClass> myObjects = Arrays.asList(mapper.readValue(json, MyClass[].class))
	 * 	 * works up to 10 time faster than TypeRefence.
	 * @return
	 */
	public static <T> List<T> parseArray(String json, Class<T[]> clazz){
		T[] result = null;
		try {
			result = objectMapper.readValue(json, clazz);
		} catch (Exception e) {
			log.error("Json转换错误：", e);
		}
		if (result == null) {
			return Collections.emptyList();
		}
		return Arrays.asList(result);
	}


	/**
	 * 转换成json节点，即map
	 * @param jsonStr
	 * @return
	 */
	public static JsonNode parseJson(String jsonStr) {
		JsonNode jsonNode = null;
		try {
			jsonNode = objectMapper.readTree(jsonStr);
		} catch (Exception e) {
			log.error("Json转换错误：", e);
		}
		return jsonNode;
	}
}
