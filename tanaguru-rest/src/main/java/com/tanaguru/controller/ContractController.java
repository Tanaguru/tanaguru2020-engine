package com.tanaguru.controller;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.domain.constant.ContractAuthorityName;
import com.tanaguru.domain.constant.EContractRole;
import com.tanaguru.domain.dto.ContractDTO;
import com.tanaguru.domain.entity.membership.contract.Contract;
import com.tanaguru.domain.entity.membership.contract.ContractAppUser;
import com.tanaguru.domain.entity.membership.contract.ContractAuthority;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.domain.exception.CustomInvalidEntityException;
import com.tanaguru.repository.*;
import com.tanaguru.service.ContractService;
import com.tanaguru.service.TanaguruUserDetailsService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/contracts")
public class ContractController {
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final ContractService contractService;
    private final ContractUserRepository contractUserRepository;
    private final TanaguruUserDetailsService tanaguruUserDetailsService;

    public ContractController(
            UserRepository userRepository,
            ContractRepository contractRepository,
            ContractService contractService,
            ContractUserRepository contractUserRepository,
            TanaguruUserDetailsService tanaguruUserDetailsService) {

        this.userRepository = userRepository;
        this.contractRepository = contractRepository;
        this.contractService = contractService;
        this.contractUserRepository = contractUserRepository;
        this.tanaguruUserDetailsService = tanaguruUserDetailsService;
    }

