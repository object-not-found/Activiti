package com.yu.demo.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@MapperScan("com.yu.demo.dao")
@EnableTransactionManagement
public class MybatisConfig {

	private static final String MAPPER_XML = "classpath*:com/yu/demo/mapper/*.xml";
	private static final String MAPPER_DOMAIN = "com.yu.demo.domain";
	@Bean(name = "sqlSessionFactory")
	public SqlSessionFactory sqlSessionFactory(DataSource ds) throws Exception{
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(ds);
		PathMatchingResourcePatternResolver pmr = new PathMatchingResourcePatternResolver();
		sqlSessionFactoryBean.setMapperLocations(pmr.getResources(MAPPER_XML));
		sqlSessionFactoryBean.setTypeAliasesPackage(MAPPER_DOMAIN);
		return sqlSessionFactoryBean.getObject();
	}
}

