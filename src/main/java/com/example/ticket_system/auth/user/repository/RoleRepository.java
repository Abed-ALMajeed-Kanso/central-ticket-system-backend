package com.example.ticket_system.auth.user.repository;

import com.example.ticket_system.auth.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByName(String name);

}
