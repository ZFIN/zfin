package org.zfin.framework.presentation;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.properties.ZfinDatabaseProperty;

import java.util.Arrays;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

@Log4j2
@RestController
@RequestMapping("/devtool/database-properties")
public class DatabasePropertyController {

    @GetMapping("/home")
    public ModelAndView homePage() {
        return new ModelAndView("dev-tools/database-properties");
    }

    @GetMapping("/key-names")
    public List<String> getKeyNames() {
        return Arrays.stream(ZfinDatabaseProperty.KeyName.values())
                .map(Enum::name)
                .toList();
    }

    @GetMapping
    public List<ZfinDatabaseProperty> getAll() {
        return getInfrastructureRepository().getAllZfinDatabaseProperties();
    }

    @PostMapping
    public ZfinDatabaseProperty save(@RequestBody ZfinDatabaseProperty property) {
        InfrastructureRepository repo = getInfrastructureRepository();
        HibernateUtil.createTransaction();
        repo.saveZfinDatabaseProperty(property);
        HibernateUtil.flushAndCommitCurrentSession();
        return property;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        InfrastructureRepository repo = getInfrastructureRepository();
        HibernateUtil.createTransaction();
        repo.deleteZfinDatabaseProperty(id);
        HibernateUtil.flushAndCommitCurrentSession();
    }

}
