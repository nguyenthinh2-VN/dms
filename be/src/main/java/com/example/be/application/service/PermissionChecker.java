package com.example.be.application.service;

import com.example.be.domain.entity.Permission;
import com.example.be.domain.entity.Rule;
import com.example.be.domain.entity.User;
import com.example.be.domain.exception.ForbiddenException;
import com.example.be.domain.repository.PermissionRepository;
import com.example.be.domain.repository.RuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionChecker {

    private final RuleRepository ruleRepository;
    private final PermissionRepository permissionRepository;

    public PermissionChecker(RuleRepository ruleRepository, PermissionRepository permissionRepository) {
        this.ruleRepository = ruleRepository;
        this.permissionRepository = permissionRepository;
    }

    public boolean hasPermission(User user, String permissionCode) {
        if (user == null || user.getId() == null) {
            return false;
        }
        
        Optional<Permission> permissionOpt = permissionRepository.findByCode(permissionCode);
        if (permissionOpt.isEmpty()) {
            return false;
        }
        
        Long permissionId = permissionOpt.get().getId();
        List<Rule> rules = ruleRepository.findByUserId(user.getId());
        
        for (Rule rule : rules) {
            if (rule.getPermissionId().equals(permissionId) && "GRANTED".equals(rule.getStatus())) {
                return true;
            }
        }
        
        return false;
    }

    public void requirePermission(User user, String permissionCode) {
        if (!hasPermission(user, permissionCode)) {
            throw new ForbiddenException("Bạn không có quyền: " + permissionCode);
        }
    }
}
