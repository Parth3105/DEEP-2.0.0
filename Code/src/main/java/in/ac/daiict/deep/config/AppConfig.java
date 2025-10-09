package in.ac.daiict.deep.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.config")
@Setter
@Getter
public class AppConfig {
    private String path;
}
