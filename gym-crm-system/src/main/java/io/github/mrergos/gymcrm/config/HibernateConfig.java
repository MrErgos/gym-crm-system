package io.github.mrergos.gymcrm.config;

import io.github.mrergos.gymcrm.entity.*;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.hibernate.HibernateTransactionManager;
import org.springframework.orm.jpa.hibernate.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class HibernateConfig {
    private static final Logger log = LoggerFactory.getLogger(HibernateConfig.class);

    @Value("${hibernate.hbm2ddl.auto}")
    private String hbm2ddlAuto;
    @Value("${hibernate.show_sql}")
    private String showSql;
    @Value("${hibernate.format_sql}")
    private String formatSql;

    @Bean
    public LocalSessionFactoryBean sessionFactoryBean(DataSource dataSource) {
        log.info("Building Hibernate SessionFactory via LocalSessionFactoryBean");

        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setAnnotatedClasses(
                User.class,
                Trainee.class,
                Trainer.class,
                Training.class,
                TrainingType.class
        );

        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        properties.setProperty("hibernate.show_sql", showSql);
        properties.setProperty("hibernate.format_sql", formatSql);
        sessionFactoryBean.setHibernateProperties(properties);

        return sessionFactoryBean;
    }

    @Bean
    public SessionFactory sessionFactory(LocalSessionFactoryBean sessionFactoryBean) {
        SessionFactory sessionFactory = sessionFactoryBean.getObject();
        if (sessionFactory == null) {
            log.error("Failed to build SessionFactory: LocalSessionFactoryBean returned null");
            throw new IllegalStateException("SessionFactory could not be created");
        }
        return sessionFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory);
        return transactionManager;
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

}
