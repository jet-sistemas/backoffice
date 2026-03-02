package project.common.beans;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import project.common.database.seeds.SeederService;

@Startup
@ApplicationScoped
public class StartupBean {

  @Inject
  SeederService seederService;

  @PostConstruct
  void init() {
    seederService.seed();
  }
}