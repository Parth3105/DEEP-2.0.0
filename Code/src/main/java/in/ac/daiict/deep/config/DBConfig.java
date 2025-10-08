package in.ac.daiict.deep.config;

import in.ac.daiict.deep.constant.database.DBConstants;
import in.ac.daiict.deep.service.InstanceNameService;
import in.ac.daiict.deep.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@Configuration
@Slf4j
public class DBConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManagerFactoryBuilder builder;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ApplicationContext context;

    private static LocalContainerEntityManagerFactoryBean emfBean;

    @Autowired
    private InstanceNameService instanceNameService;
    @Autowired
    private UserService userService;

    @PostConstruct
    public void initDefaultSchema() {
        runFlyway(DBConstants.WORKING_INSTANCE_NAME);
        createEntityManagerFactory(DBConstants.WORKING_INSTANCE_NAME);
    }
    public void runFlyway(String newSchemaName) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(newSchemaName)
                .locations("filesystem:C:/flyway-scripts")
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
    }

    public boolean createSchemaAndSwitch(String latestInstanceName, String workingInstance) {
        File dir=new File("./src/main/java/in/ac/daiict/deep/tmp/");
        if(dir.exists()) dir.delete();
        dir.mkdirs();
        try {
            instanceNameService.migrateInstances(dir);
            String sql = String.format("ALTER SCHEMA %s RENAME TO %s", workingInstance, latestInstanceName);
            jdbcTemplate.execute(sql);
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + workingInstance);
            runFlyway(workingInstance);
            createEntityManagerFactory(workingInstance);

            return true;
        } catch (Exception e) {
            log.error("Task to create new instance failed with error: {}", e.getCause().getMessage(), e.getCause());
            return false;
        }
    }

    private void createEntityManagerFactory(String schemaName) {
        Map<String, Object> props = new HashMap<>();
//        props.put("hibernate.default_schema", schemaName);
//        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        emfBean = builder
                .dataSource(dataSource)
                .packages("in.ac.daiict.deep.entity")
                .properties(props)
                .persistenceUnit("dynamic")
                .build();
        emfBean.afterPropertiesSet();
    }

    @Primary
    public EntityManagerFactory entityManagerFactory() {
        return emfBean.getObject();
    }

    @Primary
    public PlatformTransactionManager transactionManager() {
        return new JpaTransactionManager(entityManagerFactory());
    }
}