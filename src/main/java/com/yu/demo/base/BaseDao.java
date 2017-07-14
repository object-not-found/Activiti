package com.yu.demo.base;

import java.io.Serializable;
import java.util.Map;

public interface BaseDao<T, ID extends Serializable> {
	int insert(T t);
	int update(T t);
	int delete(ID id);
	T select(Map<String, Object> map);
}
