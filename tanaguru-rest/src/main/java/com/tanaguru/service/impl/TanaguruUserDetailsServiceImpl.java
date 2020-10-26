package com.tanaguru.service.impl;

import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.membership.Act;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.repository.*;
import com.tanaguru.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tanaguru.domain.constant.ProjectAuthorityName.DELETE_AUDIT;
import static com.tanaguru.domain.constant.ProjectAuthorityName.SHOW_AUDIT;

@Transactional
@Service
@Primary
public class TanaguruUserDetailsServiceImpl implements TanaguruUserDetailsService {

    private final ContractService contractService;
    private final ProjectService projectService;
    private final ContractRepository contractRepository;
    private final ProjectRepository projectRepository;
    private final AuditService auditService;
    private final AuditRepository auditRepository;
    private final AuditSchedulerService auditSchedulerService;
    private final UserRepository userRepository;
    private final AppRoleRepository appRoleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ActRepository actRepository;

    @Autowired
    public TanaguruUserDetailsServiceImpl(ContractService contractService, ProjectService projectService, ContractRepository contractRepository, ProjectRepository projectRepository, AuditService auditService, AuditRepository auditRepository, AuditSchedulerService auditSchedulerService, UserRepository userRepository, AppRoleRepository appRoleRepository, BCryptPasswordEncoder bCryptPasswordEncoder, ActRepository actRepository) {
        this.contractService = contractService;
        this.projectService = projectService;
        this.contractRepository = contractRepository;
        this.projectRepository = projectRepository;
        this.auditService = auditService;
        this.auditRepository = auditRepository;
        this.auditSchedulerService = auditSchedulerService;
        this.userRepository = userRepository;
        this.appRoleRepository = appRoleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.actRepository = actRepository;
    }

    @PostConstruct
    public void setAdminUser() {
        userRepository.findByUsername("admin").orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("admin");
            newUser.setEmail("support@tanaguru.com");
            newUser.setEnabled(true);
            newUser.setPassword(bCryptPasswordEncoder.encode("admin"));
            newUser.setAppRole(appRoleRepository.findByName(EAppRole.SUPER_ADMIN)
                    .orElseThrow(IllegalStateException::new));
            return userRepository.save(newUser);
        });
    }

    /**
     * Encrypt and save a new password for the given user
     *
     * @param user     The user to change the password of
     * @param password The new password
     * @return The user with the new password
     */
    public User changeUserPassword(User user, String password) {
        user.setModificationPasswordTokens(new ArrayList<>());
        user.setPassword(bCryptPasswordEncoder.encode(password));
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unable to find the user"));
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                user.isAccountNonLocked(),
                user.getAppRole().getAuthorities().stream()
                        .map(appAuthority ->
                                new SimpleGrantedAuthority(appAuthority.getName())
                        ).collect(Collectors.toList())
        );
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }else{
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("Could not find user : " + username));
        }
    }

    public boolean currentUserHasAuthorityOnContract(String authority, long contractId) {
        return getCurrentUser() != null && contractService.hasAuthority(getCurrentUser(), authority, contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find contract " + contractId)), true);
    }

    public boolean currentUserHasAuthorityOnProject(String authority, long projectId) {
        return getCurrentUser() != null && projectService.hasAuthority(getCurrentUser(), authority, projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find project " + projectId)), true);
    }

    public boolean currentUserCanShowAudit(long auditId, String shareCode){
        Audit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find audit " + auditId));
        return currentUserCanShowAudit(audit, shareCode);
    }

    public boolean currentUserCanShowAudit(Audit audit, String shareCode){
        boolean result = auditService.canShowAudit(audit, shareCode);
        if(!result && getCurrentUser() != null){
            Optional<Act> actOptional = actRepository.findByAudit(audit);
            if(actOptional.isPresent()){
                result = projectService.hasAuthority(getCurrentUser(), SHOW_AUDIT, actOptional.get().getProject(), true);
            }
        }
        return result;
    }

    public boolean currentUserCanDeleteAudit(long auditId){
        Audit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find audit " + auditId));
        boolean result = false;
        Optional<Act> actOptional = actRepository.findByAudit(audit);
        if(actOptional.isPresent()){
            result = projectService.hasAuthority(getCurrentUser(), DELETE_AUDIT, actOptional.get().getProject(), true);
        }
        return result;
    }

    public boolean currentUserCanScheduleOnAudit(long auditId){
        Audit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find audit " + auditId));

        return auditSchedulerService.userCanScheduleOnAudit(getCurrentUser(), audit);
    }
    
}
