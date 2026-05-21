package com.ensah.Core.dao;

import com.ensah.Core.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {

Optional<Role> findByNomRole(String nomRole);
}