    /**
     * @return All the @see Contract the user has authority to show
     */
    @ApiOperation(
            value = "Get all contracts current user has authority on (member or not)",
            notes = "User must be logged in"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Unauthorized")
    })
    @PreAuthorize("@tanaguruUserDetailsServiceImpl.getCurrentUser() != null")
    @GetMapping(value = "/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Collection<Contract> findAllWithAuthorities() {
        return contractService.getRoleAuthorities(
                    tanaguruUserDetailsService.getCurrentUser().getAppRole().getName())
                .contains(ContractAuthorityName.SHOW_CONTRACT) ?
                contractRepository.findAll() :
                findAllOwnedOrCurrentUserIsMemberOf();
    }

    /**
     * @return All the @see Contract for a given @see User id that current user can see
     */
    @ApiOperation(
            value = "Get Contracts for a given User",
            notes = "User must have SHOW_USER authority"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "User not found")
    })
    @PreAuthorize("hasAuthority(T(com.tanaguru.domain.constant.AppAuthorityName).SHOW_USER)")
    @GetMapping(value = "/by-user/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Collection<Contract> findAllByUser(@PathVariable long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.USER_NOT_FOUND, id));
        Collection<Contract> userContracts = contractService.findByUser(user);
        if (!contractService.hasOverrideAuthority(user, ContractAuthorityName.SHOW_CONTRACT)) {
            userContracts = userContracts.stream().filter(contract ->
                    contractService.hasAuthority(
                            user,
                            ContractAuthorityName.SHOW_CONTRACT,
                            contract,
                            false)
            ).collect(Collectors.toList());
        }

        return userContracts;
    }

    /**
     * @return Current @see User 's @see Contract list
     */
    @ApiOperation(
            value = "Get All contracts current user owns or is member of",
            notes = "User must be logged in"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Unauthorized")
    })
    @PreAuthorize("@tanaguruUserDetailsServiceImpl.getCurrentUser() != null")
    @GetMapping(value = "/me", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Collection<Contract> findAllOwnedOrCurrentUserIsMemberOf() {
        return contractService.findByUser(
                tanaguruUserDetailsService.getCurrentUser());
    }

    /**
     * @return All @see Contract owned by the @see User
     */
    @ApiOperation(
            value = "Get All contracts current user owns",
            notes = "User must be logged in"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Unauthorized")
    })
    @PreAuthorize("@tanaguruUserDetailsServiceImpl.getCurrentUser() != null")
    @GetMapping(value = "/owned", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Collection<Contract> findOwned() {
        return contractService.findByOwner(
                tanaguruUserDetailsService.getCurrentUser());
    }

    /**
     * @return All @see Contract the @see User is member of
     */
    @ApiOperation(
            value = "Get All contracts current user is member of",
            notes = "User must be logged in"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Unauthorized")
    })
    @PreAuthorize("@tanaguruUserDetailsServiceImpl.getCurrentUser() != null")
    @GetMapping(value = "/member-of", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Collection<Contract> findCurrentUserIsMemberOf() {
        return contractUserRepository.findAllByUser(tanaguruUserDetailsService.getCurrentUser())
                .stream()
                .filter((contractAppUser -> contractAppUser.getContractRole().getName() != EContractRole.CONTRACT_OWNER))
                .map(ContractAppUser::getContract)
                .collect(Collectors.toList());
    }

    /**
     * @return Get one @see Contract
     */
    @ApiOperation(
            value = "Get contracts by id",
            notes = "User must have SHOW_CONTRACT authority"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Contract not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnContract(" +
                    "T(com.tanaguru.domain.constant.ContractAuthorityName).SHOW_CONTRACT, " +
                    "#id)")
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Contract findById(@PathVariable long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CONTRACT_NOT_FOUND, id));
    }

    /**
     * @return Get current @see ContractAuthority names for a given @see Contract
     */
    @ApiOperation(
            value = "Get contracts by user",
            notes = "User must logged in"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Contract not found")
    })
    @PreAuthorize("@tanaguruUserDetailsServiceImpl.getCurrentUser() != null")
    @GetMapping(value = "/{id}/authorities", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Collection<String> findAuthoritiesByContractId(@PathVariable long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CONTRACT_NOT_FOUND, id));

        User currentUser = tanaguruUserDetailsService.getCurrentUser();

        Collection<String> contractAuthorities = contractUserRepository.findByContractAndUser(contract, currentUser)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.USER_NOT_FOUND_FOR_CONTRACT, currentUser.getId() + "," + contract.getId()))
                .getContractRole().getAuthorities().stream()
                    .map((ContractAuthority::getName))
                .collect(Collectors.toList());

        //Add override authorities
        contractAuthorities.addAll(
                currentUser.getAppRole().getOverrideContractRole().getAuthorities().stream()
                        .map(ContractAuthority::getName).collect(Collectors.toList()));

        return contractAuthorities.stream().distinct().collect(Collectors.toList());
    }

    /**
     * @return Create a @see Contract
     */
    @ApiOperation(
            value = "Create a contract for user",
            notes = "User must have CREATE_CONTRACT authority"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "User not found")
    })
    @PreAuthorize("hasAuthority(T(com.tanaguru.domain.constant.AppAuthorityName).CREATE_CONTRACT)")
    @PostMapping(value = "/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Contract createContract(@RequestBody @Valid ContractDTO contract) {
        User owner = userRepository.findById(contract.getOwnerId())
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.USER_NOT_FOUND, contract.getOwnerId()));

        //TODO REMOVE LIMITATION
        Collection<Contract> owned = contractService.findByOwner(owner);
        if(owned.size()>0){
            throw new CustomInvalidEntityException(CustomError.CANNOT_CREATE_MULTIPLE_USER_CONTRACT);
        }else{
            return contractService.createContract(
                    owner,
                    contract.getName(),
                    contract.getAuditLimit(),
                    contract.getProjectLimit(),
                    contract.isRestrictDomain(),
                    contract.getDateEnd());
        }
    }

    /**
     * @return Modify a @see Contract
     */
    @ApiOperation(
            value = "Modify a contract",
            notes = "User must have MODIFY_CONTRACT authority"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Contract not found")
    })
    @PreAuthorize("hasAuthority(T(com.tanaguru.domain.constant.AppAuthorityName).MODIFY_CONTRACT)")
    @PutMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Contract modifyContract(@RequestBody @Valid ContractDTO contractDto, @PathVariable long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CONTRACT_NOT_FOUND, id));

        User owner = userRepository.findById(contractDto.getOwnerId())
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.USER_NOT_FOUND, contractDto.getOwnerId()));

        return contractService.modifyContract(
                contract,
                owner,
                contractDto.getName(),
                contractDto.getAuditLimit(),
                contractDto.getProjectLimit(),
                contractDto.getDateEnd(),
                contractDto.isRestrictDomain()
        );
    }

    /**
     * @return Delete a @see Contract
     */
    @ApiOperation(
            value = "Delete a contract",
            notes = "User must have DELETE_CONTRACT authority"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Contract not found")
    })
    @PreAuthorize("hasAuthority(T(com.tanaguru.domain.constant.AppAuthorityName).DELETE_CONTRACT)")
    @DeleteMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    void deleteContract(@PathVariable long id) {
        contractService.deleteContract(
          contractRepository.findById(id)
            .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CONTRACT_NOT_FOUND, id))
        );
    }


    /**
     * Add an @see User to a @see Contract
     * @param userId The @see User id to add
     * @param contractId The @see targeted contract id
     */
    @ApiOperation(
            value = "Add a member to a contract",
            notes = "User must have INVITE_MEMBER authority on contract"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Contract not found or User not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnContract(" +
                    "T(com.tanaguru.domain.constant.ContractAuthorityName).INVITE_MEMBER, " +
                    "#contractId)")
    @PutMapping(value = "/{contractId}/add-member/{userId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ContractAppUser addMember(@PathVariable long contractId, @PathVariable long userId){
        return contractService.addMember(
                contractRepository.findById(contractId)
                        .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CONTRACT_NOT_FOUND, contractId)),
                userRepository.findById(userId)
                        .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.USER_NOT_FOUND, userId)));
    }

    /**
     * Delete a @see ContractAppUser
     * @param userId The @see User id to remove from @see Contract
     * @param contractId The @see Contract id to remove the @see User from
     */
    @ApiOperation(
            value = "Remove a member of a contract",
            notes = "User must have REMOVE_MEMBER authority on contract"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Contract not found or User not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnContract(" +
                    "T(com.tanaguru.domain.constant.ContractAuthorityName).REMOVE_MEMBER, " +
                    "#contractId)")
    @PutMapping(value = "/{contractId}/remove-member/{userId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void removeMember(@PathVariable long contractId, @PathVariable long userId){
        contractService.removeMember(
                contractRepository.findById(contractId)
                        .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CONTRACT_NOT_FOUND, contractId)),
                userRepository.findById(userId)
                        .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.USER_NOT_FOUND, userId))
        );
    }

    @ApiOperation(
            value = "Promote a member of a contract",
            notes = "User must have PROMOTE_MEMBER authority on contract"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Contract not found or User not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnContract(" +
                    "T(com.tanaguru.domain.constant.ContractAuthorityName).PROMOTE_MEMBER, " +
                    "#contractId)")
    @PutMapping(value = "/{contractId}/promote-member/{userId}/to/{contractRole}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ContractAppUser promoteMember(@PathVariable long contractId, @PathVariable long userId, @PathVariable EContractRole contractRole){
        User current = tanaguruUserDetailsService.getCurrentUser();
        if(current.getId() == userId){
            throw new CustomForbiddenException(CustomError.CANNOT_PROMOTE_YOURSELF);
        }

        if(contractService.getContractRole(contractRole).isHidden()){
            throw new CustomInvalidEntityException(CustomError.PROJECT_CANNOT_PROMOTE_USER);
        }

        Contract contract =  contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CONTRACT_NOT_FOUND, contractId));

        ContractAppUser owner = contractUserRepository.findByContractAndContractRoleName_Owner(contract);
        if(owner.getUser().getId() == userId){
            throw new CustomInvalidEntityException(CustomError.CANNOT_PROMOTE_CONTRACT_OWNER);
        }

        ContractAppUser target = contractUserRepository.findByContractAndUser(
                contract,
                userRepository.findById(userId)
                        .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.USER_NOT_FOUND, userId))
        ).orElseThrow(() -> new CustomEntityNotFoundException(CustomError.USER_NOT_FOUND_FOR_PROJECT, userId + "," + contractId));

        target.setContractRole(contractService.getContractRole(contractRole));
        return contractUserRepository.save(target);
    }
}
