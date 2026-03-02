package backoffice.common.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ApplicationScoped
public class Beans {

  @Inject
  CurrentVertxRequest request;

  @Produces
  public ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    return modelMapper;
  }

  // @Produces
  // public Messages messages() {
  // return Messages.builder().defaultLang("en").request(request).build();
  // }

  // @Produces
  // public Gson gson() {
  // return new GsonBuilder()
  // .registerTypeAdapter(LocalDate.class, new GsonLocalDateTypeAdapter())
  // .registerTypeAdapter(LocalTime.class, new GsonLocalTimeTypeAdapter())
  // .setExclusionStrategies(new ExcludeFieldStrategy())
  // .create();
  // }
}
