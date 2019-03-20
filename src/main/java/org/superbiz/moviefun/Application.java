package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials(@Value("${VCAP_SERVICES}") String vcapServicesJson) {
        return new DatabaseServiceCredentials(vcapServicesJson);
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariConfig props = new HikariConfig();
        props.setJdbcUrl( serviceCredentials.jdbcUrl("albums-mysql") );
        return new HikariDataSource(props);
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariConfig props = new HikariConfig();
        props.setJdbcUrl( serviceCredentials.jdbcUrl("movies-mysql") );
        return new HikariDataSource(props);
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabase(Database.MYSQL);
        adapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        adapter.setGenerateDdl(true);
        return adapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean albumsEntityManagerFactory(
            DataSource albumsDataSource,
            HibernateJpaVendorAdapter adapter
    ) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(albumsDataSource);
        factory.setJpaVendorAdapter(adapter);
        factory.setPackagesToScan("org.superbiz.moviefun.albums");
        factory.setPersistenceUnitName("albums");
        return factory;

    }

    @Bean
    public LocalContainerEntityManagerFactoryBean moviesEntityManagerFactory(
            DataSource moviesDataSource,
            HibernateJpaVendorAdapter adapter
    ) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(moviesDataSource);
        factory.setJpaVendorAdapter(adapter);
        factory.setPackagesToScan("org.superbiz.moviefun.movies");
        factory.setPersistenceUnitName("movies");
        return factory;

    }

    @Bean
    public PlatformTransactionManager albumsTransactionManager(EntityManagerFactory albumsEntityManagerFactory) {
        return new JpaTransactionManager(albumsEntityManagerFactory);
    }

    @Bean
    public PlatformTransactionManager moviesTransactionManager(EntityManagerFactory moviesEntityManagerFactory) {
        return new JpaTransactionManager(moviesEntityManagerFactory);
    }



}
