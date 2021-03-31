package com.tanaguru.service.impl;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.constant.EContractRole;
import com.tanaguru.domain.entity.membership.contract.Contract;
import com.tanaguru.domain.entity.membership.contract.ContractAppUser;
import com.tanaguru.domain.entity.membership.contract.ContractAuthority;
import com.tanaguru.domain.entity.membership.contract.ContractRole;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.entity.membership.user.AppRole;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomInvalidArgumentException;
import com.tanaguru.domain.exception.CustomInvalidEntityException;
import com.tanaguru.repository.AppRoleRepository;
import com.tanaguru.repository.ContractRepository;
import com.tanaguru.repository.ContractRoleRepository;
import com.tanaguru.repository.ContractUserRepository;
import com.tanaguru.service.ContractService;
import com.tanaguru.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContractServiceImpl implements ContractService {
    private final AppRoleRepository appRoleRepository;
    private final ContractRepository contractRepository;
    private final ContractRoleRepository contractRoleRepository;
    private final ContractUserRepository contractUserRepository;
    private final ProjectService projectService;

    private Map<EContractRole, ContractRole> contractRoleMap = new EnumMap<>(EContractRole.class);
    private Map<EContractRole, Collection<String>> contractRoleAuthorityMap = new EnumMap<>(EContractRole.class);
    private Map<EAppRole, Collection<String>> contractRoleAuthorityMapByAppRole = new EnumMap<>(EAppRole.class);

    @Autowired
    public ContractServiceImpl(AppRoleRepository appRoleRepository, ContractRepository contractRepository, ContractRoleRepository contractRoleRepository, ContractUserRepository contractUserRepository, ProjectService projectService) {
        this.appRoleRepository = appRoleRepository;
        this.contractRepository = contractRepository;
        this.contractRoleRepository = contractRoleRepository;
        this.contractUserRepository = contractUserRepository;
        this.projectService = projectService;
    }

    public Map<EContractRole, ContractRole> getContractRoleMap() {
        return contractRoleMap;
    }

    public void setContractRoleMap(Map<EContractRole, ContractRole> contractRoleMap) {
        this.contractRoleMap = contractRoleMap;
    }

    public Map<EContractRole, Collection<String>> getContractRoleAuthorityMap() {
        return contractRoleAuthorityMap;
    }

    public void setContractRoleAuthorityMap(Map<EContractRole, Collection<String>> contractRoleAuthorityMap) {
        this.contractRoleAuthorityMap = contractRoleAuthorityMap;
    }

    public Map<EAppRole, Collection<String>> getContractRoleAuthorityMapByAppRole() {
        return contractRoleAuthorityMapByAppRole;
    }

    public void setContractRoleAuthorityMapByAppRole(Map<EAppRole, Collection<String>> contractRoleAuthorityMapByAppRole) {
        this.contractRoleAuthorityMapByAppRole = contractRoleAuthorityMapByAppRole;
    }

    @PostConstruct
    public void initMap() {
        for (ContractRole contractRole : contractRoleRepository.findAll()) {
            Collection<String> authorities = contractRole.getAuthorities()
                    .stream()
                    .map(ContractAuthority::getName)
                    .collect(Collectors.toList());
            contractRoleAuthorityMap.put(contractRole.getName(), authorities);
        }

        for (AppRole appRole : appRoleRepository.findAll()) {
            Collection<String> authorities = appRole.getOverrideContractRole().getAuthorities()
                    .stream()
                    .map(ContractAuthority::getName)
                    .collect(Collectors.toList());
            contractRoleAuthorityMapByAppRole.put(appRole.getName(), authorities);
        }

        for (EContractRole contractRole : EContractRole.values()) {
            contractRoleMap.put(contractRole, contractRoleRepository.findByName(contractRole)
                    .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CANNOT_FIND_CONTRACT_ROLE, contractRole.toString() )));
        }
    }

    public Page<Contract> findByUserAndContractName(String contractName, User user, Pageable pageable) {
        return contractUserRepository.findAllContractByUserAndContractNameContaining(user, contractName, pageable);
    }

    public Page<Contract> findByUser(User user, Pageable pageable) {
        return contractUserRepository.findAllContractByUser(user, pageable);
    }
    
    public Collection<Contract> findByOwner(User user) {
        return contractUserRepository.findAllByUserAndContractRole_Name_Owner(user)
                .stream().map((ContractAppUser::getContract))
                .collect(Collectors.toList());
    }

    public Collection<String> getRoleAuthorities(EContractRole contractRole) {
        return contractRoleAuthorityMap.getOrDefault(contractRole, new ArrayList<>());
    }

    public Collection<String> getRoleAuthorities(EAppRole appRole) {
        return contractRoleAuthorityMapByAppRole.getOrDefault(appRole, new ArrayList<>());
    }

    public ContractRole getContractRole(EContractRole contractRole) {
        return contractRoleMap.get(contractRole);
    }

    public boolean hasOverrideAuthority(User user, String authority) {
        return getRoleAuthorities(user.getAppRole().getName()).contains(authority);
    }

    public boolean hasAuthority(User user, String authority, Contract contract, boolean checkOverride) {
        boolean result = checkOverride && hasOverrideAuthority(user, authority);
        if (!result) {
            Optional<ContractAppUser> contractAppUser = contractUserRepository.findByContractAndUser(contract, user);
            result = contractAppUser.isPresent() &&
                    getRoleAuthorities(contractAppUser.get().getContractRole().getName())
                            .contains(authority);
        }
        return result;
    }

    public Contract createContract(User owner, String name, int auditLimit, int projectLimit, boolean restrictDomain, Date contractEnd) {
        Contract contract = new Contract();
        contract.setAuditLimit(auditLimit);
        contract.setProjectLimit(projectLimit);
        contract.setRestrictDomain(restrictDomain);
        contract.setDateEnd(contractEnd);
        contract.setDateStart(new Date());
        contract.setName(name);
        contract = contractRepository.save(contract);

        ContractAppUser contractAppUser = new ContractAppUser();
        contractAppUser.setContract(contract);
        contractAppUser.setUser(owner);
        contractAppUser.setContractRole(getContractRole(EContractRole.CONTRACT_OWNER));

        contractUserRepository.save(contractAppUser);
        return contract;
    }

    public Contract modifyContract(Contract contract, User owner, String name, int auditLimit, int projectLimit, Date contractEnd, boolean isRestrictDomain) {
        contract.setAuditLimit(auditLimit);
        contract.setProjectLimit(projectLimit);
        contract.setDateEnd(contractEnd);
        contract.setDateStart(new Date());
        contract.setName(name);
        contract.setRestrictDomain(isRestrictDomain);
        contract = contractRepository.save(contract);

        ContractAppUser contractOwner = contractUserRepository.findByContractAndContractRoleName_Owner(contract);

        //Change owner
        if(contractOwner.getUser().getId() != owner.getId()){
            ContractAppUser newOwner = contractUserRepository.findByContractAndUser(contract, owner)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.USER_NOT_CURRENT_MEMBER_CONTRACT, String.valueOf(owner.getId()) ));

            if(newOwner != null){
                newOwner.setContractRole(getContractRole(EContractRole.CONTRACT_OWNER));
                contractOwner.setContractRole(getContractRole(EContractRole.CONTRACT_MANAGER));
                contractUserRepository.save(contractOwner);
                contractUserRepository.save(newOwner);
            }
        }
        return contract;
    }

    public void deleteContract(Contract contract){
        projectService.deleteByContract(contract);
        contractUserRepository.deleteAllByContract(contract);
        contractRepository.deleteById(contract.getId());
    }

    public ContractAppUser addMember(Contract contract, User user){
        if(contractUserRepository.findByContractAndUser(contract, user).isEmpty()){
            ContractAppUser contractAppUser = new ContractAppUser();
            contractAppUser.setUser(user);
            contractAppUser.setContract(contract);
            contractAppUser.setContractRole(getContractRole(EContractRole.CONTRACT_GUEST));
            return contractUserRepository.save(contractAppUser);
        }else{
            return null;
        }
    }


    public void removeMember(Contract contract, User user){
        ContractAppUser contractAppUser = contractUserRepository.findByContractAndUser(contract, user)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.USER_NOT_CURRENT_MEMBER_CONTRACT, String.valueOf(user.getId()) ));

        if(contractAppUser.getContractRole().getName() == EContractRole.CONTRACT_OWNER){
            throw new CustomInvalidArgumentException(CustomError.CANNOT_DELETE_CONTRACT_OWNER, String.valueOf(user.getId()));
        }

        Collection<Project> projects = projectService.findAllByContractAndUser(contract, user);
        for(Project project : projects){
            projectService.removeMember(project, user);
        }
        contractUserRepository.delete(contractAppUser);
    }
}
