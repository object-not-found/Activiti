package com.yu.demo.base;

import java.io.Serializable;
import java.util.Map; 
public abstract class AbstractBaseService<T, ID extends Serializable> implements BaseService<T, ID> {
	protected BaseDao<T, ID> baseDao;
	
	public abstract void setBaseDao();

	@Override
	public T select(Map<String, Object> map) {
		this.setBaseDao();
		return baseDao.select(map);
	}

	@Override
	public int insert(T t) {
		this.setBaseDao();
		return baseDao.insert(t);
	}

	@Override
	public int delete(ID id) {
		this.setBaseDao();
		return baseDao.delete(id);
	}

	@Override
	public int update(T t) {
		this.setBaseDao();
		return baseDao.update(t);
	}
	
	
}
