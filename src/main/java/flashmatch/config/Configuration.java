package flashmatch.config;

import flashmatch.entity.Interest;
import flashmatch.entity.User;
import flashmatch.repo.InterestRepository;
import flashmatch.repo.UserRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackageClasses = {InterestRepository.class, UserRepository.class})
@EntityScan(basePackageClasses = {User.class, Interest.class})
@ComponentScan(basePackages = {"flashmatch"})
@EnableAutoConfiguration
@org.springframework.context.annotation.Configuration
public class Configuration {
}
