package com.gaekdam.gaekdambe.global.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
// Scan explicit mapper packages needed by the application to avoid scanning packages that contain JPA repository interfaces
@MapperScan(basePackages = {
        "com.gaekdam.gaekdambe.**.query.mapper",
        "com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.mapper",
        "com.gaekdam.gaekdambe.analytics_service.report.dashboard.query.mapper"
})
public class MyBatisConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);

        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:/mappers/**/*.xml")
        );

        return factoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
