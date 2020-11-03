package com.tanaguru.service.impl;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.entity.membership.user.AppAuthority;
import com.tanaguru.domain.entity.membership.user.AppRole;
import com.tanaguru.domain.exception.CustomIllegalStateException;
import com.tanaguru.repository.AppRoleRepository;
import com.tanaguru.service.AppRoleService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppRoleServiceImpl implements AppRoleService {
    private Map<EAppRole, AppRole> appRoleMap = new EnumMap<>(EAppRole.class);
    private Map<EAppRole, Collection<String>> appAuthorityByAppRoleMap = new EnumMap<>(EAppRole.class);

    private final AppRoleRepository appRoleRepository;

    public AppRoleServiceImpl(AppRoleRepository appRoleRepository) {
        this.appRoleRepository = appRoleRepository;
    }

    @PostConstruct
    public void initRoleMap() {
        for (EAppRole role : EAppRole.values()) {
            AppRole appRole = appRoleRepository.findByName(role)
                    .orElseThrow(() -> new CustomIllegalStateException(CustomError.APP_ROLE_NOT_FOUND, role.toString()));
            appRoleMap.put(role, appRole);

            appAuthorityByAppRoleMap.put(
                    role,
                    appRole.getAuthorities()
                            .stream().map(AppAuthority::getName).collect(Collectors.toList()));
        }
    }

    public Optional<AppRole> getAppRole(EAppRole appRole) {
        return Optional.ofNullable(appRoleMap.get(appRole));
    }
    public Collection<String> getAppAuthorityByAppRole(EAppRole appRole){
        return appAuthorityByAppRoleMap.get(appRole);
    }

}
