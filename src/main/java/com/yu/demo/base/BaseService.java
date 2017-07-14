package com.yu.demo.base;

import java.io.Serializable;
import java.util.Map;

public interface BaseService<T, ID extends Serializable> {
	T select(Map<String, Object> map);
	int insert(T t);
	int delete(ID id);
	int update(T t);
}
