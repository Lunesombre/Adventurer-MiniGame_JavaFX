package game.adventurer.service;

import game.adventurer.common.Localizable;
import java.util.Locale;
import java.util.Set;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LocalizationService {


  private final Set<Localizable> localizableComponents;

  public LocalizationService(Set<Localizable> localizableComponents) {
    this.localizableComponents = localizableComponents;
  }


  public void registerLocalizable(@NonNull Localizable component) {
    localizableComponents.add(component);
  }

  public void updateAllComponents(Locale newLocale) {
    log.info("Mise à jour de tous les composants pour la nouvelle locale : {}", newLocale.getDisplayLanguage());
    for (Localizable component : localizableComponents) {
      log.info("J'update la classe localizable : {}", component.getClass().getSimpleName());
      component.updateLanguage(newLocale);
    }
  }
}
